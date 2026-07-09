package ru.ssau.virtualservers.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Map;

@Service
public class TokenService {

    @Value("${JWT_SECRET}")
    private String secret;
    private final ObjectMapper mapper = new ObjectMapper();

    public String generateToken(Map<String, Object> payload) {
        try {
            String json = mapper.writeValueAsString(payload);
            String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(json.getBytes());
            String encodedSignature = calculateSignature(encodedPayload);
            return encodedPayload + "." + encodedSignature;
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при генерации токена", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 2) return false;

            String payloadPart = parts[0];
            String signaturePart = parts[1];

            if (!calculateSignature(payloadPart).equals(signaturePart)) return false;

            Map<String, Object> payload = decodePayload(payloadPart);
            long exp = ((Number) payload.get("exp")).longValue();
            return exp > System.currentTimeMillis() / 1000;

        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> decodePayload(String payloadPart) throws Exception {
        byte[] decoded = Base64.getUrlDecoder().decode(payloadPart);
        return mapper.readValue(decoded, new TypeReference<Map<String, Object>>() {}); 
    }

    private String calculateSignature(String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        mac.init(key);
        byte[] signatureBytes = mac.doFinal(data.getBytes());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}