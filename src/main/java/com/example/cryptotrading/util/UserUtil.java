package com.example.cryptotrading.util;

import lombok.experimental.UtilityClass;

/**
 * Utility for retrieving current logged-in user. Returns null when auth is not configured.
 * Used by BaseEntity for audit fields.
 */
@UtilityClass
public class UserUtil {

    public static String getCurrentLoginUser() {
        return null;
    }
}
