package com.example.finalprojectonfirebasechartapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class RegisterActivity extends AppCompatActivity {

    private EditText emailEt,passwordEt;
    private Button register;
    private TextView haveAccount;

    //progress Dailog Box to display user while registring user
    ProgressDialog progressDialog;

    //Declare an instance of FirebaseAuth
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Action bar and its title
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Create Account");

        //enable back button
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);

        //In the onCreate() method, initialize the FirebaseAuth instance
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        haveAccount =findViewById(R.id.HaveAccount);
        emailEt=findViewById(R.id.email);
        passwordEt=findViewById(R.id.password);
        register=findViewById(R.id.register);
        progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Registring User....");
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email=emailEt.getText().toString().trim();
                String password=passwordEt.getText().toString().trim();
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                    emailEt.setError("Check Email Format");
                    emailEt.setFocusable(true);
                }
                else if(password.length() < 6){
                    passwordEt.setError("Password length is atleast 6 character");
                    passwordEt.setFocusable(true);
                }else{
                    registerUser(email,password);
                }
            }
        });

        haveAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterActivity.this,LoginActivity.class));
                finish();
            }
        });

    }
    private void registerUser(String email,String password){
        progressDialog.show();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, dismiss dialog and start activity
                            progressDialog.dismiss();


                            FirebaseUser user = mAuth.getCurrentUser();
                            //Get user Email id and Uid from /auth
                            String email = user.getEmail();
                            String uid = user.getUid();
                            // When user is registered store the info to Firebase realtime database
                            //using HashMap
                            HashMap<Object, String> map=new HashMap<>();
                            //put info inti map
                            map.put("email",email);
                            map.put("uid",uid);
                            map.put("name","");
                            map.put("onlineStatus","online");
                            map.put("typing","none");
                            map.put("phone","");
                            map.put("image","");
                            map.put("cover","");


                            //Firebase Database instances
                            FirebaseDatabase database =FirebaseDatabase.getInstance();
                            //path to store User's data "Users"
                            DatabaseReference reference =database.getReference("Users");
                            //put data within hashmap in database
                            reference.child(uid).setValue(map);



                            Toast.makeText(RegisterActivity.this, "Registered...\n"+user.getEmail(), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(RegisterActivity.this, DashboardActivity.class));
                            finish();
                        } else {
                            progressDialog.dismiss();
                            // If sign in fails
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(RegisterActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
