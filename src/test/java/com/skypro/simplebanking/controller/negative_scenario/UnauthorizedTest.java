package com.skypro.simplebanking.controller.negative_scenario;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class UnauthorizedTest {
    @Autowired
    MockMvc mockMvc;
    @Test
    void request_unauthorized() throws Exception {
        mockMvc.perform(get("/user/list"))
                .andExpect(status().isUnauthorized());
    }

}
