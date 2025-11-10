package dev.annopud.jpa_demo.config;

import brave.propagation.CurrentTraceContext;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

@Configuration
public class ExecutorConfig {

//    @Bean
//    public Executor taskExecutor(CurrentTraceContext currentTraceContext) {
//        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
//        executor.setCorePoolSize(2);
//        executor.setMaxPoolSize(2);
//        executor.setQueueCapacity(500);
//        executor.setThreadNamePrefix("GithubLookup-");
//        executor.setTaskDecorator(currentTraceContext::wrap);
//
//        executor.initialize();
//        return executor;
//    }

//    @Bean("taskExecutor")
//    public Executor taskExecutor2(CurrentTraceContext currentTraceContext) {
////        ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
//        ThreadFactory factory = Thread.ofVirtual().name("v-thread-", 0L).factory();
//        ExecutorService virtualExecutor = Executors.newThreadPerTaskExecutor(factory)
//        // Wrap tasks to preserve trace context
//

    /// /
    /// /        return command -> virtualExecutor.execute(command);
//    }


//    @Bean("taskExecutor")
//    public Executor virtualTaskExecutor(CurrentTraceContext currentTraceContext) {
//        SimpleAsyncTaskExecutor executor = new SimpleAsyncTaskExecutor();
//        executor.setVirtualThreads(true); // Enable virtual threads
//        executor.setThreadNamePrefix("virtual-task-");
//        executor.setTaskDecorator(currentTraceContext::wrap);
//        return executor;
//    }
//    @Bean
//    public VirtualThreadTaskExecutor taskExecutor2(CurrentTraceContext currentTraceContext) {
//        // Optionally, provide a thread name prefix
//        return new VirtualThreadTaskExecutor("vt-thread-");
//    }

    // Commented out due to Java 21 virtual thread requirement
//    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
//    public AsyncTaskExecutor asyncTaskExecutor(CurrentTraceContext currentTraceContext) {
//        ThreadFactory factory = Thread.ofVirtual()
//            .name("vt-prefix-", 10000L)
//            .factory();
//        ExecutorService executor = Executors.newThreadPerTaskExecutor(factory);
//        TaskExecutorAdapter taskExecutorAdapter = new TaskExecutorAdapter(executor);
//        taskExecutorAdapter.setTaskDecorator(currentTraceContext::wrap);
//        return taskExecutorAdapter;
//    }

//    @Bean(TaskExecutionAutoConfiguration.APPLICATION_TASK_EXECUTOR_BEAN_NAME)
//    public AsyncTaskExecutor asyncTaskExecutor() {
//        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
//    }
}
