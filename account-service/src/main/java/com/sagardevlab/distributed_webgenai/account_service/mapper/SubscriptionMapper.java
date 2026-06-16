package com.sagardevlab.distributed_webgenai.account_service.mapper;

import com.sagardevlab.distributed_webgenai.account_service.dto.subscription.SubscriptionResponse;
import com.sagardevlab.distributed_webgenai.account_service.entity.Plan;
import com.sagardevlab.distributed_webgenai.account_service.entity.Subscription;
import com.sagardevlab.distributed_webgenai.common_lib.dto.PlanDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(Subscription subscription);

    PlanDto toPlanResponse(Plan plan);
}
