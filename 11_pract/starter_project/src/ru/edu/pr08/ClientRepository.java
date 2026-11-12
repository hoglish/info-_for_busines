package ru.edu.pr08;

import java.util.HashMap;
import java.util.Map;

/** Хранилище принятых клиентов по ИНН. */
public class ClientRepository {
    private final Map<String, Client> data = new HashMap<>();

    public void save(Client c) {
        data.put(c.inn(), c);
    }

    public Client findByInn(String inn) {
        return data.get(inn);
    }

    public int size() {
        return data.size();
    }
}
