package ru.edu.pr03;

public class ApprovalRouter {
    public static final long FINANCE_LIMIT = 500_000L;

    public ApprovalRoute route(long amount, boolean urgent, RiskLevel risk, boolean containsPersonalData) {
        if (amount < 0) {
            throw new IllegalArgumentException("amount must be >= 0, got " + amount);
        }
        if (risk == null) {
            throw new IllegalArgumentException("risk is null");
        }

        // ВАРИАНТ 26, Часть А: Запрет срочных персональных данных (Приоритет выше R1)
        if (urgent && containsPersonalData) {
            throw new IllegalArgumentException("Urgent personal data is not allowed");
        }

        // R1: Персональные данные
        if (containsPersonalData) {
            return ApprovalRoute.SECURITY_REVIEW;
        }
        // R2: Высокий риск
        if (risk == RiskLevel.HIGH) {
            return ApprovalRoute.DIRECTOR_REVIEW;
        }
        // R3: Крупная сумма
        if (amount >= FINANCE_LIMIT) {
            return ApprovalRoute.FINANCE_REVIEW;
        }
        // R4: Срочная заявка малой суммы
        if (urgent && amount < FINANCE_LIMIT) {
            return ApprovalRoute.FAST_TRACK;
        }
        // R5: Стандартный маршрут
        return ApprovalRoute.STANDARD;
    }

    // ВАРИАНТ 26, Часть Б: Метод предпроверки
    public boolean addUrgentPersonalCheck(boolean urgent, boolean containsPersonalData) {
        return !(urgent && containsPersonalData);
    }
}