package com.challenge.listeners;

import com.challenge.domain.ProcessedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.listener.StepExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TransactionsStepExecutionListener extends StepExecutionListenerSupport {

    @Value("${environmentVariableName}")
    private String environmentVariableName;

    private static Logger log = LoggerFactory.getLogger(TransactionsStepExecutionListener.class);
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        final ProcessedFile processedFile = ProcessedFile.from(stepExecution);
        final String env = System.getenv(environmentVariableName);
        try {
            processedFile.moveCompleted(env);
        } catch (Exception e) {
            log.error("Unable to move processed file into processed directory.", e);
        }
        try {
            processedFile.writeToReportFile(env);
        } catch (Exception e) {
            log.error("Unable to create report of a processed file.", e);
        }

        log.info("{}", processedFile);
        return null;
    }
}
