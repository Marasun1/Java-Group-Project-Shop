package com.store.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidationUtilTest {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @Test
    void requiredReturnsTrimmedValue() {
        String result = ValidationUtil.required("  Товар  ", "Назва");

        assertEquals("Товар", result);
    }

    @Test
    void requiredThrowsForBlankValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.required("   ", "Назва")
        );

        assertEquals("Поле \"Назва\" є обов'язковим.", exception.getMessage());
    }

    @Test
    void optionalReturnsEmptyStringForNull() {
        assertEquals("", ValidationUtil.optional(null));
    }

    @Test
    void requiredSkuAcceptsValidValue() {
        String result = ValidationUtil.requiredSku("SKU-101");

        assertEquals("SKU-101", result);
    }

    @Test
    void requiredSkuThrowsForTooShortValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.requiredSku("A")
        );

        assertEquals("SKU має містити щонайменше 2 символи.", exception.getMessage());
    }

    @Test
    void requiredSkuThrowsForInvalidCharacters() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.requiredSku("SKU 101")
        );

        assertEquals(
                "SKU може містити лише латинські літери, цифри, крапку, дефіс або підкреслення.",
                exception.getMessage()
        );
    }

    @Test
    void requiredEmailAcceptsValidEmail() {
        String result = ValidationUtil.requiredEmail("user@example.com");

        assertEquals("user@example.com", result);
    }

    @Test
    void requiredEmailThrowsForInvalidEmail() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.requiredEmail("userexample.com")
        );

        assertEquals("Email має містити @ і домен, наприклад user@example.com.", exception.getMessage());
    }

    @Test
    void positiveLongAcceptsPositiveNumber() {
        assertEquals(15L, ValidationUtil.positiveLong("15", "Кількість"));
    }

    @Test
    void positiveLongThrowsForZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.positiveLong("0", "Кількість")
        );

        assertEquals("Поле \"Кількість\" має бути більшим за 0.", exception.getMessage());
    }

    @Test
    void nonNegativeLongAcceptsZero() {
        assertEquals(0L, ValidationUtil.nonNegativeLong("0", "Кількість"));
    }

    @Test
    void nonNegativeLongThrowsForNegativeValue() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.nonNegativeLong("-5", "Кількість")
        );

        assertEquals("Поле \"Кількість\" не може бути меншим за 0.", exception.getMessage());
    }

    @Test
    void nonNegativeDecimalAcceptsCommaSeparator() {
        BigDecimal result = ValidationUtil.nonNegativeDecimal("12,50", "Ціна");

        assertEquals(new BigDecimal("12.50"), result);
    }

    @Test
    void positiveDecimalThrowsForZero() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.positiveDecimal("0", "Ціна")
        );

        assertEquals("Поле \"Ціна\" має бути більшим за 0.", exception.getMessage());
    }

    @Test
    void optionalDateTimeReturnsNullForBlankValue() {
        assertNull(ValidationUtil.optionalDateTime("   ", "Діє до", FORMATTER));
    }

    @Test
    void optionalDateTimeParsesValidValue() {
        LocalDateTime result = ValidationUtil.optionalDateTime("04.05.2026 14:30", "Діє до", FORMATTER);

        assertEquals(LocalDateTime.of(2026, 5, 4, 14, 30), result);
    }

    @Test
    void optionalDateTimeThrowsForInvalidFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.optionalDateTime("2026-05-04", "Діє до", FORMATTER)
        );

        assertEquals("Поле \"Діє до\" має формат дд.ММ.рррр гг:хх.", exception.getMessage());
    }

    @Test
    void optionalDateReturnsNullForBlankValue() {
        assertNull(ValidationUtil.optionalDate("   ", "Придатний до", DATE_FORMATTER));
    }

    @Test
    void optionalDateParsesValidValue() {
        LocalDate result = ValidationUtil.optionalDate("11.05.2026", "Придатний до", DATE_FORMATTER);

        assertEquals(LocalDate.of(2026, 5, 11), result);
    }

    @Test
    void optionalDateThrowsForInvalidFormat() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> ValidationUtil.optionalDate("2026-05-11", "Придатний до", DATE_FORMATTER)
        );

        assertEquals("Поле \"Придатний до\" має формат дд.ММ.рррр.", exception.getMessage());
    }
}
