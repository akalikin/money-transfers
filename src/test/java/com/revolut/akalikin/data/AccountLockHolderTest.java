package com.revolut.akalikin.data;

import com.revolut.akalikin.exception.AccountLockNotAcquiredException;
import com.revolut.akalikin.exception.TransientException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class AccountLockHolderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void acquiresLockForTheFirstTime() throws TransientException {
        // Given
        AccountLockHolder lockHolder = new AccountLockHolder();

        // When
        lockHolder.acquireLock("foo");

        // Then -- Nothing
    }

    @Test
    public void reacquiresLockAfterReleasing() throws TransientException {
        // Given
        AccountLockHolder lockHolder = new AccountLockHolder();
        lockHolder.acquireLock("foo");
        lockHolder.releaseLock("foo");

        // When
        lockHolder.acquireLock("foo");

        // Then -- Nothing
    }

    @Test
    public void throwsWhenLockIsAcquiredBeforeReleasing() throws TransientException {
        // Given
        AccountLockHolder lockHolder = new AccountLockHolder();
        lockHolder.acquireLock("foo");

        // Then -- expected exception
        expectedException.expect(AccountLockNotAcquiredException.class);

        // When
        lockHolder.acquireLock("foo");
    }

}
