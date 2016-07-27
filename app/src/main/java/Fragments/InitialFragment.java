package Fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.example.oessa_000.countsteps.R;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import Classes.Coordinate;
import Classes.MyFragment;
import Classes.Room;
import Classes.RoomEstimote;

import static com.example.oessa_000.countsteps.MainActivity.changeFragment;
import static com.example.oessa_000.countsteps.MainActivity.getBeaconManager;
import static com.example.oessa_000.countsteps.MainActivity.setRoom;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class InitialFragment extends MyFragment implements SensorEventListener {

    /* Step Counter Variables */
    private int stepCount = 0;
    private double prevY;
    private boolean ignoreGravityIncrease;
    private int waitingCountDown;

    /* What's going on Variable */
    private int state = 2;

    /*Room Variables */
    private ArrayList<RoomEstimote> estimoteRoomCoordinates = new ArrayList<RoomEstimote>();
    private String nearestBeacon;
    private int[] nearestBeaconRSSIs = new int[10];
    int nearestBeaconRSSIsArrayIndex = 0;
    int baseRSSI = 0;

    /* Sensors Variables */
    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private Sensor mSensorGyroscope;

    /* Buttons Variables */
    private ToggleButton stateToggle ;
    private FloatingActionButton undoButton;
    private FloatingActionButton doneButton;

    /* Walking Direction Variables */
    private int walkingDirection = 1 ; /* 1 --> forward, 2 --> backward, 3 --> left, 4 --> right */
    private boolean turningNow;

    /* Views Variables */
    private TextView stepsView;
    private TextView walkingDirectionView;
    private TextView tempView;
    private TextView instructionsView;


    public InitialFragment(){}


    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        /* Initialize Buttons */
        stateToggle = (ToggleButton) getActivity().findViewById(R.id.state);
        undoButton = (FloatingActionButton) getActivity().findViewById(R.id.undo);
        doneButton = (FloatingActionButton) getActivity().findViewById(R.id.done);
        /*Initialize Views */
        stepsView = (TextView) getActivity().findViewById((R.id.stepView));
        walkingDirectionView = (TextView) getActivity().findViewById((R.id.walkingDirection));
        tempView = (TextView)getActivity().findViewById((R.id.tempview));
        instructionsView = (TextView) getActivity().findViewById((R.id.instructions));
        /* Button Actions */
        buttonsActions();
    }


    public void buttonsActions(){
        /* State Toggle Action */
        stateToggle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                state = (state == 3)? 0 : state + 1;
                if (state == 0) {
                    stateToggle.setText("I am Walking ");
                    nextInstruction("now start walking  then press the 'I am walking button' or " +
                            "press the green button and your room will magically appear :) ");

                } else if(state == 1) {
                    stateToggle.setText("Stop Walking");
                    stepCount = 0;
                    waitingCountDown = 2;
                    ignoreGravityIncrease = false;
                    stepsView.setText("Step Count: " + stepCount);
                    nextInstruction("Press the 'Stop Walking' button when you stop walking");
                }
                else  if(state == 2){
                    stateToggle.setText("Calibrate");
                    nextInstruction("Now put the phone on top of the beacon and press the 'Calibrate' button");
                }
                else{
                    nextInstruction("Please keep the phoen on the beacon while the calibration process is in progress....");
                    Snackbar.make(view, "stepCount: " + stepCount + " steps", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        /* Done Button Action */
        doneButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state !=2) {
                    if (estimoteRoomCoordinates.size() > 3) {
                        setRoom(new Room(estimoteRoomCoordinates));
                        changeFragment(getActivity(), new RoomFragment(),"roomFragment");
                    } else {
                        Snackbar.make(view, "There is no room with only 2 walls :(", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    Snackbar.make(view, "you have to calibrate the last estimote first :(", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
        /* Undo Button Action */
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (state !=2) {
                    if (estimoteRoomCoordinates.size() > 1) {
                        estimoteRoomCoordinates.remove(estimoteRoomCoordinates.size() - 1);
                        Snackbar.make(view, "Wall removed", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    } else {
                        Snackbar.make(view, "You did nothing to undo :)", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } else {
                    Snackbar.make(view, "you have to calibrate the estimote first :(", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }


    public void connectBeaconManager(){
        /* Start Communication with Estimote Beacons */
        getBeaconManager().connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                getBeaconManager().startRanging(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        null, null));
            }
        });
        /* Search For Estimote Beacons */
        getBeaconManager().setRangingListener(new BeaconManager.RangingListener() {
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                /* no beacons found or phone is not on top of a beacon */
                if(list.size() == 0 || state != 3)
                    return ;
                /* Get Nearest Beacon From Strongest RSSI */
                String nearestBeaconID = list.get(0).getMacAddress().toString();
                int strongestRssi = list.get(0).getRssi();
                for (Beacon b : list) {
                    int rssi = b.getRssi();
                    if (rssi > strongestRssi) {
                        strongestRssi = rssi;
                        nearestBeaconID = list.get(0).getMacAddress().toString();;
                    }
                }
                nearestBeacon = nearestBeaconID;
                /* when the nearest beacon changes a person must have moved so this is a new side, reset everything */
                nearestBeaconRSSIsArrayIndex = (nearestBeacon != nearestBeaconID)? 0 : nearestBeaconRSSIsArrayIndex;
                nearestBeaconRSSIs[nearestBeaconRSSIsArrayIndex++] = strongestRssi;
                /* got 20 values then let's calculate RSSI value */
                if(nearestBeaconRSSIsArrayIndex == 10){
                    nearestBeaconRSSIsArrayIndex = 0;
                    baseRSSI = getBaseRSSI();
                    addCoordinate((float) stepCount * 0.76f);
                    stateToggle.performClick();
                }
            }
        });
    }

    public int getBaseRSSI(){
        /* get average of RSSI values */
        double avg = 0.0;
        for(int i = 0; i < nearestBeaconRSSIs.length; i++){
            avg += nearestBeaconRSSIs[i];
        }
        avg = avg/nearestBeaconRSSIs.length ;
        /* get standard deviation of the RSSI values */
        double standardDeviation = 0;
        for(int i = 0; i<nearestBeaconRSSIs.length; i++){
            standardDeviation += Math.pow((nearestBeaconRSSIs[i] - avg), 2);
        }
        standardDeviation = Math.sqrt(standardDeviation / nearestBeaconRSSIs.length) ;
        /* exclude rssi values that are higher than the standard deviation */
        ArrayList<Integer> resultsArrayList = new ArrayList<Integer>();
        for(int i = 0, j = 0; i<nearestBeaconRSSIs.length; i++){
            if(Math.abs(nearestBeaconRSSIs[i] - avg) < standardDeviation  ){
                resultsArrayList.add(nearestBeaconRSSIs[i]);
            }
        }
        /* apply a low pass vilter to the non excluded values */
        double[] resultsArray = new double[resultsArrayList.size()];
        resultsArray[0] = resultsArrayList.get(0);
        for( int i=1; i<resultsArrayList.size(); i++){
            resultsArray[i] = 0.95*resultsArray[i - 1] + 0.05 * resultsArrayList.get(i);
        }

         avg = 0.0;
        for(int i = 0; i < resultsArray.length; i++){
            avg += resultsArray[i];
        }
        return (int)Math.floor(avg/resultsArray.length);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        /* Inflate the layout for this fragment */
        return inflater.inflate(R.layout.initial_view, container, false);
    }


    public void onSensorChanged(SensorEvent event) {
        Sensor sensor = event.sensor;
        switch(sensor.getType()){
            /* Gravity Sensor For Walking and Step Count */
            case Sensor.TYPE_GRAVITY:
                if(state == 1){
                    if(ignoreGravityIncrease){
                        waitingCountDown --;
                        ignoreGravityIncrease = (waitingCountDown <= 0)? false : true;
                    }
                    if( ((int)prevY - (int) event.values[1]) >= 1 &&  (prevY - event.values[1]) >= 0.5 &&!ignoreGravityIncrease){
                        stepCount++;
                        stepsView.setText("Step Count: " + stepCount);
                        waitingCountDown = 2;
                        ignoreGravityIncrease = true;
                    }
                }
                prevY = event.values[1];
                break;
            /* Gyroscope For Getting The Walking Direction */
            case Sensor.TYPE_GYROSCOPE:
                if(state == 0){
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
                                walkingDirectionView.setText("Direction: Right");
                            }
                            else if(walkingDirection == 2) {
                                walkingDirection = 3;
                                walkingDirectionView.setText("Direction: Left");
                            }
                            else if(walkingDirection == 3) {
                                walkingDirection = 1;
                                walkingDirectionView.setText("Direction: Forward");
                            }
                            else if(walkingDirection == 4) {
                                walkingDirection = 2;
                                walkingDirectionView.setText("Direction: Backward");
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
                                walkingDirectionView.setText("Direction: Left");
                            }
                            else if(walkingDirection == 2) {
                                walkingDirection = 4;
                                walkingDirectionView.setText("Direction: Right");
                            }
                            else if(walkingDirection == 3) {
                                walkingDirection = 2;
                                walkingDirectionView.setText("Direction: Backward");
                            }
                            else if(walkingDirection == 4) {
                                walkingDirection = 1;
                                walkingDirectionView.setText("Direction: Forward");
                            }
                        }
                    }
                }
                break;
        }
    }


    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    protected void addCoordinate(float distance){
        /* Get Previous Coordinate */
        Coordinate newCoordinate =(estimoteRoomCoordinates.size()>0)? new Coordinate(((Coordinate)estimoteRoomCoordinates.get(estimoteRoomCoordinates.size()-1).getLocation()).getFirst(),((Coordinate)estimoteRoomCoordinates.get(estimoteRoomCoordinates.size()-1).getLocation()).getSecond())
                                    : new Coordinate(0,0);
        /* Edit new Coordinate According to Walking Direction  */
        switch(walkingDirection){
            case 1: newCoordinate.setSecond(newCoordinate.getSecond() + distance);
                break;
            case 2: newCoordinate.setSecond(newCoordinate.getSecond() - distance);
                break;
            case 3: newCoordinate.setFirst(newCoordinate.getFirst() + distance);
                break;
            case 4: newCoordinate.setFirst(newCoordinate.getFirst() - distance);
                break;
        }
        RoomEstimote addedEstimote;
            addedEstimote = new RoomEstimote(nearestBeacon, baseRSSI, newCoordinate);
        tempView.setText(tempView.getText()+"\n New Pair: "+ "("+(String)addedEstimote.getBeaconID()+","+
                ((Coordinate)addedEstimote.getLocation()).getFirst()+","+
                ((Coordinate)addedEstimote.getLocation()).getSecond()+")");
        estimoteRoomCoordinates.add(estimoteRoomCoordinates.size(),addedEstimote);
    }


    protected void nextInstruction(String instruction){
        /* Change The Instruction Displayed */
        instructionsView.setText(instruction);
    }


    public void onStart() {
        super.onStart();

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

    public void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this, mSensorGravity);
        mSensorManager.unregisterListener(this, mSensorGyroscope);
    }

}
