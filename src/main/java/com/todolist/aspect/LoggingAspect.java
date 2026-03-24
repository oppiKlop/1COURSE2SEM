package com.todolist.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.todolist.service.*.*(..))")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        Object[] args = joinPoint.getArgs();

        log.info("НАЧАЛО: {}.{}() | Аргументы: {}",
                className, methodName, args);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            log.info("КОНЕЦ: {}.{}() | Результат: {} | Время: {} мс",
                    className, methodName, result, (endTime - startTime));

            return result;

        } catch (Exception e) {
            log.error("{}.{}() | Ошибка: {}",
                    className, methodName, e.getMessage());
            throw e;
        }
    }
}