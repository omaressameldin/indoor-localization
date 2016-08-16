package Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.example.oessa_000.countsteps.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import Classes.Coordinate;
import Classes.HTTPRequest;
import Classes.MyFragment;
import Classes.RoomEstimote;

import static com.example.oessa_000.countsteps.MainActivity.getBeaconManager;
import static com.example.oessa_000.countsteps.MainActivity.getRoom;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class RoomFragment extends MyFragment implements SensorEventListener {

    int beaconDetectedCount = 0;
    /* Drawing Vairables */
    private Canvas canvas;
    private ImageView drawingImageView;
    private ImageView humanMarker;
    private RelativeLayout.LayoutParams params ;
    /* Location Variables */
    private ArrayList<Pair> estimoteCoordinates = new ArrayList<Pair>();
    private Coordinate location;
    /* Step Counter Variables */
    private int stepCount = 0;
    private double prevY;
    private boolean ignoreGravityIncrease;
    private int waitingCountDown = 2;
    /* Sensors Variables */
    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private Sensor mSensorGyroscope;
    /* Walking Direction Variables */
    private int walkingDirection ; /* 1 --> forward, 2 --> backward, 3 --> left, 4 --> right */
    private boolean turningNow;
    /* Server Request Variable */
    HTTPRequest http;
    /* Transfer Learning Variables */
    private BroadcastReceiver wifiReceiver;
    private WifiManager mainWifiManager;
    private ArrayList<HashMap<String,String>> accessPoints;
    private boolean finishedScan = false;
    /* Floating Action Button */
    private FloatingActionButton addRoomButton;

    public RoomFragment(){}

    public RoomFragment(int walkingDirection){
        this.walkingDirection = walkingDirection;
    }

    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        /* Hide add room Button */
        addRoomButton = (FloatingActionButton)getActivity().findViewById(R.id.addRoom);
        addRoomButton.setVisibility(View.INVISIBLE);
        /* Initialize Views */
        drawingImageView = (ImageView) getActivity().findViewById(R.id.room);
        /* Initialize Wifi Manager */
        mainWifiManager = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
        /* Initialize http */
        http = new HTTPRequest();
        /* Draw the Room*/
        drawRoom();
        /* Set up Transfer Learning */
        startLearning();

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_view, container, false);
    }


    public void drawRoom(){
        /* Get Phone's pixel dimenstion */
        Coordinate pixelDimensions = getPixelDimensions(getActivity());
        /* set Room's scaling according to phone's pixel dimensions */
        getRoom().setScaleFactor(pixelDimensions);
        getRoom().setXTranslation((float)Math.abs(getRoom().getMinValueOfCoordinates().getFirst())*getRoom().getScaleFactor());
        getRoom().setYTranslation(  Math.abs((float)getRoom().getMinValueOfCoordinates().getSecond())*getRoom().getScaleFactor()+pixelDimensions.getSecond()/4);
        /* create a new canvas for drawing the room */
        Bitmap bitmap = Bitmap.createBitmap((int) getActivity().getWindowManager()
                .getDefaultDisplay().getWidth(), (int) getActivity().getWindowManager()
                .getDefaultDisplay().getHeight(), Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        /* set Image view's bitmap */
        drawingImageView.setImageBitmap(bitmap);
        /* add walking Human */
        RelativeLayout rl = (RelativeLayout) getActivity().findViewById(R.id.rl);
        params =  new RelativeLayout.LayoutParams(96, 96);
        humanMarker = (ImageView) new ImageView(getActivity());
        humanMarker.setImageResource(R.drawable.marker);
        rl.addView(humanMarker, params);
        /* Initialize Paint */
        Paint paint = initializePaint();
        /* Scale coordiantes and connect them */
        RoomEstimote[] coordinates = getRoom().getCoordinates();
         Coordinate startPoint =  new Coordinate((coordinates[0].getLocation()).getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation(),
                 (coordinates[0].getLocation()).getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());
        Coordinate endPoint;
        for(int i = 1; i < coordinates.length; i++){
            endPoint = new Coordinate((coordinates[i].getLocation()).getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation(),
                    (coordinates[i].getLocation()).getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());
            canvas.drawLine(startPoint.getFirst(), startPoint.getSecond(), endPoint.getFirst(), endPoint.getSecond(), paint);
            startPoint =endPoint;
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
         endPoint = new Coordinate((coordinates[0].getLocation()).getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation(),
                 (coordinates[0].getLocation()).getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());
        canvas.drawLine(startPoint.getFirst(),startPoint.getSecond(),endPoint.getFirst(),endPoint.getSecond(),paint);
        setLocation( new Coordinate(coordinates[coordinates.length - 1].getLocation().getFirst(), coordinates[coordinates.length - 1].getLocation().getSecond()) );
    }


    public Coordinate getPixelDimensions( Activity mainActivity){
        DisplayMetrics dm = new DisplayMetrics();
        mainActivity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return new Coordinate(dm.widthPixels, dm.heightPixels);
    }


    public Paint initializePaint(){
        Paint paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.RED);
        paint.setStrokeWidth(6);
        return paint ;
    }

    public void startLearning() {
        if ((android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                || (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.M)) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        while (true) {
                            mainWifiManager.startScan();
                            Thread.sleep(400);
                        }
                    } catch(InterruptedException v) {
                        System.out.println(v);
                    }
                }
            };
            t.start();
            recordLearning();

        }
    }


    public void recordLearning(){
        wifiReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                List<ScanResult>results = mainWifiManager.getScanResults();
                accessPoints = new ArrayList<HashMap<String, String>>();
                for(ScanResult r : results){
                    HashMap<String,String> accessPoint = new HashMap<String,String>();
                    accessPoint.put("mac",r.BSSID);
                    accessPoint.put("rssi",r.level+"");
                    accessPoints.add(accessPoint);
                }
                finishedScan = true;
            }
        };
        getActivity().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

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
            if(getRoom().getRSSI(((int)leftHalf.get(i).second)) > getRoom().getRSSI(((int)rightHalf.get(j).second))){
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

                if(list.size() < 3)
                    return ;
                Log.e("Step Count", stepCount+"");
                Log.e("Walking Direction", walkingDirection+"");

                Coordinate sum = null;
                ArrayList<Pair> discovered = new ArrayList<Pair>();
                Log.e("RoomEstimotes Size", getRoom().getCoordinates().length+"");
                for(Beacon b : list){
                    int index = getRoom().findBeacon(b.getMacAddress().toString());
                    if(index != -1) {
                        getRoom().addRSSI(index, b.getRssi());
                        discovered.add(new Pair(b, index));
                        double distance = getRoom().getApproximateDistance(index);
                        Log.e("mac", b.getMacAddress().toString() );
                        Log.e("Base RSSI", getRoom().getBaseRSSI(index)+"");
                        Log.e("RSSI", getRoom().getRSSI(index)+"");


//                        Snackbar.make(getActivity().findViewById(R.id.rl), "mac: " +b.getMacAddress()+ "\ndistance: " + distance, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                        if(distance == 0.0){
//                            Snackbar.make(getActivity().findViewById(R.id.rl), "mac: " +b.getMacAddress()+ "\ndistance: " + distance, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                            sum = getRoom().getLocation(index);
                            stepCount = 0;
                            setLocation(sum);
                        }
                    }
                }
                if(sum != null || discovered.size() < 3 || stepCount == 0 )
                    return;
                int tempStepCount = stepCount;
                stepCount = 0;
//                ArrayList<Pair> sortedBeacons = mergeSortBeacons(discovered,0,discovered.size() -1);
                ArrayList<Pair> sortedBeacons = discovered;


                double[] xArray= new double[sortedBeacons.size()];
                double[] yArray= new double[sortedBeacons.size()];
                double[] rArray= new double[sortedBeacons.size()];
                Coordinate approximateLocation = new Coordinate(location.getFirst(), location.getSecond());
                if(walkingDirection == 1)
                    approximateLocation.setFirst(approximateLocation.getFirst()+(tempStepCount * 0.76f));
                else if(walkingDirection == 2)
                    approximateLocation.setFirst(approximateLocation.getFirst()-(tempStepCount * 0.76f));
                else if(walkingDirection == 3)
                    approximateLocation.setFirst(approximateLocation.getSecond()+(tempStepCount * 0.76f));
                else if(walkingDirection == 4)
                    approximateLocation.setFirst(approximateLocation.getSecond()-(tempStepCount * 0.76f));
                for(int i = 0; i< sortedBeacons.size() ; i++){
                    int indexOfClosestSoFar = i;
                    double shortestDistanceSoFar = approximateLocation.computeDistance(getRoom().getLocation((int)sortedBeacons.get(i).second));
                    for(int j = i + 1; j<sortedBeacons.size(); j++){
                        double beaconDistance = approximateLocation.computeDistance(getRoom().getLocation((int)sortedBeacons.get(j).second));
                        if(beaconDistance < shortestDistanceSoFar){
                            indexOfClosestSoFar = j;
                            shortestDistanceSoFar = beaconDistance;
                        }
                    }
                    Pair temp =sortedBeacons.get(i);
                    sortedBeacons.set(i, sortedBeacons.get(indexOfClosestSoFar));
                    sortedBeacons.set(indexOfClosestSoFar,temp );
                }
                String mac = "";
                for(int i = 0, j=0; i< sortedBeacons.size(); i ++){
                    if(!getRoom().getMacAddress((int)sortedBeacons.get(i).second).contains("FE") &&
                            !getRoom().getMacAddress((int)sortedBeacons.get(i).second).contains("D2") &&
                            ! getRoom().getMacAddress((int)sortedBeacons.get(i).second).contains("F1")) {
                        continue;
                    }
                    xArray[j] = getRoom().getLocation((int)sortedBeacons.get(i).second).getFirst();
                    yArray[j] = getRoom().getLocation((int)sortedBeacons.get(i).second).getSecond();
                    rArray[j] = getRoom().getApproximateDistance((int)sortedBeacons.get(i).second);
                    if(rArray[j] == -1)
                        return;
                    j++;
                    Log.e("Mac Adress",getRoom().getMacAddress((int)sortedBeacons.get(i).second));
//                    mac += getRoom().getMacAddress((int)sortedBeacons.get(i).second) + "   ";
                    Log.e("xpostion",xArray[i]+"");
                    Log.e("ypostion",yArray[i]+"");
                    Log.e("RSSI", getRoom().getRSSI((int)sortedBeacons.get(i).second)+"");
                    Log.e("distance",rArray[i]+"");
                }
                Snackbar.make(getActivity().findViewById(R.id.rl), mac, Snackbar.LENGTH_LONG).setAction("Action", null).show();
                if(sum == null){
                    int x = (int)computeX(xArray[0], xArray[1], xArray[2], yArray[0], yArray[1], yArray[2], rArray[0], rArray[1], rArray[2]);
                    int y = (int) computeY(xArray[0], xArray[1], yArray[0], yArray[1], rArray[0], rArray[1], x);
                    sum = new Coordinate(x,y);
                    setLocation(sum);
                }
            }
        });
    }


    public void setLocation(Coordinate sum){
        if(sum.getFirst() < getRoom().getMinValueOfCoordinates().getFirst())
            sum.setFirst(getRoom().getMinValueOfCoordinates().getFirst());
        if(sum.getFirst() > getRoom().getMaxValueOfCoordinates().getFirst())
            sum.setFirst(getRoom().getMaxValueOfCoordinates().getFirst());
        if(sum.getSecond() < getRoom().getMinValueOfCoordinates().getSecond())
            sum.setSecond(getRoom().getMinValueOfCoordinates().getSecond());
        if(sum.getSecond() > getRoom().getMaxValueOfCoordinates().getSecond())
            sum.setSecond(getRoom().getMaxValueOfCoordinates().getSecond());
        location = sum;
            Snackbar.make(getActivity().findViewById(R.id.rl), "location: (" + sum.getFirst() + " , " + sum.getSecond() + ")", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            params.leftMargin = (int)(sum.getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation()) + 100;
            params.topMargin = (int)(sum.getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation()) + 100;
            humanMarker.setLayoutParams(params);
            if(finishedScan) {
                finishedScan = false;
                http.addFingerPrint(this, accessPoints, sum, getRoom().getRoomID());
            }
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
        /* Initialize Sensors */
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        mSensorGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        /* Set Sensors Delay */
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorGyroscope,
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        switch(sensor.getType()){
            /* Gravity Sensor For Walking and Step Count */
            case Sensor.TYPE_GRAVITY:
                    if(ignoreGravityIncrease){
                        waitingCountDown --;
                        ignoreGravityIncrease = (waitingCountDown <= 0)? false : true;
                    }
                    if( ((int)prevY - (int) event.values[1]) >= 0.95 &&!ignoreGravityIncrease){
                        stepCount++;
                        waitingCountDown = 2;
                        ignoreGravityIncrease = true;
                    }
                prevY = event.values[1];
                break;
            /* Gyroscope For Getting The Walking Direction */
            case Sensor.TYPE_GYROSCOPE:
                    if(turningNow){
                        if(Math.abs(event.values[2]) < 0.3){
                            turningNow = false;

                        }
                    }
                        /*
                     forward + right = right
                     backward + right = left
                     left + right = forward
                     right + right = backward
                      */
                    if(!turningNow) {
                        if(event.values[2] < -1  ) {
                            turningNow = true;
                            if(walkingDirection == 1) {
                                walkingDirection = 4;
                            }
                            else if(walkingDirection == 2) {
                                walkingDirection = 3;
                            }
                            else if(walkingDirection == 3) {
                                walkingDirection = 1;
                            }
                            else if(walkingDirection == 4) {
                                walkingDirection = 2;
                            }
                        }
                        /*
                     forward + left = left
                     backward + left = right
                     left + left = backward
                     right + left = forward
                      */
                        if(event.values[2] > 1) {
                            turningNow = true;
                            if(walkingDirection == 1) {
                                walkingDirection = 3;
                            }
                            else if(walkingDirection == 2) {
                                walkingDirection = 4;
                            }
                            else if(walkingDirection == 3) {
                                walkingDirection = 2;
                            }
                            else if(walkingDirection == 4) {
                                walkingDirection = 1;
                            }
                        }
                    }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


}
