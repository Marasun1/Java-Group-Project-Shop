package com.store.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppUserTest {

    @Test
    void gettersAndSettersWorkCorrectly() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 5, 4, 16, 5);

        AppUser user = new AppUser();
        user.setId(9L);
        user.setRoleId(1L);
        user.setRoleName("ADMIN");
        user.setEmail("admin@example.com");
        user.setFullName("Іван Петренко");
        user.setPasswordHash("secret123");
        user.setActive(true);
        user.setCreatedAt(createdAt);

        assertEquals(9L, user.getId());
        assertEquals(1L, user.getRoleId());
        assertEquals("ADMIN", user.getRoleName());
        assertEquals("admin@example.com", user.getEmail());
        assertEquals("Іван Петренко", user.getFullName());
        assertEquals("secret123", user.getPasswordHash());
        assertTrue(user.getActive());
        assertEquals(createdAt, user.getCreatedAt());
    }
}
