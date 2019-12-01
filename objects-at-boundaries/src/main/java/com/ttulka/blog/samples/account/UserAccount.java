package com.ttulka.blog.samples.account;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Random;

import lombok.EqualsAndHashCode;
import lombok.NonNull;

@EqualsAndHashCode(of = "username")
class UserAccount implements Account {

    private final String username;
    private final String email;

    private byte[] encryptedPassword;
    private String salt;

    private ZonedDateTime lastLoggedIn;

    private Long id;

    private final UserAccountEntries entries;

    UserAccount(@NonNull String username, @NonNull String email, @NonNull String password,
                @NonNull UserAccountEntries entries) {
        this.username = username;
        this.email = email;
        this.salt = String.valueOf(new Random().nextInt());
        this.encryptedPassword = encryptedPassword(password, this.salt);
        this.entries = entries;
    }

    UserAccount(long id, @NonNull String username, @NonNull String email,
                @NonNull byte[] encryptedPassword, @NonNull String salt,
                ZonedDateTime lastLoggedIn,
                @NonNull UserAccountEntries entries) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.salt = salt;
        this.lastLoggedIn = lastLoggedIn;
        this.entries = entries;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void login() {
        lastLoggedIn = ZonedDateTime.now();
        update();
    }

    @Override
    public boolean canLogin(@NonNull String password) {
        return Arrays.equals(encryptedPassword, encryptedPassword(password, salt));
    }

    @Override
    public void changePassword(@NonNull String newPassword) {
        encryptedPassword = encryptedPassword(newPassword, salt);
        update();
    }

    @Override
    public void register() {
        if (isRegistered()) {
            throw new AccountException(username);
        }
        UserAccountEntries.Entry savedEntry = entries.save(new UserAccountEntries.Entry(
                null, username, email, encryptedPassword, salt, lastLoggedIn));
        id = savedEntry.id;
    }

    @Override
    public void unregister() {
        if (isRegistered()) {
            entries.deleteById(id);
        }
    }

    private void update() {
        if (isRegistered()) {
            entries.save(new UserAccountEntries.Entry(
                    id, username, email, encryptedPassword, salt, lastLoggedIn));
        }
    }

    private boolean isRegistered() {
        return id != null;
    }

    private byte[] encryptedPassword(String password, String salt) {
        return String.valueOf((password + salt).hashCode()).getBytes(StandardCharsets.UTF_8);
    }
}
