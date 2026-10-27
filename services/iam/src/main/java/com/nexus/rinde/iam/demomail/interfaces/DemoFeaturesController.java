package com.nexus.rinde.iam.demomail.interfaces;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

/** Expone solo la disponibilidad del modo; nunca destinatarios ni enlaces. */
@RestController
@RequestMapping("/api/v1/demo/features")
public class DemoFeaturesController {
  private final boolean enabled;

  public DemoFeaturesController(@Value("${rinde.demo.mailbox.enabled:false}") boolean enabled) {
    this.enabled = enabled;
  }

  @GetMapping
  public ResponseEntity<Features> features() {
    return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(new Features(enabled));
  }

  public record Features(boolean mailboxEnabled) {}
}
