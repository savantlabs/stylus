package io.savantlabs.stylus.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoggingTests {

  @Test
  void testLogging() {
    log.info("This is a testing log.");
  }
}
