package com.example.finalprojectonfirebasechartapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalprojectonfirebasechartapp.notification.Token;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.HashMap;

public class DashboardActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    // Uid of current User
    String myUid ;


    private TextView showEmail;
    ActionBar actionBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        actionBar = getSupportActionBar();
        actionBar.setTitle("Profile");

        mAuth =FirebaseAuth.getInstance();
        //Bottom navigaton
        BottomNavigationView navigationView =findViewById(R.id.navigation);
        navigationView.setOnNavigationItemSelectedListener(selectedListener);


        //home fragment transacion (default fragment)
        actionBar.setTitle("Home");
        HomeFragment f1=new HomeFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.Box,f1,"");
        ft.commit();

        checkUserState();

        //update Token
        UpdateToken(FirebaseInstanceId.getInstance().getToken());
    }

    private void UpdateToken(String token) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Token");
        Token mToken = new Token(token);
        ref.child(myUid).setValue(mToken);
    }

    private BottomNavigationView.OnNavigationItemSelectedListener selectedListener=
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.nav_home:
                            //home fragment transacion
                            actionBar.setTitle("Home");
                            HomeFragment f1=new HomeFragment();
                            FragmentTransaction ft1 = getSupportFragmentManager().beginTransaction();
                            ft1.replace(R.id.Box,f1,"");
                            ft1.commit();
                            return true;
                        case R.id.nav_profile:
                            //profile fragment transacion
                            actionBar.setTitle("Profile");
                            profileFragment f2 = new profileFragment();
                            FragmentTransaction ft2 = getSupportFragmentManager().beginTransaction();
                            ft2.replace(R.id.Box,f2,"");
                            ft2.commit();
                            return true;
                        case R.id.nav_users:
                            //users fragment transacion
                            actionBar.setTitle("User");
                            UserFragment f3 = new UserFragment();
                            FragmentTransaction ft3 = getSupportFragmentManager().beginTransaction();
                            ft3.replace(R.id.Box,f3,"");
                            ft3.commit();
                            return true;
                        case R.id.nav_chat:
                            //users fragment transacion
                            actionBar.setTitle("Chats");
                            ChartListFragment f4 = new ChartListFragment();
                            FragmentTransaction ft4 = getSupportFragmentManager().beginTransaction();
                            ft4.replace(R.id.Box,f4,"");
                            ft4.commit();
                            return true;
                    }
                    return false;
                }
            };



    private void checkUserState() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            startActivity(new Intent(DashboardActivity.this, MainActivity.class));
            finish();
        }else {
            myUid = user.getUid();
            //save Uid of Currenlt signed In user in sharedPrefences
            SharedPreferences sp = getSharedPreferences("SP_USER",MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("current_UserID",myUid);
            editor.apply();
        }
    }

    @Override
    protected void onResume() {
        checkUserState();
        showOnline();
        super.onResume();
    }

    private void showOnline(){

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myUid = user.getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus","online");
        ref.updateChildren(hashMap);
    }

    @Override
    protected void onPause() {
        super.onPause();
        storeLastSeen();
    }

    private void storeLastSeen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String myUid = user.getUid();
        Long tsLong = System.currentTimeMillis();
        final String ts = tsLong.toString();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(myUid);
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("onlineStatus",ts);
        ref.updateChildren(hashMap);
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
