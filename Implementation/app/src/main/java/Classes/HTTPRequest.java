package Classes;

import android.app.Fragment;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.oessa_000.countsteps.MainActivity;
import com.example.oessa_000.countsteps.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import Fragments.InitialFragment;
import Fragments.RoomFragment;
import Fragments.RoomsListFragment;

import static com.example.oessa_000.countsteps.MainActivity.changeFragment;
import static com.example.oessa_000.countsteps.MainActivity.getMainActivity;
import static com.example.oessa_000.countsteps.MainActivity.setRoom;

/**
 * Created by oessa_000 on 8/2/2016.
 */
public class HTTPRequest {

    /* add a new room to the databse */
    public  void addRoom(final Fragment f , String newRoomName, String newRoomDescription, final ArrayList<RoomEstimote> roomEstimotes) {
        /* server url */
        String url = "https://localization-omaressameldin1.c9users.io/rooms/create";
        final ProgressDialog progress = new ProgressDialog(f.getActivity());
        /* add a loader till the adding process is over */
        progress.setTitle("Adding");
        progress.setMessage("Wait while adding the room...");
        progress.show();
        /* bundle the values that are gonna be sent to the databse */
        Map<String,String> roomName = new HashMap<String,String>();
        roomName.put("room_name",newRoomName);
        roomName.put("description",newRoomDescription);
        Map<String, Map<String,String>> room = new HashMap<String,Map<String,String>>();
        room.put("room",roomName);
        /* start a server request using the volley api */
        RequestQueue queue = Volley.newRequestQueue(f.getActivity());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, new JSONObject(room),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        int roomId = 0;
                        try {
                            /* if the room is added add the estimotes correlated to the room */
                            if(response.has("status")){
                                roomId = (Integer) response.get("room_id");
                                Boolean noErrors = true;
                                 addEstimotes(f, roomId, roomEstimotes, progress );
                            }
                            /* if not then show a snackbar with the error */
                            else{
                                    progress.dismiss();
                                    Snackbar.make(f.getView(), "error: " + response.get("error"), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }

                        } catch (JSONException e) {
                            progress.dismiss();
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* if the server could not be reached show a snackbard with an error in server */
                Snackbar.make(f.getView(), "error: Couldn't connect to server!" , Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    Log.e("error",new String(error.networkResponse.data,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    /* add estimotes for a room */
    public void addEstimotes(final Fragment f, final int roomId, ArrayList<RoomEstimote> roomEstimotes, final ProgressDialog progress){
      /* bundle the data that are going to be sent to the server */
        Map<String,ArrayList<JSONObject>> estimotes = new HashMap<String,ArrayList<JSONObject>>();
        ArrayList<JSONObject> estimotesArrayList = new ArrayList<JSONObject>();
        for(RoomEstimote re : roomEstimotes){
            Map<String,String> estimoteInfo = new HashMap<String,String>();
            estimoteInfo.put("room_id",roomId+"");
            estimoteInfo.put("mac",re.getBeaconID());
            estimoteInfo.put("base_rssi",re.getBaseRSSI()+"");
            estimoteInfo.put("xpos",re.getLocation().getFirst()+"");
            estimoteInfo.put("ypos",re.getLocation().getSecond()+"");
            estimotesArrayList.add(new JSONObject(estimoteInfo));
        }
        estimotes.put("estimotes", estimotesArrayList);
        /* start a server request using the volley api */
        RequestQueue queue = Volley.newRequestQueue(f.getActivity());
            JsonObjectRequest addRoomEstimotesRequest = new JsonObjectRequest(Request.Method.POST, "https://localization-omaressameldin1.c9users.io/estimotes/createroomestimotes", new JSONObject(estimotes),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            /* if request succeeds switch to the RoomListFragment and dismiss the progress bar*/
                            if(response.has("status")){
                                progress.dismiss();
                                ((MainActivity)f.getActivity()).changeFragment(f.getActivity(), new RoomsListFragment(),"RoomListFragment");
                            }
                            /* if not show a snackbar with error and delete the created room */
                            else{
                                progress.dismiss();
                                Snackbar.make(f.getView(), "error: " + response.get("error"), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                deleteRoom(f.getActivity(), roomId);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* if server could not be reached try to delete room and show error with inability to connect to server */
                Snackbar.make(f.getView(), "error: Couldn't connect to server!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                deleteRoom(f.getActivity(), roomId);
                    progress.dismiss();
                try {
                    Log.e("error",  new String(error.networkResponse.data,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
            queue.add(addRoomEstimotesRequest);
    }

    /* get all rooms on server for RoomListFragment */
    public void getAllRooms(Fragment f, final RecyclerView allRooms, final TextView errorView, final SwipeRefreshLayout swipeRefreshLayout){
        String url = "https://localization-omaressameldin1.c9users.io/rooms";
        final RequestQueue queue = Volley.newRequestQueue(f.getActivity());
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        /* if response came with no rooms show that no rooms exist */
                        if(response.length() == 0){
                            errorView.setText("Error: " + "No Rooms Available!" );
                            errorView.setVisibility(View.VISIBLE);
                            allRooms.setVisibility(View.INVISIBLE);

                        }
                        /* else bundle data into hashmap and set a roomlistadapter with the rooms */
                        else{

                            try{
                                ArrayList<HashMap<String,String>> rooms = new  ArrayList<HashMap<String,String>>();
                                for(int i = 0; i<response.length(); i++){
                                    HashMap<String,String> hm = new HashMap<String,String>();
                                    hm.put("roomName",((JSONObject)response.get(i)).getString("room_name"));
                                    hm.put("roomDescription",((JSONObject)response.get(i)).getString("description"));
                                    hm.put("roomID",((JSONObject)response.get(i)).getString("room_id"));
                                    hm.put("estimotesCount",((JSONObject)response.get(i)).getString("estimotes_count"));
                                    rooms.add(hm);
                                }
                                allRooms.setAdapter(new RoomListAdapter(rooms));
                                errorView.setVisibility(View.INVISIBLE);
                                allRooms.setVisibility(View.VISIBLE);
                            }
                            catch(JSONException e){}

                        }
                        swipeRefreshLayout.setRefreshing(false);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* if server could not be reached show error message */
                errorView.setText("Error: Server Error"  );
                errorView.setVisibility(View.VISIBLE);
                allRooms.setVisibility(View.INVISIBLE);
                swipeRefreshLayout.setRefreshing(false);

            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }
    /* deletes a room */
    public void deleteRoom(Activity a, int roomID){
        /* just send a request with roomid for deletion */
        RequestQueue queue = Volley.newRequestQueue(a);
        String url = "https://localization-omaressameldin1.c9users.io/rooms/destroy/"+roomID;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    /* get a specific room's estimotes info */
    public void getRoomEstimotes(final View v, int roomID, final AlertDialog.Builder adb, final ArrayList<HashMap<String,String>> estimotes){
        String url = "https://localization-omaressameldin1.c9users.io/estimotes/roomestimotes/"+roomID;
        /* set a progress bar to wait for server's response */
        final ProgressDialog progress = new ProgressDialog(v.getContext());
        progress.setTitle("Loading");
        progress.setMessage("Wait while getting estimotes...");
        progress.show();
        /* start a server request using the volley api */
        final RequestQueue queue = Volley.newRequestQueue(v.getContext());
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        /* dismiss progress bar */
                        progress.dismiss();
                        /* if room has does not have estimotes show message */
                        if(response.length() == 0){
                            adb.setMessage("No Estimtotes in this room!");
                        }
                        /* else bundle estimotes' info in hashmap and start an EstimoteListAdapter and add the view to the alert dialog box */
                        else{
                            Log.e("Success: ", "estimotes exist" );

                            try{
                                for(int i = 0; i<response.length(); i++){
                                    HashMap<String,String> hm = new HashMap<String,String>();
                                    hm.put("mac",((JSONObject)response.get(i)).getString("mac"));
                                    hm.put("xpos",((JSONObject)response.get(i)).getString("xpos"));
                                    hm.put("ypos",((JSONObject)response.get(i)).getString("ypos"));
                                    hm.put("base_rssi",((JSONObject)response.get(i)).getString("base_rssi"));
                                    hm.put("id",((JSONObject)response.get(i)).getString("id"));
                                    estimotes.add(hm);
                                }
                                RecyclerView allEstimotes = new RecyclerView(v.getContext());
                                LinearLayoutManager llm = new LinearLayoutManager(v.getContext());
                                allEstimotes.setLayoutManager(llm);
                                allEstimotes.setAdapter( new EstimotesListAdapter(estimotes));
                                adb.setView(allEstimotes);
                            }
                            catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                        AlertDialog ad = adb.create();
                        ad.show();
                        /* change alert dialog box button colors and reset flags to show keyboard */
                        ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(v.getContext().getResources().getColor(R.color.colorAccent));
                        ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(v.getContext().getResources().getColor(R.color.colorAccent));
                        ad.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* if there is a server error remove progress bar and show error message in alert dialog box */
                progress.dismiss();
                adb.setMessage("Server Error!");
                AlertDialog ad = adb.create();
                ad.show();
                ad.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(v.getContext().getResources().getColor(R.color.colorAccent));
                ad.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(v.getContext().getResources().getColor(R.color.colorAccent));
                ad.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
        });
// Add the request to the RequestQueue.
        queue.add(jsonArrayRequest);
    }

    /* update estimotes with new data */
    public void editEstimotes(final View v, final ArrayList<HashMap<String,String>> estimotes){
        /* set a progress bar while waiting for server response */
        final ProgressDialog progress = new ProgressDialog(v.getContext());
        progress.setTitle("Updating");
        progress.setMessage("Wait while updating estimotes...");
        progress.show();
        /* send a volley server request for each estimote */
        for(int i= 0; i<estimotes.size(); i++){
            RequestQueue queue = Volley.newRequestQueue(v.getContext());
            final int finalI = i;
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://localization-omaressameldin1.c9users.io/estimotes/update", new JSONObject(estimotes.get(i)),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            /* if error response show snackbar with error */
                            if (!response.has("status")) {
                                try {
                                    Snackbar.make(v, "error: " + response.get("error"), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                } catch (JSONException e) {
                                    Snackbar.make(v, "error: doesnt exist!", Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();                            }
                            }
                            else{
                            /* if final request dismiss progress bar */
                            }
                            if(finalI == estimotes.size() - 1)
                                progress.dismiss();
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    /* if server error show error message */
                    Snackbar.make(v, "error: Couldn't connect to server!", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    if(finalI == estimotes.size() - 1)
                        progress.dismiss();
                    try {
                        Log.e("error", new String(error.networkResponse.data,"UTF-8"));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Content-Type", "application/json; charset=utf-8");
                    return headers;
                }
            };
            queue.add(jsonObjectRequest);
        }
    }

    /* check if there is a room with same name */
    public void checkIfRoomNameExists(final View v, final String roomName, final String roomDescription){
        final ProgressDialog progress = new ProgressDialog(v.getContext());
        /* validating new room name */
        progress.setTitle("Validating");
        progress.setMessage("Wait while validating the room name...");
        progress.show();
        final Map<String, Map<String,String>> room = new HashMap<String,Map<String,String>>();
        Map<String,String> roomInfo = new HashMap<String,String>();
        /* bundle data to be sent to server */
        roomInfo.put("room_name",roomName);
        roomInfo.put("description",roomDescription);
        room.put("room", roomInfo);
        /* start a new volley request */
        RequestQueue queue = Volley.newRequestQueue(v.getContext());
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, "https://localization-omaressameldin1.c9users.io/rooms/exists", new JSONObject(room),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        /* if error resposne show error message */
                        if (!response.has("status")) {
                            try {
                                Snackbar.make(v, "error: " + response.get("error"), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            } catch (JSONException e) {
                           }
                        }
                        /* else change fromgment to InitialFragment to start drawing a room */
                        else{
                            ((MainActivity)v.getContext()).changeFragment((Activity) v.getContext(), new InitialFragment(roomName, roomDescription), "InitialFragment");
                        }
                        progress.dismiss();
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                /* if server error show error message */
                Snackbar.make(v, "error: Couldn't connect to server!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                try {
                    Log.e("error", new String(error.networkResponse.data,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                progress.dismiss();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(jsonObjectRequest);
    }

    /* get rroom data for RoomFragment */
    public void getRoomData(final View v, final int roomID, final ArrayList<HashMap<String,String>> estimotes){
        String url = "https://localization-omaressameldin1.c9users.io/estimotes/roomestimotes/"+roomID;
        /* show progress bar while gettign data from server */
        final ProgressDialog progress = new ProgressDialog(v.getContext());
        progress.setTitle("Setting up");
        progress.setMessage("Wait while Setting up room...");
        progress.show();
        /* start new server request using volley api */
        final RequestQueue queue = Volley.newRequestQueue(v.getContext());
        final JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url,null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        progress.dismiss();
                        /* if room has no estimtoes show snackbar with error message */
                        if(response.length() == 0){
                            Snackbar.make(v, "error: " + "This room has no estimoes!", Snackbar.LENGTH_LONG)
                                    .setAction("Action", null).show();
                        }
                        /* else put response in hasmap */
                        else{
                            try{
                                for(int i = 0; i<response.length(); i++){
                                    HashMap<String,String> hm = new HashMap<String,String>();
                                    hm.put("mac",((JSONObject)response.get(i)).getString("mac"));
                                    hm.put("xpos",((JSONObject)response.get(i)).getString("xpos"));
                                    hm.put("ypos",((JSONObject)response.get(i)).getString("ypos"));
                                    hm.put("base_rssi",((JSONObject)response.get(i)).getString("base_rssi"));
                                    hm.put("id",((JSONObject)response.get(i)).getString("id"));
                                    estimotes.add(hm);
                                }
                                /* create a new array of RoomEstimotes with the data in hashmap then set a new room with the array and roomID then change fragment to room fragment*/
                                ArrayList<RoomEstimote> roomEstimotes = new ArrayList<RoomEstimote>();
                                for(HashMap<String,String> e: estimotes){
                                    RoomEstimote re = new RoomEstimote(e.get("mac"), Integer.parseInt(e.get("base_rssi")), new Coordinate(Float.parseFloat(e.get("xpos")),Float.parseFloat(e.get("ypos"))));
                                    roomEstimotes.add(re);
                                }
                                setRoom(new Room(roomEstimotes, roomID));
                                changeFragment(((MainActivity)v.getContext()), new RoomFragment(1),"RoomFragment");
                            }
                            catch(JSONException e){
                                e.printStackTrace();
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progress.dismiss();
                Snackbar.make(v, "error: " + "Server error", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        queue.add(jsonArrayRequest);
    }

    /* add a new fingerprint to database */
    public void addFingerPrint(final Fragment f, ArrayList<HashMap<String,String>> accessPoints, Coordinate location, int roomID){
        /* bundle info to send to server */
        HashMap<String,Float> locationHash = new HashMap<String,Float>();
        locationHash.put("xpos",location.getFirst());
        locationHash.put("ypos",location.getSecond());
        HashMap<String,Object> fingerprint= new  HashMap<String,Object>  ();
            fingerprint.put("accesspoints", accessPoints);
            fingerprint.put("location", new JSONObject(locationHash));
            fingerprint.put("room_id", roomID);
        /* start a new server request using volley api */
        RequestQueue queue = Volley.newRequestQueue(f.getActivity());
        JsonObjectRequest addRoomEstimotesRequest = new JsonObjectRequest(Request.Method.POST, "https://localization-omaressameldin1.c9users.io/fingerprints/create", new JSONObject(fingerprint),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                            if(response.has("status")){
                                try {
                                    Log.e("sucess",response.get("status").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            else{
                                try {
                                    Log.e("error",response.get("error").toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    Log.e("FingerprintERROR",  new String(error.networkResponse.data,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(addRoomEstimotesRequest);
    }

    /* get a location using fingerprints saved in datanse */
    public void getFingerPrintLocation(final Fragment f, ArrayList<HashMap<String,String>> accessPoints, int roomID){
        /* bundle info to send to database */
        HashMap<String,Object> infoToFindFingerPrint= new  HashMap<String,Object>  ();
        infoToFindFingerPrint.put("accesspoints", accessPoints);
        infoToFindFingerPrint.put("room_id", roomID);
        /* start a new server request using volley api */
        RequestQueue queue = Volley.newRequestQueue(f.getActivity());
        JsonObjectRequest addRoomEstimotesRequest = new JsonObjectRequest(Request.Method.POST, "https://localization-omaressameldin1.c9users.io/fingerprints/findlocation", new JSONObject(infoToFindFingerPrint),
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            ((RoomFragment)f).setLocation(new Coordinate(Float.parseFloat(response.get("xpos").toString()), Float.parseFloat(response.get("ypos").toString())));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                try {
                    Log.e("FingerprintERROR",  new String(error.networkResponse.data,"UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                Toast.makeText( getMainActivity(), "ServerError",
                        Toast.LENGTH_SHORT).show();
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/json; charset=utf-8");
                return headers;
            }
        };
        queue.add(addRoomEstimotesRequest);
    }
}
