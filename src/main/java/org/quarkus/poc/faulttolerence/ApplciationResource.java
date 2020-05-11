package org.quarkus.poc.faulttolerence;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.eclipse.microprofile.faulttolerance.Fallback;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.faulttolerance.Timeout;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

/**
 * @author saravankumarr
 *
 */
@Path("/application")
@Produces(MediaType.APPLICATION_JSON)
public class ApplciationResource {

    private static final Logger LOGGER = Logger.getLogger(ApplciationResource.class);

    @Inject
    private ApplicationRepositoryService applicationRepositoryService;

    private AtomicLong counter = new AtomicLong(0);

    /**Make retry until it sucess
     * @return
     */
    @GET
    @Retry(maxRetries = 4)
    public List<Application> applications() {
        final Long invocationNumber = counter.getAndIncrement();

        maybeFail(String.format("ApplicationResource#coffees() invocation #%d failed", invocationNumber));

        LOGGER.infof("ApplicationResource#Applications() invocation #%d returning successfully", invocationNumber);
        return applicationRepositoryService.getAllApplication();
    }

    /**
     * Failure on 50% time 
     * @param failureLogMessage
     */
    private void maybeFail(String failureLogMessage) {
        if (new Random().nextBoolean()) {
            LOGGER.error(failureLogMessage);
            throw new RuntimeException("Resource failure.");
        }
    }
    
    /**
     * Get Recommended Systems
     * @param id
     * @return
     */
    @GET
    @Path("/{id}/OtherService")
    @Fallback(fallbackMethod = "fallbackRecommendations")
    @Timeout(250)
    public List<Application> recommendations(@PathParam int id) {
        long started = System.currentTimeMillis();
        final long invocationNumber = counter.getAndIncrement();

        try {
            randomDelay();
            LOGGER.infof("ApplicationResource#recommendations() invocation #%d returning successfully", invocationNumber);
            return applicationRepositoryService.getRecommendations(id);
        } catch (InterruptedException e) {
            LOGGER.errorf("ApplicationResource#recommendations() invocation #%d timed out after %d ms",
                    invocationNumber, System.currentTimeMillis() - started);
            return null;
        }
    }
   
    /**
     * In case if its failed then belew default available service
     * invoked
     * @param id
     * @return
     */
    public List<Application> fallbackRecommendations(int id) {
      LOGGER.info("Falling back to RecommendationResource#fallbackRecommendations()");
      // safe bet, return something that everybody likes
      return Collections.singletonList(applicationRepositoryService.getApplicationById(1001));
  }
    

    /**
     * Create some randon delay
     * @throws InterruptedException
     */
    private void randomDelay() throws InterruptedException {
        Thread.sleep(new Random().nextInt(500));
    }
}