package Classes;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.oessa_000.countsteps.MainActivity;
import com.example.oessa_000.countsteps.R;

import java.util.ArrayList;
import java.util.HashMap;

import Fragments.RoomFragment;

import static com.example.oessa_000.countsteps.MainActivity.changeFragment;
import static com.example.oessa_000.countsteps.MainActivity.setRoom;

/**
 * Created by oessa_000 on 8/2/2016.
 */
public class RoomListAdapter extends RecyclerView.Adapter {
    /* Rooms Info Variables */
    ArrayList<HashMap<String,String>> data;
    /* Rooms Estimotes */
    ArrayList<ArrayList<HashMap<String,String>>> estimotes;
    /* HTTP Variable */
    HTTPRequest http;


    public RoomListAdapter(ArrayList<HashMap<String, String>> data){
        /*Initialize Data */
        this.data = data;
        /* Initialize Estimotes Array to match number of rooms Available */
        estimotes = new ArrayList<ArrayList<HashMap<String,String>>>();
        for(int i= 0 ;i<data.size(); i++)
            estimotes.add(new ArrayList<HashMap<String,String>>());
        /* Initialize HTTP variable */
        http = new HTTPRequest();
    }

    /* List item View Class */
    public static class RoomViewHolder extends RecyclerView.ViewHolder {
        /* Room Item Variable */
        RelativeLayout infoSectionView;
        TextView roomName;
        TextView roomDescription;
        TextView roomID;
        TextView estimoteCount;
        ImageButton deleteRoom;
        ImageButton editRoom;


        RoomViewHolder(View itemView) {
            super(itemView);
            /* get Items From layout */
            infoSectionView = (RelativeLayout)itemView.findViewById(R.id.infoSection);
            roomName = (TextView)itemView.findViewById(R.id.roomName);
            roomDescription = (TextView)itemView.findViewById(R.id.description);
            estimoteCount = (TextView)itemView.findViewById(R.id.estimotesCount);
            roomID = (TextView)itemView.findViewById(R.id.roomID);
            deleteRoom = (ImageButton)itemView.findViewById(R.id.deleteRoom);
            editRoom = (ImageButton)itemView.findViewById(R.id.editRoom);
        }
    }


        @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.room_list_item, parent, false);
            RoomViewHolder pvh = new RoomViewHolder(v);
            return pvh;
        }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        /* set Room Info */
        ((RoomViewHolder)holder).roomName.setText(Html.fromHtml("<b>Room Name:</b> " + data.get(position).get("roomName")));
        ((RoomViewHolder)holder).roomDescription.setText(data.get(position).get("roomDescription"));
        ((RoomViewHolder)holder).estimoteCount.setText(Html.fromHtml("<b>Estimotes Count:</b> " + data.get(position).get("estimotesCount")));
        ((RoomViewHolder)holder).roomID.setText(data.get(position).get("roomID"));
        buttonActions((RoomViewHolder)holder, position);
    }


    public void buttonActions(final RoomViewHolder holder, final int position){
        /* Info Section Click Listener */
        holder.infoSectionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                http.getRoomData(view, Integer.parseInt(data.get(position).get("roomID")), estimotes.get(position));
            }
        });
        /* deleteRoom Button Listener */
        holder.deleteRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                /* Alert to check if the user is sure he/she wants to delete the room */
                AlertDialog.Builder deleteAlertBuilder =  new AlertDialog.Builder(view.getContext());
                deleteAlertBuilder.setTitle("Delete "+data.get(position).get("roomName"));
                deleteAlertBuilder.setMessage("Are you sure you want to delete this Room?");
                deleteAlertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /*if the user is sure delete it from both the database and the UI */
                        http.deleteRoom((Activity)view.getContext(),Integer.parseInt(((RoomViewHolder)holder).roomID.getText().toString()));
                        delete(position);
                    }
                });
                deleteAlertBuilder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* If he/she cancels do nothing */
                    }
                });
                deleteAlertBuilder.setIcon(R.drawable.bin);
                AlertDialog deleteAlert = deleteAlertBuilder.create();
                deleteAlert.show();
                /* change alert buttons colors */
                deleteAlert.getButton(DialogInterface.BUTTON_NEGATIVE).setTextColor(view.getContext().getResources().getColor(R.color.colorAccent));
                deleteAlert.getButton(DialogInterface.BUTTON_POSITIVE).setTextColor(view.getContext().getResources().getColor(R.color.colorAccent));
            }
        });
        /* Edit Estimotes Button Listener */
        holder.editRoom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                /* Box to Edit Room Estimotes */
                AlertDialog.Builder editAlertBuilder =  new AlertDialog.Builder(view.getContext());
                editAlertBuilder.setTitle("Edit "+data.get(position).get("roomName"));
                editAlertBuilder.setPositiveButton("done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        /* If the user is sure he/she wants to edit the estimotes update database nad teh room has estimotes edit the databse */
                        if(estimotes.get(position).size() > 0)
                            http.editEstimotes(view, estimotes.get(position));
                    }
                })
                        .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                        /* If he/she cancels do nothing */
                            }
                        });
                editAlertBuilder.setIcon(R.drawable.edit);
                /* Set up a new estimotes info array */
                estimotes.set(position, new ArrayList<HashMap<String, String>>());
                /*get Estimotes from databse and finish setting up the alert dialog */
                http.getRoomEstimotes(view, Integer.parseInt(data.get(position).get("roomID")), editAlertBuilder, estimotes.get(position));
            }
        });

    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void delete(int position) {
       /*delete the room from the UI and update the position */
        data.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, getItemCount());
    }
}
