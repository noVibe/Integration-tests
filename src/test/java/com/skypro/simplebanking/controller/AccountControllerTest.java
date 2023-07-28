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

@SpringBootTest
@AutoConfigureMockMvc
class AccountControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    TestData testData;
    @Autowired
    TestSupport support;

    @Test
    @WithMockUser
    void getUserAccount() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long accountId = support.getAccountIdByCurrency(user, currency);
            mockMvc.perform(get("/account/{id}", accountId)
                            .with(user(authUser)))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$").isMap(),
                            jsonPath("$.length()").value(AccountCurrency.values().length),
                            jsonPath("$.id").value(accountId),
                            jsonPath("$.currency").value(currency.name()),
                            jsonPath("$.amount").value(user.getAccounts().stream()
                                    .filter(a -> a.getAccountCurrency().equals(currency))
                                    .mapToLong(Account::getAmount)
                                    .findAny().orElseThrow())
                    );
        }
    }

    @Test
    @Transactional
    void depositToAccount() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        int deposit = 100;
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long accountId = support.getAccountIdByCurrency(user, currency);
            JSONObject body = new JSONObject().put("amount", deposit);
            mockMvc.perform(post("/account/deposit/{id}", accountId)
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.toString()))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.id").value(accountId),
                            jsonPath("$.currency").value(currency.name()),
                            jsonPath("$.amount").value(user.getAccounts().stream()
                                    .filter(a -> a.getAccountCurrency().equals(currency))
                                    .mapToLong(Account::getAmount)
                                    .map(m -> m + deposit)
                                    .findFirst().orElseThrow()
                            )

                    );
        }
    }

    @Test
    void withdrawFromAccount() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        int withdraw = 1;
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            JSONObject body = new JSONObject().put("amount", withdraw);
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long accountId = support.getAccountIdByCurrency(user, currency);
            mockMvc.perform(post("/account/withdraw/{id}", accountId)
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.toString()))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.id").value(accountId),
                            jsonPath("$.currency").value(currency.name()),
                            jsonPath("$.amount").value(user.getAccounts().stream()
                                    .filter(a -> a.getAccountCurrency().equals(currency))
                                    .mapToLong(Account::getAmount)
                                    .map(m -> m - withdraw)
                                    .findFirst().orElseThrow()
                            )

                    );
        }
    }
}