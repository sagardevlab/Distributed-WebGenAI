package com.sagardevlab.distributed_webgenai.account_service.dto.subscription;

import com.sagardevlab.distributed_webgenai.common_lib.dto.PlanDto;

import java.time.Instant;

public record SubscriptionResponse(
        PlanDto plan,
        String status,
        Instant currentPeriodEnd,
        Long tokensUsedThisCycle
) {
}
