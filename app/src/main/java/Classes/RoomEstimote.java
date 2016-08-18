package Classes;

import android.widget.Toast;

import java.util.ArrayList;

import static com.example.oessa_000.countsteps.MainActivity.getMainActivity;

/**
 * Created by oessa_000 on 7/25/2016.
 */
public class RoomEstimote {
    private Coordinate location;
    private int baseRSSI;
    private String beaconID;
    private double standardDeviation;
    private int  oldestRSSIIndex;
    private double currentRSSI;
    private ArrayList<Integer> allRSSIs;
    private int size = 10;
    private double avg = 0;
    private double alpha = 0.95;



    public RoomEstimote( String beaconID, int baseRSSI, Coordinate location){
        this.location = location;
        this.baseRSSI = baseRSSI;
        this.beaconID = beaconID;
        standardDeviation = 0;
        oldestRSSIIndex = 0;
        currentRSSI = 0;
        allRSSIs = new ArrayList<Integer>();
    }

    public double getRSSIFilteredValue(){

            return currentRSSI;
    }

    public void addRSSIValue(int RSSI){
//        currentRSSI = (currentRSSI != 0)? alpha*currentRSSI + (1-alpha) * RSSI: RSSI;

        /* low pass filter on new RSSI value */
        int lowPassFilteredRSSI = (currentRSSI !=  0)?(int)(alpha *currentRSSI + (1-alpha) * RSSI) : RSSI ;
        /* if no standard deviation set yet just add the value */
        if(avg == 0) {
            currentRSSI = lowPassFilteredRSSI;
            allRSSIs.add(lowPassFilteredRSSI);

            if(allRSSIs.size() == size){
                 /* set the first standard deviation after getting 10 values */
                standardDeviationFilter();
            }
        }
        /* else check if the difference is lower than the standard deviation */
        else  if(Math.abs(avg - lowPassFilteredRSSI) < standardDeviation){
            currentRSSI = lowPassFilteredRSSI;
            if(allRSSIs.size() == size)
                allRSSIs.set(oldestRSSIIndex++, lowPassFilteredRSSI);
            else
                allRSSIs.add(oldestRSSIIndex++, lowPassFilteredRSSI);
            if(oldestRSSIIndex == size )
                oldestRSSIIndex = 0;
            /* set the new standard deviation value */
            standardDeviationFilter();
        }
    }


    public void standardDeviationFilter(){
        /* calculate Average of RSSI values */
         avg = 0;
        for(int i = 0; i < allRSSIs.size(); i++){
            avg += allRSSIs.get(i);
        }
        avg = avg/allRSSIs.size() ;
        /* calculate standard deviation of RSSI values */
        standardDeviation = 0;
        for(int i = 0; i<allRSSIs.size(); i++){
            standardDeviation += Math.pow((allRSSIs.get(i) - avg), 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / allRSSIs.size()) ;
        for(int i=0 ; i< allRSSIs.size(); i++){
            if(Math.abs(allRSSIs.get(i) - avg) > standardDeviation) {
                allRSSIs.remove(i);
                i--;
            }
        }
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
        if(avg == 0)
            return -1;
        if( Math.abs(baseRSSI - currentRSSI) < 5 )
            return 0.0;
        else if( Math.abs(baseRSSI - currentRSSI) < 13)
            return 0.5;
        else if( Math.abs(baseRSSI - currentRSSI) < 25)
            return 1.0;
        else if( Math.abs(baseRSSI - currentRSSI) < 30)
            return 1.5;
        else
            return 100;
    }

}
