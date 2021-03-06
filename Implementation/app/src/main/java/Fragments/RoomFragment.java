package Fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.Environment;
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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
    private Coordinate approximateLocation;
    private boolean stillLearning = true ; //true if estimtoes & false if fingerprinting
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
    /* Setup wait Variable */
    ProgressDialog progress;
    /*Estimotes Log File */
    File estimoteLogFile;
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
        /* add a loader */
        progress = new ProgressDialog(this.getActivity());
        progress.setTitle("Adjusting");
        progress.setMessage("Wait while adjusting the estimotes...");
        if(stillLearning)
            progress.show();
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
        /* check if marshamllow or more to ask for permission then start scannign for wifi */
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
                /* get wifi scan results */
                List<ScanResult>results = mainWifiManager.getScanResults();
                accessPoints = new ArrayList<HashMap<String, String>>();
                for(ScanResult r : results){
                    HashMap<String,String> accessPoint = new HashMap<String,String>();
                    accessPoint.put("mac",r.BSSID);
                    accessPoint.put("rssi",r.level+"");
                    accessPoints.add(accessPoint);
                }
                finishedScan = true;
                /* if fingerpringint localization is enabled get location from databse */
                if(!stillLearning)
                    http.getFingerPrintLocation(getFragmentManager().findFragmentByTag("RoomFragment"), accessPoints, getRoom().getRoomID());
            }
        };
        getActivity().registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
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
                /* if less than 3 beaoons are discovered return or wifi fingerprinting is enabled */
                if(list.size() < 3 || !stillLearning)
                    return ;
                /* add boolean to check if still setting up standard deviation */
                Boolean progressBoolean = true;
                Coordinate sum = null;
                /* add estimotes related to the room */
                ArrayList<Pair> discovered = new ArrayList<Pair>();
             for(Beacon b : list){
                    int index = getRoom().findBeacon(b.getMacAddress().toString());
                    if(index != -1) {
                        getRoom().addRSSI(index, b.getRssi());
                        double distance = getRoom().getApproximateDistance(index);
                        discovered.add(new Pair(b, index));
                        /* if too close from an estimote then user has same position */
                        if(distance == 0.0){
                            sum = getRoom().getLocation(index);
                            stepCount = 0;
                            setLocation(sum);
                        }
                        /* if still settign up set progress boolean to false */
                        if(distance == -1)
                            progressBoolean = false;
                    }
                }
                /* if no more setting up remove progress bar */
                if(progressBoolean)
                    progress.dismiss();
                /* if the user has not moved or was too clsoe from a beacon or less than 3 beacons are discovered return */
                if(sum != null || discovered.size() < 3 || stepCount == 0 )
                    return;
                /* save step count and reset */
                int tempStepCount = stepCount;
                stepCount = 0;
                ArrayList<Pair> sortedBeacons = discovered;
                /* calculate approximate location based on pedometer and walking direction */
                approximateLocation = new Coordinate(location.getFirst(), location.getSecond());
                if(walkingDirection == 1)
                    approximateLocation.setSecond(approximateLocation.getSecond()+(tempStepCount * 0.76f));
                else if(walkingDirection == 2)
                    approximateLocation.setSecond(approximateLocation.getSecond()-(tempStepCount * 0.76f));
                else if(walkingDirection == 3)
                    approximateLocation.setFirst(approximateLocation.getFirst()+(tempStepCount * 0.76f));
                else if(walkingDirection == 4)
                    approximateLocation.setFirst(approximateLocation.getFirst()-(tempStepCount * 0.76f));
                /* sort beacons by closest to that approximate lcoation */
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
                /* calculate position from closest three beacons */
                double[] xArray= new double[3];
                double[] yArray= new double[3];
                double[] rArray= new double[3];
                for(int i = 0; i< 3; i ++){
                    xArray[i] = getRoom().getLocation((int)sortedBeacons.get(i).second).getFirst();
                    yArray[i] = getRoom().getLocation((int)sortedBeacons.get(i).second).getSecond();
                    rArray[i] = getRoom().getApproximateDistance((int)sortedBeacons.get(i).second);
                    if(rArray[i] == -1)
                        return;
                }
                int x = (int)computeX(xArray[0], xArray[1], xArray[2], yArray[0], yArray[1], yArray[2], rArray[0], rArray[1], rArray[2]);
                int y = (int) computeY(xArray[0], xArray[1], yArray[0], yArray[1], rArray[0], rArray[1], x);
                sum = new Coordinate(x,y);
                setLocation(sum);
            }
        });
    }


    public void setLocation(Coordinate sum){
        /* if location x or y is out of room bounds get the equivalant one from approximate location */
        if(sum.getFirst() < getRoom().getMinValueOfCoordinates().getFirst() || sum.getFirst() > getRoom().getMaxValueOfCoordinates().getFirst() )
            sum.setFirst(approximateLocation.getFirst());
        if(sum.getSecond() < getRoom().getMinValueOfCoordinates().getSecond() || sum.getSecond() > getRoom().getMaxValueOfCoordinates().getSecond() )
            sum.setSecond(approximateLocation.getSecond());
        location = sum;
            Snackbar.make(getActivity().findViewById(R.id.rl), "location: (" + sum.getFirst() + " , " + sum.getSecond() + ")", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            params.leftMargin = (int)(sum.getFirst() * getRoom().getScaleFactor() + getRoom().getXTranslation());
            params.topMargin = (int)(sum.getSecond() * getRoom().getScaleFactor() + getRoom().getYTranslation());
            humanMarker.setLayoutParams(params);
            if(finishedScan) {
                finishedScan = false;
                if(stillLearning)
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
