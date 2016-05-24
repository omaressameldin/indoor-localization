package Fragments;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.oessa_000.countsteps.R;

import Classes.MyFragment;

/**
 * Created by oessa_000 on 5/12/2016.
 */
public class TestFragment extends MyFragment implements SensorEventListener {
    @Override
    public void connectBeaconManager() {

    }
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private SensorManager sensorManager;
    // sensor gravity
    private Sensor sensorGravity;
    private Sensor sensorMagnetic;
    private double bearing = 0;

    private TextView degree;
    private TextView acc;
    private TextView stepView;
    private TextView thresholdView;
    private SeekBar seek;
    private ToggleButton countToggle;
    private GeomagneticField geomagneticField;

    private int stepCount;
    private boolean toggle;
    private double prevY;
    private double prevZ;
    private double threshold;
    private boolean ignore;
    private int countdown;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        degree = (TextView) getActivity().findViewById((R.id.degree));
        acc = (TextView) getActivity().findViewById((R.id.accelerometer));
        thresholdView = (TextView) getActivity().findViewById((R.id.thresholdView));
        stepView = (TextView) getActivity().findViewById((R.id.stepView));
        countToggle = (ToggleButton) getActivity().findViewById(R.id.countToggle);
        seek = (SeekBar) getActivity().findViewById(R.id.seek);

        seek.setProgress(0);
        seek.incrementProgressBy(1);
        seek.setMax(80);
        // keep screen light on (wake lock light)

        implementListeners();
    }
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.test_view, container, false);
    }


    protected float[] lowPassFilter( float[] input, float[] output ) {
        if ( output == null ) return input;
        for ( int i=0; i<input.length; i++ ) {
            output[i] = output[i] + 1.0f * (input[i] - output[i]);
        }
        return output;
    }

    @Override
    public void onStart() {
        super.onStart();
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        sensorManager.registerListener(this, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onStop() {
        super.onStop();
        // remove listeners
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
    }



    public void onSensorChanged(SensorEvent event){
        boolean accelOrMagnetic = false;
        Sensor sensor = event.sensor;
        switch (sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                smoothed = lowPassFilter(event.values, gravity);
                gravity[0] = smoothed[0];
                gravity[1] = smoothed[1];
                gravity[2] = smoothed[2];
//                if (ignore) {
//                    countdown--;
//                    ignore = (countdown < 0) ? false : ignore;
//                } else
//                    countdown = 20;
                if (toggle && (Math.abs(prevY - gravity[1]) > threshold) ) {
                    stepCount++;
                    stepView.setText("Step Count: " + stepCount);
                    ignore = true;
                }
                prevY = gravity[1];
                prevZ = gravity[2];
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
    }


    public void implementListeners(){
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,boolean fromUser) {
                threshold = ((double)seek.getProgress()) * 0.01;
                thresholdView.setText("Threshold: "+ threshold);
            }
        });

        countToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle = !toggle ;
                if(toggle){
                    stepCount = 0;
                    countdown = 0;
                    ignore = true;
                    stepView.setText("Step Count: " + stepCount);
                }
            }
        });

    }

    private void updateTextDirection(double bearing) {
        int range = (int) (bearing / (360f / 16f));
        String dirTxt = "";

        if (range == 15 || range == 0)
            dirTxt = "N";
        if (range == 1 || range == 2)
            dirTxt = "NE";
        if (range == 3 || range == 4)
            dirTxt = "E";
        if (range == 5 || range == 6)
            dirTxt = "SE";
        if (range == 7 || range == 8)
            dirTxt = "S";
        if (range == 9 || range == 10)
            dirTxt = "SW";
        if (range == 11 || range == 12)
            dirTxt = "W";
        if (range == 13 || range == 14)
            dirTxt = "NW";

        degree.setText("" + ((int) bearing) + ((char) 176) + " "
                + dirTxt); // char 176 ) = degrees ...
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && accuracy == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            // manage fact that compass data are unreliable ...
            // toast ? display on screen ?
        }
    }
}
