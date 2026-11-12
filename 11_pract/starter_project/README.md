# Практическая работа 11

Файлы, исключения и устойчивый пакетный импорт в Java (импорт карточек клиентов из CSV).
Вариант 26: обязательное поле `website` (часть А) и метод `addWebsiteDomain` (часть Б).

## Структура

- `src/ru/edu/pr08/` — исходный код
- `data/sample.csv` — тестовые данные (`inn;name;email;website;expected`)
- `diagrams/` — Activity-диаграмма и диаграмма классов (`.dot`, `.png`)
- `DECISIONS.md` — описание варианта, решения, ответы на вопросы
- `REPORT.docx` — отчёт

## Формат файла для импорта

`inn;name;email;website`, разделитель `;`, первая строка — заголовок, пробелы вокруг полей не допускаются,
пустые строки и строки с `#` пропускаются.

## Сборка и запуск

```bash
./run_selfcheck.sh                        # unit-проверки + data-driven режим (Windows: run_selfcheck.bat)
./run_selfcheck.sh --interactive          # ввод строки inn;name;email;website, выход — exit
./run_selfcheck.sh --import путь/к/файлу  # импорт своего файла и вывод ImportResult
```

Ручная сборка:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr08.SelfCheck
```
