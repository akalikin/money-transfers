package com.revolut.akalikin.operation;

import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountInsufficientFundsException;
import com.revolut.akalikin.exception.AccountLockNotAcquiredException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.PermanentException;
import com.revolut.akalikin.exception.TransientException;
import com.revolut.akalikin.model.Account;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class TransferOperationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    AccountLockHolder accountLockHolder;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void transferOperationMovesMoneyBetweenAccounts() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));
        store.storeAccount(new Account("bar"));

        // When
        transferOperation.executeMoneyTransfer("foo", "bar", 500L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(0L));
        assertThat(store.getAccount("bar").getBalance(), equalTo(500L));
        verify(accountLockHolder).releaseLock("foo");
        verify(accountLockHolder).releaseLock("bar");
    }

    @Test
    public void transferOperationThrowsWhenNotEnoughFundsAndDoesntMoveMoney() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));
        store.storeAccount(new Account("bar"));

        // Then - expected exception
        expectedException.expect(AccountInsufficientFundsException.class);

        // When
        transferOperation.executeMoneyTransfer("foo", "bar", 501L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(500L));
        assertThat(store.getAccount("bar").getBalance(), equalTo(0L));
        verify(accountLockHolder).releaseLock("foo");
        verify(accountLockHolder).releaseLock("bar");
    }


    @Test
    public void transferOperationThrowsWhenLockNotAcquiredAndDoesntMoveMoney() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));
        store.storeAccount(new Account("bar"));
        doThrow(AccountLockNotAcquiredException.class).when(accountLockHolder).acquireLock("bar");


        // Then - expected exception
        expectedException.expect(AccountLockNotAcquiredException.class);

        // When
        transferOperation.executeMoneyTransfer("foo", "bar", 500L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(500L));
        assertThat(store.getAccount("bar").getBalance(), equalTo(0L));
        verify(accountLockHolder).releaseLock("foo");
        verify(accountLockHolder).releaseLock("bar");
    }

    @Test
    public void transferOperationThrowsWhenInvalidAmountRecieved() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));
        store.storeAccount(new Account("bar"));
        doThrow(AccountLockNotAcquiredException.class).when(accountLockHolder).acquireLock("bar");


        // Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        // When
        transferOperation.executeMoneyTransfer("foo", "bar", -5L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(500L));
        assertThat(store.getAccount("bar").getBalance(), equalTo(0L));
        verify(accountLockHolder).releaseLock("foo");
        verify(accountLockHolder).releaseLock("bar");
    }

    @Test
    public void transferOperationThrowsWhenInvalidAccountIDRecieved() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));
        store.storeAccount(new Account("bar"));
        doThrow(AccountLockNotAcquiredException.class).when(accountLockHolder).acquireLock("bar");


        // Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        // When
        transferOperation.executeMoneyTransfer("foo", "", 500L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(500L));
        assertThat(store.getAccount("bar").getBalance(), equalTo(0L));
        verify(accountLockHolder).releaseLock("foo");
        verify(accountLockHolder).releaseLock("bar");
    }

}
