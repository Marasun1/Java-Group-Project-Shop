package com.store.util;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Допоміжні методи для налаштування типових колонок JavaFX-таблиць.
 */
public final class TableColumnUtil {

    private TableColumnUtil() {
    }

    /**
     * Налаштовує колонку з датою й часом для відображення значень у заданому форматі.
     *
     * @param column колонка таблиці з типом {@link LocalDateTime}
     * @param formatter форматер для відображення значення
     * @param <T> тип рядка таблиці
     */
    public static <T> void configureDateTimeColumn(
            TableColumn<T, LocalDateTime> column,
            DateTimeFormatter formatter
    ) {
        column.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.format(formatter));
            }
        });
    }
}
