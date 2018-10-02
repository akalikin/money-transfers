package com.revolut.akalikin.operation.validation;


import com.revolut.akalikin.exception.AccountIdMalformedException;
import com.revolut.akalikin.exception.InvalidInitialBalanceException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.InvalidTransferAmountException;

public class ValidationUtils {

    public static void validateId(String accountId) throws InvalidRequestException {
        if (accountId == null || accountId.equals("")) {
            throw new AccountIdMalformedException(accountId);
        }
    }

    public static void validateAmount(Long amount) throws InvalidRequestException {
        if (amount == null || amount <= 0) {
            throw new InvalidTransferAmountException(amount);
        }
    }

    public static void validateBalance(Long balance) throws InvalidRequestException {
        if (balance == null || balance < 0) {
            throw new InvalidInitialBalanceException(balance);
        }
    }
}
