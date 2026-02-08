package com.github.dimitryivaniuta.gateway.aml.screening;

import com.github.dimitryivaniuta.gateway.aml.domain.WatchlistEntry;
import java.util.List;

/** Match result with score and reasons. */
public record MatchResult(WatchlistEntry entry, double score, List<MatchReason> reasons) {}
