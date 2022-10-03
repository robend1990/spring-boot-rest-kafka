package com.faceit.assessment.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;

import java.util.concurrent.CountDownLatch;

@Slf4j
public class KafkaTestConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private ConsumerRecord<String, String> event;

    @KafkaListener(topics = "users")
    public void receive(ConsumerRecord<String, String> consumerRecord) {
        event = consumerRecord;
        log.info("Consumed event {}", consumerRecord.toString());
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public ConsumerRecord<String, String> getEvent() {
        return event;
    }
}
