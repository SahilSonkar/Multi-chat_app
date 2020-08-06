package com.example.finalprojectonfirebasechartapp.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectonfirebasechartapp.ChartActivity;
import com.example.finalprojectonfirebasechartapp.Models.ModelUser;
import com.example.finalprojectonfirebasechartapp.R;
import com.squareup.picasso.Picasso;

import java.util.List;

public class AdapterUsers extends RecyclerView.Adapter<AdapterUsers.MyHolder>{

    Context context;
    List<ModelUser> userList;

    public AdapterUsers(Context context, List<ModelUser> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //infalte layout (row_user.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_users, parent, false);
        return new MyHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {

        //get data
        final String userUid =userList.get(position).getUid();
        String userName=userList.get(position).getName();
        final String userEmail=userList.get(position).getEmail();
        String userImage =userList.get(position).getImage();
        //set data
        holder.mEmailUser.setText(userEmail);
        holder.mNameUser.setText(userName);
        try{
            Picasso.get().load(userImage)
                    .placeholder(R.drawable.ic_defaultuser)
                    .into(holder.mAvatarUser);
        }catch (Exception e){

        }


        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Click User from list to start charting/messaginf
                // by putting the uid of User
                // we are using that Uid to identify the user we are gonna chat
                Toast.makeText(context, ""+userEmail, Toast.LENGTH_SHORT).show();
                Intent intent =new Intent(context, ChartActivity.class);
                intent.putExtra("UserUid",userUid);
                context.startActivity(intent);
            }
        });



    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    //View holder
    class MyHolder extends RecyclerView.ViewHolder {

        ImageView mAvatarUser;
        TextView mNameUser , mEmailUser;
        public MyHolder(@NonNull View itemView) {
            super(itemView);

            //Init Views
            mAvatarUser =itemView.findViewById(R.id.AvatarUser);
            mNameUser =itemView.findViewById(R.id.NameUser);
            mEmailUser =itemView.findViewById(R.id.EmailUser);

        }
    }
}
