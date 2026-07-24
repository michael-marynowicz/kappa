package com.company.sprintreporter.config;

import com.company.sprintreporter.domain.entity.Plan;
import com.company.sprintreporter.infrastructure.persistence.PlanRepository;
import com.company.sprintreporter.infrastructure.persistence.SubscriptionRepository;
import com.company.sprintreporter.infrastructure.persistence.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@Profile("local")
@RequiredArgsConstructor
public class LocalDevInitializer implements ApplicationRunner {

    private final PlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    @Value("${dev.admin-email:}")
    private String devAdminEmail;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Plan proPlan = planRepository.findByCode("pro").orElse(null);
        if (proPlan == null) {
            log.warn("PRO plan not found — skipping local upgrade");
            return;
        }

        int upgraded = subscriptionRepository.findAll().stream()
                .filter(sub -> !sub.getPlan().getId().equals(proPlan.getId()))
                .mapToInt(sub -> {
                    sub.setPlan(proPlan);
                    subscriptionRepository.save(sub);
                    log.info("Upgraded subscription {} to PRO plan", sub.getId());
                    return 1;
                })
                .sum();

        if (upgraded > 0) {
            log.info("Local dev: upgraded {} subscription(s) to PRO", upgraded);
        } else {
            log.info("Local dev: all subscriptions already on PRO plan");
        }

        // Mark configured dev account as email-verified
        if (devAdminEmail != null && !devAdminEmail.isBlank()) {
            userRepository.findByEmail(devAdminEmail).ifPresent(user -> {
                if (!Boolean.TRUE.equals(user.getEmailVerified())) {
                    user.setEmailVerified(true);
                    userRepository.save(user);
                    log.info("Local dev: marked {} as email-verified", devAdminEmail);
                }
            });
        }
    }
}
