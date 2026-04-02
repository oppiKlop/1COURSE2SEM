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
    @Around("execution(* ..service..*(..))")
    public Object log(ProceedingJoinPoint jp) throws Throwable {
        System.out.println("START " + jp.getSignature());
        Object res = jp.proceed();
        System.out.println("END " + jp.getSignature());
        return res;
    }
}