package com.example.service;

import com.example.domain.Account;
import com.example.domain.AccountStatus;
import com.example.domain.AccountUser;
import com.example.dto.AccountDto;
import com.example.dto.AccountInfo;
import com.example.exception.AccountException;
import com.example.repository.AccountRepository;
import com.example.repository.AccountUserRepository;
import com.example.type.ErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.swing.text.html.Option;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/*
@SpringBootTest
    // 선언한 클래스와 그 클래스의 하위클래스까지 bean으로 다 Test 컨테이너에 띄움
        // -> 의존성 주입(@Autowired)을 받아 쉽게 테스트 가능
    // 그치만 @BeforeEach로 사전에 데이터를 다 저장해야한다
    // 문제점: 동일 테스트인데 맞는 경우도 있고 틀린 경우도 있다, 모든 클래스가 bean으로 띄워져서 에러를 파악이 힘들다
class AccountServiceTest {

    @Autowired
    private AccountService accountService;


    @BeforeEach
    void init(){
        accountService.createAccount();
    }



    @Test
    void testSomething(){
        String something = "Hello "+"World";

        assertEquals("Hello World", something);
    }




    // 문제점: 아래와 같이 동일 테스트인데 맞는 경우도 있고 틀린 경우도 있다 -- > mockito가 해결

    @Test
    @DisplayName("Test 이름 변경")
        void testGetAccount1(){
        Account account = accountService.getAccount(2L);

        assertEquals("40000",account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }


    @Test
        void testGetAccount2(){
        Account account = accountService.getAccount(2L);

        assertEquals("40000",account.getAccountNumber());
        assertEquals(AccountStatus.IN_USE, account.getAccountStatus());
    }
*/
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock // accountRepository를 가짜로 만들어준다(의존성 주입과 비슷하게 생성해줌)
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @InjectMocks
    private AccountService accountService;


    @Test
    void createAccountSuccess() {
        // 기존 계정으로 새로운 계좌 만들기
        // 신규 계좌는 기존 계정, 새로운 계좌번호가 필요


        //given ->  테스트로 만들 계정, 계좌 번호 각각 생성

        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.findFirstByOrderByIdDesc())
                //  새로운 계좌 번호를 추가하기 위한 마지막에 추가한 계좌번호를 찾는다
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .build()));

        // 앞서서 찾은 정보를 통해 저장소에 정보를 저장
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000015")
                        .build());

        // save를 any(Object와 같이 기본 클래스) 라서
        // AccountService에 있는 Account에 대한 정보만 있으면 다른 정보들은 무시된다
                        /*
                        * accountRepository.save(
                         Account.builder()
                        .accountUser(accountUser)
                        .accountStatus(IN_USE)
                        .accountNumber(newAccountNumber)
                        .balance(initialBalance)
                        .registeredAt(LocalDateTime.now())
                        .build())
                        * */


        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        // 최근 조회한 계좌번호를 찾아서 +1을 한 로직이 맞는 지 체크하는 부분


        //when -> 만든 테스트 구동
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then

        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        // assertEquals("1000000013",accountDto.getAccountNumber());
        // 1000000015 를 넣어줘도 테스트는 성립 --> 문제점
        assertEquals("1000000013", captor.getValue().getAccountNumber());

    }

    @Test
    void createFirstAccount() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findFirstByOrderByIdDesc())
                .willReturn(Optional.empty());
        given(accountRepository.save(any()))
                .willReturn(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000015")
                        .build());

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.createAccount(1L, 1000L);

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000000", captor.getValue().getAccountNumber());
    }


    @Test
    @DisplayName("해당 유저 없음 - 계좌 생성 실패")
    void createAccount_UserNotFound() {

        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());

      /*    사실 밑에 테스트는 의미가 없음 어차피

      given(accountRepository.findFirstByOrderByIdDesc())
                  .willReturn(Optional.empty());

            given(accountRepository.save(any()))
                    .willReturn(Account.builder()
                            .accountUser(user)
                            .accountNumber("1000000015")
                            .build());
                            */

        //when

        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());

    }


    @Test
    @DisplayName("유저 당 최대 계좌는 10개")
    void createAccount_maxAccountIs10() {
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));

        given(accountRepository.countByAccountUser(any()))
                .willReturn(10);

        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.createAccount(1L, 1000L));

        //then
        assertEquals(ErrorCode.MAX_ACCOUNT_PER_USER_10, accountException.getErrorCode());
    }


    @Test
    void deleteAccountSuccess() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);

        //when
        AccountDto accountDto = accountService.deleteAccount(1L, "1234567890");

        //then
        verify(accountRepository, times(1)).save(captor.capture());
        assertEquals(12L, accountDto.getUserId());
        assertEquals("1000000012", captor.getValue().getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, captor.getValue().getAccountStatus());
    }

    @Test
    @DisplayName("해당 유저 없음 - 계좌 해지 실패")
    void deleteAccountFailed_UserNotFound() {
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해당 계좌 없음 - 계좌 해지 실패")
    void deleteAccountFailed_AccountNotFound() {
        //given
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then

        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, accountException.getErrorCode());
    }

    @Test
    @DisplayName("계좌 소유주 다름")
    void deleteAccountFailed_userUnMatch() {
        //given
        AccountUser pobi = AccountUser.builder()
                .name("Pobi")
                .build();
            pobi.setId(12L);
        AccountUser herry = AccountUser.builder()
                .name("Herry")
                .build();
            herry.setId(13L);
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(herry)
                        .accountNumber("1000000012")
                        .balance(0L)
                        .build()));
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, accountException.getErrorCode());
    }

    @Test
    @DisplayName("해지 계좌는 해지할 수 없다")
    void deleteAccountFailed_alreadyUnregistered() {

        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .balance(0L)
                        .accountNumber("1000000012")
                        .build()));
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));
        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, accountException.getErrorCode());
    }


    @Test
    @DisplayName("해지계좌는 잔액이 없어야 한다.")
    void deleteAccountFailed_balanceNotEmpty() {
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                        .accountUser(user)
                        .accountNumber("1000000012")
                        .balance(100L)
                        .build()));
        //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.deleteAccount(1L, "1234567890"));

        //then
        assertEquals(ErrorCode.BALANCE_NOT_EMPTY, accountException.getErrorCode());
    }

    @Test
    void successGetAccountByUserId() {

        // given
        AccountUser user = AccountUser.builder()
                .name("Pobi").build();
        user.setId(12L);
        List<Account> accounts = Arrays.asList(
                Account.builder()
                        .accountNumber("111111111")
                        .accountUser(user)
                        .balance(1000L)
                        .build(),
                Account.builder()
                        .accountNumber("222222222")
                        .accountUser(user)
                        .balance(2000L)
                        .build(),
                Account.builder()
                        .accountNumber("333333333")
                        .accountUser(user)
                        .balance(3000L)
                        .build()
        );
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        given(accountRepository.findByAccountUser(any()))
                .willReturn(accounts);

        //when

        List<AccountDto> accountDtos = accountService.getAccountByUserId(1L);

        //then

        assertEquals(3, accounts.size());
        assertEquals("111111111", accountDtos.get(0).getAccountNumber());
        assertEquals(1000, accountDtos.get(0).getBalance());
        assertEquals("222222222", accountDtos.get(1).getAccountNumber());
        assertEquals(2000, accountDtos.get(1).getBalance());
        assertEquals("333333333", accountDtos.get(2).getAccountNumber());
        assertEquals(3000, accountDtos.get(2).getBalance());


    }

    @Test
        void failedToGetAccounts(){
            //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.empty());
            //when
        AccountException accountException = assertThrows(AccountException.class,
                () -> accountService.getAccountByUserId(1L));

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, accountException.getErrorCode());
        }

}



                /*
    @Test
    @DisplayName("계좌 조회 성공")
        void testSuccess(){
        //given
        given(accountRepository
                .findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                        .build()));
        //when
        ArgumentCaptor<Long> captor = ArgumentCaptor.forClass(Long.class);

        Account account = accountService.getAccount(4555L); // anyLong이라 아무 숫자나 넣어도 됨


        //then
        verify(accountRepository, times(1)).findById(captor.capture());
        // 계좌 조회할때는 findById는 한번 호출되어야 한다
        verify(accountRepository, times(0)).save(any());
        // 계좌 조회할때는 save가 0번 호출되어야 한다

        assertEquals(4555L, captor.getValue());
        assertNotEquals(45551L, captor.getValue());


        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
        }


    @Test
    @DisplayName("계좌 조회 실패")
    void testFailedToSearchAccount(){
        //given

        //when
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () ->
                accountService.getAccount(-10L));
        // -10L을 주면 런타임예외가 발생할 것이다

        //then

        assertEquals("Minus", runtimeException.getMessage());

    }



    @Test
    void testSomething(){
        String something = "Hello "+"World";

        assertEquals("Hello World", something);
    }


    @Test
    @DisplayName("Test 이름 변경")
    void testGetAccount1(){
        //given
        given(accountRepository
                .findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                        .build()));
        //when
        Account account = accountService.getAccount(4555L);

        //then

        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }


    @Test
    void testGetAccount2(){
        //given
        given(accountRepository
                .findById(anyLong()))
                .willReturn(Optional.of(Account.builder()
                        .accountStatus(AccountStatus.UNREGISTERED)
                        .accountNumber("65789")
                        .build()));
        //when
        Account account = accountService.getAccount(4555L);

        //then

        assertEquals("65789", account.getAccountNumber());
        assertEquals(AccountStatus.UNREGISTERED, account.getAccountStatus());
    }

*/