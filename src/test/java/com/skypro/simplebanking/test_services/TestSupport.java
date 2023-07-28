package com.skypro.simplebanking.test_services;

import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import org.springframework.stereotype.Component;

@Component
public class TestSupport {
    public long getAccountIdByCurrency(User user, AccountCurrency currency) {
        return user.getAccounts().stream()
                .filter(a -> a.getAccountCurrency().equals(currency))
                .mapToLong(Account::getId)
                .findAny().orElseThrow();
    }
}
