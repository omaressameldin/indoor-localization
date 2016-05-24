package Classes;

import java.util.ArrayList;

/**
 * Created by oessa_000 on 5/23/2016.
 */
public class Circular2DArray {
    private ArrayList<Double>[] array;
    private int size;
    private int[] whereToAdd;
    public Circular2DArray(int dim1, int s){
        array = new ArrayList[dim1];
        for(int i =0; i <dim1; i++)
            array[i]= new ArrayList<Double>();
        size = s;
        whereToAdd = new int[dim1];
    }
    public void add(int dim1, double value){
        if(array[dim1].size()<size){
            array[dim1].add(whereToAdd[dim1]++, value) ;
        }
        else
            array[dim1].set(whereToAdd[dim1]++, value);
        if(whereToAdd[dim1] >= size)
            whereToAdd[dim1] = 0;
    }
    public double getAvg(int index){
        if(array[index].size() < size)
            return -1;
        else{
            double avg = 0;
            for(double d: array[index]){
                avg += d;
            }
            return avg/size;
        }
    }
}
