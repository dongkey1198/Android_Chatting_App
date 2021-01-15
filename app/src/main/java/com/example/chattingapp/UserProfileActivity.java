package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Models.Friend;
import com.example.chattingapp.Models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.EventListener;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private TextView profile_email, profile_name, profile_age, profile_phone_num;
    private CircleImageView profile_image;
    private Button sned_message, add_friend_btn, delete_friend_btn,back_button;

    private Intent intent;
    private String userId;
    private boolean flag;

    private FirebaseUser fuser;
    private DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        profile_email = (TextView)findViewById(R.id.profile_email);
        profile_name = (TextView)findViewById(R.id.profile_name);
        profile_age = (TextView)findViewById(R.id.profile_age);
        profile_phone_num = (TextView)findViewById(R.id.profile_phone);
        profile_image = (CircleImageView)findViewById(R.id.profile_image);

        sned_message = (Button)findViewById(R.id.send_message);
        sned_message.setOnClickListener(this);

        add_friend_btn = (Button)findViewById(R.id.add_friend);
        add_friend_btn.setOnClickListener(this);

        delete_friend_btn = (Button)findViewById(R.id.delete_friend);
        delete_friend_btn.setOnClickListener(this);

        back_button = (Button)findViewById(R.id.back_button);
        back_button.setOnClickListener(this);

        //현재사용자 정보
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        getUserData();
        getButtonText();
    }

    private void getButtonText() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Friends").child(fuser.getUid());
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Friend f = dataSnapshot.getValue(Friend.class);
                    if(f.getId().equals(userId)){
                        flag = true;
                        add_friend_btn.setVisibility(View.GONE);
                    }
                }

                if(flag == true){
                    delete_friend_btn.setVisibility(View.VISIBLE);
                }
                else{
                    add_friend_btn.setVisibility(View.VISIBLE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void getUserData() {
        intent = getIntent();
        userId = intent.getStringExtra("userID");

        reference = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                profile_email.setText(user.getEmail());
                profile_name.setText(user.getName());
                profile_age.setText(user.getAge());
                profile_phone_num.setText(user.getPhone_num());

                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.drawable.default_img);
                }
                else{
                    Glide.with(UserProfileActivity.this).load(user.getImageURL()).into(profile_image);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.back_button:
                finish();
                break;

            case R.id.send_message:
                Intent sendMessage = new Intent(UserProfileActivity.this, MessageActivity.class);
                sendMessage.putExtra("userID", userId);
                startActivity(sendMessage);
                break;

            case R.id.add_friend:
                if(flag == false){
                    DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference();
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("id", userId);
                    friendRef.child("Friends").child(fuser.getUid()).child(userId).setValue(hashMap);
                    Toast.makeText(UserProfileActivity.this, "친구 추가 완료...", Toast.LENGTH_SHORT).show();
                    flag = true;
                    if(flag == true){
                        add_friend_btn.setVisibility(View.GONE);
                    }
                }
                break;

            case R.id.delete_friend:

                DatabaseReference friendRef = FirebaseDatabase.getInstance().getReference("Friends").child(fuser.getUid());
                friendRef.child(userId).removeValue();
                Toast.makeText(UserProfileActivity.this, "친구 삭제 완료...", Toast.LENGTH_SHORT).show();
                delete_friend_btn.setVisibility(View.GONE);
                flag = false;
                if(flag == false){
                    add_friend_btn.setVisibility(View.VISIBLE);
                }
                break;

        }

    }
}