package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.zalando.logbook.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Custom API audit implementation to replace audit4j ApiAudit
 * Intercepts HTTP requests and responses for auditing
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiAudit implements Sink {

    private final AuditService customAuditService;
    private final AuditPropertiesConfiguration env;

	
	@Override
	public boolean isActive() {
		boolean active = !env.isAuditApiDisabled();
		System.out.println("ApiAudit.isActive() called - returning: " + active + ", auditApiDisabled: " + env.isAuditApiDisabled());
        return active;
    }

	@Override
	public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
		System.out.println("ApiAudit.write(Precorrelation) called for path: " + request.getPath());
		if (!request.getPath().contains("/audit") && !isPathExcluded(request.getPath())) {
			try {
				auditBeforeApiCall(request);
			} catch (Exception ex) {
                try {
                    auditErrorApiCall(request, ex);
                } catch (Exception e) {
                    log.error("Error in API error audit: {}", e.getMessage(), e);
                }
            }
        } else {
            log.debug("CustomApiAudit.write(Precorrelation) - path excluded: {}", request.getPath());
        }
    }

    @Override
    public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
        log.debug("CustomApiAudit.write(Correlation) called for path: {}", request.getPath());
        if (!request.getPath().contains("/audit") && !isPathExcluded(request.getPath())) {
            try {
                auditAfterApiCall(request, response, correlation);
            } catch (Exception ex) {
                log.error("Error in API audit after call: {}", ex.getMessage(), ex);
            }
        }
    }

    /**
     * Audit before API call
     */
    private void auditBeforeApiCall(HttpRequest request) {
        try {
            String actor = getCurrentActor();
            String origin = getOrigin(request);
            
            Map<String, Object> details = new HashMap<>();
            details.put("httpMethod", request.getMethod());
            details.put("path", request.getPath());
            details.put("query", request.getQuery());
            details.put("scheme", request.getScheme());
            details.put("contentType", request.getContentType());
            details.put("userAgent", getUserAgent(request));
            details.put("headers", getHeadersAsString(request));
            
            if (request.getBody() != null) {
                String body = request.getBody().toString();
                // Apply sensitive data masking if enabled
                if (env.isSensitiveDataMaskingEnabled()) {
                    body = PrivacyAwareUtils.maskSensitiveData(body, env.getSensitiveDataKeys());
                }
                details.put("body", body);
            }

            customAuditService.logApiAudit(
                request.getMethod(),
                request.getPath(),
                actor,
                origin,
                "PENDING",
                request.getContentType(),
                getUserAgent(request),
                    "API_REQUEST"
            );
        } catch (Exception e) {
            log.error("Error in auditBeforeApiCall: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit after API call
     */
    private void auditAfterApiCall(HttpRequest request, HttpResponse response, Correlation correlation) {
        try {
            String actor = getCurrentActor();
            String origin = getOrigin(request);
            
            Map<String, Object> details = new HashMap<>();
            details.put("httpMethod", request.getMethod());
            details.put("path", request.getPath());
            details.put("query", request.getQuery());
            details.put("responseStatus", String.valueOf(response.getStatus()));
            details.put("contentType", response.getContentType());
            details.put("responseTime", correlation.getDuration().toMillis() + "ms");
            details.put("headers", getHeadersAsString(response));
            
            if (response.getBody() != null) {
                String responseBody = response.getBody().toString();
                // Apply sensitive data masking if enabled
                if (env.isSensitiveDataMaskingEnabled()) {
                    responseBody = PrivacyAwareUtils.maskSensitiveData(responseBody, env.getSensitiveDataKeys());
                }
                details.put("responseBody", responseBody);
            }

            customAuditService.logApiAudit(
                request.getMethod(),
                request.getPath(),
                actor,
                origin,
                String.valueOf(response.getStatus()),
                response.getContentType(),
                getUserAgent(request),
                "API_RESPONSE"
            );
        } catch (Exception e) {
            log.error("Error in auditAfterApiCall: {}", e.getMessage(), e);
        }
    }

    /**
     * Audit API error
     */
    private void auditErrorApiCall(HttpRequest request, Exception ex) {
        try {
            String actor = getCurrentActor();
            String origin = getOrigin(request);
            
            Map<String, Object> details = new HashMap<>();
            details.put("httpMethod", request.getMethod());
            details.put("path", request.getPath());
            details.put("query", request.getQuery());
            details.put("exceptionType", ex.getClass().getSimpleName());
            details.put("message", ex.getMessage());
            details.put("userAgent", getUserAgent(request));

            customAuditService.logErrorAudit(
                "API_ERROR",
                actor,
                origin,
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                details
            );
        } catch (Exception e) {
            log.error("Error in auditErrorApiCall: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if path should be excluded from auditing
     */
    private boolean isPathExcluded(String requestPath) {
        List<String> pathsList = Arrays.asList(env.getExcludedApiPath().split("\\s*,\\s*"));
        return pathsList.stream().anyMatch(requestPath::equals);
    }

    /**
     * Get current actor from security context
     */
    private String getCurrentActor() {
        try {
            // Try to get from Spring Security context if available
            Class<?> securityContextClass = Class.forName("org.springframework.security.core.context.SecurityContextHolder");
            Object context = securityContextClass.getMethod("getContext").invoke(null);
            
            if (context != null) {
                Object authentication = context.getClass().getMethod("getAuthentication").invoke(context);
                if (authentication != null) {
                    Boolean isAuthenticated = (Boolean) authentication.getClass().getMethod("isAuthenticated").invoke(authentication);
                    if (isAuthenticated != null && isAuthenticated) {
                        String name = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                        if (name != null && !"anonymousUser".equals(name)) {
                            return name;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Could not get actor from security context: {}", e.getMessage());
        }

        return "ANONYMOUS";
    }

    /**
     * Get origin from request
     */
    private String getOrigin(HttpRequest request) {
        try {
            return request.getRemote();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Get user agent from request
     */
    private String getUserAgent(HttpRequest request) {
        try {
            Object userAgent = request.getHeaders().get("User-Agent");
            return userAgent != null ? userAgent.toString() : "UNKNOWN";
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * Get headers as string representation
     */
    private String getHeadersAsString(HttpMessage message) {
        try {
            StringBuilder headers = new StringBuilder();
            message.getHeaders().forEach((key, value) -> {
                if (headers.length() > 0) {
                    headers.append(", ");
                }
                headers.append(key).append(": ").append(value);
            });
            return headers.toString();
        } catch (Exception e) {
            return "Error getting headers: " + e.getMessage();
        }
    }
}
