package dev.annopud.jpa_demo.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LoggingListener {

    static final Logger log = LoggerFactory.getLogger(LoggingListener.class);

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMPLETION)
//    @Transactional(propagation = Propagation.NOT_SUPPORTED)
//    @EventListener
    @Async
    public void handleLogging(LoggingEvent event) throws InterruptedException {
//        log.info("Start handle logging, {}", TransactionAspectSupport.currentTransactionStatus().getTransactionName());
        log.info("Start handle logging");
        Thread.sleep(5000);
        log.info("End handle logging");
    }

    public static class LoggingEvent {
        private final String message;

        public LoggingEvent(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }
}
