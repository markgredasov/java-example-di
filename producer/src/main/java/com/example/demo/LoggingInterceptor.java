package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;

@Component
public class LoggingInterceptor implements HandlerInterceptor {
  private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    request.setAttribute("startTime", Instant.now());
    request.setAttribute("method", request.getMethod());
    request.setAttribute("uri", request.getRequestURI());
    var traceID = (String) request.getAttribute("traceID");


    log.info(">>> {} {} registered new request | traceID: {}", request.getMethod(), request.getRequestURI(), traceID);
    return true;
  }


  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    Instant startTime = (Instant) request.getAttribute("startTime");
    long latency = java.time.Duration.between(startTime, Instant.now()).toMillis();

    String method = (String) request.getAttribute("method");
    String uri = (String) request.getAttribute("uri");
    var traceID =  (String) request.getAttribute("traceID");

    log.info("<<< {} {} | status: {} | latency: {}ms | traceID: {}", method, uri, response.getStatus(), latency, traceID);
  }
}