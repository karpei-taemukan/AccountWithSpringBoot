package com.example.service;

import com.example.domain.Account;
import com.example.domain.AccountStatus;
import com.example.domain.AccountUser;
import com.example.domain.Transaction;
import com.example.dto.TransactionDto;
import com.example.exception.AccountException;
import com.example.repository.AccountRepository;
import com.example.repository.AccountUserRepository;
import com.example.repository.TransactionRepository;
import com.example.type.ErrorCode;
import com.example.type.TransactionResultType;
import com.example.type.TransactionType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountUserRepository accountUserRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private TransactionService transactionService;



    @Test
        void successUseBalance(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
            //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(user));
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000")
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(TransactionType.USE)
                                .transactionResultType(TransactionResultType.S)
                                .transactedAt(LocalDateTime.now())
                                .amount(1000L)
                                .balanceSnapshot(9000L)
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
            //when
        TransactionDto transactionDto = transactionService.useBalance(1L, "1000000000", 2000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(8000L , captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(1000L, transactionDto.getAmount());
        }


        @Test
        @DisplayName("해당 유저 없음 - 잔액 사용 실패")
            void useBalance_UserNotFound(){
                //given
            given(accountUserRepository.findById(anyLong()))
                    .willReturn(Optional.empty());
                //when
            AccountException exception = assertThrows(AccountException.class,
                    () -> transactionService.useBalance(1L, "1000000000", 1000L));
            //then
            assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        }

        @Test
        @DisplayName("해당 계좌 없음 - 잔액 사용 실패")
            void useBalance_AccountNotFound(){
            AccountUser user = AccountUser.builder()
                    .name("Pobi")
                    .build();
            user.setId(12L);
                //given
                given(accountUserRepository.findById(anyLong()))
                        .willReturn(Optional.of(user));
                given(accountRepository.findByAccountNumber(anyString()))
                        .willReturn(Optional.empty());
                //when
            AccountException exception = assertThrows(AccountException.class,
                    () -> transactionService.useBalance(1L, "1000000000", 1000L));
            //then
            assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
             }

             @Test
             @DisplayName("계좌 소유주 다름 - 잔액 사용 실패")
                 void useBalance_userUnMatch(){
                 AccountUser Pobi = AccountUser.builder()
                         .name("Pobi")
                         .build();
                 Pobi.setId(12L);
                 AccountUser Herry = AccountUser.builder()
                         .name("Herry")
                         .build();
                 Herry.setId(13L);
                     //given
                 given(accountUserRepository.findById(anyLong()))
                         .willReturn(Optional.of(Pobi));
                 given(accountRepository.findByAccountNumber(anyString()))
                         .willReturn(Optional.of(Account.builder()
                                 .accountUser(Herry)
                                 .balance(0L)
                                 .accountNumber("10000000012")
                                 .build()));
                     //when
                 AccountException exception = assertThrows(AccountException.class,
                         ()-> transactionService.useBalance(1L, "1000000000", 1000L));
                     //then
                 assertEquals(ErrorCode.USER_ACCOUNT_UN_MATCH, exception.getErrorCode());
    }

    @Test
            @DisplayName("해지 계좌는 사용할 수 없다")
        void useBalance_alreadyUnregistered(){
        AccountUser Pobi = AccountUser.builder()
                .name("Pobi")
                .build();
        Pobi.setId(12L);
            //given
        given(accountUserRepository.findById(anyLong()))
                .willReturn(Optional.of(Pobi));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(Account.builder()
                                .accountUser(Pobi)
                                .accountStatus(AccountStatus.UNREGISTERED)
                                .balance(0L)
                                .accountNumber("10000000012")
                        .build()));
            //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.useBalance(1L, "1234567890", 1000L));

        //then
        assertEquals(ErrorCode.ACCOUNT_ALREADY_UNREGISTERED, exception.getErrorCode());
        }

        @Test
        @DisplayName("거래 금액이 잔액보다 큰 경우")
            void useBalance_exceedAmount(){
            AccountUser user = AccountUser.builder()
                    .name("Pobi")
                    .build();
            user.setId(12L);
            Account account = Account.builder()
                    .accountUser(user)
                    .accountStatus(AccountStatus.IN_USE)
                    .balance(100L)
                    .accountNumber("10000000012")
                    .build();
                //given
            given(accountUserRepository.findById(anyLong()))
                    .willReturn(Optional.of(user));
            given(accountRepository.findByAccountNumber(anyString()))
                    .willReturn(Optional.of(account));
                //when
            AccountException exception = assertThrows(AccountException.class,
                    () -> transactionService.useBalance(1L, "1234567890", 1000L));

            //then
            // 거래 금액이 잔액보다 큰 경우이라서 계좌에 직접적인 영향을 줄수 있으므로 저장하면 안된다
            assertEquals(ErrorCode.AMOUNT_EXCEED_BALANCE,exception.getErrorCode());
            verify(transactionRepository, times(0)).save(any());
            }



    @Test
    // 같은 계정으로 여러 계좌로 거래를 시도할 경우
    @DisplayName("실패 트랜잭션 저장 성공")
    void saveFailedTransactione(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000")
                .build();
        //given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(TransactionType.USE)
                               // .transactionResultType(TransactionResultType.S) 있으나 마나이다
                                .transactionId("transactionId")
                                .transactedAt(LocalDateTime.now())
                               // .amount(1000L) 있으나 마나이다
                              //  .balanceSnapshot(9000L) 있으나 마나이다
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        /*
        *      return transactionRepository.save(...)
        *
        *      captor는 Repository에서 save할때를 capture
        *
        * */

        //when
        transactionService.saveFailedTransaction("1000000000", 2000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(2000L, captor.getValue().getAmount());
        assertEquals(10000L , captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.F, captor.getValue().getTransactionResultType());
    }
    @Test
    void successCancelBalance(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        //given
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000000")
                .build();
        // findBy ~ -> SELECT
        Transaction transaction
                 = Transaction.builder()
                .account(account)
                .transactionType(TransactionType.USE)
                .transactionResultType(TransactionResultType.S)
                .transactionId("transactionId")
                .transactedAt(LocalDateTime.now())
                .amount(1000L)
                .build();
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));

        given(transactionRepository.save(any()))
                .willReturn(
                        Transaction.builder()
                                .account(account)
                                .transactionType(TransactionType.CANCEL)
                                .transactionResultType(TransactionResultType.S)
                                .transactionId("transactionIdForCancel")
                                .transactedAt(LocalDateTime.now())
                                .build()
                );

        ArgumentCaptor<Transaction> captor = ArgumentCaptor.forClass(Transaction.class);
        //when
        TransactionDto transactionDto = transactionService.cancelBalance(
                "transactionId",
                "1000000000",
                1000L);
        //then
        verify(transactionRepository, times(1)).save(captor.capture());
        assertEquals(11000L , captor.getValue().getBalanceSnapshot());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(TransactionType.CANCEL, transactionDto.getTransactionType());
        assertEquals(1000L, captor.getValue().getAmount());
    }


    @Test
    @DisplayName("해당 계좌 없음 - 잔액 사용 취소 실패")
    void cancelTransaction_AccountNotFound(){

        //given
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.empty());
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(
                        Transaction.builder().build()
                ));
        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.ACCOUNT_NOT_FOUND, exception.getErrorCode());
    }
    @Test
    @DisplayName("원 사용 거래 없음 - 잔액 사용 취소 실패")
        void cancelTransaction_TransactionNotFound(){
            //given
            given(transactionRepository.findByTransactionId(anyString()))
                    .willReturn(Optional.empty());

            //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));

        //then

        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }


    @Test
    @DisplayName("거래와 계좌가 매칭 실패 - 잔액 사용 취소 실패")
        void cancelTransaction_TransactionAccountUnMatch(){
            AccountUser user = AccountUser.builder()
                    .name("Pobi")
                    .build();
            user.setId(12L);
            Account account = Account.builder()
                    .accountUser(user)
                    .accountStatus(AccountStatus.IN_USE)
                    .balance(10000L)
                    .accountNumber("1000000012")
                    .build();
            account.setId(1L);
            Account accountNotUse = Account.builder()
                    .accountUser(user)
                    .accountStatus(AccountStatus.IN_USE)
                    .balance(10000L)
                    .accountNumber("1000000013")
                    .build();
        account.setId(2L);
            Transaction transaction =
                    Transaction.builder()
                            .account(account)
                            .transactionId("transactionId")
                            .transactionType(TransactionType.USE)
                            .transactionResultType(TransactionResultType.S)
                            .amount(1000L)
                            .balanceSnapshot(1000L)
                            .transactedAt(LocalDateTime.now())
                            .build();
            //given
            given(transactionRepository.findByTransactionId(anyString()))
                    .willReturn(Optional.of(transaction));
            given(accountRepository.findByAccountNumber(anyString()))
                    .willReturn(Optional.of(accountNotUse));

            //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance("transactionId", "1000000000", 1000L));
        //then
        assertEquals(ErrorCode.TRANSACTION_ACCOUNT_UN_MATCH, exception.getErrorCode());
        }


    @Test
    @DisplayName("거래금액과 최소금액이 다름 - 잔액 사용 취소 실패")
    void cancelTransaction_CancelMustFully(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        account.setId(1L);
        Transaction transaction =
                Transaction.builder()
                        .account(account)
                        .transactionId("transactionId")
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(2000L)
                        .balanceSnapshot(9000L)
                        .transactedAt(LocalDateTime.now())
                        .build();
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1000000000",
                        1000L));
        //then
        assertEquals(ErrorCode.CANCEL_MUST_FULLY, exception.getErrorCode());
    }

    @Test
    @DisplayName("취소는 1년까지만 가능 - 잔액 사용 취소 실패")
    void cancelTransaction_TooOldOrder(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        account.setId(1L);
        Transaction transaction =
                Transaction.builder()
                        .account(account)
                        .transactionId("transactionId")
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                        .build();
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
        given(accountRepository.findByAccountNumber(anyString()))
                .willReturn(Optional.of(account));

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.cancelBalance(
                        "transactionId",
                        "1000000000",
                        1000L));
        //then
        assertEquals(ErrorCode.TOO_OLD_ORDER_TO_CANCEL, exception.getErrorCode());
    }


    @Test
        void successQueryTransaction(){
        AccountUser user = AccountUser.builder()
                .name("Pobi")
                .build();
        user.setId(12L);
        Account account = Account.builder()
                .accountUser(user)
                .accountStatus(AccountStatus.IN_USE)
                .balance(10000L)
                .accountNumber("1000000012")
                .build();
        Transaction transaction =
                Transaction.builder()
                        .account(account)
                        .transactionId("transactionId")
                        .transactionType(TransactionType.USE)
                        .transactionResultType(TransactionResultType.S)
                        .amount(1000L)
                        .balanceSnapshot(9000L)
                        .transactedAt(LocalDateTime.now().minusYears(1).minusDays(1))
                        .build();
            //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.of(transaction));
            //when
        TransactionDto transactionDto = transactionService.queryTransaction("trxId");
        //then
        assertEquals(TransactionType.USE, transactionDto.getTransactionType());
        assertEquals(TransactionResultType.S, transactionDto.getTransactionResultType());
        assertEquals(1000L, transactionDto.getAmount());
        assertEquals("transactionId", transactionDto.getTransactionId());
    }


    @Test
    @DisplayName("원 거래 없음 - 거래 조회 실패")
    void queryTransaction_TransactionNotFound(){
        //given
        given(transactionRepository.findByTransactionId(anyString()))
                .willReturn(Optional.empty());

        //when
        AccountException exception = assertThrows(AccountException.class,
                () -> transactionService.queryTransaction("transactionId"));

        //then

        assertEquals(ErrorCode.TRANSACTION_NOT_FOUND, exception.getErrorCode());
    }
}