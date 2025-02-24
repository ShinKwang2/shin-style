package shinstyle.batch.config;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@SpringBatchTest
@SpringBootTest
class CustomerFileJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    @Qualifier("customerFileJob")
    private Job customerFileJob;

    @Test
    void customerFileJobTest() throws Exception {
        // Given
        JobParameters jobParameters = new JobParametersBuilder(jobExplorer)
                .addString("datetime", LocalDateTime.now().toString())
                .toJobParameters();
        jobLauncherTestUtils.setJob(customerFileJob);

        // When
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // Then
        assertThat(ExitStatus.COMPLETED).isEqualTo(jobExecution.getExitStatus());
    }
}
