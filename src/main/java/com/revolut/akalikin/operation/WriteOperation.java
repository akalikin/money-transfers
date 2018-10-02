package com.revolut.akalikin.operation;

import com.google.inject.Inject;
import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.PermanentException;
import com.revolut.akalikin.exception.TransientException;
import com.revolut.akalikin.model.Account;

import java.util.Optional;

import static com.revolut.akalikin.operation.validation.ValidationUtils.validateBalance;
import static com.revolut.akalikin.operation.validation.ValidationUtils.validateId;

/**
 * Business logic for writing Accounts.
 */
public class WriteOperation {

    private final AccountLockHolder lockHolder;
    private final Store store;

    @Inject
    public WriteOperation(Store store, AccountLockHolder lockHolder) {
        this.store = store;
        this.lockHolder = lockHolder;
    }

    public Account createAccount(String accountId, Optional<Long> initialBalance) throws PermanentException, TransientException {
        validateId(accountId);
        validateBalance(initialBalance.orElse(0L));
        try {
            store.getAccount(accountId);
        } catch (AccountNotFoundException e) {
            try {
                lockHolder.acquireLock(accountId);
                Account newAccount = new Account(accountId, initialBalance.orElse(0L));
                store.storeAccount(newAccount);
                return newAccount;
            } finally {
                lockHolder.releaseLock(accountId);
            }
        }
        throw new AccountAlreadyExistsException(accountId);
    }
}
