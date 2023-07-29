package com.skypro.simplebanking.testData;

import com.github.javafaker.Faker;
import com.skypro.simplebanking.dto.BankingUserDetails;
import com.skypro.simplebanking.entity.Account;
import com.skypro.simplebanking.entity.AccountCurrency;
import com.skypro.simplebanking.entity.User;
import com.skypro.simplebanking.repository.AccountRepository;
import com.skypro.simplebanking.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class TestData {
    final AccountRepository accountRepository;
    final UserRepository userRepository;
    final Faker faker = new Faker();
    final PasswordEncoder encoder;
    public final Map<String, String> USERNAME_PASSWORD;
    public final List<User> USERS;
    public final List<BankingUserDetails> AUTH_USERS;

    public TestData(AccountRepository accountRepository,
                    UserRepository userRepository,
                    PasswordEncoder encoder,
                    @Value("${test.database.seed}") int seed) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.encoder = encoder;
        this.USERNAME_PASSWORD = getUsernamePasswordMap(seed);
        this.USERS = seedUsers();
        this.AUTH_USERS = getAuthUsers();
    }

    private List<BankingUserDetails> getAuthUsers() {
        return USERS.stream()
                .map(u ->
                        new BankingUserDetails(u.getId(), u.getUsername(), USERNAME_PASSWORD.get(u.getUsername()), false))
                .toList();
    }

    private User fillUserWithAccounts(User user) {
        for (AccountCurrency currency : AccountCurrency.values()) {
            Account account = new Account();
            account.setUser(user);
            account.setAccountCurrency(currency);
            account.setAmount(faker.random().nextLong(Long.MAX_VALUE - 2) + 1);
            user.getAccounts().add(account);
            accountRepository.save(account);
        }
        return user;
    }

    public BankingUserDetails randomAuthUser() {
        return AUTH_USERS.get(faker.random().nextInt(AUTH_USERS.size()));
    }

    public User user(BankingUserDetails user) {
        return USERS.get((int)user.getId() - 1);
    }
    public User randomUser() {
        return USERS.get(faker.random().nextInt(USERS.size()));
    }

    public Map<String, String> getUsernamePasswordMap(int size) {
        Map<String, String> usernamePassword = new HashMap<>();
        usernamePassword.put("username", "password");
        String username;
        while (usernamePassword.size() < size) {
            username = faker.name().username();
            usernamePassword.put(username, faker.internet().password());
        }
        return Collections.unmodifiableMap(usernamePassword);
    }

    private List<User> seedUsers() {
        return USERNAME_PASSWORD.entrySet().stream()
                .map(m -> {
                    User user = new User();
                    user.setUsername(m.getKey());
                    user.setPassword(encoder.encode(m.getValue()));
                    user.setAccounts(new ArrayList<>());
                    return user;
                })
                .map(userRepository::save)
                .map(this::fillUserWithAccounts)
                .toList();
    }
    public BankingUserDetails authUserWithInvalidPassword() {
        BankingUserDetails valid = randomAuthUser();
        return new BankingUserDetails(
                valid.getId(),
                valid.getUsername(),
                USERNAME_PASSWORD.get(valid.getUsername()) + "x",
                false);
    }

}
