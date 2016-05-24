package Fragments;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.example.oessa_000.countsteps.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Classes.Coordinate;
import Classes.MyFragment;
import Classes.Room;

import static com.example.oessa_000.countsteps.MainActivity.getBeaconManager;
import static com.example.oessa_000.countsteps.MainActivity.getRoom;
import static com.example.oessa_000.countsteps.MainActivity.setRoom;
import static com.example.oessa_000.countsteps.MainActivity.setRoomScaleFactor;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class RoomFragment extends MyFragment {

    int beaconDetectedCount = 0;
    private Canvas canvas;
    ImageView drawingImageView;
    ImageView humanMarker;
    Coordinate min;
    Coordinate max;
    RelativeLayout.LayoutParams params ;
    private ArrayList<Pair> estimoteCoordinates = new ArrayList<Pair>();
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //static test
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("34355,23519", new Coordinate(0,0)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("42969,34317", new Coordinate(0,1.6f)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("11988,45406", new Coordinate(0,4)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("53886,34857", new Coordinate(-4,4)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("40516,26553", new Coordinate(-5.6f,4)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("27817,37349", new Coordinate(-5.6f,1.6f)));
         estimoteCoordinates.add(estimoteCoordinates.size(),new Pair("62267,24288", new Coordinate(-5.6f,0)));

        setRoom(new Room(estimoteCoordinates));


        Coordinate pixelDimensions = getPixelDimensions(getActivity());

        Bitmap bitmap = Bitmap.createBitmap((int) getActivity().getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getActivity().getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);

        drawingImageView = (ImageView) getActivity().findViewById(R.id.room);
        drawingImageView.setImageBitmap(bitmap);

        RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.rl);
        params =  new RelativeLayout.LayoutParams(96, 96);
        humanMarker = (ImageView) new ImageView(getActivity());
        humanMarker.setImageResource(R.drawable.marker);
        rl.addView(humanMarker, params);

        params.leftMargin = 1340;
        params.topMargin = 1340;


        Paint paint = initializePaint();
        getRoom().setScaleFactor(pixelDimensions);
        getRoom().setXTranslation((float)Math.abs(getRoom().getMinValueOfCoordinates().getFirst())*getRoom().getScaleFactor());
        getRoom().setYTranslation(  Math.abs((float)getRoom().getMinValueOfCoordinates().getSecond())*getRoom().getScaleFactor()+pixelDimensions.getSecond()/4);
        drawRoom(paint);
        min = getRoom().getMinValueOfCoordinates();
        max = getRoom().getMaxValueOfCoordinates();
        Log.e("min", "("+min.getFirst()+","+min.getSecond()+")");
        Log.e("max", "("+max.getFirst()+","+max.getSecond()+")");

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_view, container, false);
    }

    public Paint initializePaint(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);
        return paint ;
    }

    public Coordinate getPixelDimensions( Activity mainActivity){
        DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new Coordinate(dm.widthPixels, dm.heightPixels);
    }


    public void drawRoom( Paint paint){
//        canvas.translate(getRoom().getXTranslation(),getRoom().getYTranslation());
        ArrayList<Pair> coordinates = getRoom().getCoordinates();
        // Coordinate startPoint = (coordinates.size() > 0)? (Coordinate)coordinates.get(0).second : new Coordinate(0,0) ;
        // for(int i = 1; i < coordinates.size(); i++){
        //     ((Coordinate)coordinates.get(i).second).setFirst(((Coordinate)coordinates.get(i).second).getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation());
        //     ((Coordinate)coordinates.get(i).second).setSecond(((Coordinate)coordinates.get(i).second).getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());
        //     canvas.drawLine(startPoint.getFirst(), startPoint.getSecond(), ((Coordinate)coordinates.get(i).second).getFirst(), ((Coordinate)coordinates.get(i).second).getSecond(), paint);
        //     startPoint = (Coordinate)coordinates.get(i).second;
        // }
        // canvas.drawLine(startPoint.getFirst(),startPoint.getSecond(),((Coordinate)coordinates.get(0).second).getFirst(),((Coordinate)coordinates.get(0).second).getSecond(),paint);


         Coordinate startPoint = (coordinates.size() > 0)? (Coordinate)coordinates.get(0).second : new Coordinate(0,0) ;
        for(int i = 0; i < coordinates.size(); i++){
            ((Coordinate)coordinates.get(i).second).setFirst(((Coordinate)coordinates.get(i).second).getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation());
            ((Coordinate)coordinates.get(i).second).setSecond(((Coordinate)coordinates.get(i).second).getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());

            if(i != 0)
                canvas.drawLine(startPoint.getFirst(), startPoint.getSecond(), ((Coordinate)coordinates.get(i).second).getFirst(), ((Coordinate)coordinates.get(i).second).getSecond(), paint);
            startPoint = (Coordinate)coordinates.get(i).second;
            if(i == 1)
                paint.setColor(Color.GREEN);
            if(i == 2)
                paint.setColor(Color.BLUE);
            if(i == 3)
                paint.setColor(Color.YELLOW);
            if(i == 4)
                paint.setColor(Color.MAGENTA);
            if(i == 5)
                paint.setColor(Color.BLACK);
            if(i == 6)
                paint.setColor(Color.CYAN);
        }
        canvas.drawLine(startPoint.getFirst(),startPoint.getSecond(),((Coordinate)coordinates.get(0).second).getFirst(),((Coordinate)coordinates.get(0).second).getSecond(),paint);
    }


    public ArrayList<Pair> mergeSortBeacons(ArrayList<Pair> notSorted, int start, int end){
        if(start - end == 0) {
            ArrayList<Pair> p = new ArrayList<Pair>();
            p.add(notSorted.get(start));
            return p;
        }

        ArrayList<Pair> sorted = new ArrayList<Pair>();
        ArrayList<Pair> leftHalf = mergeSortBeacons(notSorted, start, start + (end - start)/2);
        ArrayList<Pair> rightHalf = mergeSortBeacons(notSorted, start + 1 + (end - start)/2 , end);

        int i = 0;
        int j = 0;
        while (i < leftHalf.size() && j < rightHalf.size()){
            if(((Beacon)leftHalf.get(i).first).getRssi() > ((Beacon)rightHalf.get(j).first).getRssi()){
                sorted.add(leftHalf.get(i));
                i++;
            }
            else{
                sorted.add(rightHalf.get(j));
                j++;
            }
        }
        while(i<leftHalf.size()){
            sorted.add(leftHalf.get(i));
            i++;
        }
        while(j<rightHalf.size()){
            sorted.add(rightHalf.get(j));
            j++;
        }
        return sorted;
    }

    public void connectBeaconManager(){
        getBeaconManager().connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                getBeaconManager().startRanging(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        null, null));
            }
        });
        getBeaconManager().setRangingListener(new BeaconManager.RangingListener() {
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                // if(list.size() < 1)
                //     return ;
                // temporary[beaconDetectedCount ++] = Utils.computeAccuracy(list.get(0));
                // if(five){
                //     avg = (temporary[0] + temporary[1] + temporary[2] + temporary[3] + temporary[4]) / 5 ;
                // }
                // if(beaconDetectedCount == 5){
                //     beaconDetectedCount = 0;
                //     five = true;
                // }
                // Snackbar.make(getActivity().findViewById(R.id.rl), "Normal Accuracy: " +   Utils.computeAccuracy(list.get(0)) + "\n"  + "RSSI: " + list.get(0).getRssi() , Snackbar.LENGTH_LONG)
                //         .setAction("Action", null).show();

                if(list.size() < 3)
                    return ;
//                     Snackbar.make(getActivity().findViewById(R.id.rl), "in" , Snackbar.LENGTH_LONG)
//                         .setAction("Action", null).show();

                Log.e("fragment","room Fragment");
                ArrayList<Pair> discovered = new ArrayList<Pair>();
                for(Beacon b : list){
                    int index = getRoom().findBeacon(b.getMajor()+","+b.getMinor());
                    if(index != -1)
                        discovered.add(new Pair(b,index));
                }

                 ArrayList<Pair> sortedBeacons = mergeSortBeacons(discovered,0,discovered.size() -1);
                 if(sortedBeacons.size() < 3)
                    return;

                Snackbar.make(getActivity().findViewById(R.id.rl), "Beacon1: "+ "("+ ((Beacon)sortedBeacons.get(0).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")" , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                 double x1 = getRoom().getXCoordinate((int)sortedBeacons.get(0).second);
                 double y1 = getRoom().getYCoordinate((int)sortedBeacons.get(0).second);
                 double r1 = getRoom().getApproximateDistance((int)sortedBeacons.get(0).second, Utils.computeAccuracy((Beacon) sortedBeacons.get(0).first))*getRoom().getScaleFactor();

                Snackbar.make(getActivity().findViewById(R.id.rl), "Beacon2: "+ "("+ ((Beacon)sortedBeacons.get(1).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")" , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                 double x2 = getRoom().getXCoordinate((int)sortedBeacons.get(1).second);
                 double y2 = getRoom().getYCoordinate((int)sortedBeacons.get(1).second);
                 double r2 = getRoom().getApproximateDistance((int)sortedBeacons.get(1).second, Utils.computeAccuracy((Beacon) sortedBeacons.get(1).first))*getRoom().getScaleFactor();

                Snackbar.make(getActivity().findViewById(R.id.rl), "Beacon3: "+ "("+ ((Beacon)sortedBeacons.get(2).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")" , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                 double x3 = getRoom().getXCoordinate((int)sortedBeacons.get(2).second);
                 double y3 = getRoom().getYCoordinate((int)sortedBeacons.get(2).second);
                 double r3 = getRoom().getApproximateDistance((int)sortedBeacons.get(2).second, Utils.computeAccuracy((Beacon) sortedBeacons.get(2).first))*getRoom().getScaleFactor();
                 int x = (int)computeX(x1, x2, x3, y1, y2, y3, r1, r2, r3);
                 int y = (int) computeY(x1, x2, y1, y2, r1, r2, x);
                 Coordinate sum = new Coordinate(x,y);
                                  
//                 if(discovered.size()<3)
//                     return ;
//
//
//                     ArrayList<Coordinate> locations = new ArrayList<Coordinate>();
//                     for(int i = 0; i< discovered.size();i++){
//                         // double x1 = ((Coordinate)discovered.get(i).second).getFirst();
//                         // double y1 = ((Coordinate)discovered.get(i).second).getSecond();
//                         // double r1 = Utils.computeAccuracy((Beacon) discovered.get(i).first)*getRoom().getScaleFactor();
//                         double x1 = getRoom().getXCoordinate((int)discovered.get(i).second);
//                         double y1 = getRoom().getYCoordinate((int)discovered.get(i).second);
//                         double r1 = getRoom().getApproximateDistance((int)discovered.get(i).second, Utils.computeAccuracy((Beacon) discovered.get(i).first))*getRoom().getScaleFactor();
//                         if(r1 < 0)
//                             continue;
//                         for(int j=i + 1; j< discovered.size(); j++){
//                             // double x2 = ((Coordinate)discovered.get(j).second).getFirst();
//                             // double y2 = ((Coordinate)discovered.get(j).second).getSecond();
//                             // double r2 = Utils.computeAccuracy((Beacon) discovered.get(j).first)*getRoom().getScaleFactor();
//                             double x2 = getRoom().getXCoordinate((int)discovered.get(j).second);
//                             double y2 = getRoom().getYCoordinate((int)discovered.get(j).second);
//                             double r2 = getRoom().getApproximateDistance((int)discovered.get(j).second, Utils.computeAccuracy((Beacon) discovered.get(j).first))*getRoom().getScaleFactor();
//                             if(r2 < 0)
//                                 continue;
//                             for( int k = j + 1; k< discovered.size(); k++  ){
//                                 // double x3 = ((Coordinate)discovered.get(k).second).getFirst();
//                                 // double y3 = ((Coordinate)discovered.get(k).second).getSecond();
//                                 // double r3 = Utils.computeAccuracy((Beacon) discovered.get(k).first)*getRoom().getScaleFactor();
//                                 double x3 = getRoom().getXCoordinate((int)discovered.get(k).second);
//                                 double y3 = getRoom().getYCoordinate((int)discovered.get(k).second);
//                                 double r3 = getRoom().getApproximateDistance((int)discovered.get(k).second, Utils.computeAccuracy((Beacon) discovered.get(k).first))*getRoom().getScaleFactor();
//                                 if(r3 < 0)
//                                     continue;
//                                 double x = computeX(x1, x2, x3, y1, y2, y3, r1, r2, r3);
//                                 double y = computeY(x1, x2, y1, y2, r1, r2, x);
//                                 if(Double.isNaN(x) || Double.isNaN(y))
//                                     continue;
//                                 Log.e("foundLocation", "x1: "+ x1+",x2: "+x2 +",x3: "+ x3+ ",y1: "+y1+",y2: "+y2+",y3: "+y3+",r1: "+r1+",r2: "+r2+",r3: "+r3);
//                                 locations.add(locations.size(), new Coordinate((float)x, (float)y));
//                             }
//                         }
//                     }
//                     Coordinate sum = new Coordinate(0,0);
//                     for(Coordinate l : locations){
//                         sum.setFirst(sum.getFirst() + l.getFirst());
//                         sum.setSecond(sum.getSecond() + l.getSecond());
//                     }
//                     sum.setFirst((int)Math.floor(sum.getFirst()/locations.size()));
//                     sum.setSecond((int)Math.floor(sum.getSecond()/locations.size()));

                    if(sum.getFirst() >= min.getFirst() && sum.getFirst() <= max.getFirst() && sum.getSecond() >= min.getSecond() && sum.getSecond() <= max.getSecond()  ){

                        params.leftMargin = (int)sum.getFirst();
                        params.topMargin = (int)sum.getSecond();
                        Snackbar.make(getActivity().findViewById(R.id.rl), "location: (" + sum.getFirst() +" , "+ sum.getSecond()+")" +
                                        "Beacon1: "+ "(" + ((Beacon)sortedBeacons.get(0).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")"+
                                        "Beacon2: "+ "("+ ((Beacon)sortedBeacons.get(1).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")"+
                                        "Beacon3: "+ "("+ ((Beacon)sortedBeacons.get(2).first).getMajor() +","+ ((Beacon)sortedBeacons.get(0).first).getMinor()+")"
                                , Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                        humanMarker.setLayoutParams(params);

                    }


                }
        });
    }


    public double computeX(double x1, double x2, double x3, double y1, double y2, double y3, double r1, double r2, double r3){

        double eq1 = r3*r3 - r2*r2 - x3*x3 - y3*y3 +x2*x2 + y2 * y2 ;
        double eq2 = r1*r1 - r2*r2 - x1*x1 -y1*y1 + x2*x2 +y2*y2 ;
        double eq3 = 2*y2 - 2*y3;
        double eq4 = -2*y1 + 2*y2;
        double eq5 = eq2 * eq3;
        double eq6 = eq5/eq4;
        double numerator = eq1 - eq6;

        double eq7 = -2*x3 +2*x2;
        double eq8 = -2*x1 + 2*x2;
        double eq9 = 2*y2 - 2*y3;
        double eq10 = -2*y1 + 2*y2;
        double eq11 = eq8 * eq9;
        double eq12 = eq11 / eq10;
        double denominator = eq7 - eq12;

        return numerator / denominator;
    }

    public double computeY(double x1, double x2, double y1, double y2, double r1, double r2, double x){
        double eq1 = r1*r1 - r2*r2 - x1*x1 - y1*y1 +x2*x2 +y2*y2;
        double eq2 = -2*x1 + 2*x2;
        double eq3 = x*eq2;
        double numerator = eq1 - eq3;

        double denominator = -2*y1+ 2*y2;

        return numerator / denominator;
    }

    public void onResume() {
        super.onResume();

    }
}
