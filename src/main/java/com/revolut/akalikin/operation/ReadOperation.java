package com.revolut.akalikin.operation;

import com.google.inject.Inject;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.model.Account;

import java.util.Collection;

import static com.revolut.akalikin.operation.validation.ValidationUtils.validateId;

/**
 * Business logic for retrieving accounts from the store.
 */
public class ReadOperation {

    private final Store accountStore;

    @Inject
    public ReadOperation(Store accountStore) {
        this.accountStore = accountStore;
    }

    public Account readAccount(String accountId) throws AccountNotFoundException, InvalidRequestException {
        validateId(accountId);
        return accountStore.getAccount(accountId);
    }

    public Collection<Account> readAccounts() {
        return accountStore.getAccounts();
    }
}
