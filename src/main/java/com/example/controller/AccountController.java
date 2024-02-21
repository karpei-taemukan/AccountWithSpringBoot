package com.example.controller;

import com.example.domain.Account;
import com.example.domain.AccountUser;
import com.example.dto.AccountDto;
import com.example.dto.AccountInfo;
import com.example.dto.CreateAccount;
import com.example.dto.DeleteAccount;
import com.example.service.AccountService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;


/*
* 외부 -> controller 으로만 접속
* controller -> service 으로만 접속
* service -> repository 으로만 접속
* */


@RestController
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @PostMapping("/account")
    private CreateAccount.Response createAccount(
            @RequestBody @Valid CreateAccount.Request request
    ) {

        return CreateAccount.Response.from(
                accountService.createAccount(
                request.getUserId(),
                request.getInitialBalance()
                )
        );
    }


    @DeleteMapping("/account")
    private DeleteAccount.Response deleteAccount(
            @RequestBody @Valid DeleteAccount.Request request
    ) {

        return DeleteAccount.Response.from(
                accountService.deleteAccount(
                        request.getUserId(),
                        request.getAccountNumber()
                )
        );
    }


    @GetMapping("/account")
    public List<AccountInfo> getAccountsByUserId(

            //#########################################################################
            // AccountDto 를 모아서 List로 안쓰고 새롭게 AccountInfo 라는 DTO를 생성한 이유
            // 다목적으로 DTO를 사용할 경우 필연적으로 복잡한 동작을 하고
            // 그에 따라서 의도치 않은 동작으로 인한 에러 발생이 쉬움
            //#########################################################################

            @RequestParam("user_id") Long userId
    ){
      return accountService.getAccountByUserId(userId).stream()
              .map(AccountDto -> AccountInfo.builder()
                      .accountNumber(AccountDto.getAccountNumber())
                      .balance(AccountDto.getBalance())
                      .build())
              .collect(Collectors.toList());
    }

    @GetMapping("/account/{id}")
    private Account getAccount(@PathVariable(name = "id") Long id){
        return accountService.getAccount(id);
    }


   /*
   @GetMapping("/create-account")
    private String createAccount(){
        accountService.createAccount();
        return "success";
    }*/


}
