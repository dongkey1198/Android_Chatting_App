package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.EventLog;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Base64;
import java.util.EventListener;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQEUST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQEUST_CODE_SELECT_IMAGE = 2;
    private static final int REQEUST_CODE_CAPTURE_IMAGE = 3;

    private TextView profile_email;
    private EditText profile_name, profile_age, profile_phone_num;
    private CircleImageView profile_image;
    private Button back_button, profile_edit_button;
    private Dialog dialog;

    private DatabaseReference reference;
    private FirebaseUser fuser;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri;
    private Uri downloadUri;
    private Uri currentUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profile_email = (TextView)findViewById(R.id.profile_email);
        profile_name = (EditText)findViewById(R.id.profile_name);
        profile_age = (EditText)findViewById(R.id.profile_age);
        profile_phone_num = (EditText)findViewById(R.id.profile_phone);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // 이미지 클릭시 다이얼로그 나옴
        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        profile_image.setOnClickListener(this);

        //수정 완료후 업데이트
        profile_edit_button = (Button)findViewById(R.id.profile_edit_btn);
        profile_edit_button.setOnClickListener(this);

        // 이전페이지로 돌아가기
        back_button = (Button)findViewById(R.id.profile_exit);
        back_button.setOnClickListener(this);

        //기존 데이터 가져오기
        getCurrentData();
        getPerMissions();
    }

    private void getPerMissions() {
        //앨범에서 이미지를 가져오려면 External Stroage에 접속할수있게 권한을 줘야한다.
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    REQEUST_CODE_STORAGE_PERMISSION);
        }

        if(ContextCompat.checkSelfPermission(SettingActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.CAMERA}, REQEUST_CODE_STORAGE_PERMISSION);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.profile_image:
                getImageUpdateDialog();
                break;

            case R.id.profile_edit_btn:
                upDateUserInfo();
                break;

            case R.id.profile_exit:
                finish();
                break;
        }
    }

    //다이얼로그를 통해 앨범에서 이미지를 업데결정
    //카메라를 통해 이미지를 업데이트 할건지 결정
    private void getImageUpdateDialog() {

        dialog = new Dialog(SettingActivity.this);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            dialog.getWindow().setBackgroundDrawable(getDrawable(R.drawable.background));
        }
        dialog.setContentView(R.layout.custom_dialogg);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        //디바이스 앨범에서 사진읽어오기
        ImageView album = (ImageView)dialog.findViewById(R.id.album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               openImage();
            }
        });

        // 카메라를 통해 사진 읽어오기
        ImageView camera = (ImageView)dialog.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                captureImage();
            }
        });

        //customDialog 보여주기
        dialog.show();

    }

    //앨범에서 이미지 가져오기
    private void openImage(){

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQEUST_CODE_SELECT_IMAGE);
        }
    }

    private void captureImage() {

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQEUST_CODE_CAPTURE_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQEUST_CODE_SELECT_IMAGE && data != null && data.getData() != null){

            imageUri = data.getData();
            Log.d("mylog", "albume: " + imageUri);
            profile_image.setImageURI(imageUri);
            dialog.dismiss();

            uploadPicture();

        }
        else if(requestCode == REQEUST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK){

            Bitmap image = (Bitmap)data.getExtras().get("data");
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte b[] = stream.toByteArray();
            profile_image.setImageBitmap(image);

            final String randomKey = UUID.randomUUID().toString();
            StorageReference riversRef = storageReference.child("images/" + randomKey);
            riversRef.putBytes(b).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            Log.d("mylog", "uri: " + uri);
                            imageUri = uri;
                        }
                    });
                }
            });

            dialog.dismiss();

        }
    }

    //Firebase Storage에 이미지 저장
    private void uploadPicture() {

        final String randomKey = UUID.randomUUID().toString();
        StorageReference riversRef = storageReference.child("images/" + randomKey);
        riversRef.putFile(imageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {

                    }
                });
    }

    //사용자 정보 업데이트
    private void upDateUserInfo() {

        if(imageUri == null){
            imageUri = currentUri;
        }

        fuser = FirebaseAuth.getInstance().getCurrentUser();
        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(fuser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                String imguri = imageUri.toString();
                Log.d("mylog", "imguri: " + imguri);

                HashMap<String, Object> hashMap = new HashMap<>();
                hashMap.put("imageURL", imguri);
                hashMap.put("name", profile_name.getText().toString());
                hashMap.put("age", profile_age.getText().toString());
                hashMap.put("phone_num", profile_phone_num.getText().toString());

                reference.child(fuser.getUid()).updateChildren(hashMap);

                Toast.makeText(SettingActivity.this, "프로필 수정 완료", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    // 기존의 사용자 정보를 ProfileFragment 로 부터 전달받아 화면에 출력한다.
    private void getCurrentData() {
        Intent intent = getIntent();

        String email = intent.getStringExtra("email");
        String name = intent.getStringExtra("name");
        String age = intent.getStringExtra("age");
        String image = intent.getStringExtra("image");
        String phone = intent.getStringExtra("phone");

        Uri imguri = Uri.parse(image);
        currentUri = imguri;

        if(image.equals("default")){
                profile_image.setImageResource(R.drawable.default_img);
        }
        else{
            Glide.with(SettingActivity.this).load(imguri).into(profile_image);
        }
        profile_email.setText(email);
        profile_name.setText(name);
        profile_age.setText(age);
        profile_phone_num.setText(phone);

    }
}