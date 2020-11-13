package com.example.blognews.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blognews.Activities.ui.Profile.ProfileFragment;
import com.example.blognews.Activities.ui.Settings.SettingsFragment;
import com.example.blognews.Activities.ui.home.HomeFragment;
import com.example.blognews.Models.Post;
import com.example.blognews.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class NavHome extends AppCompatActivity {

    private static final int PREQCODE = 2;
    private static final int REQUESCODE = 2;
    private AppBarConfiguration mAppBarConfiguration;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    Dialog popAddPost;
    ImageView popupUserImage, popupPostImage, popupAddBtn;
    TextView popupTitle, popupDescription;
    ProgressBar popupClickProgress;
    Uri pickedImgUri = null;
    private Intent loginActivity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        loginActivity = new Intent(this, com.example.blognews.Activities.LoginActivity.class);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(loginActivity);
            finish();
        }else{
            updateNavHeader();
        }
        iniPopup();
        setupPopupImageClick();
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popAddPost.show();
                Toast.makeText(NavHome.this,"Create New Post", Toast.LENGTH_LONG).show();
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings, R.id.nav_signout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()) {
                    // Here in each case statement you need to pass the id of the menu items and then call startActivity(new Intent()) in each case statement.
                    case R.id.nav_signout:
                        mAuth.signOut();
                        startActivity(loginActivity);
                        finish();
                        break;
                    case R.id.nav_home:
                        getSupportActionBar().setTitle("Home");
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                        break;
                    case R.id.nav_profile:
                        getSupportActionBar().setTitle("Profile");
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ProfileFragment()).commit();
                        break;
                    case R.id.nav_settings:
                        getSupportActionBar().setTitle("Settings");
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new SettingsFragment()).commit();
                        break;
                    default:
                        fab.show();
                }
                drawer.closeDrawers();
                return true;
            }
        });

    }

    private void iniPopup() {
        Context context;
        popAddPost = new Dialog(this);
        popAddPost.setContentView(R.layout.popup_add_post);
        popAddPost.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popAddPost.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popAddPost.getWindow().getAttributes().gravity = Gravity.TOP;

        popupUserImage = popAddPost.findViewById(R.id.popup_user_image);
        popupPostImage = popAddPost.findViewById(R.id.popup_image);
        popupPostImage.setImageDrawable(getResources().getDrawable(R.drawable.bg_post_image));
        popupTitle = popAddPost.findViewById(R.id.popup_title);
        popupDescription = popAddPost.findViewById(R.id.popup_description);
        popupAddBtn = popAddPost.findViewById(R.id.popup_add);
        popupClickProgress = popAddPost.findViewById(R.id.popup_progressBar);
        Glide.with(NavHome.this).load(currentUser.getPhotoUrl()).into(popupUserImage);
        popupAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupAddBtn.setVisibility(View.INVISIBLE);
                popupClickProgress.setVisibility(View.VISIBLE);

                if(!popupTitle.getText().toString().isEmpty()
                && !popupDescription.getText().toString().isEmpty() && pickedImgUri != null){
                    StorageReference  storageReference = FirebaseStorage.getInstance().getReference().child("blog_images");
                    Long tsLong = System.currentTimeMillis() / 1000;
                    String ts = tsLong.toString();
                    StorageReference imageFilePath = storageReference.child("image-"+ ts + ".jpg");
//                    Toast.makeText(NavHome.this, pickedImgUri.getLastPathSegment(), Toast.LENGTH_LONG).show();
//                    StorageReference imageFilePath = storageReference.child(pickedImgUri.getLastPathSegment());
                    imageFilePath.putFile(pickedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String imageDownloadLink = uri.toString();
                                    Post post = new Post(popupTitle.getText().toString(), popupDescription.getText().toString(), imageDownloadLink, currentUser.getUid(), currentUser.getPhotoUrl().toString());
                                    addPost(post);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showMessage(e.getMessage());
                                    popupClickProgress.setVisibility(View.INVISIBLE);
                                    popupAddBtn.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    });
                }else{
                    if(popupTitle.getText().toString().isEmpty() && popupDescription.getText().toString().isEmpty() && pickedImgUri == null){
                        showMessage("Please verify all input fields and choose Post Image");
                    }else if(pickedImgUri == null){
                        showMessage("Please verify choose Post Image!");
                    }else if(popupTitle.getText().toString().isEmpty() || popupDescription.getText().toString().isEmpty()){
                        showMessage("Please verify all input fields!");
                    }
                    popupAddBtn.setVisibility(View.VISIBLE);
                    popupClickProgress.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void addPost(Post post) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Post").push();
        String key = myRef.getKey();
        post.setPostKey(key);
        myRef.setValue(post).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                showMessage("Post Added successfully");
                popupClickProgress.setVisibility(View.INVISIBLE);
                popupAddBtn.setVisibility(View.VISIBLE);
                popupPostImage.setImageDrawable(getResources().getDrawable(R.drawable.bg_post_image));
                popupTitle.setText("");
                popupDescription.setText("");
                popAddPost.dismiss();
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(NavHome.this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader(){
        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_userName);
        TextView navEmail = headerView.findViewById(R.id.nav_userEmail);
        ImageView navUserPhoto = headerView.findViewById(R.id.nav_userPhoto);
        navEmail.setText(currentUser.getEmail());
        navUsername.setText(getDisplayName(currentUser));
        Toast.makeText(NavHome.this, currentUser.getDisplayName(), Toast.LENGTH_LONG).show();
        Glide.with(this).load(currentUser.getPhotoUrl()).into(navUserPhoto);
    }

    public static String getDisplayName(FirebaseUser user) {
        String displayName = user.getDisplayName();
        if (!TextUtils.isEmpty(displayName)) {
            return displayName;
        }

        for (UserInfo userInfo : user.getProviderData()) {
            if (!TextUtils.isEmpty(userInfo.getDisplayName())) {
                return userInfo.getDisplayName();
            }
        }

        return null;
    }

    private void setupPopupImageClick() {
        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestForPermission();
            }
        });
    }

    private void checkAndRequestForPermission() {
        if(ContextCompat.checkSelfPermission(NavHome.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(NavHome.this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                Toast.makeText(NavHome.this, "Please accept for required permission", Toast.LENGTH_SHORT).show();
            }
            else{
                ActivityCompat.requestPermissions(NavHome.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PREQCODE);
            }
        } else {
            openGallery();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, REQUESCODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUESCODE && data != null) {
            pickedImgUri = data.getData();
            popupPostImage.setImageURI(pickedImgUri);
        }
    }
}