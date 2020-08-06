package com.example.finalprojectonfirebasechartapp;


import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

import static android.app.Activity.RESULT_OK;
import static android.widget.Toast.LENGTH_SHORT;


/**
 * A simple {@link Fragment} subclass.
 */
public class profileFragment extends Fragment {
    private static final String TAG = "profileFragment";

    //Firebase
    FirebaseAuth mAuth;
    FirebaseUser user;
    FirebaseDatabase database;
    DatabaseReference reference;
    //storage
    StorageReference storageReference;
    //parh where images of users profile and cover will be stored
    String storagePath="Users_Profile_Cover_Image/";


    //Handle
    TextView nameTv,emaiTv,phoneTv;
    ImageView profileImageTv,coverImageTv;
    FloatingActionButton fab;

    //Image Uri
    Uri Image_Uri;

    //For profile Or coverPhoto
    String profileOrCoverPhoto;

    //progress Dialpg
    ProgressDialog pd;

    //permission Constraints
    private  static final  int CAMERA_REQUEST_CODE = 100;
    private  static final  int STORAGE_REQUEST_CODE = 200;
    private  static final  int IMAGE_PICK_GALLERY_CODE = 300;
    private  static final  int IMAGE_PICK_CAMERA_CODE = 400;

    //Arrays of permission to be requested
    String cameraPermission[];
    String storagePerission[];

    //indicator
    private int indicator = 0;

    //User info for quick load
    private HashMap<String,Object> info = new HashMap<>();

    public profileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth =FirebaseAuth.getInstance();
        user =mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance(); //Firebase storage refrence
        reference =database.getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        //Iniatialise arrays of permission
        cameraPermission =new String[]{Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE};
        storagePerission =new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};



        //Initialise progrsss dialog
        pd =new ProgressDialog(getActivity());



        //Initialise Handle
        nameTv =view.findViewById(R.id.nameTv);
        emaiTv =view.findViewById(R.id.emailTv);
        phoneTv =view.findViewById(R.id.phoneTv);
        coverImageTv =view.findViewById(R.id.cover);
        profileImageTv =view.findViewById(R.id.AvatarId);
        fab=view.findViewById(R.id.fab);

//        LoadUserInfoIfitIsEmpty();


        //Retrieve data Once


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditProfileDialog();
            }
        });

       Retrievedata();
        return  view;
    }

    private void Retrievedata()
    {
        Query query = reference.orderByChild("email").equalTo(user.getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {


                //Check Until Required data get
                for(DataSnapshot ds:dataSnapshot.getChildren())
                {
                    String name = ""+ds.child("name").getValue();
                    String email =""+ds.child("email").getValue();
                    String phone= ""+ds.child("phone").getValue();
                    String image = ""+ds.child("image").getValue();
                    String cover =""+ds.child("cover").getValue();

                    //load user info in hashMap to quick show to users whenever the fragment is opened


                    nameTv.setText(name);
                    emaiTv.setText(email);
                    phoneTv.setText(phone);
                    try {
                        //if image is received then set
                        Picasso.get().load(image).placeholder(R.drawable.ic_defaultuser).into(profileImageTv);
                    }catch (Exception e){
                        //if there is exeption while getting image
                        Toast.makeText(getActivity(), ""+e.getMessage()+" OnCreate1", LENGTH_SHORT).show();
                    }
                    try {
                        //if image is received then set
                        Picasso.get().load(cover).placeholder(R.drawable.ic_defaultuser).into(coverImageTv);
                    }catch (Exception e){
                        //if there is exeption while getting image
                        Toast.makeText(getActivity(), ""+e.getMessage()+"OnCreate2", LENGTH_SHORT).show();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }



    private boolean checkStoragePermission(){
        //check storag permission is enable or not
        //return tru if enabled
        //return falseif not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result;
    }
    private void requestStoragePermission(){
        //request runtime storage permission
        requestPermissions(storagePerission,STORAGE_REQUEST_CODE);
    }

    private boolean checkCameraPermission(){
        //check storag permission is enable or not
        //return tru if enabled
        //return falseif not enabled
        boolean result = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.CAMERA)
                ==(PackageManager.PERMISSION_GRANTED);
        boolean result1 = ContextCompat.checkSelfPermission(getActivity(),Manifest.permission.WRITE_EXTERNAL_STORAGE)
                ==(PackageManager.PERMISSION_GRANTED);
        return result && result1;
    }
    private void requestCameraPermission(){
        //request runtime storage permission
        requestPermissions(cameraPermission,CAMERA_REQUEST_CODE);
    }

    private void showEditProfileDialog() {

        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose Action");
        String[] option={"Edit Profile Picture","Edit Cover Photo","Edit Name","Edit Phone Number"};
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Handle dialog item click
                if(which ==0){
                    //Edit Profile Click
                    pd.setMessage("Updating profile photo");
                    profileOrCoverPhoto="image"; //Changing profile picture ,make sure to assign same value
                    shoeImagePicDialog();

                }else if(which==1){
                    //Edit Cover Photo
                    pd.setMessage("Updating cover photo");
                    profileOrCoverPhoto="cover"; //Changing cover picture ,make sure to assign same value
                    shoeImagePicDialog();

                }else if(which==2){
                    //Edit Name click
                    pd.setMessage("Updating name");
                    //calling method and pass key "name" as parameter to update it's value in database
                    showNamePhoneUpdateDialog("name");
                }else if(which==3){
                    //Edit Phone Number
                    pd.setMessage("Updating phone number");
                    showNamePhoneUpdateDialog("phone");


                }

            }

        });
        builder.create().show();

    }

    private void showNamePhoneUpdateDialog(final String key) {
        //parameter "key" will contain value:
        // either "name" which is key in user's database which is used to update user's name
        // or "phone" which is key in user's database which is used to update user's phone

        //custom dialog
        AlertDialog.Builder builder =new AlertDialog.Builder(getActivity());
        builder.setTitle("Update "+key); //e-g. Update'name or phone
        //set layout of dialog
        LinearLayout linearLayout = new LinearLayout(getActivity());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setPadding(10,10,10,10);
        //add edit Text
        final EditText editText =new EditText(getActivity());
        editText.setHint("Enter "+key); //e-g. Hint :-  edit name or phone
        linearLayout.addView(editText);

        builder.setView(linearLayout);

        //add button to dialog to update
        builder.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //input text from edit text
                String value = editText.getText().toString().trim();
                //validate if user has entered something or not
                if(!TextUtils.isEmpty(value)){
                    pd.show();
                    HashMap<String,Object> result= new HashMap<>();
                    result.put(key, value);

                    reference.child(user.getUid()).updateChildren(result)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Retrievedata();
                            pd.dismiss();
                            Toast.makeText(getActivity(), "Updated..", LENGTH_SHORT).show();

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Retrievedata();
                            pd.dismiss();
                            Toast.makeText(getContext(), ""+e.getMessage(), LENGTH_SHORT).show();
                        }
                    });
                }else{
                    Toast.makeText(getContext(), "Please enter "+key, LENGTH_SHORT).show();
                }
            }
        });
        //add button to dialog to cancle
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        //create and show dialog
        builder.create().show();
    }

    private void shoeImagePicDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Pick Image From");
        String[] option = {"Camera", "Gallery"};
        builder.setItems(option, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                //Handle dialog item click
                if (which == 0) {
                    //Camera Clicked
                    if(!checkCameraPermission()){
                        requestCameraPermission();
                    }else{
                        pickFromCamera();
                    }
                } else if (which == 1) {
                    //Gallery Clicked
                   if(!checkStoragePermission()){
                       requestStoragePermission();
                   }else{
                       pickFromGallery();
                   }
                }
            }
        });
        builder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //This method is call when user press allow or deny from permission request dialog
        //here we will handle permission cases(allowed & denied)
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                //picking from camera ,first check if camera and storage permission allowed or not
                if(grantResults.length >0){
                    boolean cameraAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean writeStorageAccepted =grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    if(cameraAccepted && writeStorageAccepted){
                        //permission Enabled
                        pickFromCamera();
                    }else {
                        //permission denied
                        Toast.makeText(getActivity(), "please,Enable Camera and Storage  Permission", LENGTH_SHORT).show();
                    }
                    }
            }
            break;
            case STORAGE_REQUEST_CODE:{

                //picking from gallery ,first check if storage permission allowed or not
                if(grantResults.length >0){
                    boolean writeStorageAccepted =grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    if(writeStorageAccepted){
                        //permission Enabled
                        pickFromGallery();
                    }else {
                        //permission denied
                        Toast.makeText(getActivity(), "please,Enable Storage Permission", LENGTH_SHORT).show();
                    }
                }


            }
            break;
        }
    }

    private void pickFromGallery() {
        //pick from Gallery
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent,IMAGE_PICK_GALLERY_CODE);
    }

    private void pickFromCamera() {

        //Intent of picking of image from device camera
        ContentValues values =new ContentValues();
        values.put(MediaStore.Images.Media.TITLE,"temp pic");
        values.put(MediaStore.Images.Media.DESCRIPTION,"temp description");

        //put Image Uri
        Image_Uri =getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values);

        //Intent to start camera
        Intent cameraInntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraInntent.putExtra(MediaStore.EXTRA_OUTPUT,Image_Uri);
        startActivityForResult(cameraInntent,IMAGE_PICK_CAMERA_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        //This method will be cal after picking image from camera or gallery

        if(resultCode == RESULT_OK ){

            if(requestCode == IMAGE_PICK_GALLERY_CODE){
                //image is pick from gallery, get Uri of image
                Image_Uri= data.getData();
                uploadProfileCoverPhoto(Image_Uri);
            }
            if(requestCode == IMAGE_PICK_CAMERA_CODE){
                //image is pick from camera, get Uri of image
//                Image_Uri= data.getData();
                uploadProfileCoverPhoto(Image_Uri);

            }
        }




        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfileCoverPhoto(Uri uri) {

        pd.show();
        //instead of creating separate function for profile picture and cover photo
        // I am doing work for both in same function
        //Before doing work ,I will check the string (Either string ="Image" or string ="cover"
        // it will clear to me  what should i do after that

        // The parameter "Image_Uri" contains the uri of image picked from camera or gallery
        // we will use the UID of the currently signed in user as name of the image so there
        // will be only one image profile and one image for eacn user


        //path and name of user image to be stored in firebase storage
        // e.g,Users_Profile_Cover_Image/image_e122424252.jpg
        // e.g,Users_Profile_Cover_Image/cover_e122424252.jpg
        String filePathAndName = storagePath+""+profileOrCoverPhoto+"_"+user.getUid();

         StorageReference storageReference2nd=storageReference.child(filePathAndName);
         storageReference2nd.putFile(uri)
         .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
             @Override
             public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                 //image is uploaded to store , now get it's uri and store in user's database
                 Task<Uri> uriTask = taskSnapshot.getStorage().getDownloadUrl();
                 while (!uriTask.isSuccessful());
                 Uri downloadUri = uriTask.getResult();
                 //check image is uploaded or not or url is received
                 if(uriTask.isSuccessful()){
                     //image uploaded
                     //add/update uri in users database
                     HashMap<String,Object> hashMap =new HashMap<>();
                     //first parameter is profileOrCover that has value "image" or "cover"
                     //which are keys in user's databe where url of image will be stored
                     //ans second parameter is downoaded uri
                     hashMap.put(profileOrCoverPhoto, downloadUri.toString());
                     reference.child(user.getUid()).updateChildren(hashMap)
                             .addOnSuccessListener(new OnSuccessListener<Void>() {
                                 @Override
                                 public void onSuccess(Void aVoid) {
                                     //url in databse of user is added succesfully
                                     //dismiss dialog bos
                                     Retrievedata();
                                     pd.dismiss();
                                     Toast.makeText(getActivity(),"image updated..", LENGTH_SHORT).show();

                                 }
                             })
                             .addOnFailureListener(new OnFailureListener() {
                                 @Override
                                 public void onFailure(@NonNull Exception e) {
                                     //Error in adding url of database of user
                                     //dismmis dialog box
                                     Retrievedata();
                                     pd.dismiss();
                                     Toast.makeText(getActivity(),"Error in updating image..", LENGTH_SHORT).show();
                                 }
                             });
                 }

             }
         })
         .addOnFailureListener(new OnFailureListener() {
             @Override
             public void onFailure(@NonNull Exception e) {
                 //There were some error(s) ,get and show error message ,dismiss dialog

                 pd.dismiss();
                 Toast.makeText(getContext(), ""+e.getMessage(), LENGTH_SHORT).show();
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
        if(id == R.id.add_Post){
            startActivity( new Intent(getActivity(),AddPost.class));
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
