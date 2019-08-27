package com.victor.bank.transfer.services;

import com.victor.bank.transfer.mock.ClientRepositoryMock;
import com.victor.bank.transfer.mock.TransactionRepositoryMock;
import com.victor.bank.transfer.models.requests.TransferReq;
import com.victor.bank.transfer.models.response.TransferResp;
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.models.Client;
import com.victor.bank.transfer.repositories.models.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class TransferServiceImplTest {

    private TransferService transferService;
    private TransactionRepository transactionRepository;
    private ClientRepository clientRepository;

    @BeforeEach
    void setUp() {
        transactionRepository = new TransactionRepositoryMock();
        clientRepository = new ClientRepositoryMock();
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