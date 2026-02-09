package com.github.dimitryivaniuta.gateway.aml.pii;

/** Tokenized value wrapper (token + key id). */
public record TokenizedValue(String token, String keyId) {}
