package com.sagardevlab.distributed_webgenai.account_service.config;

import com.sagardevlab.distributed_webgenai.account_service.entity.Plan;
import com.sagardevlab.distributed_webgenai.account_service.repository.PlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the plan catalogue on first start. Users without a paid subscription fall back to the Free plan,
 * so it must always exist.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PlanSeeder implements ApplicationRunner {

    public static final String FREE_PLAN_NAME = "Free";

    private final PlanRepository planRepository;

    @Value("${stripe.price.pro:price_pro_placeholder}")
    private String proPriceId;

    @Value("${stripe.price.business:price_business_placeholder}")
    private String businessPriceId;

    @Override
    public void run(ApplicationArguments args) {
        if (planRepository.count() > 0) return;

        planRepository.save(plan(FREE_PLAN_NAME, null, 3, 100_000, 1, false));
        planRepository.save(plan("Pro", proPriceId, 20, 2_000_000, 3, false));
        planRepository.save(plan("Business", businessPriceId, 100, 0, 10, true));
        log.info("Seeded default plans");
    }

    private Plan plan(String name, String stripePriceId, int maxProjects, int maxTokensPerDay,
                      int maxPreviews, boolean unlimitedAi) {
        Plan plan = new Plan();
        plan.setName(name);
        plan.setStripePriceId(stripePriceId);
        plan.setMaxProjects(maxProjects);
        plan.setMaxTokensPerDay(maxTokensPerDay);
        plan.setMaxPreviews(maxPreviews);
        plan.setUnlimitedAi(unlimitedAi);
        plan.setActive(true);
        return plan;
    }
}
