package com.revolut.akalikin.controller;

import com.revolut.akalikin.data.AccountLockHolder;
import com.revolut.akalikin.data.InMemoryStore;
import com.revolut.akalikin.data.Store;
import com.revolut.akalikin.exception.AccountAlreadyExistsException;
import com.revolut.akalikin.exception.AccountInsufficientFundsException;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.exception.InvalidTransferAmountException;
import com.revolut.akalikin.model.Account;
import com.revolut.akalikin.operation.ReadOperation;
import com.revolut.akalikin.operation.TransferOperation;
import com.revolut.akalikin.operation.WriteOperation;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.ws.rs.core.Response;
import java.util.Collection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

public class AccountTransferServiceControllerTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private ReadOperation readOperation;
    private WriteOperation writeOperation;
    private TransferOperation transferOperation;
    private Store store;

    @Before
    public void before() {
        store = new InMemoryStore();
        AccountLockHolder lockHolder = new AccountLockHolder();
        readOperation = new ReadOperation(store);
        writeOperation = new WriteOperation(store, lockHolder);
        transferOperation = new TransferOperation(store, lockHolder);
    }

    @Test
    public void readOperationsReturnExpectedAccount() {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);
        Account foo = new Account("foo", 1337L);
        store.storeAccount(foo);

        //When
        Response accountsResponse = controller.getAccounts();
        Response accountResponse = controller.getAccount("foo");

        //Then
        Collection<Account> actualAccounts = (Collection<Account>) accountsResponse.getEntity();
        assertThat(accountsResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(actualAccounts.size(), equalTo(1));
        assertThat(actualAccounts, containsInAnyOrder(foo));

        Account actualAccount = (Account) accountResponse.getEntity();
        assertThat(accountResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(actualAccount, equalTo(foo));
    }

    @Test
    public void readOperationsWithNoAccounts() {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);

        //When
        Response accountsResponse = controller.getAccounts();
        Response accountResponse = controller.getAccount("foo");

        //Then
        Collection<Account> actualAccounts = (Collection<Account>) accountsResponse.getEntity();
        assertThat(accountsResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(actualAccounts.size(), equalTo(0));

        String exceptionMessage = (String) accountResponse.getEntity();
        assertThat(accountResponse.getStatus(), equalTo(Response.Status.NOT_FOUND.getStatusCode()));
        assertThat(exceptionMessage, containsString(AccountNotFoundException.class.getCanonicalName()));
    }

    @Test
    public void writeOperationStoresTheAccount() throws AccountNotFoundException {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);

        //When
        Response writeResponse = controller.createAccount("foo", 150L);

        //Then
        assertThat(writeResponse.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        Account newAccount = (Account) writeResponse.getEntity();
        assertThat(newAccount.getAccountId(), equalTo("foo"));
        assertThat(newAccount.getBalance(), equalTo(150L));
        assertThat(store.getAccount("foo").getBalance(), equalTo(150L));
    }

    @Test
    public void writeOperationConflictOnRepeatedWrite() throws AccountNotFoundException {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);

        //When
        Response firstResponse = controller.createAccount("foo", 150L);

        //Then
        assertThat(firstResponse.getStatus(), equalTo(Response.Status.CREATED.getStatusCode()));
        Account newAccount = (Account) firstResponse.getEntity();
        assertThat(newAccount.getAccountId(), equalTo("foo"));
        assertThat(newAccount.getBalance(), equalTo(150L));
        assertThat(store.getAccount("foo").getBalance(), equalTo(150L));

        // When
        Response secondResponse = controller.createAccount("foo", 150L);

        // Then
        assertThat(secondResponse.getStatus(), equalTo(Response.Status.CONFLICT.getStatusCode()));
        String exceptionMessage = (String) secondResponse.getEntity();
        assertThat(exceptionMessage, containsString(AccountAlreadyExistsException.class.getCanonicalName()));
        assertThat(store.getAccount("foo").getBalance(), equalTo(150L));
    }

    @Test
    public void transferOperationMovesMoneyCorrectly() throws AccountNotFoundException {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);
        Account from = new Account("from", 1000L);
        Account to = new Account("to", 1000L);
        store.storeAccount(from);
        store.storeAccount(to);

        //When
        Response transferResponse = controller.transfer("from", "to", 500L);

        //Then
        assertThat(transferResponse.getStatus(), equalTo(Response.Status.OK.getStatusCode()));
        assertThat(store.getAccount("from").getBalance(), equalTo(500L));
        assertThat(store.getAccount("to").getBalance(), equalTo(1500L));
    }

    @Test
    public void transferOperationInsufficientFunds() throws AccountNotFoundException {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);
        Account from = new Account("from", 0L);
        Account to = new Account("to", 1000L);
        store.storeAccount(from);
        store.storeAccount(to);

        //When
        Response transferResponse = controller.transfer("from", "to", 500L);

        //Then
        assertThat(transferResponse.getStatus(), equalTo(Response.Status.PRECONDITION_FAILED.getStatusCode()));
        String exceptionMessage = (String) transferResponse.getEntity();
        assertThat(exceptionMessage, containsString(AccountInsufficientFundsException.class.getCanonicalName()));
        assertThat(store.getAccount("from").getBalance(), equalTo(0L));
        assertThat(store.getAccount("to").getBalance(), equalTo(1000L));
    }

    @Test
    public void transferOperationWrongAmount() throws AccountNotFoundException {
        //Given
        AccountTransferServiceController controller = new AccountTransferServiceController(readOperation, transferOperation, writeOperation);
        Account from = new Account("from", 0L);
        Account to = new Account("to", 1000L);
        store.storeAccount(from);
        store.storeAccount(to);

        //When
        Response transferResponse = controller.transfer("from", "to", -1L);

        //Then
        assertThat(transferResponse.getStatus(), equalTo(Response.Status.BAD_REQUEST.getStatusCode()));
        String exceptionMessage = (String) transferResponse.getEntity();
        assertThat(exceptionMessage, containsString(InvalidTransferAmountException.class.getCanonicalName()));
        assertThat(store.getAccount("from").getBalance(), equalTo(0L));
        assertThat(store.getAccount("to").getBalance(), equalTo(1000L));
    }

}
