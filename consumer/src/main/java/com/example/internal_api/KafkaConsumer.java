package com.example.internal_api;

import com.example.demo.Reservation;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Component
public class KafkaConsumer {
  private static final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
  private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());;

  @KafkaListener(topics = "${spring.kafka.topic.default}")
  public void listen(ConsumerRecord<String, String> record) {
    String jsonMessage = record.value();
    log.info("kafka consumer got message from topic: {}: {}", record.topic(), jsonMessage);

    try {
      Reservation reservation = objectMapper.readValue(jsonMessage, Reservation.class);
      processReservation(reservation);

    } catch (Exception e) {
      log.error("failed to parse message to reservation: {}", e.getMessage());
      throw new RuntimeException("failed to parse reservation: " + jsonMessage, e);
    }
  }

  private void processReservation(Reservation reservation) {
    if (reservation == null) {
      throw new IllegalArgumentException("Reservation is null");
    }
  }
}