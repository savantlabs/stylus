package io.savantlabs.stylus.api.service.impl;

import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import io.savantlabs.stylus.sdk.jupyter.JupyterClient;
import io.savantlabs.stylus.sdk.jupyter.JupyterLauncher;
import jakarta.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SessionServiceImpl implements SessionService {

  public static final ConcurrentMap<String, Session> sessionStore = new ConcurrentHashMap<>();

  private final AtomicReference<JupyterClient> clientRef = new AtomicReference<>();

  @PreDestroy
  void preDestroy() {
    if (clientRef.get() != null) {
      clientRef.get().stop();
    }
  }

  @Override
  public Session startSession() {
    throw new UnsupportedOperationException();
  }

  @Override
  public List<Session> listSessions() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Session getSession(String sessionId) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void stopSession(String sessionId) {
    // stop the session's kernel first
    throw new UnsupportedOperationException();
  }

  @Override
  public Object startSessionKernel(String sessionId) {
    JupyterClient jupyterClient = getJupyterClient();
    throw new UnsupportedOperationException();
  }

  JupyterClient getJupyterClientForTest() {
    return getJupyterClient();
  }

  private JupyterClient getJupyterClient() {
    if (clientRef.get() == null) {
      synchronized (clientRef) {
        if (clientRef.get() == null) {
          JupyterClient client = JupyterLauncher.createClient();
          clientRef.set(client);
        }
      }
    }
    return clientRef.get();
  }
}
