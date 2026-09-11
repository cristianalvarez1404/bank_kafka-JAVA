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

    /*
     * Get account by account number
     * @param accountNumber
     * @return
     * */
    public AccountResponse getAccount(String accountNumber){
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return mapToResponse(account);
    }

    /*
    * Get account balance
    * @param accountNumber
    * @return
    * */
    public BigDecimal getBalance(String accountNumber){
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return account.getBalance();
    }

    /*
    * Block account - called by Fraud detection service via kafka
    * @param accountNumber
    */

    /*
    *   Block account - called by fraud detection service via kafka
    * */
    public void blockAccount(String accountNumber){
        log.info("Blocking account: {}", accountNumber);
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setAccountStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Account blocked: {}", accountNumber);
    }

    public void unblockAccount(String accountNumber){
        log.info("unblocking account: {}", accountNumber);
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setAccountStatus(AccountStatus.ACTIVE);
        accountRepository.save(account);
        log.info("Account unblocked: {}", accountNumber);
    }

    /*
    * Deduct balance from sender account.
    * Called by Transaction Service
    * @param accountNumber
    * @param amount
    */
    public void deductBalance(String accountNumber, BigDecimal amount){
        log.info("Deduction balance {} from account: {}", amount, accountNumber);
        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
        if(account.getAccountStatus() != AccountStatus.ACTIVE){
            throw new RuntimeException("Account is not active " + accountNumber);
        }

        if(account.getBalance().compareTo(amount) < 0){
            throw new RuntimeException("Insufficient funds for account " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        log.info("Balance updated. New Balance: {}", account.getBalance());
    }

    /*
    * Credit balance
    * Called by Transaction Service via kafka
    * @Param accountNumber
    * @Param amount
    * */
    public void creditBalance(String accountNumber, BigDecimal  amount){
        log.info("Crediting {} to account: {}", amount, accountNumber);

        Account account = accountRepository.findAccountByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        log.info("Balance Credited. New Balance: {}", account.getBalance());
    }

    // Generate unique 12 digit account number
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
}
