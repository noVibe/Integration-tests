package com.skypro.simplebanking.controller.negative_scenario;

import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.testData.TestData;
import com.skypro.simplebanking.test_services.TestSupport;
import org.assertj.core.api.Assertions;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class BadRequestTest {


    @Autowired
    MockMvc mockMvc;
    @Autowired
    TestData testData;
    @Autowired
    TestSupport support;
    @Test
    void not_enough_balance_to_withdraw_bad_request() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        User toUser = testData.randomUser();
        String actual;
        for (int id = 1; id <= AccountCurrency.values().length; id++) {
            AccountCurrency currency = AccountCurrency.values()[id - 1];
            long accountId = support.getAccountIdByCurrency(user, currency);
            Account account = support.getAccountById(user, accountId);
            long withdraw = account.getAmount() + 1;
            String expected = "Cannot withdraw " + withdraw + " " + currency.name();
            JSONObject body = new JSONObject().put("amount", withdraw);
            actual = mockMvc.perform(post("/account/withdraw/{id}", accountId)
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.toString()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();
            Assertions.assertThat(actual.contains(expected)).isTrue();

            long fromAccountId = support.getAccountIdByCurrency(user, currency);
            long toAccountId = support.getAccountIdByCurrency(toUser, currency);
            body.put("fromAccountId", fromAccountId)
                    .put("toUserId", toUser.getId())
                    .put("toAccountId", toAccountId);
            actual = mockMvc.perform(post("/transfer")
                            .with(user(authUser))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body.toString()))
                    .andExpect(status().isBadRequest())
                    .andReturn().getResponse().getContentAsString();

            Assertions.assertThat(actual.contains(expected)).isTrue();
        }
    }

    @Test
    void negative_amount_bad_request() throws Exception {
        String actual;
        String expected = "Amount should be more than 0";
        int negativeAmount = -100;
        BankingUserDetails authUser = testData.randomAuthUser();
        User user = testData.user(authUser);
        long accountId = support.getAccountIdByCurrency(user, AccountCurrency.EUR);
        JSONObject body = new JSONObject().put("amount", negativeAmount);
        actual = mockMvc.perform(post("/account/withdraw/{id}", accountId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(actual.contains(expected)).isTrue();

        actual = mockMvc.perform(post("/account/deposit/{id}", accountId)
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(actual.contains(expected)).isTrue();

        AccountCurrency currency = AccountCurrency.EUR;
        User toUser = testData.randomUser();
        long fromAccountId = support.getAccountIdByCurrency(user, currency);
        long toAccountId = support.getAccountIdByCurrency(toUser, currency);
        body.put("fromAccountId", fromAccountId)
                .put("toUserId", toUser.getId())
                .put("toAccountId", toAccountId);
        mockMvc.perform(post("/transfer")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body.toString()))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(actual.contains(expected)).isTrue();
    }
    @Test
    void currency_mismatch_bad_request() throws Exception {
        BankingUserDetails authUser = testData.randomAuthUser();
        User fromUser = testData.user(authUser);
        User toUser = testData.randomUser();
        String expected = "Account currencies should be same";
        long fromAccountId = support.getAccountIdByCurrency(fromUser, AccountCurrency.EUR);
        long toAccountId = support.getAccountIdByCurrency(toUser, AccountCurrency.RUB);
        long amount = 1;
        String requestBody = new JSONObject()
                .put("fromAccountId", fromAccountId)
                .put("toUserId", toUser.getId())
                .put("toAccountId", toAccountId)
                .put("amount", amount)
                .toString();
        String actual = mockMvc.perform(post("/transfer")
                        .with(user(authUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
        Assertions.assertThat(actual.contains(expected)).isTrue();
    }

    @Test
    void user_already_exists_bad_request(@Value("${app.security.admin-token}") String token) throws Exception {
        User user = testData.randomUser();
        String requestBody = new JSONObject()
                .put("username", user.getUsername())
                .put("password", "xxx")
                .toString();
        mockMvc.perform(post("/user/")
                        .header("X-SECURITY-ADMIN-KEY", token)
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
