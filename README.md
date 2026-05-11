# Warehouse Management System

Desktop-застосунок для складського обліку, створений на **JavaFX**, **Maven** та **PostgreSQL**.  
Проєкт реалізує базові бізнес-процеси складу: керування товарами, залишками, надходженнями та користувачами через зручний графічний інтерфейс.

---

## Overview

Цей застосунок побудований як навчальний CRUD-проєкт з розділенням на шари:

- **UI layer** — JavaFX + FXML
- **Controller layer** — логіка сторінок та обробка дій користувача
- **Service layer** — SQL-запити й робота з PostgreSQL
- **Model layer** — Java-моделі даних
- **Utility layer** — валідація, алерти, форматування таблиць

Проєкт орієнтований на локальний запуск і добре підходить як демонстрація роботи з:

- OOP у Java
- JavaFX інтерфейсом
- JDBC
- PostgreSQL
- Maven
- JUnit 5

---

## Features

- Управління товарами
  - створення, редагування, видалення
  - пошук за SKU та назвою

- Управління залишками
  - облік кількості товару по локаціях
  - підтримка дати придатності

- Облік надходжень
  - створення записів надходження
  - автоматичне оновлення залишків після надходження

- Управління користувачами
  - ролі користувачів
  - статус активності
  - базова форма адміністрування

- Додатково
  - валідація полів вводу
  - інформаційні, попереджувальні та помилкові alert-вікна
  - unit-тести для моделей і валідації
  - JavaDoc для основних класів

---

## Technologies Used

- **Java 25**
- **JavaFX 25**
- **Maven**
- **PostgreSQL**
- **JDBC**
- **JUnit 5**
- **IntelliJ IDEA**

---

## Project Modules

| Module | Description |
|---|---|
| `Products` | Робота з товарами: SKU, назва, категорія, одиниця виміру, опис |
| `Quantities` | Облік залишків товару по локаціях, кількість, дата придатності |
| `Receipts` | Надходження товару з автоматичним оновленням залишків |
| `Users` | Користувачі системи, ролі, username, активність |

---

## Project Structure

```text
warehouse-management-system
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java/com/store
│   │   │   ├── controller
│   │   │   ├── model
│   │   │   ├── service
│   │   │   ├── util
│   │   │   └── Main.java
│   │   └── resources
│   │       ├── db.properties
│   │       ├── fxml
│   │       └── styles
│   └── test
│       └── java/com/store
```

### Main Packages

- [src/main/java/com/store/controller](E:/project/warehouse-management-system/src/main/java/com/store/controller) — контролери сторінок
- [src/main/java/com/store/model](E:/project/warehouse-management-system/src/main/java/com/store/model) — моделі даних
- [src/main/java/com/store/service](E:/project/warehouse-management-system/src/main/java/com/store/service) — SQL та взаємодія з БД
- [src/main/java/com/store/util](E:/project/warehouse-management-system/src/main/java/com/store/util) — допоміжні класи
- [src/main/resources/fxml](E:/project/warehouse-management-system/src/main/resources/fxml) — FXML-розмітка інтерфейсу
- [src/main/resources/styles/style.css](E:/project/warehouse-management-system/src/main/resources/styles/style.css) — стилі застосунку

---

## Database Configuration

Налаштування підключення зберігаються у файлі:

- [src/main/resources/db.properties](E:/project/warehouse-management-system/src/main/resources/db.properties)

Приклад конфігурації:

```properties
db.url=jdbc:postgresql://localhost:5432/postgres
db.username=postgres
db.password=your_password
```

Перед запуском:

1. Переконайся, що **PostgreSQL запущений**
2. Створи потрібну базу даних і таблиці за твоєю схемою
3. Вкажи правильні значення у `db.properties`

---

## How to Run

### 1. Clone repository

```bash
git clone <your-repository-url>
cd warehouse-management-system
```

### 2. Configure database

Відредагуй [db.properties](E:/project/warehouse-management-system/src/main/resources/db.properties) під своє локальне підключення.

### 3. Run with Maven

```bash
mvn javafx:run
```

### 4. Run from IntelliJ IDEA

Можна також запускати проєкт через Maven configuration:

```text
javafx:run
```

---

## Testing

Для запуску тестів:

```bash
mvn test
```

У проєкті вже є unit-тести для:

- моделей
- `ValidationUtil`

---

## Documentation

Для генерації JavaDoc:

```bash
mvn javadoc:javadoc
```

Згенерована документація з’явиться в:

```text
target/reports/apidocs
```

---

## Current Architecture

Застосунок побудований за простою багатошаровою схемою:

```text
FXML -> Controller -> Service -> PostgreSQL
                  -> Model
                  -> Util
```

### Layer Responsibilities

- **FXML** — структура інтерфейсу
- **Controller** — дії користувача, заповнення таблиць, зв’язок із формами
- **Service** — SQL-запити та робота з JDBC
- **Model** — Java-об’єкти для таблиць бази даних
- **Util** — валідація, повідомлення, форматування

---

## Key Files

| File | Purpose |
|---|---|
| [pom.xml](E:/project/warehouse-management-system/pom.xml) | Maven-конфігурація, залежності, плагіни |
| [src/main/java/com/store/Main.java](E:/project/warehouse-management-system/src/main/java/com/store/Main.java) | Точка входу в застосунок |
| [src/main/java/com/store/controller/MainController.java](E:/project/warehouse-management-system/src/main/java/com/store/controller/MainController.java) | Головна навігація між сторінками |
| [src/main/java/com/store/service/DatabaseService.java](E:/project/warehouse-management-system/src/main/java/com/store/service/DatabaseService.java) | JDBC-підключення до PostgreSQL |
| [src/main/resources/fxml/main-view.fxml](E:/project/warehouse-management-system/src/main/resources/fxml/main-view.fxml) | Головний макет застосунку |

---

## Notes

- Проєкт використовує **локальне підключення до PostgreSQL**
- Інтерфейс побудований на **JavaFX FXML**
- Надходження пов’язані з оновленням залишків через сервісний шар
- Це desktop-застосунок, а не web-проєкт

---

## Status

На поточному етапі проєкт:

- збирається через Maven
- проходить unit-тести
- має JavaDoc
- підтримує базові CRUD-сценарії для основних модулів складської системи

---

## License

Цей проєкт створений для навчальних і демонстраційних цілей.
