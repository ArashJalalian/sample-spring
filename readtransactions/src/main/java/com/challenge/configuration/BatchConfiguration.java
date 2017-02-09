package com.challenge.configuration;

import com.challenge.TransactionItemProcessor;
import com.challenge.TransactionsPartitioner;
import com.challenge.domain.CustomerTransactions;
import com.challenge.domain.CustomerTransactionsMapper;
import com.challenge.listeners.JobCompletionNotificationListener;
import com.challenge.listeners.TransactionsStepExecutionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.util.Date;

@Configuration
@EnableBatchProcessing
@Import({BatchSchedulerConfiguration.class})
public class BatchConfiguration {

    private static Logger log = LoggerFactory.getLogger(BatchConfiguration.class);

    @Value("${environmentVariableName}")
    private String environmentVariableName;

    @Autowired
    private ResourcePatternResolver resourcePatternResolver;

    @Autowired
    private SimpleJobLauncher jobLauncher;

    @Autowired
    private JobBuilderFactory jobBuilderFactory;

    @Autowired
    private StepBuilderFactory stepBuilderFactory;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TaskExecutor taskExecutor;

    @Autowired
    private FlatFileItemReader<CustomerTransactions> flatFileReader;

    @Autowired
    private JobCompletionNotificationListener jobCompletionNotificationListener;


    @Scheduled(cron = "0 0 6,21 * * *")
    public void perform() throws Exception {
        log.info("Job Started at: {}", new Date());
        final JobParameters param = new JobParametersBuilder().addString("JobID",
                String.valueOf(System.currentTimeMillis())).toJobParameters();
        final JobExecution execution = jobLauncher.run(importTransactionJob(), param);
        log.info("Job finished with status: {}", execution.getStatus());
    }


    @Bean(name = "flatFileReader")
    @StepScope
    public FlatFileItemReader<CustomerTransactions> reader(
            @Value("#{stepExecutionContext['fileName']}") String filePath) {
        final FlatFileItemReader<CustomerTransactions> reader = new FlatFileItemReader<>();
        reader.setResource(resourcePatternResolver.getResource(filePath));
        reader.setLineMapper(new DefaultLineMapper<CustomerTransactions>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames(new String[] { "accountNumber", "transactionAmount" });
            }});
            setFieldSetMapper(new CustomerTransactionsMapper());
        }});
        return reader;
    }

    @Bean
    public TransactionItemProcessor processor() {
        return new TransactionItemProcessor();
    }


    @Bean
    public JdbcBatchItemWriter<CustomerTransactions> writer() {
        JdbcBatchItemWriter<CustomerTransactions> writer = new JdbcBatchItemWriter<>();
        writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
        writer.setSql("INSERT INTO customer_transactions (account_number, amount) VALUES " +
                "(:accountNumber, :transactionAmount)");
        writer.setDataSource(dataSource);
        return writer;
    }

    @Bean
    public JobCompletionNotificationListener jobCompletionNotificationListener() {
        return new JobCompletionNotificationListener();
    }

    @Bean
    public TransactionsStepExecutionListener skippedItemStepExecutionListener() {
        return new TransactionsStepExecutionListener();
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setMaxPoolSize(4);
        taskExecutor.afterPropertiesSet();
        return taskExecutor;
    }

    @Bean
    public Job importTransactionJob() {
        return jobBuilderFactory.get("importTransactionJob")
                .incrementer(new RunIdIncrementer())
                .listener(jobCompletionNotificationListener)
                .start(masterStep())
                .build();
    }


    private Step slaveStep() {
        return stepBuilderFactory.get("slave.step")
                .<CustomerTransactions, CustomerTransactions> chunk(1000)
                .reader(flatFileReader)
                .processor(processor())
                .writer(writer())
                .taskExecutor(taskExecutor)
                .throttleLimit(4)
                .listener(skippedItemStepExecutionListener())
                .build();
    }

    private Partitioner transactionsPartitioner() {
        final TransactionsPartitioner partitioner = new TransactionsPartitioner();
        final String env = System.getenv(environmentVariableName);
        File pendingDir = new File(env + File.separator + "pending");

        Resource[] resources;
        try {
            resources = resourcePatternResolver.getResources("file:" + pendingDir.getAbsolutePath() + File.separator + "*.csv");
        } catch (IOException e) {
            throw new RuntimeException("I/O problems when resolving the input file pattern.", e);
        }
        partitioner.setResources(resources);
        return partitioner;
    }

    private Step masterStep() {
        return stepBuilderFactory.get("master.step")
                .partitioner(slaveStep())
                .partitioner("slave.step", transactionsPartitioner())
                .build();
    }

}
