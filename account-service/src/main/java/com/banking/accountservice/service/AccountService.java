package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.entity.Account;
import com.banking.accountservice.entity.AccountStatus;
import com.banking.accountservice.entity.AccountType;
import com.banking.accountservice.repository.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.collection.spi.PersistentIdentifierBag;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private static SecureRandom secureRandom;

    public AccountResponse createAccount(CreateAccountRequest req){
        log.info("Creating account for: {}", req.getEmail());

        if(accountRepository.existsByEmail(req.getEmail())){
            throw new RuntimeException("Account already exists for email: " + req.getEmail());
        }

        Account account  = new Account();
        account.setAccountHolderName(req.getAccountHolderName());
        account.setEmail(req.getEmail());
        account.setPhone(req.getPhone());
        account.setAccountType(req.getAccountType());
        account.setAccountStatus(AccountStatus.ACTIVE);
        account.setBalance(req.getInitialDeposit());
        account.setAccountNumber(generateAccountNumber());
        account.setDailyTransactionLimit(
                req.getAccountType() == AccountType.SAVINGS
                ? new BigDecimal("100000")
                : new BigDecimal("500000")
        );

        Account savedAccount = accountRepository.save(account);
        log.info("Account created: {}", savedAccount.getAccountNumber());
        return mapToResponse(savedAccount);
    }

    private String generateAccountNumber() {
        String accountNumber;

        do {
            long number = secureRandom.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while(accountRepository.existsByAccount(accountNumber));

        return accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setId(account.getId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountHolderName(account.getAccountHolderName());
        response.setEmail(account.getEmail());
        response.setPhone(account.getPhone());
        response.setAccountStatus(account.getAccountStatus());
        response.setBalance(account.getBalance());
        response.setDailyTransactionLimit(account.getDailyTransactionLimit());
        response.setCreatedAt(account.getCreatedAt());

        return response;
    }

    public AccountResponse getAccount(String accountNumber){
        return null;
    }

    public BigDecimal getBalance(String accountNumber){
        return null;
    }

    public void blockAccount(String accountNumber){
        return ;
    }

    public void deductBalance(String accountNumber, BigDecimal amount){
        return;
    }

    public void creditBalance(String accountNumber, BigDecimal  amount){
        return;
    }
}
