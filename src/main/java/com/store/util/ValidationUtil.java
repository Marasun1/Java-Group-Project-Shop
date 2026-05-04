package com.store.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;

/**
 * Набір перевикористовуваних методів для перевірки даних, введених у формах.
 */
public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");

    private ValidationUtil() {
    }

    /**
     * Перевіряє, що текстове значення є обов'язковим і не порожнім.
     *
     * @param value сире введене значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return обрізане непорожнє значення
     */
    public static String required(String value, String fieldName) {
        String trimmed = value != null ? value.trim() : "";
        if (trimmed.isBlank()) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" є обов'язковим.");
        }
        return trimmed;
    }

    /**
     * Нормалізує необов'язкове текстове значення.
     *
     * @param value сире введене значення
     * @return обрізане значення або порожній рядок, якщо значення дорівнює {@code null}
     */
    public static String optional(String value) {
        return value != null ? value.trim() : "";
    }

    /**
     * Перевіряє коректність обов'язкового SKU.
     *
     * @param value сире значення SKU
     * @return нормалізований SKU
     */
    public static String requiredSku(String value) {
        String sku = required(value, "SKU");
        if (sku.length() < 2) {
            throw new IllegalArgumentException("SKU має містити щонайменше 2 символи.");
        }
        if (!SKU_PATTERN.matcher(sku).matches()) {
            throw new IllegalArgumentException("SKU може містити лише латинські літери, цифри, крапку, дефіс або підкреслення.");
        }
        return sku;
    }

    /**
     * Перевіряє коректність обов'язкового email.
     *
     * @param value сире значення email
     * @return нормалізований email
     */
    public static String requiredEmail(String value) {
        String email = required(value, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Email має містити @ і домен, наприклад user@example.com.");
        }
        return email;
    }

    /**
     * Перевіряє, що значення є додатним цілим числом.
     *
     * @param value сире числове значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return розібране додатне число
     */
    public static Long positiveLong(String value, String fieldName) {
        Long number = parseLong(value, fieldName);
        if (number <= 0) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" має бути більшим за 0.");
        }
        return number;
    }

    /**
     * Перевіряє, що значення є невід'ємним цілим числом.
     *
     * @param value сире числове значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return розібране невід'ємне число
     */
    public static Long nonNegativeLong(String value, String fieldName) {
        Long number = parseLong(value, fieldName);
        if (number < 0) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" не може бути меншим за 0.");
        }
        return number;
    }

    /**
     * Перевіряє, що значення є невід'ємним десятковим числом.
     *
     * @param value сире числове значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return розібране невід'ємне десяткове число
     */
    public static BigDecimal nonNegativeDecimal(String value, String fieldName) {
        try {
            BigDecimal decimal = new BigDecimal(required(value, fieldName).replace(',', '.'));
            if (decimal.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Поле \"" + fieldName + "\" не може бути меншим за 0.");
            }
            return decimal;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" має бути числом.");
        }
    }

    /**
     * Перевіряє, що значення є додатним десятковим числом.
     *
     * @param value сире числове значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return розібране додатне десяткове число
     */
    public static BigDecimal positiveDecimal(String value, String fieldName) {
        BigDecimal decimal = nonNegativeDecimal(value, fieldName);
        if (decimal.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" має бути більшим за 0.");
        }
        return decimal;
    }

    /**
     * Розбирає необов'язкове значення дати й часу за переданим форматером.
     *
     * @param value сире значення дати й часу
     * @param fieldName назва поля для повідомлення про помилку
     * @param formatter форматер для розбору значення
     * @return розібрана дата й час або {@code null}, якщо поле порожнє
     */
    public static LocalDateTime optionalDateTime(String value, String fieldName, DateTimeFormatter formatter) {
        String trimmed = optional(value);
        if (trimmed.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(trimmed, formatter);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" має формат дд.ММ.рррр гг:хх.");
        }
    }

    /**
     * Розбирає рядок у ціле число з базовою перевіркою обов'язковості.
     *
     * @param value сире значення
     * @param fieldName назва поля для повідомлення про помилку
     * @return розібране ціле число
     */
    private static Long parseLong(String value, String fieldName) {
        try {
            return Long.parseLong(required(value, fieldName));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Поле \"" + fieldName + "\" має бути цілим числом.");
        }
    }
}
