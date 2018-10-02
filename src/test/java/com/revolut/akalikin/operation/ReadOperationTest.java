package com.revolut.akalikin.operation;

import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.InvalidRequestException;
import com.revolut.akalikin.model.Account;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class ReadOperationTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void readAllAccountsOperationReturnsAccountsAsExpected() {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);
        Account fooAcc = new Account("foo");
        Account barAcc = new Account("bar", 100L);
        store.storeAccount(fooAcc);
        store.storeAccount(barAcc);

        // When
        Collection<Account> accounts = readOperation.readAccounts();

        // Then
        assertThat(accounts.size(), equalTo(2));
        assertThat(accounts, containsInAnyOrder(fooAcc, barAcc));
    }

    @Test
    public void readAllAccountsOperationReturnsEmptyWhenNoAccountsAdded() {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);

        // When
        Collection<Account> accounts = readOperation.readAccounts();

        // Then
        assertThat(accounts.size(), equalTo(0));
    }

    @Test
    public void readAccountReturnsTheCorrectAccount() throws InvalidRequestException, AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);
        Account fooAcc = new Account("foo");
        Account barAcc = new Account("bar", 100L);
        store.storeAccount(fooAcc);
        store.storeAccount(barAcc);

        // When
        Account fooStored = readOperation.readAccount("foo");
        Account barStored = readOperation.readAccount("bar");

        // Then
        assertThat(fooStored, equalTo(fooAcc));
        assertThat(barStored, equalTo(barAcc));
    }

    @Test
    public void readAccountThrowsWhenAccountNotFound() throws InvalidRequestException, AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);

        // Then - expected exception
        expectedException.expect(AccountNotFoundException.class);

        // When
        Account fooStored = readOperation.readAccount("foo");
    }

    @Test
    public void readAccountThrowsWhenEmptyAccountIdRequested() throws InvalidRequestException, AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);

        // Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        // When
        Account fooStored = readOperation.readAccount("");
    }

    @Test
    public void readAccountThrowsWhenNullAccountIdRequested() throws InvalidRequestException, AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        ReadOperation readOperation = new ReadOperation(store);

        // Then - expected exception
        expectedException.expect(InvalidRequestException.class);

        // When
        Account fooStored = readOperation.readAccount(null);
    }
}
