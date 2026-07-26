package com.stockinfo.util;

/**
 * Application-wide constants — avoids magic strings/numbers
 * scattered across the codebase.
 */
public final class AppConstants {

    private AppConstants() {
        // prevent instantiation
    }

    // Roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_USER = "ROLE_USER";

    // Pagination defaults
    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 10;

    // Transaction types
    public static final String TXN_BUY = "BUY";
    public static final String TXN_SELL = "SELL";

    // Activity log actions
    public static final String ACTION_LOGIN = "LOGIN";
    public static final String ACTION_REGISTER = "REGISTER";
    public static final String ACTION_BUY_STOCK = "BUY_STOCK";
    public static final String ACTION_SELL_STOCK = "SELL_STOCK";
    public static final String ACTION_ADD_WATCHLIST = "ADD_WATCHLIST";
    public static final String ACTION_REMOVE_WATCHLIST = "REMOVE_WATCHLIST";
}
