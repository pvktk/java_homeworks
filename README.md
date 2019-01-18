## Тестирование архитектур серверов

Для сборки в каталоге `server_test` выполните

```
mvn install
```

В каталоге `target` появятся `client.jar` (GUI и клиенты) и `server.jar` (сервер).

Их можно запустить например

```
java -jar server.jar
```

Результаты измерений в папке `results`.

Для файла вида `xyz.txt` описание величин находится в файле `xy_metadata.txt`.