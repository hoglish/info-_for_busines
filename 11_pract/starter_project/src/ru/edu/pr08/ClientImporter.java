package ru.edu.pr08;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Координатор пакетного импорта: чтение файла, разбор, проверка, сохранение, результат. */
public class ClientImporter {
    private final ClientCsvParser parser = new ClientCsvParser();
    private final ClientValidator validator = new ClientValidator();
    private final ClientRepository repository;

    public ClientImporter(ClientRepository r) {
        repository = r;
    }

    /**
     * R1: первая строка — заголовок, пропускается. R7: ошибка открытия файла — IOException и конец импорта.
     * R6: try/catch внутри цикла, ошибка одной строки не останавливает импорт.
     */
    public ImportResult importFile(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new IOException("Файл не найден: " + path);
        }
        ImportResult result = new ImportResult();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String header = reader.readLine();
            if (header == null) {
                return result;
            }
            int lineNumber = 1;
            String line;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.isBlank() || line.startsWith("#")) {
                    continue;
                }
                processLine(line, lineNumber, result);
            }
        }
        return result;
    }

    /** Обработка одной строки: разбор, проверка, дубликат ИНН (R8), сохранение или запись ошибки (R5). */
    public void processLine(String line, int lineNumber, ImportResult result) {
        try {
            ClientDraft draft = parser.parse(line, lineNumber);
            List<String> problems = validator.validate(draft);
            if (!problems.isEmpty()) {
                result.addError(new ImportError(lineNumber, String.join("; ", problems)));
                return;
            }
            if (repository.findByInn(draft.inn()) != null) {
                result.addError(new ImportError(lineNumber, "дубликат ИНН " + draft.inn()));
                return;
            }
            repository.save(new Client(draft.inn(), draft.name(), draft.email(), draft.website()));
            result.addAccepted();
        } catch (IllegalArgumentException e) {
            result.addError(new ImportError(lineNumber, e.getMessage()));
        }
    }
}
