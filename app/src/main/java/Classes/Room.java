package Classes;

import android.util.Pair;

import java.util.ArrayList;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class Room {
    private ArrayList<Pair> estimotescoordinates;
    private Circular2DArray distances;
    private float scaleFactor ;
    private float xTranslation;
    private float yTranslation;
    public Room(ArrayList<Pair> coordinates){
        this.estimotescoordinates = coordinates;
        distances = new Circular2DArray(coordinates.size(), 20);
    }

    public float getlongestDistance(){
        float longestDistance = 0;
        for(int i= 0; i< estimotescoordinates.size(); i++){
            Coordinate startPoint = (Coordinate)estimotescoordinates.get(i).second;
            for(int j = i; j < estimotescoordinates.size(); j++ ){
                Coordinate endPoint = (Coordinate)estimotescoordinates.get(j).second;
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
        scaleFactor = (estimotescoordinates.size() > 1 )?    scaleDimension/getlongestDistance() -1 : 0;
    }

    public Coordinate getMinValueOfCoordinates(){
        Coordinate min = new Coordinate(99999,99999);
        for(int i = 0; i< estimotescoordinates.size(); i++){
            if(((Coordinate)estimotescoordinates.get(i).second).getFirst() < min.getFirst())
                min.setFirst(((Coordinate) estimotescoordinates.get(i).second).getFirst());
            if (((Coordinate)estimotescoordinates.get(i).second).getSecond() < min.getSecond())
                min.setSecond(((Coordinate) estimotescoordinates.get(i).second).getSecond());
        }
        return min ;
    }
    public Coordinate getMaxValueOfCoordinates(){
        Coordinate max = new Coordinate(0,0);
        for(int i = 0; i< estimotescoordinates.size(); i++){
            if(((Coordinate)estimotescoordinates.get(i).second).getFirst() > max.getFirst())
                max.setFirst(((Coordinate) estimotescoordinates.get(i).second).getFirst());
            if (((Coordinate)estimotescoordinates.get(i).second).getSecond() > max.getSecond())
                max.setSecond(((Coordinate) estimotescoordinates.get(i).second).getSecond());
        }
        return max ;
    }

    public int findBeacon(String majorMinor){
        int index = 0;
        for(Pair p: estimotescoordinates){
            if(((String)p.first).equals(majorMinor)){
                return index;
            }
            index++ ;
        }
        return -1;
    }

    public float getXCoordinate(int index){
        return ((Coordinate)estimotescoordinates.get(index).second).getFirst();
    }

    public float getYCoordinate(int index){
        return ((Coordinate)estimotescoordinates.get(index).second).getSecond();
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

    public double getApproximateDistance(int index, double distance){
        distances.setStandardDeviation(index);
        distances.add(index, distance);
        return distances.getLowPassFiltered(index);
    }

    public ArrayList<Pair> getCoordinates(){
        return this.estimotescoordinates;
    }
    public float getScaleFactor(){
        return this.scaleFactor;
    }
    public Circular2DArray getDistances(){return distances;}
}
