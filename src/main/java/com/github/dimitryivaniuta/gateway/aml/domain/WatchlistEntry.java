package com.github.dimitryivaniuta.gateway.aml.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Watchlist entry snapshot.
 *
 * <p>Contains normalized fields for matching and optional address details.
 */
@Entity
@Table(name = "watchlist_entries")
@Getter @Setter @NoArgsConstructor
public class WatchlistEntry {

  @Id
  @Column(columnDefinition = "uuid")
  private UUID id;

  @ManyToOne(optional = false, fetch = FetchType.LAZY)
  @JoinColumn(name = "batch_id", nullable = false)
  private WatchlistBatch batch;

  @Column(name = "full_name", nullable = false, length = 300)
  private String fullName;

  @Column(name = "first_name", length = 120)
  private String firstName;

  @Column(name = "last_name", length = 120)
  private String lastName;

  @Column(name = "full_name_norm", nullable = false, length = 320)
  private String fullNameNorm;

  @Column(name = "last_name_norm", length = 140)
  private String lastNameNorm;

  @Column(name = "dob")
  private LocalDate dob;

  @Column(name = "nationality", length = 80)
  private String nationality;

  @Column(name = "country", length = 80)
  private String country;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", nullable = false, length = 16)
  private RiskLevel riskLevel;

  /** JSON array string of aliases. */
  @Column(name = "aliases", columnDefinition = "jsonb")
  private String aliases;

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
