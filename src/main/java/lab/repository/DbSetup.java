package lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.hibernate.Session;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class DbSetup {

    @PersistenceContext(unitName = "lab1")
    private EntityManager em;

    @Transactional
    public void run() {
        String script = read();
        Session session = em.unwrap(Session.class);
        for (String sql : split(script)) {
            session.doWork(connection -> {
                try (Statement statement = connection.createStatement()) {
                    statement.execute(sql);
                } catch (SQLException exception) {
                    String first = sql.lines().findFirst().orElse(sql);
                    throw new SQLException("Не выполнилось: " + first, exception);
                }
            });
        }
    }

    private String read() {
        try (InputStream input = DbSetup.class.getResourceAsStream("/db/functions.sql")) {
            if (input == null) {
                throw new IllegalStateException("Нет файла функций");
            }
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Не удалось прочитать функции", exception);
        }
    }

    static List<String> split(String script) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean dollar = false;
        for (int i = 0; i < script.length(); i++) {
            if (i + 1 < script.length() && script.charAt(i) == '$' && script.charAt(i + 1) == '$') {
                dollar = !dollar;
                current.append("$$");
                i++;
                continue;
            }
            char ch = script.charAt(i);
            if (ch == ';' && !dollar) {
                String sql = current.toString().trim();
                if (!sql.isEmpty()) {
                    parts.add(sql);
                }
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        String sql = current.toString().trim();
        if (!sql.isEmpty()) {
            parts.add(sql);
        }
        return parts;
    }
}
