package com.github.dimitryivaniuta.gateway.aml.pii;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Local deterministic HMAC tokenization (dev/demo).
 *
 * <p>In production, use KMS/HSM-backed mode.
 */
@Service
public class LocalHmacPiiTokenizationService implements PiiTokenizationService {

  private final String secret;
  private final String keyId;

  public LocalHmacPiiTokenizationService(
      @Value("${aml.pii.localHmac.secret}") String secret,
      @Value("${aml.pii.localHmac.keyId}") String keyId) {
    this.secret = secret;
    this.keyId = keyId;
  }

  @Override
  public TokenizedValue tokenize(String type, String value) {
    if (value == null || value.isBlank()) return new TokenizedValue(null, keyId);
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      byte[] out = mac.doFinal((type + ":" + value).getBytes(StandardCharsets.UTF_8));
      return new TokenizedValue(Base64.getUrlEncoder().withoutPadding().encodeToString(out), keyId);
    } catch (Exception e) {
      throw new IllegalStateException("Tokenization failed", e);
    }
  }
}
