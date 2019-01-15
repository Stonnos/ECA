ECA v6.4
========================================

Описание
----------------------------------------
Программная система ECA предназначена для классификации разнотипных данных (количественных, порядковых, номинальных)
из любой предметной области на основе ансамбля алгоритмов.

Необходимый софт
----------------------------------------
* jdk 1.8
* maven => 3.3.9

Описание ключевой конфигурации модуля
----------------------------------------
Программная система состоит из трех модулей:

1) eca-core - Является основным модулем системы (ядром). В нем реализованы все алгоритмы классификации,
   загрузка/сохрание исходных данных из файлов различных форматов и т.д.
2) eca-client - Реализует клиент для интеграции с сервисом Eca - service.
3) eca-gui - В данном модуле реализован графический интерфейс системы с помощью библиотеки swing.

Основные настройки для проекта находятся в файле application-config.json. Основные параметры:

1) production - вкл./выкл. режим production. Если флаг равен false, то сохранение в файл настроек интеграции с
   сервисом Eca - service становится недоступным.
2) projectInfo - данные о проекте
3) fractionDigits - дефолтное число знаков после запятой для чисел
4) minFractionDigits - минимальное число знаков после запятой для чисел
5) maxFractionDigits - максимальное число знаков после запятой для чисел
6) maxDataListSize - максимальное число листов с данными, которые могут быть загружены в программу
7) seed - параметр seed для генератора сдучайных чисел
8) icons - настройки путей к иконкам приложения
9) logotypeUrl - путь к логотипу приложения
10) tooltipDismissTime - интервал в мс. для всплывающих подсказок
11) maxThreads - максимальное число потоков (используется для параллельных алгоритмов классификации)
12) aucThresholdValue - значение границы для значения AUC
13) dateFormat - формат даты для атрибутов типа "Дата и время"
14) crossValidationConfig - настройки метода оценки точности классификатора
    * numFolds - число блоков
    * numTests - число тестов
15) experimentConfig - настройки эксперимента
    * numBestResults - число наилучших конфигураций классификаторов
    
Настройки для интеграции с сервисом Eca - service находятся в файле eca-service-config.json. Ниже приведены
основные параметры:

1) enabled - вкл./выкл. использования сервиса. Если флаг равен true, то построение всех моделей классификаторов
    будет осуществляться с помощью Eca - service.
2) evaluationUrl - url для построения модели классификатора
3) experimentUrl - url для отправки заявки на эксперимент
4) optimalClassifierUrl - url для подбора оптимальных параметров классификатора на основе базы знаний
    о построенных моделях классификаторов на данной обучающей выборке
    
Настройки параметров подключения к БД находятся в файле db-config.json. Ниже приведены основные параметры:

* driver - путь к драйверу дял конкретной СУБД
* host - хост, на котором развернута БД
* port - порт
* dataBaseName - имя БД
* login - логин пользователя БД
* password - пароль пользователя БД

Инструкция по развертыванию
----------------------------------------

1. Необходимо собрать проект с помощью команды:
    
   mvn clean install -Pprofile
   
   Где profile может принимать одно из значений:
   
   * dev - режим разработки
   * prod - режим production
   
2. Запустить приложение с помощью exe-файла ECA.exe, расположенного в папке /eca-gui/target.
   