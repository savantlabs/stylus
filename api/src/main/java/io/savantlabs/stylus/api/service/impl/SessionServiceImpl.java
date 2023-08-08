package io.savantlabs.stylus.api.service.impl;

import io.savantlabs.stylus.api.model.Session;
import io.savantlabs.stylus.api.service.SessionService;
import io.savantlabs.stylus.sdk.jupyter.JupyterClient;
import io.savantlabs.stylus.sdk.jupyter.JupyterKernel;
import io.savantlabs.stylus.sdk.jupyter.JupyterLauncher;
import jakarta.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
// @Service
@RestController
public class SessionServiceImpl implements SessionService {

  public static final ConcurrentMap<String, Session> sessionStore = new ConcurrentHashMap<>();

  private final AtomicReference<JupyterClient> clientRef = new AtomicReference<>();
  private static final List<Session> ListSession = new ArrayList<>();

  @PreDestroy
  void preDestroy() {
    if (clientRef.get() != null) {
      clientRef.get().stop();
    }
  }

  @Override
  // @RequestMapping(value="/startSession",method = GET)
  public Session startSession() {
    //    Session session = new Session(UUID.randomUUID().toString(),"");
    //    ListSession.add(session);
    String sessionId = UUID.randomUUID().toString();
    Session session = new Session(sessionId, "");
    sessionStore.put(sessionId, session);
    log.info(sessionStore.get(sessionId).toString());
    return session;
  }

  // @RequestMapping(value="/listSessions",method = GET)
  @Override
  public List<Session> listSessions() {
//    for (String k : sessionStore.keySet()) {
//      ListSession.add(sessionStore.get(k));
//    }
//    return ListSession;
    for (String k : sessionStore.keySet()) {
      Session session = sessionStore.get(k);
      if (!ListSession.contains(session)) {
        ListSession.add(session);
      }
    }
    return ListSession;
  }
  // @RequestMapping(value="/getSession/{sessionId}",method = GET)
  @Override
  public Session getSession(String sessionId) {
    //    for(Session session : ListSession)
    //    {
    //      if(session.getId().equals(sessionId))
    //      {
    //        return session;
    //      }
    //    }
    if (sessionStore.containsKey(sessionId)) {
      return sessionStore.get(sessionId);
    }
    return null;
  }
  // @RequestMapping(value="/stopSession/{sessionId}",method = GET)
  @Override
  public void stopSession(String sessionId) {
    // stop the session's kernel first
    //    for(Session session : ListSession)
    //    {
    //      if(session.getId().equals(sessionId))
    //      {
    //        if(session.getKernelId() != null)
    //        {
    //          JupyterClientImpl client = new JupyterClientImpl();
    //          client.stopKernel(session.getKernelId());
    //          ListSession.remove(session);
    //        }
    //        else
    //        {
    //          return;
    //        }
    //      }
    //    }
    Session session = sessionStore.get(sessionId);
    if (session != null && session.getKernelId() != null) {
      JupyterClient jupyterClient = getJupyterClientForTest();
      jupyterClient.stopKernel(session.getKernelId());
    }
    sessionStore.remove(sessionId);
    ListSession.remove(session);
  }
  // @RequestMapping(value="/startSessionKernel/{sessionId}",method = GET)
  @Override
  public Object startSessionKernel(String sessionId) throws InterruptedException {
    JupyterClient jupyterClient = getJupyterClientForTest();
    //    for(Session session : ListSession)
    //    {
    //      if(session.getId().equals(sessionId))
    //      {
    //        JupyterKernel kernel = jupyterClient.startKernel();
    //        session.setKernelId(kernel.getId());
    //        return kernel;
    //      }
    //    }
    //    return null;
    Session session = sessionStore.get(sessionId);
    if (session != null) {
      JupyterKernel kernel = jupyterClient.startKernel();
      session.setKernelId(kernel.getId());
      return kernel;
    }
    return null;
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
