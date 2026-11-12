# Практическая работа 12

Stream API, Comparator, Optional и функциональная обработка коллекций (аналитика по продажам).
Вариант 3 (26 mod 8 + 1): дополнительный агрегат — комиссия менеджера 20 %.

## Структура

- `src/ru/edu/pr09/` — исходный код (`Sale`, `SalesAnalyticsService`, `SaleCsvLoader`, `SelfCheck`)
- `data/sample.csv` — открытый набор данных (`id;manager;region;amount`)
- `diagrams/` — Activity-диаграмма и диаграмма классов (`.dot`, `.png`)
- `DECISIONS.md` — два принципиальных решения, вариант, ответы
- `REPORT.docx` — отчёт

## Сборка и запуск

```bash
./run_selfcheck.sh      # Windows: run_selfcheck.bat
```

Ручная сборка:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr09.SelfCheck
```
