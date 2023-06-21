package com.skypro.simplebanking.service;

import com.skypro.simplebanking.dto.TransferRequest;
import com.skypro.simplebanking.service.AccountService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {
  private AccountService accountService;

  @Transactional
  public void transfer(long id, TransferRequest transferRequest) {
    accountService.withdrawFromAccount(
        id, transferRequest.getFromAccountId(), transferRequest.getAmount());
    accountService.depositToAccount(
        transferRequest.getToUserId(),
        transferRequest.getToAccountId(),
        transferRequest.getAmount());
  }
}