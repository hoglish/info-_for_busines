package ru.edu.pr05;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BusinessTripService {
    private final List<BusinessTrip> trips = new ArrayList<>();

    // ВАРИАНТ 26, Часть А: блокировка новой заявки при наличии активной
    public BusinessTrip create(String id, LocalDate start, LocalDate end) {
        for (BusinessTrip t : trips) {
            if (t.isActive()) {
                throw new IllegalStateException("У сотрудника уже есть активная командировка");
            }
        }
        BusinessTrip trip = new BusinessTrip(id, start, end);
        trips.add(trip);
        return trip;
    }

    // ВАРИАНТ 26, Часть Б: количество активных командировок
    public int addActiveTripsCount() {
        int count = 0;
        for (BusinessTrip t : trips) {
            if (t.isActive()) {
                count++;
            }
        }
        return count;
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        List<BusinessTrip> result = new ArrayList<>();
        for (BusinessTrip t : trips) {
            if (t.getStatus() == status) result.add(t);
        }
        return Collections.unmodifiableList(result);
    }

    public List<BusinessTrip> findAll() {
        return Collections.unmodifiableList(trips);
    }
}