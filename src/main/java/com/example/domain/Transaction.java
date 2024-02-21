package com.example.domain;

import com.example.type.TransactionResultType;
import com.example.type.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
public class Transaction extends BaseEntity {

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionResultType transactionResultType;

    @ManyToOne // account 에 여러 트랜잭션 연결
    private Account account;
    private Long amount;
    private Long balanceSnapshot;


    private String transactionId;
    //PK를 그대로 쓰면 비지니스(거래횟수 노출 등)
    // 보안 측면에서 위험
    private LocalDateTime transactedAt; //거래시간 스냅샷

}
