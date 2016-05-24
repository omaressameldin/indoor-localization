package Fragments;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.util.Pair;
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

import static com.example.oessa_000.countsteps.MainActivity.changeFragment;
import static com.example.oessa_000.countsteps.MainActivity.getBeaconManager;
import static com.example.oessa_000.countsteps.MainActivity.setRoom;

/**
 * Created by oessa_000 on 3/31/2016.
 */
public class InitialFragment extends MyFragment implements SensorEventListener {
    private int toggleState = 2;
    private int stepCount = 0;
    private double prevY;
    private double prevZ;
    private boolean ignore;
    private int countdown;
    private ArrayList<Pair> estimoteCoordinates = new ArrayList<Pair>();

    private SensorManager mSensorManager;
    private Sensor mSensorGravity;
    private Sensor mSensorMagnetic;

    private ToggleButton countToggle ;

    private float[] degrees;
    private int degreeArrayIndex;
    private ArrayList<Integer> direction;
    private String nearestBeacon;
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    private double angle = 0;
    private GeomagneticField geomagneticField;

    private TextView degree;
    private TextView steps;
    private FloatingActionButton undo;
    private FloatingActionButton done;
    private TextView compassData;
    private TextView temp;
    private TextView instructions;

    public InitialFragment(){}

    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);


        countToggle = (ToggleButton) getActivity().findViewById(R.id.countToggle);
        steps = (TextView) getActivity().findViewById((R.id.stepView));
        undo = (FloatingActionButton) getActivity().findViewById(R.id.undo);
        done = (FloatingActionButton) getActivity().findViewById(R.id.done);
        compassData = (TextView) getActivity().findViewById((R.id.compassData));
        temp = (TextView)getActivity().findViewById((R.id.tempview));
        instructions = (TextView) getActivity().findViewById((R.id.instructions));

        degrees = new float[5];
        degreeArrayIndex = 0;
        direction = new ArrayList();

        buttonsActions();
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
                if(list.size() == 0)
                    return ;
                String nearestBeaconID = list.get(0).getMajor() + "," + list.get(0).getMinor();
                double strongestRssi = list.get(0).getRssi();
                Log.e("fragment","initial Fragment");
                for (Beacon b : list) {
                    double rssi = b.getRssi();
                    if (rssi > strongestRssi) {
                        strongestRssi = rssi;
                        nearestBeaconID = b.getMajor() + "," + b.getMinor();
                    }
                }
                nearestBeacon = nearestBeaconID;
            }

        });
    }

    public void buttonsActions(){
        countToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleState = (toggleState == 3)? 0 : toggleState + 1;
                if (toggleState == 0) {
                    countToggle.setText("I am Walking ");
                    nextInstruction("now start walking  then press the 'I am walking button' or " +
                            "press the green button and your room will magically appear :) ");

                } else if(toggleState == 1) {
                    countToggle.setText("Stop Walking");
                    stepCount = 0;
                    countdown = 5;
                    ignore = true;
                    steps.setText("Step Count: " + stepCount);
                    nextInstruction("Press the 'Stop Walking' button when you stop walking");
                }
                else  if(toggleState == 2){
                    countToggle.setText("Calibrate");
                    nextInstruction("Now put the phone on top of the beacon and press the 'Calibrate' button");
                }
                else{
                    addCoordinate((float) stepCount * 0.8f);
                    countToggle.performClick();
                    Snackbar.make(view, "stepCount: " + stepCount + " steps", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });

        done .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleState !=2) {
                    if (estimoteCoordinates.size() > 3) {
                        setRoom(new Room(estimoteCoordinates));
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

        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleState !=2) {
                    if (estimoteCoordinates.size() > 1) {
                        estimoteCoordinates.remove(estimoteCoordinates.size() - 1);
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

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.initial_view, container, false);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        boolean accelOrMagnetic = false;
        Sensor sensor = event.sensor;
        switch(sensor.getType()){
            case Sensor.TYPE_ACCELEROMETER:
                smoothed = lowPassFilter(event.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];
                if(ignore) {
                    countdown--;
                    ignore = (countdown < 0)? false : ignore;
                }
                else
                    countdown = 22;
                if(toggleState == 1 && (Math.abs(prevY - gravity[1]) > 0.6) && !ignore){
                    stepCount++;
                    steps.setText("Step Count: " + stepCount);
                    ignore = true;
                }
                prevY = gravity[1];
                accelOrMagnetic = true;
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                smoothed = lowPassFilter(event.values, geomagnetic);
                geomagnetic[0] = smoothed[0];
                geomagnetic[1] = smoothed[1];
                geomagnetic[2] = smoothed[2];
                accelOrMagnetic = true;
                break;
        }

        if(accelOrMagnetic && toggleState == 1 ){
            // get rotation matrix to get gravity and magnetic data
            SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
            // get angle to target
            SensorManager.getOrientation(rotation, orientation);
            // east degrees of true North
            angle = orientation[0];
            // convert from radians to degrees
            angle = Math.toDegrees(angle);

            // fix difference between true North and magnetical North
            if (geomagneticField != null) {
                angle += geomagneticField.getDeclination();
            }

            // angle must be in 0-360
            if (angle < 0) {
                angle += 360.0;
            }
            degrees[degreeArrayIndex] = Math.round(angle);
            degreeArrayIndex++;
            if (degreeArrayIndex == 5)
                direction.add(direction.size(),getDirection());
            compassData.setText("Degree: "+ Math.round(angle)+"\nDirection: "+ direction);
        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }





    protected int getDirection(){
        float meanDegree = 0f;
        for(int i =0; i < degrees.length; i++){
            meanDegree += degrees[i];
        }
        meanDegree /=5 ;
        degreeArrayIndex = 0 ;
        if( (meanDegree >= 315 && meanDegree <=360) ||( meanDegree >= 0 && meanDegree <= 45))
            return 1;//forward
        else if(meanDegree >= 135 && meanDegree <= 225)
            return 2;//backward
        else if(meanDegree >= 225 && meanDegree <= 315)
            return 3;//left
        else if(meanDegree >= 45 && meanDegree <= 135)
            return 4;//right

        return 0;
    }

    protected void addCoordinate(float distance){
        Coordinate newCoordinate =(estimoteCoordinates.size()>0)? new Coordinate(((Coordinate)estimoteCoordinates.get(estimoteCoordinates.size()-1).second).getFirst(),((Coordinate)estimoteCoordinates.get(estimoteCoordinates.size()-1).second).getSecond())
                                    : new Coordinate(0,0);
        degreeArrayIndex = 0;
        nextInstruction("please stand still for a second now");
        int avgDirection = (estimoteCoordinates.size()>0)? calculateDirection() : 0;
            switch(avgDirection){
                case 1: newCoordinate.setFirst(newCoordinate.getFirst() + distance);
                    break;
                case 2: newCoordinate.setFirst(newCoordinate.getFirst() - distance);
                    break;
                case 3: newCoordinate.setSecond(newCoordinate.getSecond() - distance);
                    break;
                case 4: newCoordinate.setSecond(newCoordinate.getSecond() + distance);
                    break;
            }
        Pair addedPair;
            addedPair = new Pair(nearestBeacon, newCoordinate);
        temp.setText(temp.getText()+"\n New Pair: "+ "("+(String)addedPair.first+","+
                ((Coordinate)addedPair.second).getFirst()+","+
                ((Coordinate)addedPair.second).getSecond()+")");
        Log.e("AVGDIRECTION", avgDirection+"");
        estimoteCoordinates.add(estimoteCoordinates.size(),addedPair);

    }

    protected int calculateDirection(){
        int count1s = 0;
        int count2s = 0;
        int count3s = 0;
        int count4s = 0;
        for(int d : direction){
            switch(d){
                case 1: count1s ++; break;
                case 2: count2s ++; break;
                case 3: count3s ++; break;
                case 4: count4s ++; break;
            }
        }
        direction.clear();
        return (count1s > count2s)? ((count1s > count3s)? ((count1s > count4s)? 1 : 4 ) : ((count3s > count4s)? 3 : 4) ) :
                ((count2s > count3s)? ((count2s > count4s)? 2 : 4 ) : ((count3s > count4s)? 3 : 4) ) ;
    }

    protected void nextInstruction(String instruction){
        instructions.setText(instruction);
    }



    public void onStart() {
        super.onStart();

    }

    public void onResume() {
        super.onResume();
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mSensorGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        mSensorManager.registerListener(this, mSensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mSensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    public void onStop(){
        super.onStop();
        mSensorManager.unregisterListener(this, mSensorGravity);
        mSensorManager.unregisterListener(this, mSensorMagnetic);
    }

}
