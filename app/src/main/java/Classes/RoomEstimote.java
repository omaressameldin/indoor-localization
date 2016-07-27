package Classes;

import java.util.ArrayList;

/**
 * Created by oessa_000 on 7/25/2016.
 */
public class RoomEstimote {
    private Coordinate location;
    private int baseRSSI;
    private String beaconID;
    private double standardDeviation;
    private double[] RSSIs;
    private int  RSSIArrayIndex;
    private double previousRSSI;

    public RoomEstimote( String beaconID, int baseRSSI, Coordinate location){
        this.location = location;
        this.baseRSSI = baseRSSI;
        this.beaconID = beaconID;
        standardDeviation = 0;
        RSSIs = new double[2];
        RSSIArrayIndex = -1;
        previousRSSI = 0;
    }

    public double getRSSIFilteredValue(){

            return previousRSSI;
    }

    public boolean addRSSIFilteredValue(int RSSI){
//        boolean isFirstRSSIValue = (previousRSSI == 0);
//        previousRSSI = (isFirstRSSIValue) ? RSSI :
//                0.95 * previousRSSI + 0.05 * RSSI;
//        return isFirstRSSIValue;

        previousRSSI = RSSI;
        return true;
    }

    public void calculateStandardDeviation(){
        /* calculate Average of RSSI values */
        double avg = 0;
        for(int i = 0; i < RSSIs.length; i++){
            avg += RSSIs[i];
        }
        avg = avg/RSSIs.length ;
        /* calculate standard deviation of RSSI values */
        for(int i = 0; i<RSSIs.length; i++){
            standardDeviation += Math.pow((RSSIs[i] - avg), 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / RSSIs.length) ;
    }

    public Coordinate getLocation(){
        return location;
    }

    public int getBaseRSSI(){
        return baseRSSI;
    }

    public String getBeaconID(){
        return beaconID;
    }
    public boolean equals(String beaconID){
        return this.beaconID.equals(beaconID);
    }

    public double getApproximateDistance(){
        if( Math.abs(baseRSSI - previousRSSI) < 5 )
            return 0.0;
        else if( Math.abs(baseRSSI - previousRSSI) < 13)
            return 0.5;
        else if( Math.abs(baseRSSI - previousRSSI) < 25)
            return 1.0;
        else if( Math.abs(baseRSSI - previousRSSI) < 30)
            return 1.5;
        else
            return 3.0;
    }

}
