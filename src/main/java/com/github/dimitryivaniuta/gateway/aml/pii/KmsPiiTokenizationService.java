package com.github.dimitryivaniuta.gateway.aml.pii;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Placeholder for a KMS/HSM-backed tokenization.
 *
 * <p>Wire this to AWS KMS / Azure Key Vault / GCP KMS (envelope encryption or deterministic tokenization).
 */
@Service
@Profile("kms")
public class KmsPiiTokenizationService implements PiiTokenizationService {

  @Override
  public TokenizedValue tokenize(String type, String value) {
    throw new UnsupportedOperationException("KMS tokenization is not wired in this demo build.");
  }
}
