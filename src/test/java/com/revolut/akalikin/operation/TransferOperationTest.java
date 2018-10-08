package com.revolut.akalikin.operation;

import com.google.common.collect.ImmutableList;
import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
import com.revolut.akalikin.exception.AccountInsufficientFundsException;
import com.revolut.akalikin.exception.AccountLockNotAcquiredException;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.exception.PermanentException;
import com.revolut.akalikin.exception.TransferRequestForTheSameAccountException;
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
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Test
    public void transferOperationThrowsWhenInvalidAccountIDRecievedToAndFromAreTheSame() throws TransientException, PermanentException {
        // Given
        Store store = new InMemoryStore();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("foo", 500L));

        // Then - expected exception
        expectedException.expect(TransferRequestForTheSameAccountException.class);

        // When
        transferOperation.executeMoneyTransfer("foo", "foo", 500L);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(500L));
    }

    @Test
    public void sameAccountsTransfersRequestedConcurrentlyAffectTheBalanceCorrectly() throws AccountAlreadyExistsException, InterruptedException, AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        AccountLockHolder accountLockHolder = new AccountLockHolder();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        store.storeAccount(new Account("a", 100L));
        store.storeAccount(new Account("b", 100L));

        // When
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        List<Future<Throwable>> futures = new LinkedList<>();
        for (int i = 0; i < 10; i++) {
            futures.add(executorService.submit(() -> {
                try {
                    transferOperation.executeMoneyTransfer("a", "b", 10L);
                    return null;
                } catch (Throwable e) {
                    return e;
                }
            }));
        }
        executorService.awaitTermination(50, TimeUnit.SECONDS);

        long total = store.getAccounts().stream().mapToLong(Account::getBalance).sum();
        assertThat(total, equalTo(200L));
        assertThat(futures.stream().allMatch(Future::isDone), equalTo(true));
        int failedCount = futures.stream().map(f -> {
            try {
                return f.get();
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()).size();
        assertThat(store.getAccount("a").getBalance(), equalTo(failedCount * 10L));
        assertThat(store.getAccount("b").getBalance(), equalTo(200L - failedCount * 10L));
    }


    @Test
    public void totalBalanceStaysConstantWhenTransactionsExecutedConcurrently() throws AccountAlreadyExistsException, InterruptedException {
        // Given
        Store store = new InMemoryStore();
        AccountLockHolder accountLockHolder = new AccountLockHolder();
        TransferOperation transferOperation = new TransferOperation(store, accountLockHolder);
        List<String> accountIds = ImmutableList.of("a", "b", "c", "d", "e");
        Random random = new Random();

        for (String id : accountIds) {
            store.storeAccount(new Account(id, 500L));
        }

        long total = store.getAccounts().stream().mapToLong(Account::getBalance).sum();
        assertThat(total, equalTo(2500L));

        // When
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 50; i++) {
            executorService.execute(() -> {
                try {
                    String from = accountIds.get(random.nextInt(accountIds.size()));
                    String to = from;
                    while (to.equals(from)) {
                        to = accountIds.get(random.nextInt(accountIds.size()));
                    }
                    transferOperation.executeMoneyTransfer(from, to, 5L);
                } catch (Throwable e) {
                    // ignore - failed transactions should not affect the total
                }
            });
        }
        executorService.awaitTermination(10, TimeUnit.SECONDS);

        total = store.getAccounts().stream().mapToLong(Account::getBalance).sum();
        assertThat(total, equalTo(2500L));
    }

}
