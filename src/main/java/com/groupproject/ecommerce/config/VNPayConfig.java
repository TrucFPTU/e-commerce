package com.groupproject.ecommerce.config;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Configuration class for VNPay payment gateway integration
 */
@Configuration
@Getter
@Slf4j
public class VNPayConfig {
    
    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.url}")
    private String vnpayUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.api-url}")
    private String apiUrl;

    /**
     * Generate HMAC SHA512 hash
     * @param key Secret key
     * @param data Data to hash
     * @return Hashed string in hex format
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            log.error("Failed to generate HMAC SHA512", e);
            throw new RuntimeException("Failed to generate HMAC SHA512", e);
        }
    }

    /**
     * Hash all fields in sorted order with URL encoding (VNPay requirement)
     * @param fields Map of field names and values
     * @param secretKey Secret key for hashing
     * @return HMAC SHA512 hash of all fields
     */
    public static String hashAllFields(Map<String, String> fields, String secretKey) {
        List<String> fieldNames = new ArrayList<>(fields.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        
        for (String fieldName : fieldNames) {
            String fieldValue = fields.get(fieldName);
            if (fieldValue != null && !fieldValue.isEmpty()) {
                hashData.append(fieldName).append("=");
                
                try {
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                } catch (Exception e) {
                    log.warn("Failed to URL encode field {}: {}", fieldName, e.getMessage());
                    hashData.append(fieldValue);
                }
                
                hashData.append("&");
            }
        }
        
        // Remove trailing '&'
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }
        
        return hmacSHA512(secretKey, hashData.toString());
    }
}
