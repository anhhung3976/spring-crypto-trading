package com.example.cryptotrading.util;

/**
 * Utility for retrieving current logged-in user. Returns null when auth is not configured.
 * Used by BaseEntity for audit fields.
 */
public final class UserUtil {

    private UserUtil() {
    }

    public static String getCurrentLoginUser() {
        return null;
    }
}
