package com.revolut.akalikin.data;

import com.google.common.collect.ImmutableList;
import com.google.inject.Singleton;
import com.revolut.akalikin.exception.AccountNotFoundException;
import com.revolut.akalikin.model.Account;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory store for the account details.
 * Currently only persists account details for the lifetime of the server.
 *
 * I probably misunderstood the suggestion in the task description,
 * and used an in-memory map instead of an in-memory database,
 * but to my judgement and in-memory database would only increase the complexity of the task.
 */
@Singleton
public class InMemoryStore implements Store {

    private final Map<String, Account> accounts;

    public InMemoryStore() {
        this.accounts = new ConcurrentHashMap<>();
    }

    @Override
    public List<Account> getAccounts() {
        return ImmutableList.copyOf(accounts.values());
    }

    @Override
    public Account getAccount(String accountId) throws AccountNotFoundException {
        if (accounts.containsKey(accountId)) {
            return accounts.get(accountId);
        }
        throw new AccountNotFoundException(accountId);
    }

    @Override
    public void storeAccount(Account account) {
        accounts.put(account.getAccountId(), account);
    }
}
