package edu.wpi.healthdataaggregator;

/**
 * Created by Adonay on 7/19/2017.
 */

/**
 *
 */
public enum SourceType {
    GOOGLEFIT,
    FITBIT,
    IHEALTH,
    JAWBONE,
    WITHINGS;

    /**
     *
     * @return
     */
    String getName() {
        switch(this){
            case GOOGLEFIT:
                return "GoogleFit";
            case FITBIT:
                return "FitBit";
            case IHEALTH:
                return "iHealth";
            case JAWBONE:
                return "JawBone";
            case WITHINGS:
                return "Withings";
            default:
                return "Unknown Source";
        }
    }

    /**
     *
     * @return
     */
    int getSourceID() {
        switch(this){
            case GOOGLEFIT:
                return 1;
            case FITBIT:
                return 2;
            case IHEALTH:
                return 3;
            case JAWBONE:
                return 4;
            case WITHINGS:
                return 5;
            default:
                return 0;
        }
    }

    /**
     *
     * @return
     */
    String getMessage(){
        switch(this){
            case GOOGLEFIT:
                return "GoogleFit collects ........";
            case FITBIT:
                return "FitBit.....";
            case IHEALTH:
                return "iHealth information......";
            case JAWBONE:
                return "UP is the health platform for JawBone.....";
            case WITHINGS:
                return "WITHINGS is Nokia's health platform......";
            default:
                return "This thing came from nowhere";
        }
    }

    /**
     *
     * @return
     */
    String getTAG(){
        switch(this){
            case GOOGLEFIT:
                return "GoogleFitConnector";
            case FITBIT:
                return "FitBitConnector";
            case IHEALTH:
                return "IHealthConnector";
            case JAWBONE:
                return "JawboneConnector";
            case WITHINGS:
                return "WithingsConnector";
            default:
                return "UnknownClass";
        }
    }
}
