package io.savantlabs.stylus.api.controller;

import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import jakarta.inject.Inject;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sessions")
public class SessionResource {

  @Inject private SessionService sessionService;

  @GetMapping("/listSessions")
  @ResponseBody
  List<Session> listSessions() {
    return sessionService.listSessions();
  }

  @PostMapping("/startSession")
  @ResponseBody
  Session startSession() {
    return sessionService.startSession();
  }
  @GetMapping("/getSession/{sessionId}")
  @ResponseBody
  Session getSession(@PathVariable(value = "sessionId") String sessionId) { return sessionService.getSession(sessionId);}
  @PostMapping("/stopSession/{sessionId}")
  @ResponseBody
  void stopSession(@PathVariable(value = "sessionId") String sessionId) {sessionService.stopSession(sessionId);}
  @GetMapping("/startSessionKernel/{sessionId}")
  @ResponseBody
  Object startSessionKernel(@PathVariable(value = "sessionId") String sessionId) throws InterruptedException {return sessionService.startSessionKernel(sessionId);}
}
