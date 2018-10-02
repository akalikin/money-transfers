package com.revolut.akalikin.operation;

import com.google.inject.Inject;
import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountInsufficientFundsException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.PermanentException;
import com.revolut.akalikin.exception.TransientException;
import com.revolut.akalikin.model.Account;

import static com.revolut.akalikin.operation.validation.ValidationUtils.validateAmount;
import static com.revolut.akalikin.operation.validation.ValidationUtils.validateId;


/**
 * Business logic for executing money transfers between accounts.
 */
public class TransferOperation {

    private final Store accountStore;
    private final AccountLockHolder lockHolder;

    @Inject
    public TransferOperation(Store accountStore, AccountLockHolder lockHolder) {
        this.accountStore = accountStore;
        this.lockHolder = lockHolder;
    }

    public void executeMoneyTransfer(String fromId, String toId, Long amount) throws PermanentException, TransientException {
        validateRequest(fromId, toId, amount);
        try {
            lockHolder.acquireLock(fromId);
            lockHolder.acquireLock(toId);

            Account from = accountStore.getAccount(fromId);
            Account to = accountStore.getAccount(toId);

            if (!from.sufficientFunds(amount)) {
                throw new AccountInsufficientFundsException(fromId, from.getBalance(), amount);
            }

            from.deductFunds(amount);
            to.addFunds(amount);

            accountStore.storeAccount(from);
            accountStore.storeAccount(to);
        } finally {
            lockHolder.releaseLock(fromId);
            lockHolder.releaseLock(toId);
        }

    }

    private void validateRequest(String fromId, String toId, Long amount) throws InvalidRequestException {
        validateId(fromId);
        validateId(toId);
        validateAmount(amount);
    }


}
