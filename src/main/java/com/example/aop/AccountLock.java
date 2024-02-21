package com.example.aop;

import java.lang.annotation.*;

@Target(ElementType.METHOD) // 어노테이션을 붙일 수 있는 타겟
@Retention(RetentionPolicy.RUNTIME) //
@Documented
@Inherited // 상속가능
public @interface AccountLock {
    long tryLockTime() default 5000L; //  해당시간동 안 기다린다
}
