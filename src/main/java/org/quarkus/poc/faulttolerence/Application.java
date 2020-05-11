package org.quarkus.poc.faulttolerence;

/**
 * @author saravanakumarr
 *
 */
public class Application {
  

    public Integer appID;
    public String applicationName;
    public String countryOfOrigin;
    public Integer applciationMaxLoad;

    public Application() {
    }

    public Application(Integer appID, String applicationName, String countryOfOrigin, Integer applciationMaxLoad) {
        this.appID = appID;
        this.applicationName = applicationName;
        this.countryOfOrigin = countryOfOrigin;
        this.applciationMaxLoad = applciationMaxLoad;
    }

}
