# flaky-test-order-demo

[![check](https://github.com/sproshchaev/flaky-test-order-demo/actions/workflows/check.yml/badge.svg)](https://github.com/sproshchaev/flaky-test-order-demo/actions/workflows/check.yml)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

Учебный пример к статье на Хабре
«[Тест падает раз в 30 прогонов, и только в CI. Где искать причину?](https://habr.com/ru/companies/otus/articles/1088888/)».

Тест скидки на первый заказ `FirstOrderDiscountIT` зелёный при обычном запуске и падает
примерно в одном прогоне из тридцати, когда классы идут в случайном порядке, как в профиле `ci`.
Виноват не он, а сосед `LoyaltyPointsIT`: тот оставляет в общей базе заказ того же клиента
и не чистит его за собой.

## Быстрый старт

Нужны JDK 21 или новее и Maven.

```bash
git clone https://github.com/sproshchaev/flaky-test-order-demo.git
cd flaky-test-order-demo
mvn test      # порядок по умолчанию: всё зелёное
./run.sh      # 300 прогонов со случайным порядком, около 8 минут
```

`run.sh` прогоняет набор с профилем `ci` для каждого сида из `seeds.txt`, считает падения,
смотрит, какой класс шёл прямо перед `FirstOrderDiscountIT`, повторяет первый упавший сид,
запускает пару классов отдельно в прямом и обратном порядке и сверяет итог с `expected.txt`.
При расхождении код выхода 1. Число в аргументе (`./run.sh 90`) запускает только первые
N сидов без сверки.

Минимальное воспроизведение, одинаковое на любой ОС, — только пара классов:

```bash
mvn test -Dtest='LoyaltyPointsIT,FirstOrderDiscountIT' -Dsurefire.runOrder=alphabetical
```

Сосед идёт первым, тест падает: `expected: <900.00> but was: <1000.00>`. С
`-Dsurefire.runOrder=reversealphabetical` порядок обратный, и тест зелёный.

## Результат

Что сверяется с `expected.txt` (от ОС не зависит):

| Проверка | Результат |
|---|---|
| Прогонов со случайным порядком | 300 |
| Тестовых классов | 30 |
| Число падений в коридоре 2…20 (99,8% исходов при частоте 1/30) | да |
| Каждое падение — сразу после `LoyaltyPointsIT` | да |
| Зелёных прогонов, где `LoyaltyPointsIT` стоял прямо перед тестом | 0 |
| Первый упавший сид повторён | 3 из 3 |
| Пара «сосед, потом тест» | падает |
| Пара «тест, потом сосед» | зелёная |
| Модель перемешивания, 100 000 порядков | 1 из 29,6 (точно 1 из 30) |

Что печатается, но не сверяется — какие именно сиды упали:

| Система | Падений из 300 | Первый упавший сид |
|---|---|---|
| macOS (APFS) | 6 | 64589922 |
| Linux (GitHub Actions, ubuntu-24.04) | 11 | 57245014 |

Сиды на двух системах не совпадают ни разу. Surefire с `runOrder=random` перемешивает
классы вызовом `Collections.shuffle(list, new Random(seed))`, но на вход берёт список
в том порядке, в каком нашёл классы на диске, а этот порядок у файловых систем разный.
Сид из журнала CI надёжно повторяет порядок там же, где его получили. На другой машине
порядок сначала сверяют по строкам `Running` в журнале, а быстрее всего запустить
подозрительную пару отдельно.

## Разбор

1. `AbstractIT` чистит таблицу заказов после каждого теста. Все классы, которые от него
   наследуются, оставляют базу пустой.
2. `LoyaltyPointsIT` от него не наследуется. Он создаёт заказ клиента `alice` и уходит,
   не убрав его.
3. `FirstOrderDiscountIT` ждёт, что у `alice` нет заказов, и проверяет скидку 10%.
   Если прямо перед ним шёл `LoyaltyPointsIT`, заказ уже есть, скидки нет:
   `expected: <900.00> but was: <1000.00>`.
4. Если между ними прошёл любой другой класс, его `@AfterEach` убрал чужой заказ,
   и тест зелёный. Поэтому падение требует, чтобы пара встала вплотную.
5. Surefire с `runOrder=random` перемешивает классы вызовом
   `Collections.shuffle(list, new Random(seed))`. Вероятность, что один класс из 30 окажется
   прямо перед другим, равна 1/30. На 300 прогонах это в среднем 10 падений с широким
   разбросом (вышло 6 и 11), а точную частоту показывает модель в `tools/OrderOdds.java`.

Исправление: `LoyaltyPointsIT` наследует `AbstractIT` или чистит данные в своём `@AfterEach`.

## Границы применимости

- Spring здесь нет. Кэшированный контекст Spring TestContext с одной H2 на все классы заменён
  статическим полем в `SharedContext`, эффект для порядка тестов тот же.
- В статье `LoyaltyPointsIT` помечен `@Transactional` и запускается с `RANDOM_PORT`: откат
  транзакции теста не трогает данные, которые сервер записал в своём потоке. Этот механизм
  описан в документации Spring Boot, а здесь он смоделирован прямо: заказ записан и не убран.
  Сниппеты Spring в статье — иллюстрация, в этом репозитории они не запускаются.
- Порядок по сиду зависит от того, в каком порядке Surefire нашёл классы на диске, поэтому
  список упавших сидов в эталон не входит. Частота и связь с соседом от этого не меняются.

## Структура

```text
pom.xml                         профиль ci с runOrder=random
run.sh                          300 прогонов, пара отдельно, сверка с expected.txt
seeds.txt                       300 фиксированных сидов
expected.txt                    эталонный итог
tools/OrderOdds.java            модель перемешивания, частота 1/30
src/main/java/com/shop/order/   правило скидки и таблица заказов в памяти
src/test/java/com/shop/
  AbstractIT.java               базовый класс с очисткой после теста
  SharedContext.java            общая база на весь прогон
  order/FirstOrderDiscountIT    тест, который падает
  loyalty/LoyaltyPointsIT       сосед, который оставляет заказ
  catalog, cart, payment, ...   28 соседей, которые за собой убирают
```

## Источники

- Maven Surefire: параметры `runOrder` и `runOrder.random.seed` —
  https://maven.apache.org/surefire/maven-surefire-plugin/test-mojo.html
- Spring Boot: тесты с реальным сервером и `@Transactional` —
  https://docs.spring.io/spring-boot/reference/testing/spring-boot-applications.html
- Spring Framework: кэширование контекста —
  https://docs.spring.io/spring-framework/reference/testing/testcontext-framework/ctx-management/caching.html

## Лицензия

MIT, см. [LICENSE](LICENSE).
