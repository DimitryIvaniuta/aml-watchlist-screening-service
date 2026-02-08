package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.api.dto.TransactionAcceptedResponse;
import com.github.dimitryivaniuta.gateway.aml.api.dto.TransactionCreateRequest;
import com.github.dimitryivaniuta.gateway.aml.domain.Transaction;
import com.github.dimitryivaniuta.gateway.aml.pii.PiiTokenizationService;
import com.github.dimitryivaniuta.gateway.aml.screening.ScreeningService;
import com.github.dimitryivaniuta.gateway.aml.util.Normalize;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Transaction intake API (async screening).
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

  private final ScreeningService screening;
  private final PiiTokenizationService pii;

  public TransactionController(ScreeningService screening, PiiTokenizationService pii) {
    this.screening = screening;
    this.pii = pii;
  }

  @PostMapping
  public ResponseEntity<TransactionAcceptedResponse> create(@Valid @RequestBody TransactionCreateRequest req) {
    Transaction tx = new Transaction();
    tx.setId(UUID.randomUUID());
    tx.setCustomerFullName(req.customerFullName());
    tx.setCustomerFullNameNorm(Normalize.norm(req.customerFullName()));
    tx.setCustomerDob(req.customerDob());
    tx.setCustomerNationality(req.customerNationality());
    tx.setCustomerCountry(req.customerCountry());
    tx.setCustomerIdNumberToken(pii.tokenize("idNumber", req.customerIdNumber()).token());
    tx.setAmount(req.amount());
    tx.setCurrency(req.currency());
    tx.setAddressLine1(req.addressLine1());
    tx.setCity(req.city());
    tx.setPostalCode(req.postalCode());
    tx.setAddressCountry(req.addressCountry());

    UUID id = screening.submit(tx);
    return ResponseEntity.accepted().body(new TransactionAcceptedResponse(id, "ACCEPTED"));
  }
}
