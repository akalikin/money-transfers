package com.revolut.akalikin.data;

import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.model.Account;

import java.util.List;


public interface Store {

    public List<Account> getAccounts();

    public Account getAccount(String accountId) throws AccountNotFoundException;

    public void storeAccount(Account account);
}
