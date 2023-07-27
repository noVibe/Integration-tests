package com.skypro.simplebanking.testData;

import com.skypro.simplebanking.dto.BankingUserDetails;
import org.springframework.stereotype.Component;

@Component
public class TestData {
   public final BankingUserDetails USER = new BankingUserDetails(1, "bob", "test", false);
}
