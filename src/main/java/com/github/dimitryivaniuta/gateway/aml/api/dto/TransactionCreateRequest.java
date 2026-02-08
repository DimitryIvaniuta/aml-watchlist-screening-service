package com.github.dimitryivaniuta.gateway.aml.api.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/** Transaction create request. */
public record TransactionCreateRequest(
    @NotBlank String customerFullName,
    LocalDate customerDob,
    String customerNationality,
    String customerCountry,
    String customerIdNumber, // raw; will be tokenized (not stored raw)
    @NotNull @Positive BigDecimal amount,
    @NotBlank String currency,
    String addressLine1,
    String city,
    String postalCode,
    String addressCountry
) {}
