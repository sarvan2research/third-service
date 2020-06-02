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
import org.eclipse.microprofile.metrics.MetricUnits;
import org.eclipse.microprofile.metrics.annotation.Counted;
import org.eclipse.microprofile.metrics.annotation.Gauge;
import org.eclipse.microprofile.metrics.annotation.Timed;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

/**
 * @author saravankumarr
 *
 */
@Path("/s3")
@Produces(MediaType.APPLICATION_JSON)
@Counted(name = "performedS3Checks", description = "How many S3 app endpoints hitted checks have been performed.")
public class ApplciationResource {

    private static final Logger LOGGER = Logger.getLogger(ApplciationResource.class);

    @Inject
    private ApplicationRepositoryService applicationRepositoryService;

    private AtomicLong counter = new AtomicLong(0);
    
    private AtomicLong failedS3Counter = new AtomicLong(0);
    
    private AtomicLong fallbackCounter = new AtomicLong(0);
    

    /**Make retry until it sucess
     * @return
     */
    @GET
    @Retry(maxRetries = 4)
    @Path("/app")
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
          fallbackCounter.getAndIncrement();
            LOGGER.error(failureLogMessage);
            throw new RuntimeException("Resource failure.");
        }
    }
    
    /**
     * Get Recommended Systems other than given
     * if not available then send default systems
     * @param id
     * @return
     */
    @GET
    @Path("/{id}/otherapp")
    @Fallback(fallbackMethod = "fallbackRecommendations")
    @Timeout(250)
    @Timed(name = "fallBackTimer", description = "A measure of how long it takes to perform recommedation.", unit = MetricUnits.MILLISECONDS)
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
    
    @Gauge(name = "S3failed", unit = MetricUnits.NONE, description = "No of times S3 application failed hit failed far")
    public AtomicLong failedS3CounterMethod() {
        return failedS3Counter;
    }
    
    @Gauge(name = "fallBackMethod", unit = MetricUnits.NONE, description = "No of times fall back method hitted called instead of actual")
    public AtomicLong fallBacks3CounterMethod() {
        return failedS3Counter;
    }
}