package com.example.demo;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
public class TraceInterceptor implements HandlerInterceptor {
  private static final String TRACE_ID_HEADER = "X-Trace-ID";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String traceID = UUID.randomUUID().toString();

    HttpServletRequest requestWithHeaders = new HttpServletRequestWrapper(request) {
      private final Map<String, String> headers = new HashMap<>();

      {
        if (headers.get(TRACE_ID_HEADER) == null) {
          headers.put(TRACE_ID_HEADER, traceID);
        };
      }

      @Override
      public String getHeader(String name) {
        if (headers.containsKey(name)) {
          return headers.get(name);
        }

        return super.getHeader(name);
      }
    };

    request.setAttribute("traceID", traceID);

    return true;
  }
}
