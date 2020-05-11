package org.quarkus.poc.faulttolerence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ApplicationRepositoryService {

    private Map<Integer, Application> applicationResource = new HashMap<>();

    public ApplicationRepositoryService() {
      applicationResource.put(1001, new Application(1001, "Service-1", "South Africa", 80));
      applicationResource.put(1002, new Application(2, "Service-2", "Namibia", 90));
      applicationResource.put(1003, new Application(3, "Service-3", "Angola", 60));
    }

    public List<Application> getAllApplication() {
        return new ArrayList<>(applicationResource.values());
    }

    public Application getApplicationById(Integer id) {
        return applicationResource.get(id);
    }

    public List<Application> getRecommendations(Integer id) {
        if (id == null) {
            return Collections.emptyList();
        }
        return applicationResource.values().stream()
                .filter(applicationAvailable -> !id.equals(applicationAvailable.appID))
                .limit(2)
                .collect(Collectors.toList());
    }
}