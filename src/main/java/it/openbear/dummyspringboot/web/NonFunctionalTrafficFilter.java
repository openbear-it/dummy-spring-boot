package it.openbear.dummyspringboot.web;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.openbear.dummyspringboot.traffic.DummyTrafficPlan;
import it.openbear.dummyspringboot.traffic.DummyTrafficPlanner;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class NonFunctionalTrafficFilter extends OncePerRequestFilter {

    private static final Logger ACCESS_LOGGER = LoggerFactory.getLogger("it.openbear.dummyspringboot.access");
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String DUMMY_TRAFFIC_HEADER = "X-Non-Functional-Traffic";

    private final DummyTrafficPlanner planner;
    private final ObjectMapper objectMapper;

    public NonFunctionalTrafficFilter(DummyTrafficPlanner planner, ObjectMapper objectMapper) {
        this.planner = planner;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = extractPath(request);
        if (isActuatorPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestId = resolveRequestId(request);
        DummyTrafficPlan plan = planner.plan();

        try {
            Thread.sleep(plan.delayMs());
        } catch (InterruptedException interruptedException) {
            Thread.currentThread().interrupt();
            throw new ServletException("Dummy traffic delay interrupted.", interruptedException);
        }

        MDC.put("request_id", requestId);
        MDC.put("request_method", request.getMethod());
        MDC.put("request_path", path);
        MDC.put("response_status", Integer.toString(plan.status().value()));
        MDC.put("response_delay_ms", Long.toString(plan.delayMs()));
        MDC.put("traffic_kind", "non_functional");

        try {
            response.setHeader(REQUEST_ID_HEADER, requestId);
            response.setHeader(DUMMY_TRAFFIC_HEADER, "true");
            response.setHeader(HttpHeaders.CACHE_CONTROL, "no-store");
            response.setStatus(plan.status().value());

            if (plan.status() != HttpStatus.NO_CONTENT) {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                response.getWriter().write(objectMapper.writeValueAsString(buildResponseBody(path, plan, requestId)));
            }

            plan.syntheticException().ifPresent(exception ->
                ACCESS_LOGGER.error("Synthetic failure generated for dummy traffic.", exception)
            );
            ACCESS_LOGGER.info("Handled non-functional traffic.");
        } finally {
            MDC.clear();
        }
    }

    private Map<String, Object> buildResponseBody(String path, DummyTrafficPlan plan, String requestId) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("requestId", requestId);
        response.put("path", path);
        response.put("status", plan.status().value());
        response.put("delayMs", plan.delayMs());
        response.put("message", "non-functional-traffic");
        return response;
    }

    private boolean isActuatorPath(String path) {
        return "/actuator".equals(path) || path.startsWith("/actuator/");
    }

    private String extractPath(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (contextPath == null || contextPath.isEmpty()) {
            return requestUri;
        }
        return requestUri.substring(contextPath.length());
    }

    private String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId;
    }
}
