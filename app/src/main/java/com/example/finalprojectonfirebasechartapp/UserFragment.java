package com.example.finalprojectonfirebasechartapp;


import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuItemCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalprojectonfirebasechartapp.Models.ModelUser;
import com.example.finalprojectonfirebasechartapp.adapters.AdapterUsers;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class UserFragment extends Fragment {


    //decleare recyclerView
    RecyclerView recyclerView;
    AdapterUsers adapterUsers;
    List<ModelUser> userList;
    FirebaseAuth mAuth;
    TextView loadingStatus;

    public UserFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Init View
        View view=inflater.inflate(R.layout.fragment_user, container, false);



        loadingStatus = view.findViewById(R.id.Loadingstatus);

        //Init Firebase
        mAuth =FirebaseAuth.getInstance();
        //Init ArrayList<ModelUser>
        userList =new ArrayList<>();

        //Inti recyclerView
        recyclerView = view.findViewById(R.id.users_recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        //ini userList
        userList =new ArrayList<>();

        //get all users
        getAllUser();


        return view;
    }

    private void getAllUser() {

        //get Current User
        final FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
        //get path of Databse containing "Users" information
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all the data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for( DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser =ds.getValue(ModelUser.class);

                    //get all users exept currently signed In users
                    if(!user.getUid().equals(modelUser.getUid())){
                        loadingStatus.setText("");
                        userList.add(modelUser);
                    }


                    //adapter
                    adapterUsers =new AdapterUsers(getActivity(),userList);

                    //set Adapter
                    recyclerView.setAdapter(adapterUsers);
                }


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private void searchUser(final String query) {


        //get Current User
        final FirebaseUser user =FirebaseAuth.getInstance().getCurrentUser();
        //get path of Databse containing "Users" information
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users");
        //get all the data from path
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for( DataSnapshot ds: dataSnapshot.getChildren()){
                    ModelUser modelUser =ds.getValue(ModelUser.class);

                    //get all searceed users exept currently signed In users
                    //condition to fullfill searches
                    // 1. User is not currently singed In Users
                    // 2. User Name  or Email contain in text as search item( case Insensitive)
                    if(!user.getUid().equals(modelUser.getUid()))
                    {
                        if(modelUser.getName().toLowerCase().contains(query.toLowerCase().trim())
                        || modelUser.getEmail().toLowerCase().contains(query.toLowerCase().trim()))
                        {
                            userList.add(modelUser);
                        }
                    }


                    //adapter
                    adapterUsers =new AdapterUsers(getActivity(),userList);

                    //Refresh List to show search Users
                    adapterUsers.notifyDataSetChanged();

                    //set Adapter
                    recyclerView.setAdapter(adapterUsers);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

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
        MenuItem item=menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //call when user press button search button from keyboard
                //if search query is not empty then search
                if(!TextUtils.isEmpty(query.trim()))
                {
                    searchUser(query);
                }else{
                    //search text is empty ,get all users
                    getAllUser();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //callled wehn user press any single button
                //if search query is not empty then search
                if(!TextUtils.isEmpty(newText.trim()))
                {
                    searchUser(newText);
                }else{
                    //search text is empty ,get all users
                    getAllUser();
                }
                return false;
            }
        });
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
