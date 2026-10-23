# Лабораторна робота №4: AOP

## Мета та структура проєкту

Система керування автосервісом використовує Spring Boot 4.1.1, Spring MVC,
Thymeleaf, Spring Data JPA, H2, Liquibase та Spring Security з Keycloak.
`RepairOrderRestController` і `RepairOrderWebController` передають операції
в `RepairOrderService`, який працює з `RepairOrderRepository` та MapStruct-мапером.

Наскрізна поведінка винесена у три окремі пари «власна анотація + аспект».
Усі анотації мають `@Retention(RetentionPolicy.RUNTIME)` і
`@Target(ElementType.METHOD)`. Кожен аспект є Spring-компонентом з `@Aspect`,
а його pointcut використовує `@annotation(...)`. Тип advice в усіх трьох — `@Around`:
він дозволяє змінити аргументи, повторити виконання або взагалі не викликати метод.

## Реалізована поведінка

| Анотація | Аспект | Робочі методи | Спостережуваний результат |
| --- | --- | --- | --- |
| `@NormalizeLicensePlate` | `NormalizeLicensePlateAspect` | `create`, `update` | `" aa 1234 bb "` перетворюється на `"AA1234BB"` до перевірки дубліката та запису |
| `@RetryTransientRead` | `RetryTransientReadAspect` | `findAll`, `findById` | Після `TransientDataAccessException` читання повторюється, у лог потрапляє номер наступної спроби |
| `@WorkshopWritable` | `WorkshopWritableAspect` | `create`, `update`, `delete` | У режимі обслуговування кидається `WorkshopReadOnlyException`; HTTP-відповідь має статус 503 |

### Нормалізація номерного знака

Аспект знаходить аргументи типу `RepairOrderDto`, створює новий DTO та передає
змінений масив аргументів у `proceed(arguments)`. Видаляються пробільні символи
Unicode; регістр змінюється через `Locale.ROOT`. Інші поля та початковий DTO
залишаються без змін. `null` передається без перетворення; перевірка обов'язкових
полів залишається відповідальністю Bean Validation.

Таким чином, номер з пробілами не обходить перевірку дубліката канонічного номера.
Дефіси та алфавіт символів аспект не змінює.

### Повторення тимчасово невдалого читання

`maxAttempts` задає загальну кількість спроб разом із початковою; типове значення — 3.
Повторюються лише винятки родини `TransientDataAccessException`. Помилка
«замовлення не знайдено», помилки цілісності даних та інші винятки одразу
передаються викликачеві. Після вичерпання ліміту передається останній виняток.
Значення ліміту менше 1 відхиляється до `proceed()`.

Повторення негайні, без очікування між спробами; кількість звернень обмежена.
Анотація застосована тільки до ідемпотентного читання. Запис повторно не виконується.

Приклад запису в лог:

```text
Retrying transient database read: method=RepairOrderService.findById(..), nextAttempt=2, maxAttempts=3
```

### Режим технічного обслуговування

Параметр `workshop.maintenance.read-only` за замовчуванням має значення `false`.
Значення `true` забороняє зміни замовлень до звернення до репозиторію.
Читання залишається доступним. `ApiExceptionHandler` перетворює виняток на
`ProblemDetail` зі статусом `503 Service Unavailable` і поясненням причини.
Режим визначається конфігурацією під час запуску застосунку.

Це операційний режим на час обслуговування БД. Перевірки ролей Keycloak
виконуються окремо наявним Spring Security.

### Порядок аспектів і транзакцій

Менше значення `@Order` означає зовнішній interceptor, який входить першим:

```text
@PreAuthorize (200)
  → WorkshopWritableAspect (300)
    → NormalizeLicensePlateAspect (400)
      → RetryTransientReadAspect (500)
        → @Transactional (LOWEST_PRECEDENCE)
          → метод сервісу
```

До конкретного методу застосовуються тільки відповідні йому анотації.
Цей порядок забезпечує перевірку ролі до maintenance-guard, нормалізацію до
перевірки дубліката і розташування retry зовні транзакції читання.
При звичайному виклику сервісу без зовнішньої транзакції кожна повторна спроба
відкриває власну транзакцію; інтеграційний тест перевіряє різні
`EntityManagerHolder` у двох спробах. Якщо викликач уже відкрив транзакцію,
стандартний `REQUIRED` приєднається до неї — це не сценарій цього тесту.

Spring Boot автоматично вмикає підтримку AOP за наявності AspectJ у classpath.
Для версії Boot у цьому проєкті додано `spring-boot-starter-aspectj`.
Використовується Spring AOP з проксі, без compile-time/load-time weaving.

## Навмисна демонстрація self-invocation

Той самий метод `RepairOrderService.findById` викликається двома шляхами:

```java
service.findById(1L);                 // Зовнішній виклик через Spring-проксі: advice працює.
service.findByIds(List.of(1L));       // Усередині сервісу виконується this.findById(id).
```

Реалізація другого шляху:

```java
public List<RepairOrderDto> findByIds(List<Long> ids) {
    return ids.stream().map(id -> this.findById(id)).toList();
}
```

| Умови | Зовнішній `findById` | `findByIds` → `this.findById` |
| --- | --- | --- |
| Перше читання падає з тимчасовим збоєм, друге успішне | Два звернення до репозиторію, DTO у результаті, запис про retry | Одне звернення, первинний виняток передається одразу, запису retry немає |
| Читання одразу успішне | Advice виконується, метод повертає DTO | Метод повертає DTO; advice не виконується, попередження про обхід проксі немає |

Посилання `this` вказує на цільовий об'єкт, тому внутрішній виклик не проходить
через Spring-проксі. Це свідомо залишена демонстрація за умовою лабораторної.
`findByIds` не має власної retry-анотації; self-injection та `AopContext` не застосовані.
Метод доступний як метод сервісу; окремий HTTP endpoint для нього не додавався.

## Тести та відповідність вимогам

| Вимога | Перевірка |
| --- | --- |
| Три анотації та три відповідні аспекти | Класи в `annotation/` та `aspect/`; застосування на робочих методах `RepairOrderService` |
| Зв'язування через `@annotation(...)` | `RepairOrderAopIntegrationTest`: реальний контекст Boot створює AOP-проксі та виконує advice для читання, створення й оновлення |
| Помітна зміна поведінки | REST повертає канонічний номер; retry повторно звертається до репозиторію та пише лог; maintenance повертає HTTP 503 |
| Зовнішній виклик активує advice | `externalCallActivatesAnnotationAdviceAndRetriesInSeparateTransactions`: spy підтверджує виклик advice, репозиторій викликається двічі |
| Self-invocation обходить той самий advice | `selfInvocationOfSameMethodDoesNotRetry`: репозиторій викликається один раз; spy аспекту не отримує жодного виклику |
| Відсутність advice без діагностичної помилки | `successfulSelfInvocationSilentlyBypassesAdvice`: нормальний результат, немає викликів spy та записів retry |
| Прямі unit-тести advice з mock `ProceedingJoinPoint` | `NormalizeLicensePlateAspectTest`, `RetryTransientReadAspectTest`, `WorkshopWritableAspectTest`: JUnit + Mockito, без Spring-контексту та без `@SpringBootTest` |
| Перевірка реальних рішень у advice | Змінені аргументи `proceed(args)`, кількість спроб, відмова від повторення постійної помилки, відсутність `proceed()` у maintenance і за неправильного ліміту |

Інтеграційні класи окремо використовують `@SpringBootTest`, справжні аспекти,
мапер, транзакційний interceptor, MVC та Security. Репозиторій замінено Mockito-біном
для детермінованого відтворення тимчасової помилки. Контекст використовує тестову
H2 у пам'яті; запущений Keycloak не потрібний, користувач задається `@WithMockUser`.

## Команди перевірки й демонстрації

Виконувати з каталогу `demo`, з JDK 17 або новішим і Maven:

```sh
# Усі тести, JAR, звіт JaCoCo і перевірка мінімального покриття 60%.
mvn verify

# Тільки прямі модульні тести трьох advice, без запуску Spring.
mvn '-Dtest=NormalizeLicensePlateAspectTest,RetryTransientReadAspectTest,WorkshopWritableAspectTest' test

# Порівняння зовнішнього виклику й самовиклику та нормалізація через REST.
mvn -Dtest=RepairOrderAopIntegrationTest test

# HTTP 503 для всіх змін, доступне читання та збереження перевірок ролей.
mvn -Dtest=WorkshopMaintenanceIntegrationTest test
```

Без встановленого Maven можна використати wrapper: `bash mvnw verify` на macOS/Linux
або `.\mvnw.cmd verify` у PowerShell. Для першого запуску потрібен доступ до Maven Central.

Для ручної перевірки працюючого застосунку використовуйте наявний Keycloak і JWT
з роллю `MANAGER` або `ADMIN`. Звичайний запуск:

```sh
mvn spring-boot:run
```

`POST /api/repair-orders` із валідними полями й номером `" aa 1234 bb "`
поверне канонічний номер `"AA1234BB"` (якщо його ще немає в базі).
Повторний POST того самого номера поверне HTTP 409.

Щоб перевірити режим обслуговування, перезапустіть застосунок:

```sh
mvn spring-boot:run -Dspring-boot.run.arguments=--workshop.maintenance.read-only=true
```

Валідні POST/PUT/DELETE з належними ролями повернуть HTTP 503, а GET залишиться
доступним. Для детермінованої демонстрації retry та self-invocation запускайте
інтеграційний тест: він сам відтворює збій БД без пошкодження даних.

Результат повної перевірки: **55 тестів, 0 помилок, 0 пропусків**.
Додано 26 тестів: 15 прямих модульних і 11 інтеграційних.
Загальне покриття рядків — **77,93%**, покриття рядків і гілок трьох аспектів — **100%**.
Звіт покриття: `target/site/jacoco/index.html`.

## Офіційна документація

- [Spring Boot: Aspect-Oriented Programming](https://docs.spring.io/spring-boot/reference/features/aop.html)
- [Spring Framework: Proxying Mechanisms — self-invocation](https://docs.spring.io/spring-framework/reference/core/aop/proxying.html)
