package org.grobid.service.main;


import com.google.common.collect.Lists;
import com.google.inject.Module;
import com.hubspot.dropwizard.guicier.GuiceBundle;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.forms.MultiPartBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.apache.commons.lang3.ArrayUtils;
import org.grobid.core.utilities.GrobidProperties;
import org.grobid.service.GrobidServiceConfiguration;
import org.grobid.service.modules.GrobidServiceModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;


public final class GrobidServiceApplication extends Application<GrobidServiceConfiguration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrobidServiceApplication.class);
    private static final String DEFAULT_CONF_LOCATION = "grobid-service/config/config.yaml";
    private static final String RESOURCES = "/api";


    // ========== Application ==========

    @Override
    public String getName() {
        return "pmi-service";
    }


    @Override
    public void initialize(Bootstrap<GrobidServiceConfiguration> bootstrap) {
        GuiceBundle<GrobidServiceConfiguration> guiceBundle = GuiceBundle.defaultBuilder(GrobidServiceConfiguration.class)
                .modules(getGuiceModules())
                .build();
        bootstrap.addBundle(guiceBundle);
        bootstrap.addBundle(new MultiPartBundle());
        bootstrap.addBundle(new AssetsBundle("/web", "/", "index.html", "grobidAssets"));
;

    }

    private List<? extends Module> getGuiceModules() {
        return Lists.newArrayList(new GrobidServiceModule());
    }

    @Override
    public void run(GrobidServiceConfiguration configuration, Environment environment) {
        LOGGER.info("Service config={}", configuration);
        environment.jersey().setUrlPattern(RESOURCES + "/*");
    }

    // ========== static ==========
    public static void main(String... args) throws Exception {
        if (ArrayUtils.getLength(args) < 2) {
            LOGGER.error("Expected 2 arguments: [0]-server, [1]-<path to config.yaml>");
            LOGGER.warn("Running with default arguments: \"server\" \"" + DEFAULT_CONF_LOCATION + "\"");
            args = new String[]{"server", DEFAULT_CONF_LOCATION};
        }

        LOGGER.info("Configuration file: {}", new File(args[1]).getAbsolutePath());
        new GrobidServiceApplication().run(args);
    }
}
