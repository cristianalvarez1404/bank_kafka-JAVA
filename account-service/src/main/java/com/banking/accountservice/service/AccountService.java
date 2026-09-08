package com.banking.accountservice.service;

import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class AccountService {

    public AccountResponse createAccount(CreateAccountRequest req){
       return null;
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
}
