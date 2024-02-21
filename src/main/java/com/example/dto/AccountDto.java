package com.example.dto;

import com.example.domain.Account;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/*
@Getter
@Setter
@ToString
@RequiredArgsConstructor
@AllArgsConstructor
//@Data --> toString가 있어서 의도치않은 데이터 노출이 있을 수 있다 (잘안씀)
@Slf4j

public class AccountDto {
    private String accountNumber;
    private String nickname;
    private LocalDateTime registeredAt;

    public void log() {
        log.error("error is occurred.");
    }
}*/


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto { // entity(Account)클래스보다 단순화된 버전으로 생성
    private Long userId;
    private String accountNumber;
    private Long balance;
    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;

    public static AccountDto fromEntity(Account account){
        // entity class(Account) 를 이용해서 AccountDto가 Account 의 변수들을 사용할 수 있게 해준다
        return AccountDto
                .builder()
                .userId(account.getAccountUser().getId())
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .registeredAt(account.getRegisteredAt())
                .unRegisteredAt(account.getUnRegisteredAt())
                .build();
    }
}