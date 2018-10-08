package com.revolut.akalikin.operation;

import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
import com.revolut.akalikin.exception.AccountLockNotAcquiredException;
import com.revolut.akalikin.exception.AccountNotFoundException;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Test
    public void concurrentCreateAccountThrowsWhenSameAccountIsCreated() throws AccountNotFoundException, InterruptedException {
        // Given
        Store store = new InMemoryStore();
        AccountLockHolder accountLockHolder = new AccountLockHolder();
        WriteOperation writeOperation = new WriteOperation(store, accountLockHolder);

        // When
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        List<Future<Throwable>> futures = new LinkedList<>();
        for (int i = 0; i < 2; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    writeOperation.createAccount("foo", Optional.of(100L));
                } catch (Throwable e) {
                    return e;
                }
                return null;
            }));
        }

        executorService.awaitTermination(10, TimeUnit.SECONDS);

        // Then
        assertThat(futures.stream().allMatch(Future::isDone), equalTo(true));
        List<Throwable> thrown = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
        assertThat(thrown.size(), equalTo(1));
        assertThat(thrown.get(0).getClass(), equalTo(AccountAlreadyExistsException.class));
        assertThat(store.getAccount("foo").getBalance(), equalTo(100L));
    }

}
