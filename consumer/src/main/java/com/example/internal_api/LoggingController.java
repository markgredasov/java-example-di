package com.example.internal_api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
public class LoggingController {
  private static final String TRACE_ID_HEADER = "X-Trace-ID";
  private static final Logger log = LoggerFactory.getLogger(LoggingController.class);

  @PostMapping("/internal/log")
  public Map<String, Object> logRequest(@RequestBody(required = false) String body, HttpServletRequest request) {
    String traceID = request.getHeader(TRACE_ID_HEADER);
    String cleanedBody = getCleanedBody(body);
    Map<String, Object> requestInfo = setRequestInfo(request, cleanedBody);

    log.info(">>> incoming request {} {} | traceID: {} | body: {}", request.getMethod(), request.getRequestURI(), traceID, cleanedBody);

    return getSuccessResponse(requestInfo);
  }

  private Map<String, Object> setRequestInfo(HttpServletRequest request, String body) {
    Map<String, Object> requestInfo = new HashMap<>();
    requestInfo.put("timestamp", Instant.now().toString());
    requestInfo.put("method", request.getMethod());
    requestInfo.put("uri", request.getRequestURI());
    requestInfo.put("body", body != null ? body : "null");

    return requestInfo;
  }

  private String getCleanedBody(String body) {
   return body.replaceAll("[\\r\\n\\t]", "").replaceAll("\\s+", " ").trim();
  }

  private Map<String, Object> getSuccessResponse(Map<String, Object> requestInfo) {
    Map<String, Object> response = new HashMap<>();
    response.put("status", "success");
    response.put("message", "request logged successfully");
    response.put("loggedRequest", requestInfo);

    return response;
  }
}