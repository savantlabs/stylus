package io.savantlabs.stylus.api.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/hello")
public class HelloResource {

  @GetMapping
  public String getHello() {
    log.warn("Hello world!");
    return "Hello world!";
  }
}
