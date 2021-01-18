package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattingapp.Adapter.MessageAdapter;
import com.example.chattingapp.Models.Chat;
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
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    private static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 104;


    private CircleImageView profile_image;
    private TextView user_name;
    private ImageButton send_button, back_button, gallary_button;
    private EditText message;

    private FirebaseUser fuser;
    private DatabaseReference reference;
    private StorageReference storageReference;

    private Intent intent;

    private MessageAdapter messageAdapter;
    private List<Chat> mChat;
    private RecyclerView recyclerView;

    private String userID;

    private Dialog dialog;

    private String currentPhotoPath;

    String imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        user_name = (TextView)findViewById(R.id.user_name);
        message = (EditText)findViewById(R.id.message);
        send_button = (ImageButton)findViewById(R.id.send_button);
        back_button = (ImageButton)findViewById(R.id.back_button);
        gallary_button = (ImageButton)findViewById(R.id.gallary);

        //체팅기록을 보여줄 RecylerView
        recyclerView = (RecyclerView)findViewById(R.id.recyler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        storageReference = FirebaseStorage.getInstance().getReference();

        //UserAdapter로 부터 받아온 사용자정보(***현재 어플을 사용자의 정보가아닌 메세지를 보내고자하는 사용자의 정보)
        intent = getIntent();
        userID = intent.getStringExtra("userID");
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

        gallary_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getImageDialog();

            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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

    private void getImageDialog() {

        dialog = new Dialog(MessageActivity.this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        }
        dialog.setContentView(R.layout.custom_dialogg);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        //customDialog 보여주기
        dialog.show();

        ImageView album = (ImageView)dialog.findViewById(R.id.album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
                else{
                    selectImage();
                }
            }
        });

        ImageView camera = (ImageView)dialog.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(MessageActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MessageActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
                }
                else{
                    dispatchTakePictureIntent();
                }
            }
        });

    }
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.chattingapp.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
            }
        }
    }

    //Codes from Android Developers website (https://developer.android.com/training/camera/photobasics)
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void selectImage() {
        Intent gallary = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(gallary.resolveActivity(getPackageManager()) != null){
            startActivityForResult(gallary, GALLERY_REQUEST_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST_CODE){
            Uri contentUri = data.getData();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
            uploadImageToFirebase(imageFileName, contentUri);
            dialog.dismiss();
        }
        else if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                File f = new File(currentPhotoPath);
                Uri contentUri = Uri.fromFile(f);
                uploadImageToFirebase(f.getName(), contentUri);
                dialog.dismiss();
            }
        }


    }

    //이미지 파이어베이스에 업로드
    private void uploadImageToFirebase(String imageFileName, Uri contentUri) {
        imageUri = contentUri.toString();
        StorageReference image = storageReference.child("images/" + imageFileName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Mylog", "Successfully uploaded an image on Firebase Storage" + uri.toString());
                        sendImage();
                    }
                });
            }
        });

    }

    //이미지 전송하기
    private void sendImage() {


        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");
        HashMap<String,Object> hashMap = new HashMap<>();
        hashMap.put("sender", fuser.getUid());
        hashMap.put("receiver", userID);
        hashMap.put("message", imageUri);
        hashMap.put("type", "image");

        databaseReference.push().setValue(hashMap);

    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == CAMERA_PERM_CODE ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
               // dispatchTakePictureIntent();
            }
            else{
                Toast.makeText(this, "카메라 권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
        else if(grantResults.length > 0 &&requestCode == REQUEST_CODE_STORAGE_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }
            else{
                Toast.makeText(this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    //보낸 메세지 데이터베이스에 저장
    private void sendMessage(String sender, String receiver, String message){

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();

        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", sender);
        hashMap.put("receiver", receiver);
        hashMap.put("message", message);
        hashMap.put("type", "string");


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