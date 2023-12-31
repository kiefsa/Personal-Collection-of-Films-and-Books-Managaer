# План тестирования
---

# Содержание
* [Введение](#instruction) 
* [Объект тестирования](#items)
* [Атрибуты качества](#qualite)
* [Риски](#risk)
* [Аспекты тестирования](#features)
* [Подходы к тестированию](#approach)
* [Представление результатов](#pass) 
* [Выводы](#conclusion)

<a name="instruction"/>
  
## Введение

Данный план тестирования предназначен для тестирования веб-приложения “Менеджер личной коллекции фильмов и книг”. Основная цель тестирования — проверка функциональности и производительности программного обеспечения.

<a name="items"/>

## Объект тестирования

Обьектом тестирования является веб-приложение “Менеджер личной коллекции фильмов и книг”, которое помогает пользователям организовать и управлять своей коллекцией фильмов и книг. Оно позволяет просматривать, добавлять и удалять в/из избранных записи о фильмах и книгах, а также осуществлять поиск для более удобного использования.
 
<a name="quality"/>

## Атрибуты качества

1. Функциональность:
    - приложение должно выполнять все заявленные функции и делать это правильно, без ошибок, замедлений и т. д.;
    - приложение не должно иметь расширенной функциональности, не заявленной в SRS (во избежание сложности использования).



 2. Производительность:
    - приложение должно работать на всех устройствах, имеющих браузер и имеющих доступ к Интернету;
  	- приложение должно корректно отображаться на ПК.
  	- приложение должно обеспечивать быстрый доступ к данным, даже при больших объемах информации.


3. Удобство использования:
    - приложение должно быть простым и удобным в использовании;
  	- приложение должно выглядеть простым и привлекательным для пользователя.

<a name="risk"/>

## Риски

Приложение не имеет совершенной системы безопасности, поэтому, если у пользователя есть возможность легко взломать идентификационные данные, злоумышленник может получить доступ к личным данным.

<a name="features"/>

## Аспекты тестирования

Для тестирования этого приложения существуют основные этапы функционального тестирования:
*	Открытие главной страницы
*	Просмотр рекомендованных фильмов/книг
*	Добавление выбранных фильмов/книг в избранное
*	Удаление выбранных фильмов/книг из избранного
*	Выход из приложения
 
### Открытие главной страницы
Этот аспект следует протестировать на:
    - открытие главной страницы по ссылке;
    - корректное отображение данных после загрузки (все веб-элементы на своих местах).

### Просмотр рекомендованных фильмов/книг
Этот аспект следует протестировать на:
   - корректное отображение данных после показа рекомендованных фильмов/книг.
   - открытие любой ссылки для просмотра, которую хочет пользователь, с помощью кнопок страницы.
 
### Добавление выбранных фильмов/книг в избранное
Этот аспект следует протестировать на:
    - корректную работу кнопки ”Добавить в избранное”
    - корректное отображение данных после добавление выбранных фильмов/книг в избранное.

### Удаление выбранных фильмов/книг из избранного
Этот аспект следует протестировать на:
   - корректную работу кнопки ”Удалить из избранного”
   - корректное удаление данных после нажатия кнопки ”Удалить из избранного”

### Выход из приложения
Этот аспект следует протестировать на:
   - успешный выход из системы после нажатия кнопки выхода.
   - корректно отображаются страницы после выхода из системы (без признаков пользователя).

<a name="approach"/>

## Подходы к тестированию
Подходы к тестированию:
*	Совместимость с браузерами
*	Нагрузочное тестирование
*	Системное тестирование
*	Интеграционное тестирование

### Совместимость с браузерами
Описание: Гарантирование работоспособности приложения в различных веб-браузерах.
Средства тестирования: Платформы для кроссбраузерного тестирования.

### Нагрузочное тестирование
Описание: Оценка производительности веб-приложения под нагрузкой для выявления узких мест и оптимизации.
Средства тестирования: Использование инструментов для создания нагрузки.

### Системное тестирование
Описание: Проверка функциональности веб-приложения в целом
Средства тестирования: Автоматизированные тесты, фреймворки для системного тестирования.

### Интеграционное тестирование
Описание: Проверка взаимодействия между различными модулями или компонентами веб-приложения.
Средства тестирования: Использование интеграционных тестовых фреймворков.

<a name="pass"/>

## Представление результатов
Результаты представлены  в документе "Результаты тестирования"

<a name="conclusion"/>

## Выводы
 Тестирование было проведено согласно плану. По результатам тестирования можно сделать следующие выводы:
- приложение выполняет все заявленные функции и делать это правильно, без ошибок, замедлений и т. д.;
- приложение работает на всех устройствах, имеющих браузер и имеющих доступ к Интернету
- приложение  удобно  в использовании,  не имеет расширенной функциональности, не заявленной в SRS.
 
