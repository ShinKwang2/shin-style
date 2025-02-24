package shinstyle.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class StepPerformanceListener implements StepExecutionListener {

    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepExecution.getExecutionContext().putLong("startTime", System.currentTimeMillis());
        log.info("Step {} 시작", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long startTime = stepExecution.getExecutionContext().getLong("startTime");
        long endTime = System.currentTimeMillis();

        log.info("Step: {} 통계", stepExecution.getStepName());
        log.info("처리 시간: {}ms", endTime - startTime);
        log.info("읽은 아이템 수: {}", stepExecution.getReadCount());
        log.info("처리된 아이템 수: {}", stepExecution.getWriteCount());
        log.info("건너뛴 아이템 수: {}", stepExecution.getSkipCount());

        return stepExecution.getExitStatus();
    }
}
