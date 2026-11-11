package com.bugbycode.aop.repository;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MethodCostAspect {
	
	private final Logger logger = LogManager.getLogger(MethodCostAspect.class);
	
	@Around("execution(* com.bugbycode.repository..*.*(..))")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        long start = System.nanoTime();

        try {
            return joinPoint.proceed();
        } catch (Throwable e) {
            throw e;
        } finally {

            long cost = TimeUnit.NANOSECONDS.toMillis(
                    System.nanoTime() - start
            );

            MethodSignature signature =
                    (MethodSignature) joinPoint.getSignature();

            Method method = signature.getMethod();

            String className = joinPoint.getTarget()
                    .getClass().getName();

            String methodName = method.getName();

            logger.debug("[METHOD COST] {}.{}() 耗时: {} ms",
                    className, methodName, cost);
        }
    }
}
