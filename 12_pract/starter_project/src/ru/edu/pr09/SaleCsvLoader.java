package ru.edu.pr09;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Загрузка продаж из CSV: id;manager;region;amount. Некорректные данные -> IAE с номером строки. */
public final class SaleCsvLoader {
    private SaleCsvLoader() {}

    public static List<Sale> load(Path file) throws IOException {
        List<String> lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        return parse(lines);
    }

    static List<Sale> parse(List<String> lines) {
        List<Sale> result = new ArrayList<>();
        Set<String> ids = new HashSet<>();
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            int n = i + 1;
            if (line.isBlank() || line.startsWith("#")) {
                continue;
            }
            String[] p = line.split(";", -1);
            if (p.length != 4) {
                throw new IllegalArgumentException("строка " + n + ": ожидается 4 поля, получено " + p.length);
            }
            if (p[0].isBlank() || p[1].isBlank() || p[2].isBlank()) {
                throw new IllegalArgumentException("строка " + n + ": id, manager и region не должны быть пустыми");
            }
            double amount;
            try {
                amount = Double.parseDouble(p[3]);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("строка " + n + ": сумма не число: '" + p[3] + "'");
            }
            if (amount < 0 || Double.isNaN(amount)) {
                throw new IllegalArgumentException("строка " + n + ": сумма не может быть отрицательной");
            }
            if (!ids.add(p[0])) {
                throw new IllegalArgumentException("строка " + n + ": повторный id " + p[0]);
            }
            result.add(new Sale(p[0], p[1], p[2], amount));
        }
        return result;
    }
}
