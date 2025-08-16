package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import org.audit4j.core.AuditManager;
import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.exception.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.zalando.logbook.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public final class ApiAudit implements Sink {

	@Autowired private AuditPropertiesConfiguration env;
	
	@Override
	public boolean isActive() {
        return !env.isAuditApiDisabled();
    }

	@Override
	public void write(final Precorrelation precorrelation, final HttpRequest request) throws IOException {
		if (!request.getPath().contains("/audit") && !isPathExcluded(request.getPath())) {
			try {
				auditBeforeApiCall(request);
			} catch (Exception ex) {
                try {
                    auditErrorApiCall(request, ex);
                } catch (HandlerException e) {
                    throw new RuntimeException(e);
                }
            }
		}
	}

	@Override
	public void write(final Correlation correlation, final HttpRequest request, final HttpResponse response) throws IOException {
		if (!request.getPath().contains("/audit") && !isPathExcluded(request.getPath())) {
			if (response.getStatus() >= 200 && response.getStatus() < 300) {
                try {
                    auditAfterApiCall(request, response);
                } catch (HandlerException e) {
                    throw new RuntimeException(e);
                }
            } else {
                try {
                    auditErrorApiCall(request, response);
                } catch (HandlerException e) {
                    throw new RuntimeException(e);
                }
            }
		}
	}

	public boolean isPathExcluded(String requestPath) {
		List<String> pathsList = Arrays.asList(env.getExcludedApiPath().split("\\s*,\\s*"));
		return pathsList.stream().anyMatch(requestPath::contains);
	}

	public void auditBeforeApiCall(HttpRequest request) throws IOException, HandlerException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setAction("API_REQUEST");
		auditEvent.addField("httpMethod", request.getMethod());
		auditEvent.addField("path", request.getPath());
		if (request.getQuery()!=null && !request.getQuery().isEmpty()) {
			auditEvent.addField("query", request.getQuery());
		}
		auditEvent.addField("scheme", request.getScheme());
		auditEvent.addField("contentType", request.getContentType());
		auditEvent.addField("body", request.getBodyAsString());
		auditEvent.addField("browser", request.getHeaders().get("user-agent").toString());
		auditEvent.addField("header", request.getHeaders().toString());
		if (env.isEncryptionEnabled()){
			AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
		}else {
			AuditManager.getInstance().audit(auditEvent);
		}
	}

	public void auditAfterApiCall(HttpRequest request, HttpResponse response) throws IOException, HandlerException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setAction("API_RESPONSE");
		auditEvent.addField("httpMethod", request.getMethod());
		auditEvent.addField("path", request.getPath());
		if (request.getQuery()!=null && !request.getQuery().isEmpty()) {
			auditEvent.addField("query", request.getQuery());
		}
		auditEvent.addField("responseStatus", String.valueOf(response.getStatus()));
		auditEvent.addField("scheme", request.getScheme());
		auditEvent.addField("contentType", request.getContentType());
		auditEvent.addField("body", request.getBodyAsString());
		auditEvent.addField("browser", request.getHeaders().get("user-agent").toString());
		auditEvent.addField("header", request.getHeaders().toString());
		if (env.isEncryptionEnabled()){
			AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
		}else {
			AuditManager.getInstance().audit(auditEvent);
		}
	}

	public void auditErrorApiCall(HttpRequest request, Exception ex) throws IOException, HandlerException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setAction("API_ERROR");
		auditEvent.addField("httpMethod", request.getMethod());
		auditEvent.addField("path", request.getPath());
		if (request.getQuery()!=null && !request.getQuery().isEmpty()) {
			auditEvent.addField("query", request.getQuery());
		}
		auditEvent.addField("exceptionType", ex.getClass().getSimpleName());
		auditEvent.addField("message", ex.getMessage());
		auditEvent.addField("scheme", request.getScheme());
		auditEvent.addField("contentType", request.getContentType());
		auditEvent.addField("body", request.getBodyAsString());
		auditEvent.addField("browser", request.getHeaders().get("user-agent").toString());
		auditEvent.addField("header", request.getHeaders().toString());
		if (env.isEncryptionEnabled()){
			AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
		}else {
			AuditManager.getInstance().audit(auditEvent);
		}
	}

	public void auditErrorApiCall(HttpRequest request, HttpResponse response) throws IOException, HandlerException {
		AuditEvent auditEvent = new AuditEvent();
		auditEvent.setAction("API_ERROR");
		auditEvent.addField("httpMethod", request.getMethod());
		auditEvent.addField("path", request.getPath());
		if (request.getQuery()!=null && !request.getQuery().isEmpty()) {
			auditEvent.addField("query", request.getQuery());
		}
		auditEvent.addField("responseStatus", String.valueOf(response.getStatus()));
		auditEvent.addField("scheme", request.getScheme());
		auditEvent.addField("contentType", request.getContentType());
		auditEvent.addField("body", request.getBodyAsString());
		auditEvent.addField("browser", request.getHeaders().get("user-agent").toString());
		auditEvent.addField("header", request.getHeaders().toString());
		if (env.isEncryptionEnabled()){
			AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
		}else {
			AuditManager.getInstance().audit(auditEvent);
		}
	}
}
