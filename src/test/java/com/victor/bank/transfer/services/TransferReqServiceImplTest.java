package com.victor.bank.transfer.services;

import com.victor.bank.transfer.configs.H2InMemoryConfig;
import com.victor.bank.transfer.datastores.H2InMemoryDataStore;
import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.models.response.TransferResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.ClientRepositoryImpl;
import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.TransactionRepositoryImpl;
import com.victor.bank.transfer.repositories.models.Client;
import com.victor.bank.transfer.repositories.models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.victor.bank.transfer.repositories.Constants.CREATE_CLIENT_TABLE_SQL;
import static com.victor.bank.transfer.repositories.Constants.CREATE_TRANSACTIONS_TABLE_SQL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TransferReqServiceImplTest {

    private TransferService transferService;
    private TransactionRepository transactionRepository;
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        var datastore = new H2InMemoryDataStore(
            H2InMemoryConfig.of("jdbc:h2:mem:", 20, List.of(CREATE_CLIENT_TABLE_SQL, CREATE_TRANSACTIONS_TABLE_SQL)));
        transactionRepository = new TransactionRepositoryImpl(datastore);
        clientRepository = new ClientRepositoryImpl(datastore);
        loadClients();
        transferService = new TransferServiceImpl(transactionRepository, clientRepository);
    }

    void loadClients() {
        clientRepository.addClient("1", 100.0).join();
        clientRepository.addClient("2", 200.0).join();
        clientRepository.addClient("3", 150.0).join();
        clientRepository.addClient("4", 50.0).join();
    }

    @Test
    void testTransfer() {
        var transferResp = transferService.transfer(new TransferReq(1L, 2L, 50.0)).join();
        var readTransactionOpt = transactionRepository.getTransaction(1L).join();
        var fromClientOpt = clientRepository.getClient(1L).join();
        var toClientOpt = clientRepository.getClient(2L).join();
        var expectedTransferResp = new TransferResp(1L, 1L, 2L, 50.0);
        var expectedTransaction = new Transaction(1L, 1L, 2L, 50.0);
        assertThat(transferResp, is(expectedTransferResp));
        assertThat(readTransactionOpt, isPresentAndIs(expectedTransaction));
        assertThat(fromClientOpt, isPresentAndIs(new Client(1L, "1", 50.0)));
        assertThat(toClientOpt, isPresentAndIs(new Client(2L, "2", 250.0)));
    }
}