package com.example.sharn.safetui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;

import static android.support.constraint.motion.MotionScene.TAG;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        HomeFragment homeFragment = new HomeFragment();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.mLayout, homeFragment).commit();

        navigationView.setCheckedItem(R.id.nav_home);



        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            public void onUserSignedIn() {
                Log.d(TAG,"User Signed In");
            }

            @Override
            public void onUserSignedOut() {
                Log.d(TAG, "User signed out");
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            HomeFragment homeFragment = new HomeFragment();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mLayout, homeFragment).commit();

        } else if (id == R.id.nav_trilat) {
            Intent dataIntent = new Intent(MainActivity.this, Trilateration.class);
            startActivity(dataIntent);

        } else if (id == R.id.nav_dynamo) {
            Intent dataIntent = new Intent(MainActivity.this, DynamoActivity.class);
            startActivity(dataIntent);

        } else if (id == R.id.nav_qa) {
            QandA qandaFragment = new QandA();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mLayout, qandaFragment).commit();

        } else if (id == R.id.nav_contact) {
            ContactUs contactUs = new ContactUs();
            FragmentManager manager = getSupportFragmentManager();
            manager.beginTransaction().replace(R.id.mLayout, contactUs).commit();


        } else if (id == R.id.nav_logout) {
            IdentityManager.getDefaultIdentityManager().signOut();
            Intent loginAgainIntent = new Intent(MainActivity.this, AuthenticatorActivity.class);
            startActivity(loginAgainIntent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
