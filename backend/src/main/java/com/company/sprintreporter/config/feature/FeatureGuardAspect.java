package com.company.sprintreporter.config.feature;

import com.company.sprintreporter.config.jwt.JwtAuthenticationToken;
import com.company.sprintreporter.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.lang.reflect.Method;

/**
 * AOP aspect that intercepts methods annotated with {@link RequiresFeature}
 * and verifies the authenticated user's organization has the required features.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class FeatureGuardAspect {

    private final SubscriptionService subscriptionService;

    @Around("@annotation(com.company.sprintreporter.config.feature.RequiresFeature) || " +
            "@within(com.company.sprintreporter.config.feature.RequiresFeature)")
    public Object checkFeatureAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        RequiresFeature annotation = getAnnotation(joinPoint);
        if (annotation == null) {
            return joinPoint.proceed();
        }

        JwtAuthenticationToken auth = getAuthentication();
        if (auth == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String[] requiredFeatures = annotation.value();
        for (String featureCode : requiredFeatures) {
            if (!subscriptionService.hasFeature(auth.getOrganizationId(), featureCode)) {
                log.warn("Feature access denied: org={} missing feature={}",
                        auth.getOrganizationId(), featureCode);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Your plan does not include access to this feature: " + featureCode);
            }
        }

        return joinPoint.proceed();
    }

    private RequiresFeature getAnnotation(ProceedingJoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();

        // Method-level takes precedence
        RequiresFeature annotation = AnnotationUtils.findAnnotation(method, RequiresFeature.class);
        if (annotation != null) {
            return annotation;
        }

        // Fall back to class-level
        return AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RequiresFeature.class);
    }

    private JwtAuthenticationToken getAuthentication() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwt) {
            return jwt;
        }
        return null;
    }
}
