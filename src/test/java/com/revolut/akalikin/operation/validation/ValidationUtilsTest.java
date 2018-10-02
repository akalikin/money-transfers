package com.revolut.akalikin.operation.validation;

import com.revolut.akalikin.exception.AccountIdMalformedException;
import com.revolut.akalikin.exception.InvalidInitialBalanceException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.InvalidTransferAmountException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.UUID;

public class ValidationUtilsTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();


    @Test
    public void doesntBlowUpOnValidID() throws InvalidRequestException {
        // When
        ValidationUtils.validateId(UUID.randomUUID().toString());

        // Then - nothing
    }

    @Test
    public void doesntBlowUpOnValidTransferAmount() throws InvalidRequestException {
        // When
        ValidationUtils.validateAmount(100L);

        // Then - nothing
    }


    @Test
    public void doesntBlowUpOnValidBalance() throws InvalidRequestException {
        // When
        ValidationUtils.validateBalance(100L);

        // Then - nothing
    }

    @Test
    public void doesntBlowUpOnZeroBalance() throws InvalidRequestException {
        // When
        ValidationUtils.validateBalance(0L);

        // Then - nothing
    }

    @Test
    public void throwsOnNullID() throws InvalidRequestException {
        // Then - nothing
        expectedException.expect(AccountIdMalformedException.class);

        // When
        ValidationUtils.validateId(null);
    }


    @Test
    public void throwsOnEmptyID() throws InvalidRequestException {
        // Then - nothing
        expectedException.expect(AccountIdMalformedException.class);

        // When
        ValidationUtils.validateId("");
    }

    @Test
    public void throwsOnNegativeBalance() throws InvalidRequestException {
        // Then - nothing
        expectedException.expect(InvalidInitialBalanceException.class);

        // When
        ValidationUtils.validateBalance(-100L);
    }

    @Test
    public void throwsOnNegativeTransferAmount() throws InvalidRequestException {
        // Then - nothing
        expectedException.expect(InvalidTransferAmountException.class);

        // When
        ValidationUtils.validateAmount(-100L);
    }

    @Test
    public void throwsOnZeroTransferAmount() throws InvalidRequestException {
        // Then - nothing
        expectedException.expect(InvalidTransferAmountException.class);

        // When
        ValidationUtils.validateAmount(0L);
    }
}
