package com.challenge;

import com.challenge.domain.CustomerTransactions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.util.*;

public class TransactionItemProcessor implements ItemProcessor<CustomerTransactions, CustomerTransactions> {

    private static final Logger log = LoggerFactory.getLogger(TransactionItemProcessor.class);
    public static Map<Long, BigDecimal> debits = new HashMap<>();
    public static Map<Long, BigDecimal> credits = new HashMap<>();
    public static Map<Long, Set<Integer>> accounts = new HashMap<>();

    private Long stepId;

    @SuppressWarnings("unused")
    @BeforeStep
    public void getInterStepData(StepExecution stepExecution) {
        stepId = stepExecution.getId();
    }

    @Override
    public CustomerTransactions process(final CustomerTransactions customerTransactions) throws Exception {
        final String accountNumber = customerTransactions.getAccountNumber();
        final BigDecimal transactionAmount = customerTransactions.getTransactionAmount();
        int accountNumberInt;
        try {
            accountNumberInt = Integer.parseInt(accountNumber);
        } catch (NumberFormatException e) {
            log.info("Account number ({}) is not valid.", accountNumber);
            return null;
        }

        updateBookkeeping(stepId, transactionAmount);
        updateAccounts(stepId, accountNumberInt);

        return new CustomerTransactions(accountNumber, transactionAmount);
    }

    private synchronized static void updateBookkeeping(long stepId, BigDecimal transactionAmount) {
        if (transactionAmount.compareTo(BigDecimal.ZERO) < 0) {
            if (!debits.containsKey(stepId)) {
                debits.put(stepId, transactionAmount.negate());
            } else {
                BigDecimal debit = debits.get(stepId);
                debits.put(stepId, debit.subtract(transactionAmount));
            }
        } else {
            if (!credits.containsKey(stepId)) {
                credits.put(stepId, transactionAmount);
            } else {
                BigDecimal credit = credits.get(stepId);
                credits.put(stepId, credit.add(transactionAmount));
            }
        }
    }


    private synchronized static void updateAccounts(long stepId, int accountNumber) {
        if (!accounts.containsKey(stepId)) {
            Set<Integer> account = new HashSet<>();
            account.add(accountNumber);
            accounts.put(stepId, account);
        } else {
            Set<Integer> accountNumbers = accounts.get(stepId);
            accountNumbers.add(accountNumber);
        }
    }
}
