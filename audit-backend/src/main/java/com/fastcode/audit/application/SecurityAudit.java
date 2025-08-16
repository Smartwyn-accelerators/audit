package com.fastcode.audit.application;

import com.fastcode.audit.AuditPropertiesConfiguration;
import com.fastcode.audit.utils.PrivacyAwareUtils;
import org.audit4j.core.AuditManager;
import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.exception.HandlerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

@Component
@ConditionalOnClass(name = "org.springframework.security.authentication.event.AuthenticationSuccessEvent")
public class SecurityAudit {

    @Autowired
    private AuditPropertiesConfiguration env;

    @EventListener
    public void onAuthenticationSuccess(Object event) throws HandlerException {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.AuthenticationSuccessEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();
                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setAction("LOGIN");
                auditEvent.addField("user", username);
                auditEvent.addField("authorities", roles);
                if (env.isEncryptionEnabled()) {
                    AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
                } else {
                    AuditManager.getInstance().audit(auditEvent);
                }
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }

    @EventListener
    public void onLogoutSuccess(Object event) throws HandlerException {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.LogoutSuccessEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();
                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setAction("LOGOUT");
                auditEvent.addField("user", username);
                auditEvent.addField("authorities", roles);

                if (env.isEncryptionEnabled()) {
                    AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
                } else {
                    AuditManager.getInstance().audit(auditEvent);
                }
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }

    @EventListener
    public void onAuthenticationFailure(Object event) throws HandlerException {
        if (event.getClass().getName().equals("org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent")) {
            try {
                Method getAuthentication = event.getClass().getMethod("getAuthentication");
                Object authentication = getAuthentication.invoke(event);
                String username = (String) authentication.getClass().getMethod("getName").invoke(authentication);
                String roles = (String) authentication.getClass().getMethod("getAuthorities")
                        .invoke(authentication).toString();
                AuditEvent auditEvent = new AuditEvent();
                auditEvent.setAction("FAILED_LOGIN");
                auditEvent.addField("user", username);
                auditEvent.addField("authorities", roles);

                if (env.isEncryptionEnabled()) {
                    AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
                } else {
                    AuditManager.getInstance().audit(auditEvent);
                }
            } catch (Exception e) {
                // Handle reflection exceptions
            }
        }
    }



    // Add methods for tracking other user activities, such as navigation and interactions
    public void trackUserNavigation(String username, String page) throws HandlerException {
        if (!env.isAuditSecurityDisabled()) {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setAction("NAVIGATION");
            auditEvent.addField("user", username);
            auditEvent.addField("page", page);

            if (env.isEncryptionEnabled()) {
                AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
            } else {
                AuditManager.getInstance().audit(auditEvent);
            }
        }
    }

    // Method to track password changes
    public void trackPasswordChange(String username) throws HandlerException {
        if (!env.isAuditSecurityDisabled()) {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setAction("PASSWORD_CHANGE");
            auditEvent.addField("user", username);

            if (env.isEncryptionEnabled()) {
                AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
            } else {
                AuditManager.getInstance().audit(auditEvent);
            }
        }
    }

    // Method to track authorization actions
    public void trackAuthorizationAction(String username, String action) throws HandlerException {
        if (!env.isAuditSecurityDisabled()) {
            AuditEvent auditEvent = new AuditEvent();
            auditEvent.setAction("AUTHORIZATION_ACTION");
            auditEvent.addField("user", username);
            auditEvent.addField("action", action);

            if (env.isEncryptionEnabled()) {
                AuditManager.getInstance().audit(PrivacyAwareUtils.encryptEvent(auditEvent, env.getEncryptionSecretKey()));
            } else {
                AuditManager.getInstance().audit(auditEvent);
            }
        }
    }
}
