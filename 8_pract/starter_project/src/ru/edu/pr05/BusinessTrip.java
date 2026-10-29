package ru.edu.pr05;

import java.time.LocalDate;

public class BusinessTrip {
    private final String id;
    private final LocalDate start;
    private final LocalDate end;
    private TripStatus status = TripStatus.DRAFT;
    private String rejectionReason;

    // TODO 1: R1, R2
    public BusinessTrip(String id, LocalDate start, LocalDate end) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id is null or blank");
        }
        if (start == null || end == null) {
            throw new IllegalArgumentException("start and end must not be null");
        }
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("start must be <= end");
        }
        this.id = id;
        this.start = start;
        this.end = end;
    }

    // TODO 2: R3
    public void submit() {
        if (status != TripStatus.DRAFT) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → SUBMITTED недопустим");
        }
        status = TripStatus.SUBMITTED;
    }

    // TODO 3: R5
    public void approve() {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → APPROVED недопустим");
        }
        status = TripStatus.APPROVED;
    }

    // TODO 4: R5, R6
    public void reject(String reason) {
        if (status != TripStatus.SUBMITTED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → REJECTED недопустим");
        }
        if (reason == null || reason.length() < 10) {
            throw new IllegalArgumentException("rejectionReason must be at least 10 characters, got "
                    + (reason == null ? 0 : reason.length()));
        }
        this.rejectionReason = reason;
        status = TripStatus.REJECTED;
    }

    // TODO 5: R7
    public void cancel() {
        if (status == TripStatus.REJECTED
                || status == TripStatus.COMPLETED
                || status == TripStatus.CANCELLED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → CANCELLED недопустим");
        }
        status = TripStatus.CANCELLED;
    }

    // TODO 6: R8, R9
    public void complete(LocalDate today) {
        if (status != TripStatus.APPROVED) {
            throw new IllegalStateException("Ошибка перехода: " + status + " → COMPLETED недопустим");
        }
        if (today == null) {
            throw new IllegalArgumentException("today must not be null");
        }
        if (today.isBefore(end)) {
            throw new IllegalStateException("complete возможен только после " + end);
        }
        status = TripStatus.COMPLETED;
    }

    // ВАРИАНТ 26, Часть Б: новый доменный метод
    public boolean isActive() {
        return status == TripStatus.SUBMITTED || status == TripStatus.APPROVED;
    }

    // Геттеры
    public String getId() { return id; }
    public LocalDate getStart() { return start; }
    public LocalDate getEnd() { return end; }
    public TripStatus getStatus() { return status; }
    public String getRejectionReason() { return rejectionReason; }
}