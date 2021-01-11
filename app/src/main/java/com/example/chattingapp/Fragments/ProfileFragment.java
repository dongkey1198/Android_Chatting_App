package com.example.chattingapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.chattingapp.LoginActivity;
import com.example.chattingapp.R;
import com.example.chattingapp.SettingActivity;
import com.example.chattingapp.Models.User;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileFragment extends Fragment implements View.OnClickListener {

    private Button logout_btn, profile_edit_button;
    private TextView profile_email, profile_name, profile_age, profile_phone_num;
    private CircleImageView profile_image;

    private FirebaseUser user;
    private DatabaseReference reference;

    private FirebaseStorage storage;
    private StorageReference storageReference;

    private String userID;

    private String user_email;
    private String user_name;
    private String user_age;
    private String user_image;
    private String user_phone_num;


    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);


        //파이어베이스에서 사용자 유니크 아이디 가져오기기
        user = FirebaseAuth.getInstance().getCurrentUser();
        userID = user.getUid(); // current user Id

        reference = FirebaseDatabase.getInstance().getReference("Users");

        profile_image = (CircleImageView)view.findViewById(R.id.profile_image);
        profile_email = (TextView)view.findViewById(R.id.profile_email);
        profile_name = (TextView)view.findViewById(R.id.profile_name);
        profile_age = (TextView)view.findViewById(R.id.profile_age);
        profile_phone_num =(TextView)view.findViewById(R.id.profile_phone);

        //프로필 수정
        profile_edit_button = (Button)view.findViewById(R.id.profile_edit_button);
        profile_edit_button.setOnClickListener(this);
        //로그아웃
        logout_btn = (Button)view.findViewById(R.id.logout_button);
        logout_btn.setOnClickListener(this);
        getUserData();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getUserData();
    }

    private void getUserData() {

        reference.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                User userProfile = snapshot.getValue(User.class);

                if(userProfile != null){
                    user_email = userProfile.getEmail();
                    user_name = userProfile.getName();
                    user_age = userProfile.getAge();
                    user_image = userProfile.getImageURL();
                    user_phone_num = userProfile.getPhone_num();

                    storage = FirebaseStorage.getInstance();
                    storageReference = storage.getReference();
                    storageReference.child(user_image).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Picasso.get().load(uri).into(profile_image);
                        }
                    });
                    profile_name.setText(user_name);
                    profile_age.setText(user_age);
                    profile_email.setText(user_email);
                    profile_phone_num.setText(user_phone_num);
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

            case R.id.logout_button:
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                builder.setTitle("로그아웃 하시겠습니까?");

                builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseAuth.getInstance().signOut();
                        Toast.makeText(getContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getActivity(), LoginActivity.class));
                    }
                });
                builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //nothing happen
                    }
                });

                AlertDialog alterDialog = builder.create();
                alterDialog.show();

                break;

            case R.id.profile_edit_button:
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                intent.putExtra("email", user_email);
                intent.putExtra("name", user_name);
                intent.putExtra("age", user_age);
                intent.putExtra("image", user_image);
                intent.putExtra("phone", user_phone_num);
                startActivity(intent);
                break;
        }

    }


}