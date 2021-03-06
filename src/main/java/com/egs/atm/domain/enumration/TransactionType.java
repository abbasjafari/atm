package com.egs.atm.domain.enumration;

/**
 * Defining transaction type
 * */
public enum TransactionType {
    DEPOSIT(1),WITHDRAWAL(-1);

    private final int value;

    TransactionType(final int newValue) {
        value = newValue;
    }

    public int getValue() { return value; }
}
