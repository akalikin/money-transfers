package com.revolut.akalikin.model;


import java.util.Objects;

/**
 * Account DTO.
 * For the sake of simplicity, assuming that all accounts operate in the same currency.
 */
public class Account {

    private final String accountId;

    private Long accountBalance;

    /**
     * Constructor.
     *
     * @param accountId      unique Account ID
     * @param accountBalance current balance of the account
     */
    public Account(String accountId, Long accountBalance) {
        this.accountId = accountId;
        this.accountBalance = accountBalance;
    }

    /**
     * Constructor with default balance.
     *
     * @param accountId unique Account ID
     */
    public Account(String accountId) {
        this.accountId = accountId;
        this.accountBalance = 0L;
    }

    public void addFunds(Long amount) {
        accountBalance += amount;
    }

    public void deductFunds(Long amount) {
        accountBalance -= amount;
    }

    public boolean sufficientFunds(Long amount) {
        return accountBalance >= amount;
    }

    public String getAccountId() {
        return accountId;
    }

    public Long getBalance() {
        return accountBalance;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        Account other = (Account) obj;
        return Objects.equals(accountId, other.accountId)
                && Objects.equals(accountBalance, other.accountBalance);
    }
}
