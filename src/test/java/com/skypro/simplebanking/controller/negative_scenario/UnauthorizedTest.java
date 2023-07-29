package com.skypro.simplebanking.controller.negative_scenario;

import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UnauthorizedTest {
    @Autowired
    MockMvc mockMvc;

    @Test
    void add_user_while_unauthorized() throws Exception {
        String requestBody = new JSONObject()
                .put("username", "uname")
                .put("password", "xxx")
                .toString();
        mockMvc.perform(post("/user/")
                        .content(requestBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
