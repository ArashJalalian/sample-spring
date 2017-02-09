package com.challenge;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.Random;

@SpringBootApplication
public class Application {
    private static final String COMMA_DELIMITER = ",";
    private static final String NEW_LINE_SEPARATOR = "\n";

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    /**
     * Call this method to create sample data in .csv format.
     */
    private static void createSampleFile(String filename) throws IOException {
        File file = new File(filename);
        Files.deleteIfExists(file.toPath());
        FileOutputStream fop = new FileOutputStream(file, true);
        Random rAccount = new Random();
        int minAccount = 1;
        int maxAccount = 1000;
        int minValue = -10000;
        int maxValue = 10000;
        for (int i=0; i<50000; i++) {
            int accountNumber = rAccount.nextInt(maxAccount-minAccount)+minAccount;
            double value = (double) (rAccount.nextInt(maxValue-minValue)+minValue)/100;
            final BigDecimal bigDecimal = BigDecimal.valueOf(value);
            fop.write((accountNumber + COMMA_DELIMITER + String.valueOf(bigDecimal) + NEW_LINE_SEPARATOR).getBytes());
        }
        fop.flush();
        fop.close();
    }
}
