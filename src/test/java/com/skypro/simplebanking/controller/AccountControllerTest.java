package com.skypro.simplebanking.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.dto.CreateUserRequest;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.repository.UserRepository;
import com.skypro.simplebanking.testData.TestData;
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
@Transactional
@Sql("/sql/fill.sql")
class AccountControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    TestData testData;

    @Test
    @WithMockUser
    void getUserAccount() throws Exception {
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            mockMvc.perform(get("/account/{id}", id)
                            .with(user(testData.USER)))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$").isMap(),
                            jsonPath("$.length()").value(AccountCurrency.values().length),
                            jsonPath("$.id").value(id),
                            jsonPath("$.currency").value(AccountCurrency.values()[id - 1].toString()),
                            jsonPath("$.amount").value(1)
                    );
        }
    }

    @Test
    @Transactional
    void depositToAccount() throws Exception {
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            JSONObject body = new JSONObject().put("amount", 99);
            mockMvc.perform(post("/account/deposit/{id}", id)
                            .with(user(testData.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.toString()))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.id").value(id),
                            jsonPath("$.amount").value(100),
                            jsonPath("$.currency").value(AccountCurrency.values()[id - 1].toString())
                    );
        }
    }

    @Test
    void withdrawFromAccount() throws Exception {
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            String requestBody = new JSONObject()
                    .put("amount", 1)
                    .toString();
            mockMvc.perform(post("/account/withdraw/{id}", id)
                            .with(user(testData.USER))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.id").value(id),
                            jsonPath("$.amount").value(0),
                            jsonPath("$.currency").value(AccountCurrency.values()[id - 1].toString())
                    );
        }
    }
}