package com.example.finalprojectonfirebasechartapp.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectonfirebasechartapp.Models.ModelChat;
import com.example.finalprojectonfirebasechartapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class adapterChat extends RecyclerView.Adapter<adapterChat.ViewHolder> {

    private static final int MSG_TYPE_LEFT = 0;
    private static final int MSG_TYPE_RIGHT =1;
    Context context;
    List<ModelChat> chatList;
    String imageUri;

//    Firebase tools
    FirebaseUser fUser;
    int index;


    public adapterChat(Context context, List<ModelChat> chatList, String imageUri) {
        this.context = context;
        this.chatList = chatList;
        this.imageUri = imageUri;
//        this.index =ind;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // inflate layout -- row_chat_left for reciever and row_chat_right for sender
        if(viewType == MSG_TYPE_RIGHT){
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_right, parent, false);
            return new ViewHolder(view);
        }else {
            View view = LayoutInflater.from(context).inflate(R.layout.row_chat_left, parent, false);
            return new ViewHolder(view);

        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {

//        final int position=positionI+index;
        //get data
        String message = chatList.get(position).getMessage();
        String timestamp =chatList.get(position).getTimestamp();

         //convert time stamp to dd/mm/yyyy hh:mm am/pm
        if(!timestamp.isEmpty()){
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(Long.parseLong(timestamp));
            String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa",cal).toString();
            holder.timestamp.setText(dateTime);
        }else{
            holder.timestamp.setText(timestamp);
        }
        //set data
        holder.message.setText(message);
        try{
            Picasso.get().load(imageUri).placeholder(R.drawable.ic_defaultuser).into(holder.mProfile);

        }catch (Exception e){
        }

        if(position==chatList.size()-1){
            if(chatList.get(position).isIsseen()){
                holder.isseenchat.setText("seen");
            }else {
                holder.isseenchat.setText("delivered");
            }
        }else{
            holder.isseenchat.setVisibility(View.GONE);
        }
        holder.messagLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Show delete message confirm dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Delete");
                builder.setMessage("Are you sure to delete this message");
                //set "Delete" button
                builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMessage(position);
                    }
                });
                //set "Cancel" button
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which){
                        dialog.dismiss();
                    }
                });
                builder.create().show();
            }
        });
    }
    private void deleteMessage(int position) {

//        Logic :--
//        Get the exact time of sent message
//        compare the time to all the message that are under
//        chat node and where both value mathes delete the message
        final String currentUid=FirebaseAuth.getInstance().getCurrentUser().getUid();

        String timeOfMessage = chatList.get(position).getTimestamp().trim();
        DatabaseReference dbref = FirebaseDatabase.getInstance().getReference("chats");
        Query query = dbref.orderByChild("timestamp").equalTo(timeOfMessage);

        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds: dataSnapshot.getChildren()){

                    //if you want to allow sender to only delete his message
                    // then compare currentUId to sender uid and give
                    // if they matches means sender trying to delete the message
                    if(ds.child("sender").getValue().equals(currentUid))
                    {
                        // we remove message and show the message
                        // " this message was deleted.." in place of that message

                        // 1 -- To remove message
                       // ds.getRef().removeValue();

                        //To add " this message was deleted.." in place of that message
                        HashMap<String, Object> hashMap =new HashMap<>();
                        hashMap.put("message" +
                                ""," this message was deleted..");
                        ds.getRef().updateChildren(hashMap);
                        Toast.makeText(context, "message deleted..", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(context, "You can delete only your message", Toast.LENGTH_SHORT).show();
                    }



                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    @Override
    public int getItemViewType(int position) {

        fUser = FirebaseAuth.getInstance().getCurrentUser();
        if(chatList.get(position).getSender().equals(fUser.getUid())){
            return MSG_TYPE_RIGHT;
        }else{
            return MSG_TYPE_LEFT;
        }

    }

    class ViewHolder extends RecyclerView.ViewHolder {

        ImageView mProfile;
        TextView message, timestamp, isseenchat;
        LinearLayout messagLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            mProfile = itemView.findViewById(R.id.profileInMessage);
            message =itemView.findViewById(R.id.messaheInMessage);
            timestamp =itemView.findViewById(R.id.timestampInMessage);
            isseenchat = itemView.findViewById(R.id.delivered);
            messagLayout = itemView.findViewById(R.id.messageLayout);

        }
    }
}
