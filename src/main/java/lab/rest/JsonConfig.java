package lab.rest;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;
import jakarta.json.bind.config.PropertyVisibilityStrategy;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.ContextResolver;
import jakarta.ws.rs.ext.Provider;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

@Provider
@Produces(MediaType.APPLICATION_JSON)
@ApplicationScoped
public class JsonConfig implements ContextResolver<Jsonb> {

    private final Jsonb jsonb = JsonbBuilder.create(new JsonbConfig().withPropertyVisibilityStrategy(new PropertyVisibilityStrategy() {
        @Override
        public boolean isVisible(Field field) {
            return !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic();
        }

        @Override
        public boolean isVisible(Method method) {
            return false;
        }
    }));

    @Override
    public Jsonb getContext(Class<?> type) {
        return jsonb;
    }
}
