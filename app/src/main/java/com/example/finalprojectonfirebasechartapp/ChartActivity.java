package com.example.finalprojectonfirebasechartapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectonfirebasechartapp.Models.ModelChat;
import com.example.finalprojectonfirebasechartapp.Models.ModelUser;
import com.example.finalprojectonfirebasechartapp.adapters.adapterChat;
import com.example.finalprojectonfirebasechartapp.notification.APIService;
import com.example.finalprojectonfirebasechartapp.notification.Client;
import com.example.finalprojectonfirebasechartapp.notification.Data;
import com.example.finalprojectonfirebasechartapp.notification.Response;
import com.example.finalprojectonfirebasechartapp.notification.Sender;
import com.example.finalprojectonfirebasechartapp.notification.Token;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;


public class ChartActivity extends AppCompatActivity {

//    View from xml
    Toolbar toolbar;
    ImageButton send;
    EditText message;
    RecyclerView recyclerViewChat;
    TextView nameChat,userStateChat;
    ImageView profileChat;

    //Firebase tools
    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference usersDbRef;


    //for checking if user has seen message or not
    ValueEventListener seenListener;
    DatabaseReference userRefForSeen;


    List<ModelChat> chatList;
    List<ModelChat> chatListPost;
    adapterChat adapterChat;


    APIService apiService;
    boolean notify=false;

    String hisUid;
    String myUid;
    String hisImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);


        profileChat =findViewById(R.id.profileChat);
        nameChat =findViewById(R.id.nameChat);
        userStateChat =findViewById(R.id.UserStateChat);
        send =findViewById(R.id.sendChat);
        message =findViewById(R.id.messagechat);
        recyclerViewChat =findViewById(R.id.recyclerViewChat);
        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("toolbar");

//        Create API service
        apiService= Client.getRetrofit("https://fcm.googleapis.com/").create(APIService.class);

        //Linearlayout for recyclerview
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        //recyclerViewProp
        recyclerViewChat.setHasFixedSize(true);
        recyclerViewChat.setLayoutManager(linearLayoutManager);

        chatListPost =new ArrayList<>();

        //Firebase Init
        firebaseAuth =FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersDbRef=firebaseDatabase.getReference("Users");

        // On Clicking user List from UsersList fragment ,we have pased Uid of User
        // to get his image ,name, email  in chat activity
        // to start chating
        Intent intent = getIntent();
        hisUid = intent.getStringExtra("UserUid");

        //search user to get that user info
        Query query =usersDbRef.orderByChild("uid").equalTo(hisUid);
        //get users image and name
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //check until required info is recieved
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    //get data
                    String name =""+ ds.child("name").getValue();
                    hisImage =""+ ds.child("image").getValue();
                    String onlinestatus =""+ ds.child("onlineStatus").getValue();
                    String Typingstatus =""+ ds.child("typing").getValue();
                    if(Typingstatus.equals(myUid))
                    {
                        userStateChat.setText("typing..");

                    }else
                    {
                        if(onlinestatus.trim().equals("online")){
                            userStateChat.setText(onlinestatus);
                        }else {
                            if(!onlinestatus.isEmpty()) {
                                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                                cal.setTimeInMillis(Long.parseLong(onlinestatus));
                                String dateTime = DateFormat.format("dd/MM/yyyy hh:mm aa", cal).toString();
                                userStateChat.setText("Last seen at "+dateTime);
                            }

                        }
                    }
                    nameChat.setText(name);
                    try {
                        // set recieved image into toolbar
                        Picasso.get().load(hisImage).placeholder(R.drawable.ic_defaultuser).into(profileChat);
                    }catch (Exception e){
                        //if there is error in recieving in image then set default image
                        Picasso.get().load(R.drawable.ic_defaultuser).into(profileChat);
                    }

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //send button
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                notify=true;
                //get text from edit text
                String text = message.getText().toString().trim();
                if(TextUtils.isEmpty(text)){
                    //text is empty
                    Toast.makeText(ChartActivity.this, "text can not be empty", Toast.LENGTH_SHORT).show();
                }else {
                    //text is not empty
                    sendMessage(text);
                }
                //Reset Edit Text
                message.setText("") ;
            }
        });

        readMessages();
        seenMessage();
        //check Eddit text Change Listener
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().trim().length()==0)
                {
                    checkTypigStatus("none");
                }else {
                    checkTypigStatus(myUid);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }




    private void readMessages() {

        chatList = new ArrayList<>();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference("chats");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                chatList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    ModelChat modelChat = ds.getValue(ModelChat.class);
                    if( modelChat.getReciever().equals(myUid) && modelChat.getSender().equals(hisUid) ||
                            modelChat.getReciever().equals(hisUid) && modelChat.getSender().equals(myUid)) {
                        chatList.add(modelChat);
                    }
                    //adapter
                    adapterChat = new adapterChat(ChartActivity.this,chatList,hisImage) ;
                    adapterChat.notifyDataSetChanged();
                    //set adapter to recyclerView
                    recyclerViewChat.setAdapter(adapterChat);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
    private void sendMessage(String text) {


        DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();
        Long tsLong = System.currentTimeMillis();
        final String ts = tsLong.toString();
//        String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Timestamp(System.currentTimeMillis()));
        //String timeStamp = new SimpleDateFormat("yyyy.MM.dd.HH.mm.ss").format(new Date())+"";
        HashMap<String, Object> hashMap =new HashMap<>();
        hashMap.put("sender",myUid);
        hashMap.put("reciever",hisUid);
        hashMap.put("message",text);
        hashMap.put("timestamp",ts);
        hashMap.put("isSeen",false);
        databaseReference.child("chats").push().setValue(hashMap);


        final String msg= text;
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ModelUser modelUser = snapshot.getValue(ModelUser.class);
                if(notify){
                    sendNotification(hisUid,modelUser.getName(),msg);
                }
                notify= false;

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendNotification(final String hisUid, String name, final String msg) {

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Token");
        Query query = ref.orderByKey().equalTo(hisUid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()){
                    Token token= ds.getValue(Token.class);
                    Data data = new Data(myUid,"name: "+msg,"New Message",hisUid,R.drawable.ic_defaultuser);

                    Sender sender = new Sender(data,token.getToken());
                    apiService.sendNotification(sender)
                            .enqueue(new Callback<Response>() {
                                @Override
                                public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {
                                    Toast.makeText(ChartActivity.this, ""+response.message(), Toast.LENGTH_SHORT).show();
                                }
                                @Override
                                public void onFailure(Call<Response> call, Throwable t) {
                                    Toast.makeText(ChartActivity.this, ""+t.getMessage(), Toast.LENGTH_SHORT).show();

                                }
                            });
                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void seenMessage() {
        userRefForSeen = FirebaseDatabase.getInstance().getReference("chats");
        seenListener = userRefForSeen.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    ModelChat chat = ds.getValue(ModelChat.class);
                    if(chat.getReciever().equals(myUid) && chat.getSender().equals(hisUid))
                    {
                        HashMap<String,Object> hashMap =new HashMap<>();
                        hashMap.put("isSeen",true);
                        ds.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void checkOnlineStatus(String status){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",status);
        //update Value of OnineStatus
        reference.updateChildren(hashMap);
    }

    private void checkTypigStatus(String typingStatus){
        DatabaseReference reference =FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("typing",typingStatus);
        //update Value of OnineStatus
        reference.updateChildren(hashMap);

    }

    @Override
    protected void onPause() {
        checkTypigStatus("none");
        userRefForSeen.removeEventListener(seenListener);
        //set the cuurent time as Last seen
        Long tsLong = System.currentTimeMillis();
        final String ts = tsLong.toString();
        checkOnlineStatus(ts);
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu ) {
        getMenuInflater().inflate(R.menu.menu_main,menu);

        menu.findItem(R.id.action_search).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    //Hnadle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
        if(id == R.id.action_logout) {
            firebaseAuth.signOut();
            checkUserState();
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserState() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user== null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }else{
            myUid=user.getUid();//Uid of currenty sign in user
        }
    }

    @Override
    protected void onStart() {
        checkUserState();
        //set the user online
        checkOnlineStatus("Online");
        super.onStart();
    }
}
