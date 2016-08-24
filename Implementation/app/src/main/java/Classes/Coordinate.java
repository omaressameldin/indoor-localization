package Classes;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class Coordinate {
    /* x and y coordinates variables */
    private float first;
    private float second;

    public Coordinate(float f, float s){
        /* Initialize Coordinates */
        first = f;
        second = s;
    }

    public void setFirst(float f){
        first = f;
    }

    public void setSecond(float s){
        second = s;
    }

    public float getFirst(){
        return first;
    }

    public float getSecond(){
        return second;
    }

    public boolean equals(Coordinate c){
        /* a coordinates is equal another coordiante if and only if both x and y are equal */
        return(c.getFirst() == this.getFirst() && c.getSecond() == this.getSecond());
    }

    public double computeDistance(Coordinate x){
        /* calculate distance by distance rule d = sqrt((x1 - x2)^2 _ (y1-y2)^2) */
        double elementOne = Math.pow((x.getSecond() - second),2);
        double elementTwo = Math.pow((x.getFirst() - first),2);
        return Math.sqrt(elementOne + elementTwo);
    }

}

