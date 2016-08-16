package Fragments;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ListFragment;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.oessa_000.countsteps.MainActivity;
import com.example.oessa_000.countsteps.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



import Classes.HTTPRequest;



/**
 * Created by oessa_000 on 8/1/2016.
 */
public class RoomsListFragment extends Fragment {

    RecyclerView allRooms;
    TextView errorView;
    HTTPRequest http;
    SwipeRefreshLayout swipeRefreshLayout;
    FloatingActionButton addRoomButton;
    @TargetApi(Build.VERSION_CODES.M)
    public void onActivityCreated(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        allRooms = (RecyclerView)getActivity().findViewById(R.id.roomList);
        swipeRefreshLayout = (SwipeRefreshLayout)getActivity().findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setProgressViewOffset(false, 0,200);
        swipeRefreshLayout.setColorSchemeResources(R.color.blueIvy, R.color.orange, R.color.colorAccent);
        errorView = (TextView)getActivity().findViewById(R.id.error);
        addRoomButton = (FloatingActionButton)getActivity().findViewById(R.id.addRoom);
        fabAction();
        LinearLayoutManager llm = new LinearLayoutManager(this.getContext());
        allRooms.setLayoutManager(llm);
        http = new HTTPRequest();
        http.getAllRooms(this, allRooms, errorView, swipeRefreshLayout);
        swipeRefresh(this);
    }

    public void fabAction(){
        addRoomButton.setVisibility(View.VISIBLE);
        addRoomButton .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                AlertDialog.Builder addRoomAlertBuilder =  new AlertDialog.Builder(view.getContext());
                final View dialogView = ((Activity)view.getContext()).getLayoutInflater().inflate(R.layout.new_room_form, null);
                addRoomAlertBuilder.setView(dialogView);
                addRoomAlertBuilder.setTitle("Add New Room");
                addRoomAlertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String roomName = ((EditText)dialogView.findViewById(R.id.roomName)).getText().toString();
                        String roomDescription = ((EditText)dialogView.findViewById(R.id.roomDescription)).getText().toString();
                        http.checkIfRoomNameExists(view,roomName, roomDescription );
                    }
                });
                addRoomAlertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                addRoomAlertBuilder.setIcon(R.drawable.door);
                AlertDialog addRoomAlert = addRoomAlertBuilder.create();
                addRoomAlert.show();
                addRoomAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(view.getContext().getResources().getColor(R.color.colorAccent));
                addRoomAlert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(view.getContext().getResources().getColor(R.color.colorAccent));
                addRoomAlert.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);

            }
        });
    }





    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.room_list_view, container, false);
    }
    public void swipeRefresh(final RoomsListFragment rlf) {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                http.getAllRooms(rlf, allRooms, errorView, swipeRefreshLayout);

            }
        });
    }

}
