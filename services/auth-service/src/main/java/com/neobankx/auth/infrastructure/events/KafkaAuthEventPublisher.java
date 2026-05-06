package com.neobankx.auth.infrastructure.events;

import com.neobankx.auth.application.AuthEventPublisher;
import com.neobankx.common.observability.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaAuthEventPublisher implements AuthEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaAuthEventPublisher.class);

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;
    private final Clock clock;

    public KafkaAuthEventPublisher(KafkaTemplate<String, AuthEvent> kafkaTemplate, Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Override
    public void publish(String topic, UUID subject, String email, Map<String, Object> metadata) {
        AuthEvent event = new AuthEvent(
                UUID.randomUUID(),
                topic,
                subject,
                email,
                clock.instant(),
                MDC.get(CorrelationIdFilter.MDC_KEY),
                Map.copyOf(metadata)
        );
        kafkaTemplate.send(topic, subject == null ? email : subject.toString(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("auth_event_publish_failed topic={} subject={}", topic, subject, failure);
                        return;
                    }
                    log.info("auth_event_published topic={} subject={}", topic, subject);
                });
    }
}

