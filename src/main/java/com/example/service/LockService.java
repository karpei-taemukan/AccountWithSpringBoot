package com.example.service;

import com.example.exception.AccountException;
import com.example.type.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockService {
    private final RedissonClient redissonClient;

    public void lock(String accountNumber){
       
        RLock rLock = redissonClient.getLock(getLockKey(accountNumber));

        log.debug("Trying lock for accountNumber : {}", accountNumber);

        try{
            // waitTime : lock을 취득하는 데 걸리는 시간
            // leaseTime : lock이 자동으로 해제가 되는 시간
            boolean isLock = rLock.tryLock(1,15, TimeUnit.SECONDS);
            if(!isLock){
                log.error("=====Lock acquisition failed=====");
                throw new AccountException(ErrorCode.ACCOUNT_TRANSACTION_LOCK);
            }
        } catch (AccountException e){
            throw e;

        } catch(Exception e){
            log.error("Redis lock failed", e);
        }
    }

    public void unlock(String accountNumber){
        log.debug("Unlock for accountNumber : {}", accountNumber);
        redissonClient.getLock(getLockKey(accountNumber)).unlock();
    }



    private static String getLockKey(String accountNumber) {
        return "ACLK:" + accountNumber;
    }
}