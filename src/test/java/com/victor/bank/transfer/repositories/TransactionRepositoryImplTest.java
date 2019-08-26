package com.victor.bank.transfer.repositories;

import com.victor.bank.transfer.configs.H2InMemoryConfig;
import com.victor.bank.transfer.datastores.DataStore;
import com.victor.bank.transfer.datastores.H2InMemoryDataStore;
import com.victor.bank.transfer.repositories.models.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static com.github.npathai.hamcrestopt.OptionalMatchers.isPresentAndIs;
import static com.victor.bank.transfer.repositories.Constants.CREATE_TRANSACTIONS_TABLE_SQL;
import static org.hamcrest.MatcherAssert.assertThat;

class TransactionRepositoryImplTest {

    private TransactionRepository transactionRepository;
    private DataStore datastore;

    @BeforeEach
    void setUp() {
        datastore = new H2InMemoryDataStore(
            H2InMemoryConfig.of("jdbc:h2:mem:", 20, List.of(CREATE_TRANSACTIONS_TABLE_SQL)));
        transactionRepository = new TransactionRepositoryImpl(datastore);
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
    void addTransaction() {
        LongStream.range(1, 10).forEach(id -> {
            var seed = new Random();
            var fromId = seed.nextLong();
            var toId = seed.nextLong();
            var transferAmount = seed.nextDouble();
            var transactionOpt = transactionRepository.addTransaction(fromId, toId, transferAmount).join();
            assertThat(transactionOpt, isPresentAndIs(new Transaction(id, fromId, toId, transferAmount)));
        });
    }

    @Test
    void getTransaction() {
        LongStream.rangeClosed(1, 10).forEach(id -> {
            var seed = new Random();
            var fromId = seed.nextLong();
            var toId = seed.nextLong();
            var transferAmount = seed.nextDouble();
            var expectedTransaction = transactionRepository.addTransaction(fromId, toId, transferAmount).join().get();
            var transactionOpt = transactionRepository.getTransaction(expectedTransaction.getId()).join();
            assertThat(transactionOpt, isPresentAndIs(expectedTransaction));
        });
        var transactionOpt = transactionRepository.getTransaction(11L).join();
        assertThat(transactionOpt, isEmpty());
    }

    @Test
    void deleteTransaction() {
        var seed = new Random();
        var fromId = seed.nextLong();
        var toId = seed.nextLong();
        var transferAmount = seed.nextDouble();
        var addedTransaction = transactionRepository.addTransaction(fromId, toId, transferAmount).join().get();
        var transactionIdOpt = transactionRepository.deleteTransaction(addedTransaction.getId()).join();
        var failDeleteTrans = transactionRepository.deleteTransaction(addedTransaction.getId()).join();
        assertThat(transactionIdOpt, isPresentAndIs(addedTransaction.getId()));
        assertThat(failDeleteTrans, isEmpty());
    }
}