package com.example.amazon.config;

public final class TestConfig {

    private static final String DEFAULT_BROWSER = "chrome";
    private static final String DEFAULT_BASE_URL = "https://www.amazon.com/";
    private static final boolean DEFAULT_HEADLESS = true;

    private TestConfig() {
    }

    public static String browser() {
        return System.getProperty("browser", DEFAULT_BROWSER).trim().toLowerCase();
    }

    public static String baseUrl() {
        return System.getProperty("baseUrl", DEFAULT_BASE_URL).trim();
    }

    public static boolean headless() {
        return Boolean.parseBoolean(System.getProperty("headless", String.valueOf(DEFAULT_HEADLESS)));
    }
}
