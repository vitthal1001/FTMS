package com.neobankx.account.infrastructure.events;

import com.neobankx.account.application.AccountEventPublisher;
import com.neobankx.common.observability.CorrelationIdFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.util.Map;
import java.util.UUID;

@Component
public class KafkaAccountEventPublisher implements AccountEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(KafkaAccountEventPublisher.class);

    private final KafkaTemplate<String, AccountEvent> kafkaTemplate;
    private final Clock clock;

    public KafkaAccountEventPublisher(KafkaTemplate<String, AccountEvent> kafkaTemplate, Clock clock) {
        this.kafkaTemplate = kafkaTemplate;
        this.clock = clock;
    }

    @Override
    public void publish(String topic, UUID accountId, Map<String, Object> metadata) {
        AccountEvent event = new AccountEvent(
                UUID.randomUUID(),
                topic,
                accountId,
                clock.instant(),
                MDC.get(CorrelationIdFilter.MDC_KEY),
                Map.copyOf(metadata)
        );
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(topic, accountId, event);
                }
            });
            return;
        }
        send(topic, accountId, event);
    }

    private void send(String topic, UUID accountId, AccountEvent event) {
        kafkaTemplate.send(topic, accountId.toString(), event)
                .whenComplete((result, failure) -> {
                    if (failure != null) {
                        log.error("account_event_publish_failed topic={} accountId={}", topic, accountId, failure);
                        return;
                    }
                    log.info("account_event_published topic={} accountId={}", topic, accountId);
                });
    }
}

