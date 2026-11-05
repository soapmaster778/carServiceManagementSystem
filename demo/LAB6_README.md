# Лабораторна робота №6 — Quartz Scheduler

- spring.quartz.overwrite-existing-jobs=false — наявні задачі в базі не перезаписуються при старті;
- префікс таблиць — QRTZ_.

Таблиці Quartz створюються Liquibase changeset-ом 004-create-quartz-tables.yaml. 

У src/main/java/com/example/demo/config/QuartzConfiguration.java знаходяться всі JobDetail і Trigger.

Для всіх job і trigger задано назву та групу car-service. Ключ job створюється через withIdentity(...), а trigger прив'язується до job через:

.forJob(jobDetail.getKey())


## 1. StaleOrderCancellationJob

Файл: src/main/java/com/example/demo/job/StaleOrderCancellationJob.java.

Job шукає замовлення зі статусом NEW, які існують довше за задану кількість днів, і змінює їхній статус на CANCELLED.

Використовуються два cron-тригери:

- morning-cancellation-trigger — щодня о 09:00, скасовує замовлення старші за 14 днів;
- weekly-cancellation-trigger — щонеділі о 18:00, перевіряє замовлення старші за 30 днів.

Часова зона обох trigger-ів — Europe/Kyiv. Для пропущеного запуску використовується withMisfireHandlingInstructionDoNothing(): Quartz не запускає стару пропущену задачу одразу після відновлення.

За замовчуванням job має параметр daysOld = 21. Trigger передає власне значення через JobDataMap, тому значення trigger-а має вищий пріоритет.

Job є мінімалістичним: він читає daysOld з JobDataMap і викликає один метод StaleOrderCancellationService.cancelOldNewOrders(...).

## 2. RepairCleanupJob

Файл: src/main/java/com/example/demo/job/RepairCleanupJob.java.

Job видаляє замовлення зі статусом CANCELLED, які старші за 60 днів. Він запускається раз на 7 днів через repair-cleanup-trigger.

Для пропущеного запуску використовується withMisfireHandlingInstructionFireNow(): після відновлення Quartz виконає очищення одразу.

## Захист від паралельного запуску

На обох jobs є анотація:

@DisallowConcurrentExecution

Вона не дозволяє Quartz одночасно запустити два екземпляри одного job. Це захищає від повторної одночасної обробки тих самих замовлень.

## Ідемпотентність

Логіка знаходиться у service-рівні та позначена @Transactional:

- StaleOrderCancellationService змінює лише NEW на CANCELLED; після першого запуску замовлення вже не NEW, тому другий запуск його не змінить;
- RepairCleanupService видаляє лише наявні старі скасовані замовлення; після видалення повторний запуск не має що видаляти.

## Обробка помилок



Якщо service кидає виняток, job виконує:

log.error("...", exception);
throw new JobExecutionException(exception, false);

false означає, що Quartz не виконує нескінченні миттєві повтори. Наступна спроба відбудеться лише у час, заданий trigger-ом.

## Тести

- StaleOrderCancellationJobTest перевіряє параметр daysOld, помилку за відсутнього параметра та відсутність нескінченного retry при помилці service;
- StaleOrderCancellationServiceTest перевіряє ідемпотентність: повторний запуск не змінює вже скасоване замовлення.
