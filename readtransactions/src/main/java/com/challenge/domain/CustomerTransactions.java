package com.challenge.domain;

import java.math.BigDecimal;

public class CustomerTransactions {
    private String accountNumber;
    private BigDecimal transactionAmount;


    CustomerTransactions() {
    }

    public CustomerTransactions(String accountNumber, BigDecimal transactionAmount) {
        this.accountNumber = accountNumber;
        this.transactionAmount = transactionAmount;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public BigDecimal getTransactionAmount() {
        return transactionAmount;
    }

    void setTransactionAmount(BigDecimal transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

}
