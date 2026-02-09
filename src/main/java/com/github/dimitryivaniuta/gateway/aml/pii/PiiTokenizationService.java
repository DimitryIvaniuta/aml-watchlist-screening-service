package com.github.dimitryivaniuta.gateway.aml.pii;

/**
 * PII-safe tokenization/hashing abstraction (KMS-style).
 *
 * <p>Use this to persist sensitive identifiers without storing raw values.
 */
public interface PiiTokenizationService {

  /**
   * Tokenize the input value (deterministic for same key id in a mode).
   *
   * @param type logical type, e.g. "idNumber"
   * @param value raw value
   * @return token + keyId
   */
  TokenizedValue tokenize(String type, String value);
}
