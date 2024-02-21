package com.example.controller;

import com.example.aop.AccountLock;
import com.example.dto.CancelBalance;
import com.example.dto.QueryTransactionResponse;
import com.example.dto.UseBalance;
import com.example.exception.AccountException;
import com.example.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/*
* 잔액 관련 컨트롤러
* 1. 잔액 사용
* 2. 잔액 사용 취소
* 3. 거래 확인
* */

@RestController
@Slf4j
@RequiredArgsConstructor
public class TransactionalController {
    private final TransactionService transactionService;
    @PostMapping("/transaction/use")
    @AccountLock
    public UseBalance.Response useBalance(
            @RequestBody @Valid UseBalance.Request request
    ) throws InterruptedException {
        //UseBalance 에서 예외 발생할 수 있음



        try {

            Thread.sleep(2000L);

            return UseBalance.Response.from(
                    transactionService.useBalance(request.getUserId(),
                            request.getAccountNumber(),
                            request.getAmount())
            );

        }
        catch (AccountException e){

            log.error("Failed to use Balance.");
            transactionService.saveFailedTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;

        }
    }


    @PostMapping("/transaction/cancel")
    @AccountLock
    public CancelBalance.Response cancelBalance(
            @Valid @RequestBody CancelBalance.Request
             request
    ){
        try {

            return CancelBalance.Response.from(
                    transactionService.cancelBalance(request.getTransactionId(),
                            request.getAccountNumber(),
                            request.getAmount())
            );

        }
        catch (AccountException e){

            log.error("Failed to use Balance.");
            transactionService.saveFailedCancelTransaction(
                    request.getAccountNumber(),
                    request.getAmount()
            );
            throw e;

        }
    }


    @GetMapping("/transaction/{transactionId}")
    public QueryTransactionResponse queryTransaction(
            @PathVariable(name = "transactionId") String transactionId
    ){
        return QueryTransactionResponse.from(
                transactionService.queryTransaction(transactionId)
        );
    }
}
