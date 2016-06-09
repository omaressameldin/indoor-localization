package Classes;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by oessa_000 on 5/23/2016.
 */
public class Circular2DArray {
    private ArrayList<Double>[] array;
    private int size;
    private int[] whereToAdd;
    private double[] standardDeviations;
    public Circular2DArray(int dim1, int s){
        array = new ArrayList[dim1];
        for(int i =0; i <dim1; i++)
            array[i]= new ArrayList<Double>();
        size = s;
        whereToAdd = new int[dim1];
        standardDeviations = new double[dim1];
        Arrays.fill(standardDeviations, -1);

    }

    public void clearStandardDeviation(int dim1){
        standardDeviations[dim1] = -1;
    }

    public void add(int dim1, double value){
        if(Math.abs(getAvg(dim1) - value) < standardDeviations[dim1])
            return ;
        if(array[dim1].size()<size) {
            array[dim1].add(whereToAdd[dim1]++, value);
        }
        else
            array[dim1].set(whereToAdd[dim1]++, value);
        if(whereToAdd[dim1] >= size)
            whereToAdd[dim1] = 0;
    }

    public void filterValues(int index) {
        double avg = getAvg(index);
        boolean changes = false;
        int arraySize = array[index].size();
        for (int i = 0, j = 0; i < arraySize; i++, j++) {
            if (Math.abs(array[index].get(j) - avg) < standardDeviations[index]) {
                array[index].remove(j);
                j--;
                changes = true;
            }
        }
        if(changes)
            whereToAdd[index] = array[index].size();
    }

    public double getLowPassFiltered(int index){
        if(array[index].size() == 1)
            return -1;
        int toBeFiltered = (whereToAdd[index] == 0)? size - 1: whereToAdd[index] - 1;
        int prevValue = toBeFiltered - 1;
        array[index].set(toBeFiltered,  0.95*array[index].get(prevValue) + 0.05*array[index].get(toBeFiltered));
//        if(array[index].size() < size)
//            return -1;
//        else
            return array[index].get(toBeFiltered);

    }

    public double getAvg(int index){
        if(array[index].size() < size)
            return -1;
        double avg = 0;
        for(double d: array[index]){
            avg += d;
        }
        return avg/size;

    }

    public void setStandardDeviation(int index){
        if(array[index].size() < size)
            return;
        double avg = getAvg(index);
        double standardDeviation = 0;
        for(double d: array[index]){
            standardDeviation +=  Math.pow((d - avg), 2);
        }
        standardDeviations[index] = Math.sqrt(standardDeviation / size) ;
        filterValues(index);
    }

    public double getMedian(int index){
        if(array[index].size() < size)
            return -1;
        double median = 0;
        double[] temp = new double[array[index].size()];
        for(int i = 0; i<temp.length; i++)
            temp[i]= array[index].get(i);
        Arrays.sort(temp);
        if(temp.length %2 == 0)
            median = (temp[temp.length / 2] + temp[temp.length / 2 - 1]) / 2;
        else
            median = temp[temp.length / 2];
        return median ;
        }
    public double getStandardDeviation(int index){return standardDeviations[index];}
}
