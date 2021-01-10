package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Fragments.ChatFragment;
import com.example.chattingapp.Fragments.FriendsFragment;
import com.example.chattingapp.Fragments.ProfileFragment;
import com.example.chattingapp.Fragments.SearchFragment;
import com.example.chattingapp.Models.User;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

// 2021-01-07
// BottomNavigationView 생성 (Fragment 4개)
// res/menu directory생성

public class MainActivity extends AppCompatActivity {

    private FirebaseUser firebaseUser;
    private DatabaseReference reference;

    private BottomNavigationView bottomNavigationView;
    private CircleImageView profile_imgae;
    private TextView user_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile_imgae = (CircleImageView)findViewById(R.id.profile_image);
        user_name = (TextView)findViewById(R.id.user_name);
        getUserInfo();

        // 가장먼저 불러오는 fragment
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new FriendsFragment()).commit();
        bottomNavigationView = (BottomNavigationView)findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;


            switch (item.getItemId()){

                case R.id.nav_friends:
                    selectedFragment = new FriendsFragment();
                    break;

                case R.id.nav_chat:
                    selectedFragment = new ChatFragment();
                    break;

                case R.id.nav_search:
                    selectedFragment = new SearchFragment();
                    break;

                case R.id.nav_profile:
                    selectedFragment = new ProfileFragment();
                    break;
            }

            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();

            return true;
            }
        });
    }

    private void getUserInfo() {

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String userID = firebaseUser.getUid();
        reference = FirebaseDatabase.getInstance().getReference().child("Users").child(userID);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user_name.setText(user.getName());
                if(user.getImageURL().equals("default")){
                    profile_imgae.setImageResource(R.drawable.default_img);
                }
                else{
                    Glide.with(MainActivity.this).load(user.getImageURL()).into(profile_imgae);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}