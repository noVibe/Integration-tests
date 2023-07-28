package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.dto.CreateUserRequest;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.testData.TestData;
import com.skypro.simplebanking.test_services.TestSupport;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc

class TransferControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    TestData testData;
    @Autowired
    TestSupport support;

    @Test
    @Transactional
    void transfer() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User fromUser = testData.user(authUser);
        User toUser = testData.randomUser();
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long fromAccountId = support.getAccountIdByCurrency(fromUser, currency);
            long toAccountId = support.getAccountIdByCurrency(toUser, currency);
            long amount = fromUser.getAccounts().stream()
                    .filter(a -> a.getId() == fromAccountId)
                    .map(Account::getAmount)
                    .mapToLong(m -> m > 1 ? m / 2 : m)
                    .findAny().orElseThrow();

            String transferBody = new JSONObject()
                    .put("fromAccountId", fromAccountId)
                    .put("toUserId", toUser.getId())
                    .put("toAccountId", toAccountId)
                    .put("amount", amount)
                    .toString();
            mockMvc.perform(post("/transfer")
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(transferBody))
                    .andExpect(status().isOk());
        }
    }
}