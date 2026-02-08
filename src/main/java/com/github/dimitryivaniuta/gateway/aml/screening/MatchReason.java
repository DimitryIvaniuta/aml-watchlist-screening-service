package com.github.dimitryivaniuta.gateway.aml.screening;

/**
 * Explainability reason item.
 */
public record MatchReason(String code, String message, double contribution) {}
