package com.victor.bank.transfer;

import com.victor.bank.transfer.configs.ConfigsProvider;
import com.victor.bank.transfer.configs.H2InMemoryConfig;
import com.victor.bank.transfer.configs.UndertowServerConfig;
import com.victor.bank.transfer.controllers.ClientController;
import com.victor.bank.transfer.controllers.ClientControllerImpl;
import com.victor.bank.transfer.controllers.TransferController;
import com.victor.bank.transfer.controllers.TransferControllerImpl;
import com.victor.bank.transfer.datastores.DataStore;
import com.victor.bank.transfer.datastores.H2InMemoryDataStore;
import com.victor.bank.transfer.models.requests.AddClientReq;
import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.models.response.ClientResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.ClientRepositoryImpl;
import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.TransactionRepositoryImpl;
import com.victor.bank.transfer.services.ClientService;
import com.victor.bank.transfer.services.ClientServiceImpl;
import com.victor.bank.transfer.services.TransferService;
import com.victor.bank.transfer.services.TransferServiceImpl;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.victor.bank.transfer.utils.PayloadSerde.deserialize;
import static com.victor.bank.transfer.utils.PayloadSerde.serialize;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

public class HttpServerTest {
    private HttpServer httpServer;
    private DataStore dataStore;
    private HttpClient client;
    private URI serverAddress;

    @BeforeEach
    void setUp() {
        final H2InMemoryConfig dataStoreConfig = ConfigsProvider.getH2InMemoryConfig();
        final UndertowServerConfig undertowServerConfig = ConfigsProvider.getUndertowServerConfig();
        dataStore = new H2InMemoryDataStore(dataStoreConfig);
        final ClientRepository clientRepository = new ClientRepositoryImpl(dataStore);
        final TransactionRepository transactionRepository = new TransactionRepositoryImpl(dataStore);
        final ClientService clientService = new ClientServiceImpl(clientRepository);
        final TransferService transferService = new TransferServiceImpl(transactionRepository, clientRepository);
        final ClientController clientController = new ClientControllerImpl(clientService);
        final TransferController transferController = new TransferControllerImpl(transferService);
        httpServer = new HttpServer(undertowServerConfig, clientController, transferController);
        httpServer.start();

        serverAddress = URI.create(String.format("http://%s:%d/", undertowServerConfig.getHost(), undertowServerConfig.getPort()));

        client = HttpClient.newHttpClient();

    }

    @AfterEach
    void tearDown() throws IOException {
        httpServer.stop();
        dataStore.close();
    }

    @Test
    void checkHealthCheck() throws Exception {
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(serverAddress.resolve("health"))
            .GET()
            .build();
        assertThat(client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body(), is(serialize("Ok").array()));
    }

    @Test
    void checkAddClients() throws Exception {
        final byte[] addNoBalanceClient = addClient(new AddClientReq("John Doe", Optional.empty()));
        final byte[] addClient = addClient(new AddClientReq("John Doe", Optional.of(1992.1)));

        final byte[] expectedNoBalanceClient = serialize(new ClientResp(1L, "John Doe", 0.0)).array();

        final byte[] expectedClient = serialize(new ClientResp(2L, "John Doe", 1992.1)).array();

        assertThat(addNoBalanceClient, is(expectedNoBalanceClient));
        assertThat(addClient, is(expectedClient));
    }

    @Test
    void checkGetClients() throws Exception {
        final byte[] addNoBalanceClient = addClient(new AddClientReq("John Doe", Optional.empty()));
        final byte[] addClient = addClient(new AddClientReq("John Doe", Optional.of(1992.1)));

        final byte[] getNoBalanceClient = getClient(1L);
        final byte[] getClient = getClient(2L);

        assertThat(addNoBalanceClient, is(getNoBalanceClient));
        assertThat(addClient, is(getClient));
    }


    @Test
    void checkConcurrentTransfers() throws Exception {
        final ClientResp client1 = deserialize(addClient(new AddClientReq("Person One", Optional.of(123.1))), ClientResp.class);
        final ClientResp client2 = deserialize(addClient(new AddClientReq("Person Two", Optional.of(432.5))), ClientResp.class);
        final Long cid1 = client1.getId();
        final Long cid2 = client2.getId();

        final Random seed = new Random();

        final List<TransferReq> requests = IntStream.rangeClosed(1, 1000).mapToObj(i -> {
            double amount = seed.nextDouble() % 10;
            return Stream.of(new TransferReq(cid1, cid2, amount), new TransferReq(cid2, cid1, amount));
        }).flatMap(Function.identity()).collect(Collectors.toList());

        Collections.shuffle(requests);

        requests.stream().forEach(this::performTransfer);

        final ClientResp resultClient1 = deserialize(getClient(cid1), ClientResp.class);
        final ClientResp resultClient2 = deserialize(getClient(cid2), ClientResp.class);

        assertThat(resultClient1.getBalance(), closeTo(client1.getBalance(), 0.000000000001));
        assertThat(resultClient2.getBalance(), closeTo(client2.getBalance(), 0.000000000001));
    }

    private void performTransfer(final TransferReq transferReq) {
        final BodyPublisher body = BodyPublishers.ofByteArray(serialize(transferReq).array());

        final HttpRequest request = HttpRequest.newBuilder()
            .uri(serverAddress.resolve("transfers"))
            .POST(body)
            .build();

        try {
            assertThat(client.send(request, HttpResponse.BodyHandlers.ofString()).statusCode(), is(StatusCodes.CREATED));
        } catch (Exception e) {
            Assertions.fail(e);
        }
    }


    private byte[] getClient(final long l) throws Exception {
        final HttpRequest request = HttpRequest.newBuilder()
            .uri(serverAddress.resolve(String.format("clients/%d", l)))
            .GET()
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
    }

    private byte[] addClient(final AddClientReq addClientReq) throws Exception {
        final BodyPublisher addClientBody = BodyPublishers.ofByteArray(serialize(addClientReq).array());

        final HttpRequest request = HttpRequest.newBuilder()
            .uri(serverAddress.resolve("clients"))
            .POST(addClientBody)
            .build();

        return client.send(request, HttpResponse.BodyHandlers.ofByteArray()).body();
    }
}
