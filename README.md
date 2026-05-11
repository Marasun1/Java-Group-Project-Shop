# Warehouse Management System

Десктопний GUI-застосунок для складського обліку.  
Застосунок дозволяє виконувати CRUD-операції та пошук для управління товарами, залишками, надходженнями та користувачами.

##  Опис проєкту

Це десктопний GUI-застосунок для роботи з базою даних складу.  
Метою є реалізація CRUD-операцій та пошуку для управління товарами, залишками, надходженнями та користувачами.

Застосунок дозволяє:
- додавати, редагувати та видаляти записи;
- переглядати інформацію у вигляді таблиць;
- виконувати пошук за різними критеріями;
- працювати з базою даних PostgreSQL через JDBC.

---

##  Використані технології

| Технологія | Версія | Призначення |
|---|---|---|
| Java | 25 | Мова програмування |
| JavaFX | 25 | Графічний інтерфейс |
| PostgreSQL | — | СУБД |
| JDBC | — | Драйвер, взаємодія з БД |
| Maven | — | Збірка та залежності |
| JUnit | 5 | Тестування |

---

##  Архітектура застосунку

```
FXML (View) → Controller → Service → PostgreSQL
                        ↕
                      Model
                        ↕
                       Util
```

Всі SQL-запити реалізовані через JDBC.

### Модулі

- `model/` — POJO-класи (`Product`, `Quantity`, `Receipt`, `User`)
- `service/` — SQL-запити та JDBC-реалізації
- `controller/` — JavaFX-контролери, обробка подій
- `util/` — валідація, повідомлення, форматування
- `resources/` — FXML-макети + CSS

---

##  Структура проєкту

```
warehouse-management-system/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/store/
    │   │   ├── controller/
    │   │   ├── model/
    │   │   ├── service/
    │   │   ├── util/
    │   │   └── Main.java
    │   └── resources/
    │       ├── db.properties
    │       ├── fxml/
    │       └── styles/
    └── test/
        └── java/com/store/
```

---

##  Схема бази даних

```
products        quantities      receipts        users
────────        ──────────      ────────        ─────
id (PK)         id (PK)         id (PK)         id (PK)
sku             product_id (FK) product_id (FK) username
name            location        quantity        role
category        quantity        receipt_date    active
unit            expiry_date
description
```

Зв'язки:
- products 1 → N quantities
- products 1 → N receipts
- receipts → автоматично оновлюють quantities

---

##  Функціонал

| Операція | Статус |
|---|---|
| Read — перегляд таблиць | ✅ |
| Create — додавання записів | ✅ |
| Update — редагування записів | ✅ |
| Delete — видалення записів | ✅ |
| Пошук товару за SKU | ✅ |
| Пошук товару за назвою | ✅ |
| Облік залишків по локаціях | ✅ |
| Підтримка дати придатності | ✅ |
| Автоматичне оновлення залишків після надходження | ✅ |
| Управління користувачами | ✅ |
| Ролі та статус активності | ✅ |
| Валідація полів вводу | ✅ |
| Alert-вікна (info / warn / error) | ✅ |