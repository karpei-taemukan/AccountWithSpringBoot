package com.example.service;

import com.example.domain.Account;
import com.example.domain.AccountStatus;
import com.example.domain.AccountUser;
import com.example.dto.AccountDto;
import com.example.dto.AccountInfo;
import com.example.exception.AccountException;
import com.example.repository.AccountUserRepository;
import com.example.repository.AccountRepository;
import com.example.type.ErrorCode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.example.domain.AccountStatus.IN_USE;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountUserRepository accountUserRepository;
 /*
    //@Autowired
    private final AccountRepository accountRepository;
    // final 로 선언하면 생성자를 통해서만 데이터 삽입가능


   @Transactional
    public void createAccount(){
        Account account =
                Account.builder()
                        .accountNumber("40000")
                        .accountStatus(AccountStatus.IN_USE)
                        .build();
        accountRepository.save(account);
    }*/


    /**
     // 사용자가 있는 지 조회
     // 계좌의 번호 생성하고
     // 계좌를 저장하고 그 정보를 남긴다

     createAccount 가  Account를 controller에 리턴하는 건 별로 바람직하지 못함
     controller는 Account의 정보의 일부를 필요로 할지 아님
     다 필요로 할지 아님
     더 많은 정보를 필요로 할지 모름 (상황에 따라 바뀔수 있다)

     그렇다고 DB에 1대1 매칭한 Entity인 Account를 바꾸면 안된다


     ===> 차라리 Service와 Controller 메서드 간 통신할때 쓰는 별도의 DTO를 만든다 (lazy loading도 해결)

     */
    @Transactional
    public AccountDto createAccount(Long userId, Long initialBalance){

        AccountUser accountUser = getAccountUser(userId);

        validateCreateAccount(accountUser);

        String newAccountNumber = accountRepository.findFirstByOrderByIdDesc()
                .map(account -> (Integer.parseInt(account.getAccountNumber())) + 1 + "")
                .orElse("1000000000");

        return AccountDto.fromEntity(
                accountRepository.save(
                Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build()
        ));
    }

    private void validateCreateAccount(AccountUser accountUser) {
        if(accountRepository.countByAccountUser(accountUser) == 10){
            throw new AccountException(ErrorCode.MAX_ACCOUNT_PER_USER_10);
        }
    }

    @Transactional
    public Account getAccount(Long id) {
        if(id < 0){
            throw new RuntimeException("Minus");
        }
        return accountRepository.findById(id).get();
    }

    @Transactional
    public AccountDto deleteAccount(Long userId, String accountNumber) {
        AccountUser accountUser = getAccountUser(userId);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountException(ErrorCode.ACCOUNT_NOT_FOUND));

        validateDeleteAccount(accountUser, account);

        account.setAccountStatus(AccountStatus.UNREGISTERED);
        account.setUnRegisteredAt(LocalDateTime.now());

        accountRepository.save(account);

        return AccountDto.fromEntity(account);
    }

    private void validateDeleteAccount(AccountUser accountUser, Account account){
        if(!Objects.equals(accountUser.getId(), account.getAccountUser().getId())){
            throw new AccountException(ErrorCode.USER_ACCOUNT_UN_MATCH);
        }


        if(account.getAccountStatus() == AccountStatus.UNREGISTERED){
            throw new AccountException(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED);
        }

        if(account.getBalance() > 0){
            throw new AccountException(ErrorCode.BALANCE_NOT_EMPTY);
        }

    }

    @Transactional
    public List<AccountDto> getAccountByUserId(Long userId) {
        AccountUser accountUser = getAccountUser(userId);

        List<Account> accounts = accountRepository.findByAccountUser(accountUser);

        return accounts.stream()
                .map(AccountDto::fromEntity)
                // .map(account -> AccountDto.fromEntity(account))
                .collect(Collectors.toList());
    }

    private AccountUser getAccountUser(Long userId) {
        return accountUserRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ErrorCode.USER_NOT_FOUND));

    }
}