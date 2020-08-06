package com.example.finalprojectonfirebasechartapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class AddPost extends AppCompatActivity {


    //Firebasse tools
    FirebaseAuth mAuth;
    ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        mAuth = FirebaseAuth.getInstance();
        actionBar = getSupportActionBar();
        actionBar.setTitle("Add Post");
        //enable backpress button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {

        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.findItem(R.drawable.ic_addpost).setVisible(false);
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        if(id == R.id.action_logout) {
            storeLastSeen();
            mAuth.signOut();
            checkUserState();
        }
        return super.onOptionsItemSelected(item);
    }

    private void storeLastSeen() {
        FirebaseUser user = mAuth.getInstance().getCurrentUser();
        final String myUid = user.getUid();
        Long tsLong = System.currentTimeMillis();
        final String ts = tsLong.toString();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",ts);
        ref.updateChildren(hashMap);
    }
    private void checkUserState() {
        if(FirebaseAuth.getInstance().getCurrentUser()==null) {
            startActivity(new Intent(AddPost.this, MainActivity.class));
            finish();
        }else{

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkUserState();
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeLastSeen();
    }
}
