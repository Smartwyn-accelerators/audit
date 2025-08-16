package com.fastcode.audit.utils;

import org.audit4j.core.dto.AuditEvent;
import org.audit4j.core.dto.Field;
import org.audit4j.core.exception.HandlerException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PrivacyAwareUtils {

	/**
	 * Masks sensitive data in the provided message based on user-specified keys.
	 *
	 * @param message The log message.
	 * @param sensitiveKeys A map of keys and their corresponding regex patterns to be masked.
	 * @return The message with sensitive data masked.
	 */
	public static String maskSensitiveData(String message, Map<String, String> sensitiveKeys) {
		if (message == null) {
			return null;
		}
		for (Map.Entry<String, String> entry : sensitiveKeys.entrySet()) {
			String key = entry.getKey();
			String pattern = entry.getValue();
			message = maskPattern(pattern, "****", message);
		}
		return message;
	}

	/**
	 * Replaces occurrences of the given pattern in the message with the provided mask.
	 *
	 * @param pattern The regular expression pattern for sensitive data.
	 * @param mask The mask to replace sensitive data with.
	 * @param message The log message.
	 * @return The message with sensitive data replaced by the mask.
	 */
	private static String maskPattern(String pattern, String mask, String message) {
		Pattern regexPattern = Pattern.compile(pattern);
		Matcher matcher = regexPattern.matcher(message);
		return matcher.replaceAll(mask);
	}


	private static SecretKey getSecretKey(String encryptedSecretKey) {
		try {
			byte[] decodedKey = Base64.getDecoder().decode(encryptedSecretKey);
			return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
		} catch (Exception e) {
			System.out.println("Failed to set encryption key " + e.getMessage());
			return null;
		}
	}

	public static String encryptMessage(String message, String encryptedSecretKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(encryptedSecretKey));
			byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			return message;
		}
	}
	public static AuditEvent encryptEvent(AuditEvent event, String encryptedSecretKey) throws HandlerException {
		ArrayList<Field> newFields = new ArrayList<>();
		for (Field field : event.getFields()) {
			String updatedValue = PrivacyAwareUtils.encryptMessage(field.getValue(), encryptedSecretKey);
			newFields.add(new Field(field.getName(), updatedValue, field.getType()));
		}
		event.setFields(newFields);
		return event;
	}
}

