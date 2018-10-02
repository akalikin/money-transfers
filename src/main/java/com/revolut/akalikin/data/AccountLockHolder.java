package com.revolut.akalikin.data;

import com.google.inject.Singleton;
import com.revolut.akalikin.exception.AccountLockNotAcquiredException;
import com.revolut.akalikin.exception.TransientException;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Singleton
public class AccountLockHolder {

    private static final Long TIMEOUT = 5000L;

    private final ConcurrentHashMap<String, Semaphore> accountLocks;

    public AccountLockHolder() {
        this.accountLocks = new ConcurrentHashMap<>();
    }

    public void acquireLock(String accountId) throws TransientException {
        accountLocks.putIfAbsent(accountId, new Semaphore(1));
        try {
            if (!accountLocks.get(accountId).tryAcquire(TIMEOUT, TimeUnit.MILLISECONDS)) {
                throw new AccountLockNotAcquiredException(accountId);
            }
        } catch (InterruptedException e) {
            throw new AccountLockNotAcquiredException(accountId, e);
        }
    }

    public void releaseLock(String accountId) {
        accountLocks.computeIfPresent(accountId, (id, lock) -> {
            lock.release();
            return lock;
        });
    }
}
