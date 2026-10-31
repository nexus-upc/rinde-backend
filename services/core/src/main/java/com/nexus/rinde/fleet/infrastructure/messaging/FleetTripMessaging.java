package com.nexus.rinde.fleet.infrastructure.messaging;

import com.nexus.rinde.fleet.infrastructure.persistence.jpa.repositories.VehicleRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Confirma estado de flota y recibo del evento en la misma transacción. */
@Component
public class FleetTripMessaging {
  private final VehicleRepository vehicles;
  private final JdbcTemplate jdbc;

  public FleetTripMessaging(VehicleRepository vehicles, JdbcTemplate jdbc) {
    this.vehicles = vehicles;
    this.jdbc = jdbc;
  }

  @RabbitListener(
      queues =
          "${rinde.messaging.fleet.queue:${rinde.messaging.exchange:rinde.events}.fleet.trip-events}")
  @Transactional
  public void receive(Event event, org.springframework.amqp.core.Message message) {
    if (event.eventId() == null || event.tenantId() == null || event.vehicleId() == null)
      throw new IllegalArgumentException("Evento de viaje inválido.");
    int inserted =
        jdbc.update(
            "insert into fleet.processed_trip_events(event_id,processed_at) values(?,?) on conflict"
                + " do nothing",
            event.eventId(),
            java.sql.Timestamp.from(Instant.now()));
    if (inserted == 0) return;
    String key = message.getMessageProperties().getReceivedRoutingKey();
    vehicles
        .findByIdAndTenantId(event.vehicleId(), event.tenantId())
        .ifPresent(
            vehicle -> {
              if ("trip.started".equals(key)) vehicle.startTrip();
              else if ("trip.finished".equals(key)) vehicle.finishTrip();
              else throw new IllegalArgumentException("Tipo de evento de viaje inválido.");
            });
  }

  public record Event(UUID eventId, UUID tenantId, UUID vehicleId) {}

  @Configuration
  public static class Topology {
    @Bean
    public Declarables fleetTripBindings(
        @Value("${rinde.messaging.exchange:rinde.events}") String exchange,
        @Value(
                "${rinde.messaging.fleet.queue:${rinde.messaging.exchange:rinde.events}.fleet.trip-events}")
            String name) {
      var topic = new TopicExchange(exchange, true, false);
      var queue =
          QueueBuilder.durable(name)
              .deadLetterExchange(exchange + ".fleet.dead")
              .deadLetterRoutingKey(name)
              .build();
      var dead = new DirectExchange(exchange + ".fleet.dead", true, false);
      var deadQueue = QueueBuilder.durable(name + ".dead").build();
      return new Declarables(
          topic,
          queue,
          dead,
          deadQueue,
          BindingBuilder.bind(queue).to(topic).with("trip.started"),
          BindingBuilder.bind(queue).to(topic).with("trip.finished"),
          BindingBuilder.bind(deadQueue).to(dead).with(name));
    }
  }
}
