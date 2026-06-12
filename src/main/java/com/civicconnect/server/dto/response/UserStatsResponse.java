package com.civicconnect.server.dto.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserStatsResponse {

    private long totalReports;
    private long totalUpvotesReceived;
    private long issuesResolved;
    private long issuesPending;
    private long issuesInProgress;
}
