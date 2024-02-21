package com.example.controller;

import com.example.domain.Account;
import com.example.domain.AccountStatus;
import com.example.domain.AccountUser;
import com.example.dto.AccountDto;
import com.example.dto.CreateAccount;
import com.example.dto.DeleteAccount;
import com.example.exception.AccountException;
import com.example.service.AccountService;
/*
import com.example.service.RedisTestService;
*/
import com.example.type.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultMatcher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(AccountController.class)
class AccountControllerTest {
    @MockBean
    private AccountService accountService;

/*    @MockBean
    private RedisTestService redisTestService;*/

    // ServiceTest 처럼 Mockebean 들을 Injection 을 안해줘도 된다
    // Movkbean 은 이름처럼 bean으로 등록한 Mock 이기때문에 자동으로 bean으로 등록이 되서
    // AccountController 에 주입이 된다


    @Autowired
    private MockMvc mockMvc;


    @Autowired
    private ObjectMapper objectMapper;
    // json -> object object -> json 상호 변환시키는 objectMapper

    @Test
        void successCreateAccount() throws Exception {
            //given
        given(accountService.createAccount(anyLong(),anyLong()))
                // 어떤 값이든 넣어서 createAccount를 호출
                .willReturn(AccountDto.builder()
                        .userId(1L)
                        .accountNumber("1234567890")
                        .registeredAt(LocalDateTime.now())
                        .unRegisteredAt(LocalDateTime.now())
                        .build());
            //when
            //then

        mockMvc.perform(post("/account")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                        new CreateAccount.Request(3333L, 1111L)
                )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1L))
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andDo(print());
        }


    @Test
        void successGetAccount() throws Exception {
            //given
        Account account = Account.builder()
                .accountNumber("3456")
                .accountStatus(AccountStatus.IN_USE)
                .build();
        account.setId(213123L);

        given(accountService.getAccount(anyLong()))
                .willReturn(account);
            //when
            //then
        mockMvc.perform(get("/account/876"))
                .andDo(print())
                .andExpect(jsonPath("$.id").value(213123L))
                .andExpect(jsonPath("$.accountNumber").value("3456"))
                .andExpect(jsonPath("$.accountStatus").value("IN_USE"))
                .andExpect(status().isOk());
        // $. -> json 시작점
        }

        @Test
            void successDeleteAccount() throws Exception{
                //given
            given(accountService.deleteAccount(anyLong(), anyString()))
                    .willReturn(AccountDto.builder()
                            .userId(1L)
                            .accountNumber("1234567890")
                            .registeredAt(LocalDateTime.now())
                            .unRegisteredAt(LocalDateTime.now())
                            .build());
                //when
                //then
            mockMvc.perform(delete("/account")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(
                            new DeleteAccount.Request(3333L, "9876543210")
                            // userId랑 accountNumber에 사실 어떤값이든 상관 없다
                            // anyLong(), anyString() 이기 때문이다
                    )))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.userId").value(1))
                    .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                    .andDo(print());
            }


            @Test
                void successGetAccountsByUserId() throws Exception {
                    //given
                List<AccountDto> accountDtos =
                        Arrays.asList(AccountDto.builder()
                                        .accountNumber("1234567890")
                                        .balance(1000L)
                                .build(),
                                AccountDto.builder()
                                        .accountNumber("111111111")
                                        .balance(1000L)
                                        .build(),
                                AccountDto.builder()
                                        .accountNumber("222222222")
                                        .balance(1000L)
                                        .build());
                given(accountService.getAccountByUserId(anyLong()))
                        .willReturn(accountDtos);
                    //when
                    //then

                mockMvc.perform(get("/account?user_id=1"))
                        .andDo(print())
                        .andExpect(jsonPath("$[0].accountNumber").value("1234567890"))
                        .andExpect(jsonPath("$[0].balance").value(1000))
                        .andExpect(jsonPath("$[1].accountNumber").value("111111111"))
                        .andExpect(jsonPath("$[1].balance").value(1000))
                        .andExpect(jsonPath("$[2].accountNumber").value("222222222"))
                        .andExpect(jsonPath("$[2].balance").value(1000));
                }

                @Test
                    void failGetAccount() throws Exception{
                        //given
                    given(accountService.getAccount(anyLong()))
                            .willThrow(new AccountException(
                                    ErrorCode.ACCOUNT_NOT_FOUND
                            ));
                        //when
                        //then
                    mockMvc.perform(get("/account/123"))
                            .andDo(print())
                            .andExpect(jsonPath("$.errorCode").value("ACCOUNT_NOT_FOUND"))
                            .andExpect(jsonPath("$.errorMessage").value("계좌가 없습니다"))
                            .andExpect(status().isOk());
                    }
}