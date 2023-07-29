package com.skypro.simplebanking.controller.negative_scenario;

import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.testData.TestData;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
public class ForbiddenTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    TestData testData;
    @Test
    void access_user_role_endpoint_while_admin_forbidden(@Value("${app.security.admin-token}") String token) throws Exception {
        BankingUserDetails authUSer = testData.randomAuthUser();
        mockMvc.perform(get("/user/me")
                        .header("X-SECURITY-ADMIN-KEY", token)
                        .with(user(authUSer)))
                .andExpect(status().isForbidden());
    }
}
