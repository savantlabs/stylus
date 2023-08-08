package io.savantlabs.stylus.api.controller;

import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
//@RequestMapping("/sessions")
public class SessionResource {

  @Inject private SessionService sessionService;

  @GetMapping("/sessions")
  @ResponseBody
  List<Session> listSessions() {
    return sessionService.listSessions();
  }

  @PostMapping("/sessions")
  @ResponseBody
  Session startSession() {
    return sessionService.startSession();
  }

  @GetMapping("/sessions/{sessionId}")
  @ResponseBody
  Session getSession(@PathVariable(value = "sessionId") String sessionId) {
    return sessionService.getSession(sessionId);
  }

  @DeleteMapping("/sessions/{sessionId}")
  @ResponseBody
  void stopSession(@PathVariable(value = "sessionId") String sessionId) {
    sessionService.stopSession(sessionId);
  }

  @PostMapping("/sessions/{sessionId}/kernels")
  @ResponseBody
  Object startSessionKernel(@PathVariable(value = "sessionId") String sessionId)
      throws InterruptedException {
    return sessionService.startSessionKernel(sessionId);
  }
}
