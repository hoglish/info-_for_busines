package ru.edu.pr02;

public class TimesheetAnalyzer {

    public static int[] parseHours(String line) {
        if (line == null) {
            throw new IllegalArgumentException("Ошибка: структура -- null input");
        }
        String[] parts = line.split(";", -1);
        if (parts.length != 6) {
            throw new IllegalArgumentException("Ошибка: структура -- ожидалось 6 значений, получено " + parts.length);
        }
        int[] result = new int[5];
        for (int i = 0; i < 5; i++) {
            String val = parts[i];
            if (!val.matches("^(0|[1-9][0-9]*)$")) {
                throw new IllegalArgumentException("Ошибка: позиция " + (i + 1) + " -- неверный формат значения '" + val + "'");
            }
            int number = Integer.parseInt(val);
            if (number < 0 || number > 16) {
                throw new IllegalArgumentException("Ошибка: позиция " + (i + 1) + " -- значение вне диапазона [0, 16]");
            }
            result[i] = number;
        }
        String rateStr = parts[5];
        try {
            Double.parseDouble(rateStr);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Ошибка: позиция 6 -- неверный формат ставки '" + rateStr + "'");
        }
        return result;
    }

    public static int total(int[] hours) {
        if (hours == null || hours.length != 5) {
            throw new IllegalArgumentException("Ошибка: некорректный массив часов");
        }
        int sum = 0;
        for (int h : hours) {
            sum += h;
        }
        return sum;
    }

    public static double average(int[] hours) {
        if (hours == null || hours.length != 5) {
            throw new IllegalArgumentException("Ошибка: некорректный массив часов");
        }
        return Math.round(total(hours) * 100.0 / hours.length) / 100.0;
    }

    public static int overtimeDays(int[] hours) {
        if (hours == null || hours.length != 5) {
            throw new IllegalArgumentException("Ошибка: некорректный массив часов");
        }
        int count = 0;
        for (int h : hours) {
            if (h > 8) {
                count++;
            }
        }
        return count;
    }

    public static int weeklyOvertime(int[] hours) {
        if (hours == null || hours.length != 5) {
            throw new IllegalArgumentException("Ошибка: некорректный массив часов");
        }
        int sum = total(hours);
        return Math.max(0, sum - 40);
    }

    public static double addPayForWeek(int[] hours, double rate) {
        if (hours == null || hours.length != 5) {
            throw new IllegalArgumentException("Ошибка: некорректный массив часов");
        }
        int totalHours = total(hours);
        int overtime = weeklyOvertime(hours);
        return (totalHours * rate) + (overtime * rate * 1.5);
    }
}