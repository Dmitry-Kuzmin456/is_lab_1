package lab.repository;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.context.Initialized;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class DbSetupStarter {

    @Inject
    private DbSetup setup;

    public void onStart(@Observes @Initialized(ApplicationScoped.class) Object event) {
        setup.run();
    }
}
