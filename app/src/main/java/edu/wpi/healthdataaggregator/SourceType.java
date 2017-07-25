package edu.wpi.healthdataaggregator;

/**
 * Created by Adonay on 7/19/2017.
 */

public enum SourceType {
    GOOGLEFIT,
    FITBIT,
    IHEALTH;

    String getName() {
        switch(this){
            case GOOGLEFIT:
                return "GoogleFit";
            case FITBIT:
                return "FitBit";
            case IHEALTH:
                return "iHealth";
            default:
                return "Unknown Source";
        }
    }

    int getSourceID() {
        switch(this){
            case GOOGLEFIT:
                return 1;
            case FITBIT:
                return 2;
            case IHEALTH:
                return 3;
            default:
                return 0;
        }
    }

    String getMessage(){
        switch(this){
            case GOOGLEFIT:
                return "GoogleFit collects this this this";
            case FITBIT:
                return "FitBit collects ur stuff";
            case IHEALTH:
                return "iHealth make that and that that";
            default:
                return "This thing came from nowhere";
        }
    }

    String getTAG(){
        switch(this){
            case GOOGLEFIT:
                return "GoogleFitConnector";
            case FITBIT:
                return "FitBitConnector";
            case IHEALTH:
                return "IHealthConnector";
            default:
                return "UnknownClass";
        }
    }
}
