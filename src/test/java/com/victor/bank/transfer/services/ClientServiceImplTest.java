package com.victor.bank.transfer.services;

import com.victor.bank.transfer.mock.ClientRepositoryMock;
import com.victor.bank.transfer.models.requests.AddClientReq;
import com.victor.bank.transfer.models.response.ClientResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ClientServiceImplTest {

    private ClientRepository clientRepository;
    private ClientService clientService;

    @BeforeEach
    void setUp() {
        clientRepository = new ClientRepositoryMock();
        clientService = new ClientServiceImpl(clientRepository);
    }

    @Test
    void addClient() {
        var emptyClient = clientService.addClient(new AddClientReq("John Doe", Optional.empty())).join();
        var client = clientService.addClient(new AddClientReq("John Doe2", Optional.of(12.2))).join();
        assertThat(emptyClient, is(new ClientResp(1L,"John Doe", 0D )));
        assertThat(client, is(new ClientResp(2L,"John Doe2", 12.2 )));

    }

    @Test
    void getClient() {
        var emptyClient = clientService.addClient(new AddClientReq("John Doe", Optional.empty())).join();
        var client = clientService.addClient(new AddClientReq("John Doe2", Optional.of(12.2))).join();
        var getEmptyClient = clientService.getClient(emptyClient.getId()).join();
        var getClient = clientService.getClient(client.getId()).join();
        assertThat(getEmptyClient, is(emptyClient));
        assertThat(getClient, is(client));
    }
}