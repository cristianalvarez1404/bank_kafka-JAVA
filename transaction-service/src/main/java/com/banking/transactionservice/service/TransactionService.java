package com.banking.transactionservice.service;

import com.banking.transactionservice.dto.TransactionResponse;
import com.banking.transactionservice.dto.TransferRequest;

import java.util.List;

public class TransactionService {

    public TransactionResponse transfer(TransferRequest request){
        return null;
    }

    public TransactionResponse getTransaction(String transactionId){
        return null;
    }

    public List<TransactionResponse> getTransactionHistory(String accountNumber){
        return null;
    }

    public TransactionResponse verifyOTP(String transactionId, String otp){
        return null;
    }

}
