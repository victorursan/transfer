package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.configs.H2InMemoryConfig;
import com.victor.bank.transfer.datastores.DataStore;
import com.victor.bank.transfer.datastores.H2InMemoryDataStore;
import com.victor.bank.transfer.repositories.models.Client;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.victor.bank.transfer.repositories.Constants.CREATE_CLIENT_TABLE_SQL;
import static org.hamcrest.MatcherAssert.assertThat;

class ClientRepositoryImplTest {

    private ClientRepository clientRepository;
    private DataStore datastore;

    @BeforeEach
    void setUp() {
        datastore = new H2InMemoryDataStore(
            H2InMemoryConfig.of("jdbc:h2:mem:", 20, List.of(CREATE_CLIENT_TABLE_SQL)));
        clientRepository = new ClientRepositoryImpl(datastore);
    }

    @AfterEach
    void tearDown() {
        try {
            datastore.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void addClient() {
        var name = "abc";
        LongStream.rangeClosed(1, 10).forEach(id -> {
            var initBalance = new Random().nextDouble();
            var newClientOpt = clientRepository.addClient(name, initBalance).join();
            assertThat(newClientOpt, isPresentAndIs(new Client(id, name, initBalance)));
        });

    }

    @Test
    void updateClientBalance() {
        var name = "abc";
        var initBalance = 1.0;
        var newBalance = 3.0;
        var client = clientRepository.addClient(name, initBalance).join().get();
        var updatedIdOpt = clientRepository.updateClientBalance(client.getId(), newBalance).join();
        assertThat(updatedIdOpt, isPresentAndIs(client.getId()));
        var newClientOpt = clientRepository.getClient(client.getId()).join();
        assertThat(newClientOpt, isPresentAndIs(new Client(client.getId(), client.getFullName(), newBalance)));
    }

    @Test
    void getClient() {
        var name = "abc";
        LongStream.rangeClosed(1, 10).forEach(id -> {
            var seed = new Random();
            var initBalance = seed.nextDouble();
            var expectedClient = clientRepository.addClient(name, initBalance).join().get();
            var clientOpt = clientRepository.getClient(id).join();
            assertThat(clientOpt, isPresentAndIs(expectedClient));
        });
    }

}
