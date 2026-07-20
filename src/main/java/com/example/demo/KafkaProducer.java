package com.example.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

@Service
public class KafkaProducer {
  private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
  private final KafkaTemplate<String, Object> kafkaTemplate;

  @Value("${spring.kafka.topic.default}")
  private String topic;

  public KafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  public void sendMessage(Object message) {
    sendMessage(topic, message);
  }

  private void sendMessage(String topic, Object message) {
    try {
      SendResult<String, Object> result = kafkaTemplate.send(topic, message).get();
      log.info("message sent successfully to topic: {}, partition: {}, offset: {}",
              topic, result.getRecordMetadata().partition(),
              result.getRecordMetadata().offset());
    } catch (Exception e) {
      log.error("error sending message to topic: {}", topic, e);
      throw new RuntimeException("failed to send message to kafka", e);
    }
  }
}
