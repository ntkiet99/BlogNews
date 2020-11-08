package com.example.blognews.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blognews.Activities.ui.Profile.ProfileFragment;
import com.example.blognews.Activities.ui.Settings.SettingsFragment;
import com.example.blognews.Activities.ui.home.HomeFragment;
import com.example.blognews.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

    private AppBarConfiguration mAppBarConfiguration;

    FirebaseAuth mAuth;
    FirebaseUser currentUser;
    private Intent loginActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_profile, R.id.nav_settings, R.id.nav_signout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        loginActivity = new Intent(this, com.example.blognews.Activities.LoginActivity.class);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(loginActivity);
            finish();
        }else{
            updateNavHeader();
        }

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

//        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
//            @Override
//            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
//                int menuId = destination.getId();
//                switch (menuId){
//                    case R.id.nav_signout:{
//                        mAuth.signOut();
//                        startActivity(loginActivity);
//                        finish();
//                        break;
//                    }
//                    default:
//                        fab.show();
//                }
//            }
//        });
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

    public  void updateNavHeader(){
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



}