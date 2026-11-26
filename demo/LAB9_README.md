Лабораторна робота №9. Взаємодія сервісів.

Сервіс repair-orders-service запитує в repair-estimates-service дані каталогу ремонтних послуг. Доступні коди brakes, oil і diagnostics. Дані демонстраційні, а не ціни з реальної бази.

У сервісі замовлень є три дії: отримати вартість запчастин, вартість роботи та тривалість ремонту. Для кожної дії є окремий інтерфейс: PartsPriceLookup, LaborPriceLookup і RepairDurationLookup. Контролер звертається до цих інтерфейсів, а не до HTTP або gRPC напряму.

Сервіс оцінок надає однаковий каталог через REST GET /api/repair-catalog/{code} і gRPC метод GetRepairInfo. Контракт gRPC знаходиться у repair-contract/src/main/proto/repair_catalog.proto. Java-класи генеруються Maven під час збірки в target, їх не потрібно комітити.

У сервісі замовлень властивість repair.catalog.transport обирає http або grpc. Невідоме значення зупиняє запуск. HTTP-клієнт описаний декларативно через HttpExchange. Для з'єднання та читання задано окремі тайм-аути, а для кожного виклику gRPC задано deadline. Адреси й тайм-аути можна змінити в application.yml або через змінні середовища.

gRPC-сервер за замовчуванням вимкнений. Увімкнути його можна змінною REPAIR_GRPC_ENABLED=true. Стандартний порт 9090. У Docker Compose використовується ім'я repair-estimates-service, а для локального запуску — localhost.

Відсутній код послуги повертає 404, недоступність сервісу — 503. Для gRPC сервер повертає NOT_FOUND. Обидва клієнтські адаптери перетворюють помилки транспорту на власні винятки.

Для складання з папки demo запустити .\mvnw.cmd verify. Для запуску Docker виконати docker compose up --build. Для вибору gRPC перед запуском Compose встановити в PowerShell $env:REPAIR_CATALOG_TRANSPORT = "grpc". За замовчуванням використовується HTTP.

Для перевірки сервісу оцінок відкрити http://localhost:8083/api/repair-catalog/brakes. У сервісі замовлень доступні GET /api/repair-services/brakes/parts-price, GET /api/repair-services/brakes/labor-price і GET /api/repair-services/brakes/duration на порту 8081. Вони захищені наявною авторизацією Keycloak і вимагають роль MANAGER або ADMIN.
