package Classes;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class Coordinate {

    private float first;
    private float second;

    public Coordinate(float f, float s){
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
        return(c.getFirst() == this.getFirst() && c.getSecond() == this.getSecond());
    }

}

