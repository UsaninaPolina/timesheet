Система списания занятости
==========================

Общее назначение системы
------------------------

Система списания занятости предназначена для ежедневного учета трудозатрат сотрудников организации на выполнение работ по различным проектам. Предусмотрена возможность детализации работ по проектам и отдельным задачам. Система позволяет контролировать своевременность заполнения сотрудниками отчетности с учетом организационных ролей и иерархической структуры управления организацией. Существует возможность формирования аналитических отчетов за различные периоды в разрезах подразделения, проекта или отдельного сотрудника.

Авторизация в системе реализована с помощью сервера OpenLDAP. Используемая СУБД - PostgresSQL 9.1. Рассылка уведомлений сотрудникам возможна с помощью любого внешнего smtp сервера.

Подключение к smtp серверу
--------------------------

Для функционирования системы рассылки уведомлений пользователям необходимо настроить подключение к почтовому серверу. В файле timesheet.properties для этого предназначены следующие свойства:

*	mail.send.enable=false – глобальный флаг, позволяет полностью отключить email рассылку.
*	mail.transport.protocol=smtp – протокол 
*	mail.smtp.host=smtp.yandex.ru – адрес почтового сервера
*	mail.smtp.auth=true – необходимость авторизации при отправке сообщений
*	mail.smtp.port=25 – порт почтового сервера
*	mail.username=username – имя почтового аккаунта
*	mail.password=userpassword – пароль 

Значения этих параметров могут различаться у различных почтовых хостеров. При значении параметра mail.send.enable=false подключение к smtp серверу не выполняется, поэтому настройки можно не делать.

Подключение к LDAP серверу
--------------------------

Для работы системы авторизации необходимо соединение с LDAP сервером. Для нужд разработки достаточно использования OpenLDAP. 
Скачать дистрибутив для windows: http://www.userbooster.de/en/download/openldap-for-windows.aspx, для Linux: http://www.openldap.org/software/download/. После установки автоматически запустится сервис «OpenLDAP Service». Его необходимо остановить и распаковать приложенный архив LDAP.zip в папку установки OpenLDAP. После чего, снова запустить сервер.

В файле конфигурации timesheet.properties изменить строки:

*	ldap.userDn=cn=Manager,dc=example,dc=com
*	ldap.password=secret
*	ldap.base=dc=example,dc=com
*	ldap.url=ldap://localhost:389
*	ldap.domain=example.com
*	ldap.search.pattern=(uid={0})

В LDAP прописаны следующие пользователи:

1.	Роли сотрудников: AnalystA, AnalystA2, DeveloperB1, DeveloperB2, DeveloperB3, TesterC1, TesterC2, TesterC3. Сотрудники имеют права на списание занятости и просмотр истории списаний.
2.	Роли менеджера: ManagerA, ManagerB, ManagerC. Менеджеры имеют права на списание занятости и формирование аналитических отчетов.
3.	Роль администратора: Boss. Администратор имеет права на формирование аналитических отчетов и выполнение служебных функций (синхронизация и рассылка уведомлений).
Пароли соответствуют логинам.

Подключение к базе данных
-------------------------

Все данные о списаниях занятости хранятся в базе данных.  Необходимо скачать и установить PostgresSQL 9.1. Развернуть приложенный дамп тестовой базы данных (можно использовать БД и пользователя по умолчанию).

В файле конфигурации timesheet.properties изменить строки:

*	db.username=ИМЯ-ПОЛЬЗОВАТЕЛЯ
*	db.password=ПАРОЛЬ
*	db.driver=org.postgresql.Driver
*	db.url=jdbc\:postgresql\://localhost\:5432/<наименование базы данных>
*	db.dialect=org.hibernate.dialect.PostgreSQLDialect

Запуск приложения
-----------------

Для сборки и запуска приложения необходимо установить Maven (http://maven.apache.org). Тестовое приложение запускается в контейнере Jetty.
Запуск приложения осуществляется вызовом run.bat.
