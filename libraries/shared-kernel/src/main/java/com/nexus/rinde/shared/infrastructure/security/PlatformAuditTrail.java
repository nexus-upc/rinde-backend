package com.nexus.rinde.shared.infrastructure.security;

import com.nexus.rinde.shared.interfaces.rest.PageResource;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;

/** Auditoría transaccional en el esquema propio del servicio que ejecuta la acción. */
public final class PlatformAuditTrail {
  private final JdbcTemplate jdbc;
  private final String table;

  public PlatformAuditTrail(JdbcTemplate jdbc, String schema) {
    if (!List.of("iam", "subscription").contains(schema))
      throw new IllegalArgumentException("Esquema de auditoría inválido.");
    this.jdbc = jdbc;
    this.table = schema + ".platform_audit";
  }

  public void record(
      PlatformPrincipal actor,
      String action,
      String type,
      UUID target,
      String reason,
      String before,
      String after,
      Instant at) {
    jdbc.update(
        "insert into "
            + table
            + " (id, actor_id, actor_email, action, target_type, target_id, reason, before_state,"
            + " after_state, occurred_at) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
        UUID.randomUUID(),
        actor.id(),
        actor.email(),
        action,
        type,
        target,
        reason,
        before,
        after,
        Timestamp.from(at));
  }

  public PageResource<Entry> list(int page, int size) {
    if (page < 0 || page > 1000000 || size < 1 || size > 100)
      throw new IllegalArgumentException("Paginación inválida.");
    Long total = jdbc.queryForObject("select count(*) from " + table, Long.class);
    var items =
        jdbc.query(
            "select * from " + table + " order by occurred_at desc, id desc limit ? offset ?",
            (rs, row) ->
                new Entry(
                    rs.getObject("id", UUID.class),
                    rs.getString("actor_email"),
                    rs.getString("action"),
                    rs.getString("target_type"),
                    rs.getObject("target_id", UUID.class),
                    rs.getString("reason"),
                    rs.getString("before_state"),
                    rs.getString("after_state"),
                    rs.getTimestamp("occurred_at").toInstant()),
            size,
            (long) page * size);
    return new PageResource<>(List.copyOf(items), page, size, total == null ? 0 : total);
  }

  public record Entry(
      UUID id,
      String actorEmail,
      String action,
      String targetType,
      UUID targetId,
      String reason,
      String beforeState,
      String afterState,
      Instant occurredAt) {}
}
