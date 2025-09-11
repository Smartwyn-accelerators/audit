package com.fastcode.audit.utils;

import com.fastcode.audit.domain.AuditEvent;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
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
	/**
	 * Encrypts sensitive data in the audit event elements
	 */
	public static AuditEvent encryptEvent(AuditEvent event, String encryptedSecretKey) {
		if (event == null || event.getElements() == null) {
			return event;
		}
		
		try {
			Map<String, Object> elements = event.getElements();
			Map<String, Object> encryptedElements = encryptMap(elements, encryptedSecretKey);
			event.setElements(encryptedElements);
		} catch (Exception e) {
			System.err.println("Failed to encrypt audit event: " + e.getMessage());
		}
		return event;
	}

	/**
	 * Masks sensitive data in the audit event elements
	 */
	public static AuditEvent maskSensitiveEvent(AuditEvent event, Map<String, String> sensitiveKeys) {
		if (event == null || event.getElements() == null) {
			return event;
		}
		
		try {
			Map<String, Object> elements = event.getElements();
			Map<String, Object> maskedElements = maskMap(elements, sensitiveKeys);
			event.setElements(maskedElements);
		} catch (Exception e) {
			System.err.println("Failed to mask audit event: " + e.getMessage());
		}
		return event;
	}

	/**
	 * Encrypts all string values in a map
	 */
	public static Map<String, Object> encryptMap(Map<String, Object> map, String encryptedSecretKey) {
		Map<String, Object> encryptedMap = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				encryptedMap.put(entry.getKey(), encryptMessage((String) value, encryptedSecretKey));
			} else if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = (Map<String, Object>) value;
				encryptedMap.put(entry.getKey(), encryptMap(mapValue, encryptedSecretKey));
			} else {
				encryptedMap.put(entry.getKey(), value);
			}
		}
		return encryptedMap;
	}

	/**
	 * Masks sensitive data in all string values in a map
	 */
	public static Map<String, Object> maskMap(Map<String, Object> map, Map<String, String> sensitiveKeys) {
		Map<String, Object> maskedMap = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				maskedMap.put(entry.getKey(), maskSensitiveData((String) value, sensitiveKeys));
			} else if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = (Map<String, Object>) value;
				maskedMap.put(entry.getKey(), maskMap(mapValue, sensitiveKeys));
			} else {
				maskedMap.put(entry.getKey(), value);
			}
		}
		return maskedMap;
	}

	/**
	 * Encrypts a message using secure layout with key and salt
	 */
	public static String encryptMessageWithSecureLayout(String message, String key, String salt) {
		try {
			// Generate key from provided key and salt
			SecretKey secretKey = generateKeyFromKeyAndSalt(key, salt);
			
			// Generate random IV
			byte[] iv = new byte[16];
			new SecureRandom().nextBytes(iv);
			IvParameterSpec ivSpec = new IvParameterSpec(iv);
			
			// Encrypt the message
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
			byte[] encryptedBytes = cipher.doFinal(message.getBytes(StandardCharsets.UTF_8));
			
			// Combine IV and encrypted data
			byte[] combined = new byte[iv.length + encryptedBytes.length];
			System.arraycopy(iv, 0, combined, 0, iv.length);
			System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);
			
			return Base64.getEncoder().encodeToString(combined);
		} catch (Exception e) {
			System.err.println("Failed to encrypt message with secure layout: " + e.getMessage());
			return message;
		}
	}

	/**
	 * Decrypts a message using secure layout with key and salt
	 */
	public static String decryptMessageWithSecureLayout(String encryptedMessage, String key, String salt) {
		try {
			// Generate key from provided key and salt
			SecretKey secretKey = generateKeyFromKeyAndSalt(key, salt);
			
			// Decode the message
			byte[] combined = Base64.getDecoder().decode(encryptedMessage);
			
			// Extract IV and encrypted data
			byte[] iv = new byte[16];
			byte[] encryptedBytes = new byte[combined.length - 16];
			System.arraycopy(combined, 0, iv, 0, 16);
			System.arraycopy(combined, 16, encryptedBytes, 0, encryptedBytes.length);
			
			// Decrypt the message
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
			byte[] decryptedBytes = cipher.doFinal(encryptedBytes);
			
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			System.err.println("Failed to decrypt message with secure layout: " + e.getMessage());
			return encryptedMessage;
		}
	}

	/**
	 * Encrypts all string values in a map using secure layout
	 */
	public static Map<String, Object> encryptMapWithSecureLayout(Map<String, Object> map, String key, String salt) {
		Map<String, Object> encryptedMap = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				encryptedMap.put(entry.getKey(), encryptMessageWithSecureLayout((String) value, key, salt));
			} else if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = (Map<String, Object>) value;
				encryptedMap.put(entry.getKey(), encryptMapWithSecureLayout(mapValue, key, salt));
			} else {
				encryptedMap.put(entry.getKey(), value);
			}
		}
		return encryptedMap;
	}

	/**
	 * Decrypts all string values in a map using secure layout
	 */
	public static Map<String, Object> decryptMapWithSecureLayout(Map<String, Object> map, String key, String salt) {
		Map<String, Object> decryptedMap = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				decryptedMap.put(entry.getKey(), decryptMessageWithSecureLayout((String) value, key, salt));
			} else if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = (Map<String, Object>) value;
				decryptedMap.put(entry.getKey(), decryptMapWithSecureLayout(mapValue, key, salt));
			} else {
				decryptedMap.put(entry.getKey(), value);
			}
		}
		return decryptedMap;
	}

	/**
	 * Generates a secret key from the provided key and salt using PBKDF2
	 */
	private static SecretKey generateKeyFromKeyAndSalt(String key, String salt) throws Exception {
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] keyBytes = sha.digest((key + salt).getBytes(StandardCharsets.UTF_8));
		return new SecretKeySpec(keyBytes, "AES");
	}

	/**
	 * Decrypts a message using the original encryption method
	 */
	public static String decryptMessage(String encryptedMessage, String encryptedSecretKey) {
		try {
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, getSecretKey(encryptedSecretKey));
			byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedMessage));
			return new String(decryptedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return encryptedMessage;
		}
	}

	/**
	 * Decrypts all string values in a map using the original encryption method
	 */
	public static Map<String, Object> decryptMap(Map<String, Object> map, String encryptedSecretKey) {
		Map<String, Object> decryptedMap = new java.util.HashMap<>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			Object value = entry.getValue();
			if (value instanceof String) {
				decryptedMap.put(entry.getKey(), decryptMessage((String) value, encryptedSecretKey));
			} else if (value instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, Object> mapValue = (Map<String, Object>) value;
				decryptedMap.put(entry.getKey(), decryptMap(mapValue, encryptedSecretKey));
			} else {
				decryptedMap.put(entry.getKey(), value);
			}
		}
		return decryptedMap;
	}
}

