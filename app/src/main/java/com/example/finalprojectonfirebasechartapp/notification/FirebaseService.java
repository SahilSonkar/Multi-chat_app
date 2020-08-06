package com.example.finalprojectonfirebasechartapp.notification;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessagingService;

public class FirebaseService extends FirebaseMessagingService {


    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
        String  Refreshtoken = FirebaseInstanceId.getInstance().getToken();
        if(user!=null){
            UpdateToken(Refreshtoken);
        }
    }

    private void UpdateToken(String refreshtoken) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Token token = new Token(refreshtoken);
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("Token");
        ref.child(user.getUid()).setValue(token);
    }
}
