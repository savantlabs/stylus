package io.savantlabs.stylus.api.controller;

import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import jakarta.inject.Inject;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/sessions")
public class SessionResource {

  @Inject private SessionService sessionService;

  @GetMapping
  @ResponseBody
  List<Session> listSessions() {
    return sessionService.listSessions();
  }

  @PostMapping
  @ResponseBody
  Session startSession() {
    return sessionService.startSession();
  }
}
