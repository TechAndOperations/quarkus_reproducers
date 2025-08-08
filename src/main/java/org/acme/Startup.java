package org.acme;

import io.quarkus.logging.Log;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.List;

@ApplicationScoped
public class Startup {

    @ConfigProperty(name = "foo", defaultValue = "xx")
    String foo;

    void onStart(@Observes StartupEvent ev) {
        getResource("application.yaml");
        getResource("application-it.yaml");
        getResource("application-toto.yaml");
        Log.info("foo=" + foo);
        Log.info("quarkus.native.resources.includes=" + ConfigProvider.getConfig().getOptionalValue("quarkus.native.resources.includes", String.class));
    }

    private void getResource(String name) {
        Log.info(name+" => " + Thread.currentThread().getContextClassLoader().getResource(name));
    }
}
