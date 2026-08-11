package com.company.sprintreporter.config.feature;

import java.lang.annotation.*;

/**
 * Guards an endpoint: the authenticated user's organization must have ALL listed features
 * in their active subscription plan. If any is missing, returns 403 Forbidden.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresFeature {

    /**
     * Feature codes required (logical AND — all must be present).
     */
    String[] value();
}
