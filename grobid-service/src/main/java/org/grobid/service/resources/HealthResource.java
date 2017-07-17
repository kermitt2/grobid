package org.grobid.service.resources;

import com.codahale.metrics.health.HealthCheck;
import org.grobid.service.GrobidServiceConfiguration;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

@Path("health")
@Singleton
@Produces("application/json;charset=UTF-8")
public class HealthResource extends HealthCheck {

    @Inject
    private GrobidServiceConfiguration configuration;

    @Inject
    public HealthResource() {
    }

    @GET
    public Response alive() {
        return Response.ok().build();
    }

    @Override
    protected Result check() throws Exception {
        return configuration.getGrobid().getGrobidHome() != null ? Result.healthy() :
                Result.unhealthy("Grobid home is null in the configuration");
    }
}
