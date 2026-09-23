package com.sagardevlab.distributed_webgenai.intelligence_service.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.function.Supplier;

/**
 * Spring AI runs advisors and tool calls on Reactor threads, where the request's SecurityContext
 * (and therefore the JWT forwarded by the Feign interceptor) is not available. This re-establishes it.
 */
public final class SecurityContextUtils {

    private SecurityContextUtils() {
    }

    public static <T> T callAs(Authentication authentication, Supplier<T> action) {
        if (authentication == null) {
            return action.get();
        }

        SecurityContext previous = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        try {
            return action.get();
        } finally {
            SecurityContextHolder.setContext(previous);
        }
    }
}
