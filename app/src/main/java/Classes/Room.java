package Classes;

import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class Room {
    private RoomEstimote[] roomEstimotes;
    private double[] rssis;
    private float scaleFactor ;
    private float xTranslation;
    private float yTranslation;
    private Coordinate minValueOfCoordinates;
    private Coordinate maxValueOfCoordinates;
    private int roomID;

    public Room(ArrayList<RoomEstimote> coordinates, int roomID){
        this.roomID = roomID;
        this.roomEstimotes = new RoomEstimote[coordinates.size()];
        for(int i = 0; i< roomEstimotes.length; i++){
           this.roomEstimotes[i] = coordinates.get(i);
        }
        setMaxValueOfCoordinates();
        setMinValueOfCoordinates();
        rssis = new double[coordinates.size()];
        Arrays.fill(rssis,99);
    }

    public float getlongestDistance(){
        float longestDistance = 0;
        for(int i= 0; i< roomEstimotes.length; i++){
            Coordinate startPoint = (Coordinate)roomEstimotes[i].getLocation();
            for(int j = i; j < roomEstimotes.length; j++ ){
                Coordinate endPoint = (Coordinate)roomEstimotes[j].getLocation();
                if(!(endPoint.getFirst() == startPoint.getFirst() || endPoint.getSecond() ==startPoint.getSecond()) )
                    continue ;
                float testDistance= (float)Math.sqrt(Math.pow( ((float) startPoint.getFirst() - (float) endPoint.getFirst() ),2) +
                        Math.pow( ((float) startPoint.getSecond() - (float) endPoint.getSecond() ),2));
                longestDistance = (testDistance > longestDistance)? testDistance : longestDistance ;
            }

        }
        return longestDistance ;
    }

    public void setScaleFactor(Coordinate pixelDimensions){
        int scaleDimension = Math.min((int) pixelDimensions.getFirst(), (int) pixelDimensions.getSecond());
        scaleFactor = (roomEstimotes.length > 1 )?    scaleDimension/getlongestDistance() -1 : 0;
    }

    private void setMinValueOfCoordinates(){
        minValueOfCoordinates = new Coordinate(99999,99999);
        for(int i = 0; i< roomEstimotes.length; i++){
            if(((Coordinate)roomEstimotes[i].getLocation()).getFirst() < minValueOfCoordinates.getFirst())
                minValueOfCoordinates.setFirst(((Coordinate) roomEstimotes[i].getLocation()).getFirst());
            if (((Coordinate)roomEstimotes[i].getLocation()).getSecond() < minValueOfCoordinates.getSecond())
                minValueOfCoordinates.setSecond(((Coordinate) roomEstimotes[i].getLocation()).getSecond());
        }

    }
    private void setMaxValueOfCoordinates(){
         maxValueOfCoordinates = new Coordinate(0,0);
        for(int i = 0; i< roomEstimotes.length; i++){
            if(((Coordinate)roomEstimotes[i].getLocation()).getFirst() > maxValueOfCoordinates.getFirst())
                maxValueOfCoordinates.setFirst(((Coordinate) roomEstimotes[i].getLocation()).getFirst());
            if (((Coordinate)roomEstimotes[i].getLocation()).getSecond() > maxValueOfCoordinates.getSecond())
                maxValueOfCoordinates.setSecond(((Coordinate) roomEstimotes[i].getLocation()).getSecond());
        };
    }

    public void addRSSI(int index, int rssi){
          roomEstimotes[index].addRSSIValue(rssi);

    }

    public double getRSSI(int index){
        return  roomEstimotes[index].getRSSIFilteredValue();
    }

    public int getRoomID(){
        return this.roomID;
    }

    public int findBeacon(String mac){
        int index = 0;
        for(RoomEstimote re: roomEstimotes){
            if(re.equals(mac)){
                return index;
            }
            index++ ;
        }
        return -1;
    }

    public double getApproximateDistance(int index){
        return roomEstimotes[index].getApproximateDistance();
    }

    public String getMacAddress(int index){
        return roomEstimotes[index].getBeaconID();
    }
    public int getBaseRSSI(int index){
        return roomEstimotes[index].getBaseRSSI();
    }

    public Coordinate getLocation(int index){
        return roomEstimotes[index].getLocation();
    }

    public float getXTranslation(){
        return xTranslation;
    }

    public float getYTranslation(){
        return yTranslation;
    }

    public void setXTranslation(float xt){
        xTranslation = xt;
    }

    public void setYTranslation(float yt){
        yTranslation = yt;
    }

    public Coordinate getMinValueOfCoordinates(){return minValueOfCoordinates;}

    public Coordinate getMaxValueOfCoordinates(){return maxValueOfCoordinates;}

    public RoomEstimote[] getCoordinates(){
        return this.roomEstimotes;
    }
    public float getScaleFactor(){
        return this.scaleFactor;
    }
}
