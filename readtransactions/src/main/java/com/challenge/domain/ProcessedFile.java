package com.challenge.domain;

import com.challenge.TransactionItemProcessor;
import org.springframework.batch.core.StepExecution;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.NumberFormat;

public class ProcessedFile {

    private final Path fileProcessed;
    private final int totalAccounts;
    private final BigDecimal totalDebits;
    private final BigDecimal totalCredit;
    private final int skippedTransactions;

    private ProcessedFile(Path fileProcessed, int totalAccounts, BigDecimal totalDebits,
                          BigDecimal totalCredit, int skippedTransactions) {
        this.fileProcessed = fileProcessed;
        this.totalAccounts = totalAccounts;
        this.totalDebits = totalDebits;
        this.totalCredit = totalCredit;
        this.skippedTransactions = skippedTransactions;
    }

    public static ProcessedFile from(final StepExecution stepExecution) {
        final int skipped = stepExecution.getFilterCount();
        final String filePath = stepExecution.getExecutionContext().get("fileName").toString().replace("file:", "");
        final Long stepId = stepExecution.getId();
        final int nAccounts = TransactionItemProcessor.accounts.get(stepId).size();
        final BigDecimal totalDebit = TransactionItemProcessor.debits.get(stepId);
        final BigDecimal totalCredit = TransactionItemProcessor.credits.get(stepId);

        return new ProcessedFile(Paths.get(filePath), nAccounts, totalDebit, totalCredit, skipped);
    }


    public void moveCompleted(String rootPath) throws IOException {
        final File processedDir = Paths.get(rootPath, "processed").toFile();
        if (!processedDir.exists()) {
            if (!processedDir.mkdir()) {
                throw new IOException("Unable to create processed directory. " + processedDir.getAbsolutePath());
            }
        }
        final Path processedTarget = Paths.get(processedDir.getAbsolutePath(), fileProcessed.getFileName().toString());
        Files.move(fileProcessed, processedTarget);
    }

    public void writeToReportFile(String rootPath) throws IOException {
        final File reportsDir = Paths.get(rootPath, "reports").toFile();
        if (!reportsDir.exists()) {
            if (!reportsDir.mkdir()) {
                throw new IOException("Unable to create reports directory." + reportsDir.getAbsolutePath());
            }
        }
        final String fileDate = fileProcessed.getFileName().toString().split("-")[1];
        final String reportFilename = "finance_customer_transactions_report-" +
                fileDate.replace(".csv", ".txt");
        final Path reportPath = Paths.get(reportsDir.getAbsolutePath(), reportFilename);
        final FileOutputStream fop = new FileOutputStream(reportPath.toFile(), false);
        try {
            fop.write(this.toString().getBytes());
        } finally {
            fop.flush();
            fop.close();
        }
    }

    @Override
    public String toString() {
        return  "File Processed: " + fileProcessed.getFileName().toString() +
                "\nTotal Accounts: " + totalAccounts +
                "\nTotal Debits   : " + NumberFormat.getCurrencyInstance().format(totalDebits) +
                "\nTotal Credits  : " + NumberFormat.getCurrencyInstance().format(totalCredit) +
                "\nSkipped Transactions: " + skippedTransactions;
    }

}
