package com.github.dimitryivaniuta.gateway.aml.api;

import com.github.dimitryivaniuta.gateway.aml.api.dto.DlqMessageDetailsDto;
import com.github.dimitryivaniuta.gateway.aml.api.dto.DlqMessageDto;
import com.github.dimitryivaniuta.gateway.aml.dlq.DlqMessage;

/** Maps DLQ entities to DTOs. */
public final class DlqDtoMapper {
  private DlqDtoMapper() {}

  public static DlqMessageDto toDto(DlqMessage m) {
    return new DlqMessageDto(
        m.getId(), m.getTopic(), m.getPartition(), m.getOffset(), m.getMessageKey(), m.getEventType(), m.getOutboxId(),
        m.getAggregateType(), m.getAggregateId(), m.getError(), m.getReceivedAt(), m.getStatus(), m.getReplayAttempts(),
        m.getNextAttemptAt(), m.getLastError(), m.getReplayedAt(), m.getApprovalStatus(), m.getRequestedBy(),
        m.getRequestedAt(), m.getApprovedBy(), m.getApprovedAt(), m.getCorrelationId()
    );
  }

  public static DlqMessageDetailsDto toDetailsDto(DlqMessage m) {
    return new DlqMessageDetailsDto(
        m.getId(), m.getTopic(), m.getPartition(), m.getOffset(), m.getMessageKey(), m.getEventType(), m.getOutboxId(),
        m.getAggregateType(), m.getAggregateId(), m.getPayload(), m.getError(), m.getReceivedAt(), m.getStatus(),
        m.getReplayAttempts(), m.getNextAttemptAt(), m.getLastError(), m.getReplayedAt(), m.getApprovalStatus(),
        m.getRequestedBy(), m.getRequestedAt(), m.getApprovedBy(), m.getApprovedAt(), m.getCorrelationId()
    );
  }
}
