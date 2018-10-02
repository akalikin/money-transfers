package com.revolut.akalikin.data;

import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.model.Account;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

public class InMemoryStoreTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void storesTheAccount() throws AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        Account foo = new Account("foo", 100L);

        // When
        store.storeAccount(foo);

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(100L));
        assertThat(store.getAccounts(), containsInAnyOrder(foo));
    }

    @Test
    public void storesMultipleAccounts() throws AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        Account foo = new Account("foo", 100L);
        Account bar = new Account("bar", 100L);

        // When
        store.storeAccount(foo);
        store.storeAccount(bar);

        // Then
        assertThat(store.getAccounts().size(), equalTo(2));
        assertThat(store.getAccounts(), containsInAnyOrder(foo, bar));
    }

    @Test
    public void overwritesAnExistingAccount() throws AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        Account foo = new Account("foo", 100L);
        store.storeAccount(foo);

        // When
        store.storeAccount(new Account("foo", 105L));

        // Then
        assertThat(store.getAccount("foo").getBalance(), equalTo(105L));
    }

    @Test
    public void throwsWhenAccountNotFound() throws AccountNotFoundException {
        // Given
        Store store = new InMemoryStore();
        Account foo = new Account("foo", 100L);

        // Then - expected exception
        expectedException.expect(AccountNotFoundException.class);

        // When
        store.getAccount("foo");
    }



}
