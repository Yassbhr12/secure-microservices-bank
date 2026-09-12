package com.securebank.account.exception;

public class AccountNotFoundException
    extends RuntimeException {

    public AccountNotFoundException() {
        super("Account not found");
    }
}
