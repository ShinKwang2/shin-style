package shinstyle.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

@Slf4j
public class JobLoggerListener implements JobExecutionListener {

    // 보통은 Job 시작전에 알림을 보내고 Job 끝난 후에 알림을 보내도록 설정한다.

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Job 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Job 종료: {} (상태: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());
    }
}
