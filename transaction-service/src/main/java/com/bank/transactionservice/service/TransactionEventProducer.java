package com.bank.transactionservice.service;

import com.bank.transactionservice.event.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "bank.transactions.created";

    public void publishTransactionCreated(TransactionCreatedEvent event) {
        // Clé du message = accountId (consigne Partie F.2)
        kafkaTemplate.send(TOPIC, event.accountId().toString(), event);
    }
}
