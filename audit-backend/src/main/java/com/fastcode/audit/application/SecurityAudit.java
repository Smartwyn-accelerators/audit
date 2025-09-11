package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Component
@ConditionalOnClass(name = "org.springframework.security.authentication.event.AuthenticationSuccessEvent")
public class SecurityAudit {

    @Autowired
    private AuditPropertiesConfiguration env;

    @Autowired
    private AuditService customAuditService;


    @EventListener
    public void onAuthenticationSuccess(Object event) {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.AuthenticationSuccessEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();

                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("authorities", roles);
                details.put("authenticationMethod", "LOGIN");
                details.put("result", "SUCCESS");

                customAuditService.logSecurityAudit(
                        "AUTH_SUCCESS",
                        username,
                        "SECURITY_CONTEXT",
                        "User authentication successful",
                        details
                );
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }

    @EventListener
    public void onLogoutSuccess(Object event) {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.LogoutSuccessEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();

                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("authorities", roles);
                details.put("authenticationMethod", "LOGOUT");
                details.put("result", "SUCCESS");

                customAuditService.logSecurityAudit(
                        "AUTH_LOGOUT",
                        username,
                        "SECURITY_CONTEXT",
                        "User logout successful",
                        details
                );
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }

    @EventListener
    public void onAuthenticationFailure(Object event) {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();

                Map<String, Object> details = new HashMap<>();
                details.put("username", username);
                details.put("authorities", roles);
                details.put("authenticationMethod", "LOGIN");
                details.put("result", "FAILURE");
                details.put("failureReason", "BAD_CREDENTIALS");

                customAuditService.logSecurityAudit(
                        "AUTH_FAILURE",
                        username,
                        "SECURITY_CONTEXT",
                        "User authentication failed",
                        details
                );
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }



    // Add methods for tracking other user activities, such as navigation and interactions
    public void trackUserNavigation(String username, String page) {
        if (!env.isAuditSecurityDisabled()) {
            Map<String, Object> details = new HashMap<>();
            details.put("username", username);
            details.put("page", page);
            details.put("navigationEvent", true);

            // Apply sensitive data masking if enabled
            if (env.isSensitiveDataMaskingEnabled()) {
                details = PrivacyAwareUtils.maskMap(details, env.getSensitiveDataKeys());
            }

            customAuditService.logSecurityAudit(
                    "USER_NAVIGATION",
                    username,
                    "SECURITY_CONTEXT",
                    "User navigation tracked",
                    details
            );
        }
    }

    // Method to track password changes
    public void trackPasswordChange(String username) {
        if (!env.isAuditSecurityDisabled()) {
            Map<String, Object> details = new HashMap<>();
            details.put("username", username);
            details.put("passwordChangeEvent", true);

            // Apply sensitive data masking if enabled
            if (env.isSensitiveDataMaskingEnabled()) {
                details = PrivacyAwareUtils.maskMap(details, env.getSensitiveDataKeys());
            }

            customAuditService.logSecurityAudit(
                    "PASSWORD_CHANGE",
                    username,
                    "SECURITY_CONTEXT",
                    "Password change tracked",
                    details
            );
        }
    }

    // Method to track authorization actions
    public void trackAuthorizationAction(String username, String action) {
        if (!env.isAuditSecurityDisabled()) {
            Map<String, Object> details = new HashMap<>();
            details.put("username", username);
            details.put("action", action);
            details.put("authorizationEvent", true);

            // Apply sensitive data masking if enabled
            if (env.isSensitiveDataMaskingEnabled()) {
                details = PrivacyAwareUtils.maskMap(details, env.getSensitiveDataKeys());
            }

            customAuditService.logSecurityAudit(
                    "AUTHORIZATION_ACTION",
                    username,
                    "SECURITY_CONTEXT",
                    "Authorization action tracked",
                    details
            );
        }
    }
}
