package com.example.repository;

import com.example.domain.Account;
import com.example.domain.AccountUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findFirstByOrderByIdDesc(); // 가장 최근 계좌번호를 가져올때 사용
    // 맨처음 계좌번호를 가져올때는 없을 수도 있기때문에 Optional로 받아준다

    Integer countByAccountUser(AccountUser accountUser); // 한 계정이 10개 이상의 계좌를 가질 수 없다

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByAccountUser(AccountUser accountUser);
}
