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
    private Circular2DArray distances;
    private float scaleFactor ;
    private float xTranslation;
    private float yTranslation;

    public Room(ArrayList<RoomEstimote> coordinates){

        this.roomEstimotes = new RoomEstimote[coordinates.size()];
        for(int i = 0; i< roomEstimotes.length; i++){
           this.roomEstimotes[i] = coordinates.get(i);
        }
        rssis = new double[coordinates.size()];
        Arrays.fill(rssis,99);
        distances = new Circular2DArray(coordinates.size(), 15);
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

    public Coordinate getMinValueOfCoordinates(){
        Coordinate min = new Coordinate(99999,99999);
        for(int i = 0; i< roomEstimotes.length; i++){
            if(((Coordinate)roomEstimotes[i].getLocation()).getFirst() < min.getFirst())
                min.setFirst(((Coordinate) roomEstimotes[i].getLocation()).getFirst());
            if (((Coordinate)roomEstimotes[i].getLocation()).getSecond() < min.getSecond())
                min.setSecond(((Coordinate) roomEstimotes[i].getLocation()).getSecond());
        }
        return min ;
    }
    public Coordinate getMaxValueOfCoordinates(){
        Coordinate max = new Coordinate(0,0);
        for(int i = 0; i< roomEstimotes.length; i++){
            if(((Coordinate)roomEstimotes[i].getLocation()).getFirst() > max.getFirst())
                max.setFirst(((Coordinate) roomEstimotes[i].getLocation()).getFirst());
            if (((Coordinate)roomEstimotes[i].getLocation()).getSecond() > max.getSecond())
                max.setSecond(((Coordinate) roomEstimotes[i].getLocation()).getSecond());
        }
        return max ;
    }

    public boolean addRSSI(int index, int rssi){
         return roomEstimotes[index].addRSSIFilteredValue(rssi);

    }

    public double getRSSI(int index){
        return  roomEstimotes[index].getRSSIFilteredValue();

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
//        distances.setStandardDeviation(index);
//        distances.add(index, distance);
//        return distances.getLowPassFiltered(index);
//        return distances.getSmallest(index);
        return roomEstimotes[index].getApproximateDistance();
    }

    public float getXCoordinate(int index){
        return ((Coordinate)roomEstimotes[index].getLocation()).getFirst();
    }

    public float getYCoordinate(int index){
        return ((Coordinate)roomEstimotes[index].getLocation()).getSecond();
    }

    public String getMacAddress(int index){
        return roomEstimotes[index].getBeaconID();
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

    public RoomEstimote[] getCoordinates(){
        return this.roomEstimotes;
    }
    public float getScaleFactor(){
        return this.scaleFactor;
    }
    public Circular2DArray getDistances(){return distances;}
}
