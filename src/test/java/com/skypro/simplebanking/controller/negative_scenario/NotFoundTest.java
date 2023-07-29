package com.skypro.simplebanking.controller.negative_scenario;

import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.testData.TestData;
import com.skypro.simplebanking.test_services.TestSupport;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class NotFoundTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    TestData testData;
    @Autowired
    TestSupport support;
    @Test
    void transfer_forbidden_fromAccountId_not_found() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User fromUser = testData.user(authUser);
        User toUser = testData.randomUser();
        User wrongUser;
        do {
            wrongUser = testData.randomUser();
        } while (wrongUser.equals(fromUser));
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long fromAccountId = support.getAccountIdByCurrency(wrongUser, currency);
            long toAccountId = support.getAccountIdByCurrency(toUser, currency);
            long amount = wrongUser.getAccounts().stream()
                    .filter(a -> a.getId() == fromAccountId)
                    .map(Account::getAmount)
                    .mapToLong(m -> m > 1 ? m / 2 : m)
                    .findAny().orElseThrow();
            String requestBody = new JSONObject()
                    .put("fromAccountId", fromAccountId)
                    .put("toUserId", toUser.getId())
                    .put("toAccountId", toAccountId)
                    .put("amount", amount)
                    .toString();
            mockMvc.perform(post("/transfer")
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isNotFound());
        }
    }
    @Test
    void account_not_found() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        JSONObject body = new JSONObject().put("amount", 1);
        long wrongAccountId = user.getAccounts().stream()
                .reduce(0L, (a, b) -> a + b.getAmount(), Long::sum);
        mockMvc.perform(get("/account/{id}", wrongAccountId)
                        .with(user(authUser)))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/account/withdraw/{id}", wrongAccountId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/account/deposit/{id}", wrongAccountId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isNotFound());

        User toUser = testData.randomUser();
        long toAccountId = support.getAccountIdByCurrency(toUser, AccountCurrency.RUB);
        long amount = 1;
        body = new JSONObject()
                .put("fromAccountId", wrongAccountId)
                .put("toUserId", toUser.getId())
                .put("toAccountId", toAccountId)
                .put("amount", amount);
        mockMvc.perform(post("/transfer")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isNotFound());

        wrongAccountId = toUser.getAccounts().stream()
                .reduce(0L, (a, b) -> a + b.getAmount(), Long::sum);
        body.put("fromAccountId", support.getAccountIdByCurrency(user, AccountCurrency.EUR))
                .put("toAccountId", wrongAccountId);
        mockMvc.perform(post("/transfer")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isNotFound());
    }

}
