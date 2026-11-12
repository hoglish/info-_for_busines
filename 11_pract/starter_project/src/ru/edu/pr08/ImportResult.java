package ru.edu.pr08;

import java.util.ArrayList;
import java.util.List;

/** Результат пакетного импорта: принятые, отклонённые, ошибки с номерами строк. */
public class ImportResult {
    private int accepted;
    private final List<ImportError> errors = new ArrayList<>();

    public void addAccepted() {
        accepted++;
    }

    public void addError(ImportError e) {
        errors.add(e);
    }

    public int accepted() {
        return accepted;
    }

    /** Число отклонённых строк = число зафиксированных ошибок. */
    public int rejected() {
        return errors.size();
    }

    public List<ImportError> errors() {
        return List.copyOf(errors);
    }

    /** Ошибки в виде «строка N: причина». */
    public List<String> errorMessages() {
        List<String> result = new ArrayList<>();
        for (ImportError e : errors) {
            result.add(e.format());
        }
        return result;
    }

    @Override
    public String toString() {
        return "ImportResult[accepted=" + accepted + ", rejected=" + rejected() + "]";
    }
}
