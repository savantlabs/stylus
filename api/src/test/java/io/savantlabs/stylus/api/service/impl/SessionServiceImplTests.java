package io.savantlabs.stylus.api.service.impl;

import io.savantlabs.stylus.api.framework.FunctionalTestsBase;
import io.savantlabs.stylus.api.service.SessionService;
import io.savantlabs.stylus.test.TestTag;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SessionServiceImplTests extends FunctionalTestsBase {

  @Inject private SessionService sessionService;

  @Test
  @Tag(TestTag.FUNCTIONAL)
  void testStartSessionAndKernel() {

  }

}
