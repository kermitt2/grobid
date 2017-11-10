package org.grobid.service.module;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.DefaultConfigurationFactoryFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.setup.Environment;
import org.grobid.service.GrobidServiceConfiguration;
import org.grobid.service.modules.GrobidServiceModule;
import org.hibernate.validator.HibernateValidator;

import javax.validation.Validation;
import javax.validation.ValidatorFactory;

public class GrobidServiceModuleTest extends GrobidServiceModule {

    public static final String TEST_CONFIG_FILE = "src/test/resources/setup/config/test-config.yaml";

    public GrobidServiceModuleTest() {
        super();
    }

    @Override
    public void configure(Binder binder) {
        super.configure(binder);
    }


    @Provides
    @Singleton
    @Override
    public GrobidServiceConfiguration getConfiguration() {
        ObjectMapper objectMapper = Jackson.newObjectMapper();

        ValidatorFactory validatorFactory = Validation
                .byProvider(HibernateValidator.class)
                .configure()
//                .addValidatedValueHandler(new OptionalValidatedValueUnwrapper())
                .buildValidatorFactory();


        final ConfigurationFactory<GrobidServiceConfiguration> configurationFactory =
                new DefaultConfigurationFactoryFactory<GrobidServiceConfiguration>()
                        .create(GrobidServiceConfiguration.class,
                                validatorFactory.getValidator(), objectMapper, "dw");

        try {
            return configurationFactory.build(new FileConfigurationSourceProvider(), TEST_CONFIG_FILE);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Provides
    protected Environment getEnvironment() {
        return new Environment("test-grobid-service-env", new ObjectMapper(), null, new MetricRegistry(),
                this.getClass().getClassLoader());
    }


}