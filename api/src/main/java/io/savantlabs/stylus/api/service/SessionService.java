package io.savantlabs.stylus.api.service;

import io.savantlabs.stylus.api.model.Session;
import java.util.List;

public interface SessionService {

  // >>>>> session management >>>>>
  Session startSession();

  List<Session> listSessions();

  Session getSession(String sessionId);

  void stopSession(String sessionId);

  // <<<<< session management <<<<<


  // >>>>> kernel management >>>>>

  // should return JupyterKernel, after merging Test2
  Object startSessionKernel(String sessionId);

  // <<<<< kernel management <<<<<

}
