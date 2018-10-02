package com.revolut.akalikin.operation;

import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
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

import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

public class WriteOperationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    AccountLockHolder accountLockHolder;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void writeOperationStoresTheAccount() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);

        //When
        writeOperation.createAccount("foo", Optional.empty());

        //Then
        Account fooStored = store.getAccount("foo");
        assertThat(fooStored.getBalance(), equalTo(0L));
        verify(accountLockHolder).releaseLock("foo");
    }


    @Test
    public void writeOperationStoresTheAccountWithProvidedBalance() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);

        //When
        writeOperation.createAccount("foo", Optional.of(500L));

        //Then
        Account fooStored = store.getAccount("foo");
        assertThat(fooStored.getBalance(), equalTo(500L));
        verify(accountLockHolder).releaseLock("foo");
    }

    @Test
    public void writeOperationWillThrowIfTheSameIdIsUsed() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);
        writeOperation.createAccount("foo", Optional.empty());

        //Then - expected exception
        expectedException.expect(AccountAlreadyExistsException.class);

        //When
        writeOperation.createAccount("foo", Optional.empty());

        //Then
        verify(accountLockHolder).releaseLock("foo");
    }

    @Test
    public void writeOperationWillThrowIfLockCantBeAcquired() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);
        doThrow(AccountLockNotAcquiredException.class).when(accountLockHolder).acquireLock("foo");

        //Then - expected exception
        expectedException.expect(AccountLockNotAcquiredException.class);

        //When
        writeOperation.createAccount("foo", Optional.empty());

        //Then
        verify(accountLockHolder).releaseLock("foo");
    }

    @Test
    public void writeOperationWillThrowIfInvalidBalanceProvided() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);

        //Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        //When
        writeOperation.createAccount("foo", Optional.of(-1L));

        //Then
        verify(accountLockHolder).releaseLock("foo");
    }

    @Test
    public void writeOperationWillThrowIfInvalidAccountIdProvided() throws TransientException, PermanentException {
        //Given
        Store store = new InMemoryStore();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);

        //Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        //When
        writeOperation.createAccount("", Optional.empty());

        //Then
        verify(accountLockHolder).releaseLock("foo");
    }

}
