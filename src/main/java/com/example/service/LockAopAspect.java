package com.example.service;

import com.example.aop.AccountLockIdInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LockAopAspect {
    private final LockService lockService;
    @Around("@annotation(com.example.aop.AccountLock) && args(request)")
    public Object aroundMethod(
            ProceedingJoinPoint proceedingJoinPoint,
            AccountLockIdInterface request
            /*
            * AccountLockIdInterface 로 가져온 이유는
            * UseBalance.request, CancelBalance.request 로
            * 한쪽의 request로 가져오면 에러가 날 수 있음
            * 만약 UseBalance는 UseBalance.request로 가져와야되나
            * CancelBalance.request로 가져오면 에러
            *
            * 그래서 인터페이스로 Getter 이름 처럼 선언해서 자동으로 두 타입을 사용 가능
            * */
    ) throws Throwable{
        // lock 취득 시도
        lockService.lock(request.getAccountNumber());

        try{
            return proceedingJoinPoint.proceed();
        }finally {
            // lock 해제
            lockService.unlock(request.getAccountNumber());
        }
    }
}
