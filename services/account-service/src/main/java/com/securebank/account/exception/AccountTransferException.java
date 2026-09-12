package com.securebank.account.exception;

public class AccountTransferException
    extends RuntimeException {

    private final String code;

    public AccountTransferException(
        String code,
        String message
    ) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
