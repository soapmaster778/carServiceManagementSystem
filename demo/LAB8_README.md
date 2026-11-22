Лабораторна робота №8. Message brokers.

У роботі реалізовано взаємодію сервісу ремонтних замовлень і сервісу сповіщень через RabbitMQ. Прямих HTTP-викликів між цими сервісами немає.

repair-orders-service є producer і працює на порту 8081. customer-alerts-service є consumer і працює на порту 8084. common-events містить тільки спільний контракт повідомлень без бізнес-логіки сервісів. RabbitMQ працює на порту 5672, а його management UI доступний на порту 15672.

Producer створює три події: REPAIR_ORDER_CREATED після створення нового замовлення, REPAIR_STARTED після зміни статусу на IN_PROGRESS і REPAIR_COMPLETED після зміни статусу на COMPLETED.

Кожна подія має eventId, ідентифікатор замовлення, дані клієнта, номер автомобіля та час створення. Подія надсилається тільки після успішного commit транзакції через TransactionalEventListener з фазою AFTER_COMMIT. Якщо транзакція завершується помилкою або rollback, повідомлення в RabbitMQ не надсилається.

Exchange і queue є durable, повідомлення є persistent. Для некоректних повідомлень створено dead-letter exchange і dead-letter queue. Якщо customer-alerts-service вимкнений, RabbitMQ зберігає повідомлення у queue. Після повторного запуску customer-alerts-service обробляє накопичені повідомлення.

Consumer зберігає отримане сповіщення у власну H2 базу даних. Таблиця customer_alerts створюється через Liquibase. Переглянути збережені сповіщення можна за адресою http://localhost:8084/api/customer-alerts.

Для ідемпотентності у кожного збереженого сповіщення є унікальний sourceEventId. Якщо та сама подія надходить повторно, другий запис не створюється.

Адреса RabbitMQ береться зі змінної середовища RABBITMQ_HOST. При запуску з IDE використовується localhost. У Docker Compose використовується значення rabbitmq, тобто ім'я сервісу broker у compose.yaml.

Для збірки потрібно перейти до папки demo і виконати команду .\mvnw.cmd verify. Для запуску RabbitMQ і двох сервісів у Docker потрібно виконати команду docker compose up --build. RabbitMQ management UI доступний за адресою http://localhost:15672. Логін guest, пароль guest.

Тести RabbitRepairOrderEventPublisherTest, RepairOrderServiceTest і CustomerAlertServiceTest перевіряють persistent повідомлення, публікацію після commit, відсутність події при помилці та ідемпотентну обробку повторної події.
