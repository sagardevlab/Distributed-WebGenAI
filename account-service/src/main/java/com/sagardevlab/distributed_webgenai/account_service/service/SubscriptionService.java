package com.sagardevlab.distributed_webgenai.account_service.service;


import com.sagardevlab.distributed_webgenai.account_service.dto.subscription.SubscriptionResponse;
import com.sagardevlab.distributed_webgenai.common_lib.dto.PlanDto;
import com.sagardevlab.distributed_webgenai.common_lib.enums.SubscriptionStatus;

import java.time.Instant;

public interface SubscriptionService {
    SubscriptionResponse getCurrentSubscription();

    void activateSubscription(Long userId, Long planId, String subscriptionId, String customerId);

    void updateSubscription(String gatewaySubscriptionId, SubscriptionStatus status, Instant periodStart, Instant periodEnd, Boolean cancelAtPeriodEnd, Long planId);

    void cancelSubscription(String gatewaySubscriptionId);

    void renewSubscriptionPeriod(String subId, Instant periodStart, Instant periodEnd);

    void markSubscriptionPastDue(String subId);

    PlanDto getCurrentSubscribedPlanByUser();
}
