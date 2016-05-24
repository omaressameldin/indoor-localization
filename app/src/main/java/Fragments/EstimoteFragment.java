package Fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.Utils;
import com.example.oessa_000.countsteps.R;

import java.util.List;
import java.util.UUID;

/**
 * Created by oessa_000 on 4/25/2016.
 */
public class EstimoteFragment extends Fragment {

    private BeaconManager beaconManager;


    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        beaconManager = new BeaconManager(getActivity().getApplicationContext());
        final TextView distanceView =  (TextView) getActivity().findViewById(R.id.estimoteDistance);
        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(new Region(
                        "monitored region",
                        UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"),
                        34355, 23519));
            }
        });

        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> list) {
                if (!list.isEmpty()) {
                    Beacon nearestBeacon = list.get(0);
                    String distance = "Distance to Estimote: " + Utils.computeAccuracy(nearestBeacon);
                    distanceView.setText(distance);

                }
            }
        });

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.estimote_view, container, false);

    }


    @Override
    public void onResume() {
        super.onResume();

        SystemRequirementsChecker.checkWithDefaultDialogs(getActivity());
    }



}
