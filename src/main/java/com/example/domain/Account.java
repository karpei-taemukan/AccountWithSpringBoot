package com.example.domain;

import com.example.exception.AccountException;
import com.example.type.ErrorCode;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity // 일종의 DB테이블 설정 클래스
public class Account extends BaseEntity {

    @ManyToOne// user가 account를 10개까지 가질수 있음
    private AccountUser accountUser;

    private String accountNumber;

    @Enumerated(EnumType.STRING) // db에 enum 값을 0,1,2 형식으로 DB에 저장을 안하게 해줌
    private AccountStatus accountStatus;

    private Long balance;

    private LocalDateTime registeredAt;
    private LocalDateTime unRegisteredAt;


    public void useBalance(Long amount){
        if(amount > balance){
            throw new AccountException(ErrorCode.AMOUNT_EXCEED_BALANCE);
        }
        balance-=amount;
    }

    public void cancelBalance(Long amount) {
        if(amount < 0){
            throw new AccountException(ErrorCode.INVALID_REQUEST);
        }
        balance+=amount;
    }
}