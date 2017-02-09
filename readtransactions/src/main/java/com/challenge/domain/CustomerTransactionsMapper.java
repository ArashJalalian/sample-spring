package com.challenge.domain;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class CustomerTransactionsMapper implements FieldSetMapper<CustomerTransactions> {
    @Override
    public CustomerTransactions mapFieldSet(FieldSet fieldSet) throws BindException {
        CustomerTransactions customerTransactions = new CustomerTransactions();
        customerTransactions.setAccountNumber(fieldSet.readString(0));
        customerTransactions.setTransactionAmount(fieldSet.readBigDecimal(1));
        return customerTransactions;
    }
}
