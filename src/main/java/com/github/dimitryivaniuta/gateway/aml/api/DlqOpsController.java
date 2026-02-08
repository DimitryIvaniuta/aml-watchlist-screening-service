package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.api.dto.DlqMessageDetailsDto;
import com.github.dimitryivaniuta.gateway.aml.api.dto.DlqMessageDto;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqApprovalStatus;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqMessageStatus;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqOpsService;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqReplayService;
import java.security.Principal;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Enterprise ops API:
 * - DTO responses (no entities)
 * - pagination + multi-sort (e.g. sort=receivedAt,desc&sort=nextAttemptAt,asc)
 * - filters + ops-friendly searchQ across aggregateId, outboxId, messageKey, eventType
 * - RBAC + true 4-eyes (requester != approver)
 */
@RestController
@RequestMapping("/api/dlq/messages")
public class DlqOpsController {

  private final DlqOpsService ops;
  private final DlqReplayService replay;

  public DlqOpsController(DlqOpsService ops, DlqReplayService replay) {
    this.ops = ops;
    this.replay = replay;
  }

  @PreAuthorize("hasAnyRole('OPS_REQUESTER','OPS_APPROVER')")
  @GetMapping
  public ResponseEntity<Page<DlqMessageDto>> list(
      @RequestParam(name = "status", required = false) DlqMessageStatus status,
      @RequestParam(name = "approval", required = false) DlqApprovalStatus approval,
      @RequestParam(name = "eventType", required = false) String eventType,
      @RequestParam(name = "aggregateId", required = false) String aggregateId,
      @RequestParam(name = "outboxId", required = false) UUID outboxId,
      @RequestParam(name = "searchQ", required = false) String searchQ,
      @PageableDefault(size = 20, sort = "receivedAt", direction = Sort.Direction.DESC) Pageable pageable
  ) {
    return ResponseEntity.ok(
        ops.search(status, approval, eventType, aggregateId, outboxId, searchQ, pageable)
            .map(DlqDtoMapper::toDto));
  }

  @PreAuthorize("hasAnyRole('OPS_REQUESTER','OPS_APPROVER')")
  @GetMapping("/{id}")
  public ResponseEntity<DlqMessageDetailsDto> get(@PathVariable("id") long id) {
    return ResponseEntity.ok(DlqDtoMapper.toDetailsDto(ops.getById(id)));
  }

  @PreAuthorize("hasRole('OPS_REQUESTER')")
  @PostMapping("/{id}/request")
  public ResponseEntity<DlqMessageDto> request(@PathVariable("id") long id, Principal principal) {
    return ResponseEntity.ok(DlqDtoMapper.toDto(ops.requestReplay(id, principal.getName())));
  }

  @PreAuthorize("hasRole('OPS_APPROVER')")
  @PostMapping("/{id}/approve")
  public ResponseEntity<DlqMessageDto> approve(@PathVariable("id") long id, Principal principal) {
    return ResponseEntity.ok(DlqDtoMapper.toDto(ops.approveReplay(id, principal.getName())));
  }

  @PreAuthorize("hasRole('OPS_APPROVER')")
  @PostMapping("/{id}/replay")
  public ResponseEntity<DlqReplayService.ReplayResult> replayById(@PathVariable("id") long id) {
    return ResponseEntity.ok(replay.replayById(id));
  }
}
