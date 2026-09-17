package com.svi.tictactoe.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(com.svi.tictactoe.controller..*)")
    public void controllers() {
    }

    @Before("controllers()")
    public void logBefore(JoinPoint joinPoint) {
        if (log.isDebugEnabled()) {
            log.debug("Request: {} args={}", signature(joinPoint), Arrays.toString(joinPoint.getArgs()));
        }
    }

    @AfterReturning(pointcut = "controllers()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        if (log.isDebugEnabled()) {
            log.debug("Response: {} returned {}", signature(joinPoint), describe(result));
        }
    }

    @AfterThrowing(pointcut = "controllers()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        log.warn("<!> {} threw {}: {}", signature(joinPoint), ex.getClass().getSimpleName(), ex.getMessage());
    }

    private String signature(JoinPoint joinPoint) {
        return joinPoint.getSignature().getDeclaringType().getSimpleName() + "." + joinPoint.getSignature().getName();
    }

    private String describe(Object result) {
        if (result instanceof ResponseEntity<?> response) {
            return "status " + response.getStatusCode() + " body=" + response.getBody();
        }
        return String.valueOf(result);
    }
}
