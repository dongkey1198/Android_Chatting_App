package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Adapter.MessageAdapter;
import com.example.chattingapp.Models.Chat;
import com.example.chattingapp.Models.User;
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

import de.hdodenhof.circleimageview.CircleImageView;

// 사용자가 메세지를 전송할경우
// sender = 현재 어플리케이션을 사용중인 사용자
// reciever = 메세지를 받은 사용자
// message = 메세지
// 들을 데이터 베이스에 "Chats"라는 노드에 저장한다.

//메세지를 읽어들일때는 sender와 reciever안에 저장된 각 사용자들의 아이디값들을 비교하여
//그에해당되는 메세지 내용들반 불러와 화면에 출력해준다.

public class MessageActivity extends AppCompatActivity {

   private CircleImageView profile_image;
   private TextView user_name;
   private ImageButton send_button;
   private EditText message;

   private FirebaseUser fuser;
   private DatabaseReference reference;

   private Intent intent;

   private MessageAdapter messageAdapter;
   private List<Chat> mChat;
   private RecyclerView recyclerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        user_name = (TextView)findViewById(R.id.user_name);
        message = (EditText)findViewById(R.id.message);
        send_button = (ImageButton)findViewById(R.id.send_button);

        //체팅기록을 보여줄 RecylerView
        recyclerView = (RecyclerView)findViewById(R.id.recyler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        //UserAdapter로 부터 받아온 사용자정보(***현재 어플을 사용자의 정보가아닌 메세지를 보내고자하는 사용자의 정보)
        intent = getIntent();
        String userID = intent.getStringExtra("userID");
        fuser = FirebaseAuth.getInstance().getCurrentUser();

        //메세지 전송하기
        send_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = message.getText().toString();
                if(!msg.equals("")){
                    sendMessage(fuser.getUid(), userID, msg);
                }
                else{
                    Toast.makeText(MessageActivity.this, "입력값이 없습니다!!!", Toast.LENGTH_SHORT).show();
                }

                message.setText("");

            }
        });

        //메세지를 받는 사용자의 프로필 사진과 이름을 데이터베이스로 부터 불러온다.
        reference = FirebaseDatabase.getInstance().getReference("Users").child(userID);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                user_name.setText(user.getName());

                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.drawable.default_img);
                }
                else{
                    Glide.with(MessageActivity.this).load(user.getImageURL()).into(profile_image);
                }

                readMessage(fuser.getUid(), userID , user.getImageURL());

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //보낸 메세지 데이터베이스에 저장
    private void sendMessage(String sender, String receiver, String message){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);

        databaseReference.child("Chats").push().setValue(hashMap);

    }

    //모든 메세지 내용을 읽어온다.
    private void readMessage(String myid, String userid, String imageurl){

        mChat = new ArrayList<>();

        reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    Chat chat = dataSnapshot.getValue(Chat.class);

                    assert chat != null;
                    if(chat.getReceiver().equals(myid) && chat.getSender().equals(userid) ||
                    chat.getReceiver().equals(userid) && chat.getSender().equals(myid)){
                       mChat.add(chat);
                    }
                }
                messageAdapter = new MessageAdapter(MessageActivity.this, mChat, imageurl);
                recyclerView.setAdapter(messageAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

}