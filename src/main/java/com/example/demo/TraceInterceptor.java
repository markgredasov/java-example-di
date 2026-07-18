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
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String traceID = UUID.randomUUID().toString();

    HttpServletRequest requestWithHeaders = new HttpServletRequestWrapper(request) {
      private final Map<String, String> headers = new HashMap<>();


      {
        headers.put("X-Trace-ID", traceID);
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
