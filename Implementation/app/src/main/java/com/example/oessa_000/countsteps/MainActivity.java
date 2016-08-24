package com.example.oessa_000.countsteps;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.SystemRequirementsChecker;

import Classes.Coordinate;
import Classes.MyFragment;
import Classes.Room;
import Fragments.RoomsListFragment;

public class MainActivity extends AppCompatActivity {

    static Room room;
    static String fragmentTag;
    static BeaconManager beaconManager;
    private static Activity mainActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        beaconManager = new BeaconManager(getApplicationContext());
        mainActivity = this;
        changeFragment(this,new RoomsListFragment(),"RoomsListFragment");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void disconnectBeaconManager(){
        beaconManager.disconnect();
    }

    public static  void setRoom(Room r){ room = r; }
    public static Room getRoom(){
        return room ;
    }
    public static  void changeFragment(Activity a, android.app.Fragment fragment, String ft){
        fragmentTag = ft;
      a.getFragmentManager().beginTransaction().replace(R.id.fragment, fragment, ft).commit();
        if(fragment instanceof  MyFragment)
            ((MyFragment)fragment).connectBeaconManager();
        fragmentTag = ft;
    }
    public static String getFragmentTag(){
        return fragmentTag ;
    }
    public static BeaconManager getBeaconManager(){
        return beaconManager ;
    }
    public static void setRoomScaleFactor(Coordinate pixelDimensions){room.setScaleFactor(pixelDimensions);}

    protected void onStart() {
        super.onStart();
    }

    protected void onResume() {
        super.onResume();
        SystemRequirementsChecker.checkWithDefaultDialogs(this);
        android.app.Fragment f = (android.app.Fragment)getFragmentManager().findFragmentByTag(getFragmentTag());
        if(f instanceof  MyFragment)
            ((MyFragment)f).connectBeaconManager();
    }

    protected void onStop(){
        super.onStop();
        disconnectBeaconManager();
    }
    @Override
    public void onBackPressed(){
        if(fragmentTag.equals("RoomFragment") || fragmentTag.equals("InitialFragment") )
            changeFragment(this, new RoomsListFragment(), "RoomListFragment");
        else
            super.onBackPressed();

    }

    public static Activity getMainActivity(){
        return mainActivity;
    }
}
