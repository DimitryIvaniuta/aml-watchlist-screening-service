package com.github.dimitryivaniuta.gateway.aml.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Transaction submitted for AML screening.
 */
@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor
public class Transaction {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @Column(name = "customer_full_name", nullable = false, length = 300)
  private String customerFullName;

  @Column(name = "customer_full_name_norm", nullable = false, length = 320)
  private String customerFullNameNorm;

  @Column(name = "customer_dob")
  private LocalDate customerDob;

  @Column(name = "customer_nationality", length = 80)
  private String customerNationality;

  @Column(name = "customer_country", length = 80)
  private String customerCountry;

  @Column(name = "customer_id_number_token", length = 128)
  private String customerIdNumberToken;

  @Column(nullable = false, precision = 19, scale = 2)
  private BigDecimal amount;

  @Column(nullable = false, length = 8)
  private String currency;

  @Column(name = "address_line1", length = 200)
  private String addressLine1;

  @Column(name = "city", length = 120)
  private String city;

  @Column(name = "postal_code", length = 40)
  private String postalCode;

  @Column(name = "address_country", length = 80)
  private String addressCountry;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt = Instant.now();
}
