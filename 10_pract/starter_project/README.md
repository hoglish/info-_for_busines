# Практическая работа 10

Интерфейсы, полиморфизм и замена условной логики в Java (система политик доставки).
Вариант 26: тип доставки LOCKER (часть А) и метод `addLockerCost` (часть Б).

## Структура

- `src/ru/edu/pr07/` — исходный код
- `data/sample.csv` — контрольные значения (`type;distanceKm;weightKg;orderAmount;expected`)
- `diagrams/` — Activity-диаграмма и диаграмма классов (`.dot`, `.png`)
- `DECISIONS.md` — описание варианта, решения, ответы на вопросы
- `REPORT.docx` — отчёт

## Сборка и запуск

```bash
./run_selfcheck.sh                 # unit-проверки + data-driven режим (Windows: run_selfcheck.bat)
./run_selfcheck.sh --interactive   # ввод: type distanceKm weightKg orderAmount, выход — exit
```

Ручная сборка:

```bash
mkdir -p out
javac -encoding UTF-8 -d out $(find src -name "*.java")
java -cp out ru.edu.pr07.SelfCheck
```
