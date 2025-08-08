package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class Startup {

    void onStart(@Observes StartupEvent ev) {
        getResource("application.properties");
        getResource("application-nativeit.properties");
        getResource("application-toto.properties");
    }

    private void getResource(String name) {
        Log.info(name+" => " + Thread.currentThread().getContextClassLoader().getResource(name));
    }
}
