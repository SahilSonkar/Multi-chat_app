package com.example.finalprojectonfirebasechartapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;


public class ChartListFragment extends Fragment {



    //Firebase tools
    FirebaseAuth mAuth;
    public ChartListFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =inflater.inflate(R.layout.fragment_chart_list, container, false);

        //Instantiate firebase tools
        mAuth = FirebaseAuth.getInstance();
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);// to show option menu in fragment
        super.onViewCreated(view, savedInstanceState);
    }

    //inflate Option menu
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main,menu);
        //To hide "AddPost"
        menu.findItem(R.drawable.ic_addpost).setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    //Hnadle menu item clicks
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id=item.getItemId();
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
            startActivity(new Intent(getActivity(), MainActivity.class));
            getActivity().finish();
        }
    }
}
