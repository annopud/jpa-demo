package dev.annopud.jpa_demo.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {

    public static class CreateSuccessEvent {
        private final String status;

        public CreateSuccessEvent(String status) {
            this.status = status;
        }

        public String getStatus() {
            return status;
        }
    }

    @EventListener
    public void loggingEvent(CreateSuccessEvent event) {
        System.out.println(event);
        System.out.println("after event");
    }
}
