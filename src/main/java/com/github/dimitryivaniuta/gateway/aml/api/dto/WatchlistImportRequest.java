package com.github.dimitryivaniuta.gateway.aml.api.dto;

import com.github.dimitryivaniuta.gateway.aml.domain.RiskLevel;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.List;

/** Watchlist import request. */
public record WatchlistImportRequest(
    @NotBlank String source,
    List<Item> items
) {
  public record Item(
      @NotBlank String fullName,
      String firstName,
      String lastName,
      LocalDate dob,
      String nationality,
      String country,
      RiskLevel riskLevel,
      List<String> aliases,
      String addressLine1,
      String city,
      String postalCode,
      String addressCountry
  ) {}
}
