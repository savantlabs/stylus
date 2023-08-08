package io.savantlabs.stylus.api.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.savantlabs.stylus.api.framework.FunctionalTestsBase;
import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import io.savantlabs.stylus.test.TestTag;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Slf4j
public class SessionServiceImplTests extends FunctionalTestsBase {

  @Inject private SessionService sessionService;

  @Test
  @Tag(TestTag.FUNCTIONAL)
  void testStartSessionAndKernel() throws Exception {
    //  JupyterClient client = ((SessionServiceImpl) sessionService).getJupyterClientForTest();
    Session session = sessionService.startSession();
    // Verifying that the session's ID is not null
    assertNotNull(session.getId());
    // Verifying that the session's kernelId is initially an empty string
    assertEquals("", session.getKernelId());
    // Starting a kernel for the session
    Object kernel = sessionService.startSessionKernel(session.getId());
    // Verifying that the session's kernelId is not null
    assertNotNull(session.getKernelId());
  }

  //  @Test
  //  @Tag(TestTag.FUNCTIONAL)
  //  void apiTestingStartSession() throws Exception {
  //    StylusApplication.main(new String[]{});
  //
  //    ApiProxy proxy = getApiProxy();
  //    JsonNode res = proxy.get(getRestEndpoint("/startSession"), JsonNode.class);
  //    System.out.println("resp: {}"+res); // to be removed
  //
  //    mockMvc.perform(get("/api/startSession"))
  //        .andExpect(status().isOk());
  //  }
}
