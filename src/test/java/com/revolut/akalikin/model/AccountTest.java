package com.revolut.akalikin.model;

import org.junit.Test;

import java.util.UUID;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

public class AccountTest {

    @Test
    public void fundsAddedChangesTheBalance() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 5000L);

        // When
        account.addFunds(500L);

        // Then
        assertThat(account.getBalance(), equalTo(5500L));
    }

    @Test
    public void fundsDeductedChangesTheBalance() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 5000L);

        // When
        account.deductFunds(500L);

        // Then
        assertThat(account.getBalance(), equalTo(4500L));
    }

    @Test
    public void insufficientFundsWhenAmountIsLargerThanBalance() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 100L);

        // When
        boolean sufficient = account.sufficientFunds(500L);

        // Then
        assertThat(sufficient, equalTo(false));
    }

    @Test
    public void sufficientFundsWhenAmountIsSmallerThanBalance() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 500L);

        // When
        boolean sufficient = account.sufficientFunds(100L);

        // Then
        assertThat(sufficient, equalTo(true));
    }

    @Test
    public void sufficientFundsWhenAmountIsEqualToBalance() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 500L);

        // When
        boolean sufficient = account.sufficientFunds(500L);

        // Then
        assertThat(sufficient, equalTo(true));
    }

    @Test
    public void balanceBecomesNegativeIfDeductedTooMuch() {
        // Given
        Account account = new Account(
                UUID.randomUUID().toString(), 100L);

        // When
        account.deductFunds(500L);

        // Then
        assertThat(account.getBalance(), equalTo(-400L));
    }

}
