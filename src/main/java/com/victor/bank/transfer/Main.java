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
import com.victor.bank.transfer.repositories.ClientRepository;
import com.victor.bank.transfer.repositories.ClientRepositoryImpl;
import com.victor.bank.transfer.repositories.TransactionRepository;
import com.victor.bank.transfer.repositories.TransactionRepositoryImpl;
import com.victor.bank.transfer.services.ClientService;
import com.victor.bank.transfer.services.ClientServiceImpl;
import com.victor.bank.transfer.services.TransferService;
import com.victor.bank.transfer.services.TransferServiceImpl;

public class Main {
    public static void main(String[] args) {
        final H2InMemoryConfig dataStoreConfig = ConfigsProvider.getH2InMemoryConfig();
        final UndertowServerConfig undertowServerConfig = ConfigsProvider.getUndertowServerConfig();
        final DataStore dataStore = new H2InMemoryDataStore(dataStoreConfig);
        final ClientRepository clientRepository = new ClientRepositoryImpl(dataStore);
        final TransactionRepository transactionRepository = new TransactionRepositoryImpl(dataStore);
        final ClientService clientService = new ClientServiceImpl(clientRepository);
        final TransferService transferService = new TransferServiceImpl(transactionRepository, clientRepository);
        final ClientController clientController = new ClientControllerImpl(clientService);
        final TransferController transferController = new TransferControllerImpl(transferService);
        final HttpServer httpServer = new HttpServer(undertowServerConfig, clientController, transferController);
        httpServer.start();
    }
}
