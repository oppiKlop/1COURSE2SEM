package com.todolist.aspect;

import com.todolist.exception.TaskNotFoundException;
import java.util.Arrays;
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

  @Around("execution(* com.todolist.service..*(..))")
  public Object log(ProceedingJoinPoint jp) throws Throwable {
    String signature = jp.getSignature().toShortString();
    Object[] args = jp.getArgs();

    log.info("Service start: {} args={}", signature, Arrays.toString(args));
    try {
      Object result = jp.proceed();
      log.info("Service end: {} result={}", signature, result);
      return result;
    } catch (TaskNotFoundException ex) {
      log.info("Service not found: {} args={} — {}", signature, Arrays.toString(args), ex.getMessage());
      throw ex;
    } catch (Throwable ex) {
      log.error("Service error: {} args={}", signature, Arrays.toString(args), ex);
      throw ex;
    }
  }
}