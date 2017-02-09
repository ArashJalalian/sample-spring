package com.challenge;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;


import com.challenge.configuration.ApplicationConfiguration;
import com.challenge.configuration.BatchConfiguration;
import com.challenge.configuration.BatchSchedulerConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@ContextConfiguration(classes = {ApplicationConfiguration.class, BatchConfiguration.class,
        BatchSchedulerConfiguration.class})
@TestPropertySource(locations="classpath:application.properties")
@RunWith(SpringJUnit4ClassRunner.class)
public class StepJobTests {

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job job;

    @Autowired
    private JobRepository jobRepository;


    @Test
    public void testLaunchJob() throws Exception {
        final JobParameters param = new JobParametersBuilder().addString("JobID",
                String.valueOf(System.currentTimeMillis())).toJobParameters();
        jobLauncher.run(job, param);
        assertThat(jobRepository.getLastJobExecution("importTransactionJob", param).getExitStatus(),
                is(ExitStatus.COMPLETED));
    }
}
