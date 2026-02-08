package com.github.dimitryivaniuta.gateway.aml.api.dto;

import java.util.UUID;

/** Async submission response. */
public record TransactionAcceptedResponse(UUID transactionId, String status) {}
