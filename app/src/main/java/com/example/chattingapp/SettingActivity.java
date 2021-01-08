package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.InputStream;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    private static final int REQEUST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQEUST_CODE_SELECT_IMAGE = 2;
    private static final int REQEUST_CODE_CAPTURE_IMAGE = 3;

    private FirebaseUser user;
    private DatabaseReference reference;
    private FirebaseStorage storage;
    private StorageReference storageReference;


    private TextView profile_email;
    private EditText profile_name, profile_age, profile_phone_num;
    private CircleImageView profile_image;
    private Button back, profile_edit_button;
    private Dialog dialog;

    private Uri imageUri;
    private String _email, _name, _age, _image,  _phone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profile_email = (TextView)findViewById(R.id.profile_email);
        profile_name = (EditText)findViewById(R.id.profile_name);
        profile_age = (EditText)findViewById(R.id.profile_age);
        profile_phone_num = (EditText)findViewById(R.id.profile_phone);

        profile_image = (CircleImageView)findViewById(R.id.profile_image);
        profile_image.setOnClickListener(this);

        profile_edit_button = (Button)findViewById(R.id.profile_edit_btn);
        profile_edit_button.setOnClickListener(this);

        back = (Button)findViewById(R.id.profile_exit);
        back.setOnClickListener(this);

        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();


        getCurrentData();
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

        ImageView album = (ImageView)dialog.findViewById(R.id.album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //앨범에서 이미지를 가져오려면 External Stroage에 접속할수있게 권한을 줘야한다.
                if(ContextCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(SettingActivity.this,
                            new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQEUST_CODE_STORAGE_PERMISSION);
                } else{
                    selectImage();
                }
            }
        });

        ImageView camera = (ImageView)dialog.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if(intent.resolveActivity(getPackageManager()) != null){
                    startActivityForResult(intent, REQEUST_CODE_CAPTURE_IMAGE);
                }
            }
        });

        dialog.show();


    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(intent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(intent, REQEUST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQEUST_CODE_SELECT_IMAGE && resultCode == RESULT_OK){
            if(data != null){
                //Uri selectedImage = data.getData();
                imageUri = data.getData();
                if(imageUri != null){
                    try{

                        InputStream inputStream = getContentResolver().openInputStream(imageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        profile_image.setImageBitmap(bitmap);
                        dialog.dismiss();

                    }catch (Exception e){
                        Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
        else if(requestCode == REQEUST_CODE_CAPTURE_IMAGE && resultCode == RESULT_OK){

            if(data != null){
                //수정해야함
                imageUri = data.getData();

                Bundle capturedImage = data.getExtras();
                Bitmap bitmap = (Bitmap)capturedImage.get("data");
                profile_image.setImageBitmap(bitmap);
                dialog.dismiss();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == REQEUST_CODE_STORAGE_PERMISSION && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                selectImage();
            }else{
                Toast.makeText(SettingActivity.this, "권한이 필요합니다.", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void upDateUserInfo() {

        upLoadImg();
        _email = profile_email.getText().toString();
        _name = profile_name.getText().toString();
        _age = profile_age.getText().toString();
        _phone = profile_phone_num.getText().toString();

        HashMap<String, Object> user_profile = new HashMap<String, Object>();
        user_profile.put("email", _email);
        user_profile.put("name", _name);
        user_profile.put("age", _age);
        user_profile.put("phone_num", _phone);
        user_profile.put("imageURL", _image);

        user = FirebaseAuth.getInstance().getCurrentUser();
        String userID = user.getUid();

        reference = FirebaseDatabase.getInstance().getReference("Users");
        reference.child(userID).updateChildren(user_profile);

        Toast.makeText(this, "프로필이 업데이트 되었습니다.", Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(SettingActivity.this, MainActivity.class);
//        startActivity(intent);
        finish();

    }

    private void upLoadImg() {

        if(imageUri == null){
            _image = "default";
        }
        else{
            final String randomKey = UUID.randomUUID().toString();
            _image = "images/"+randomKey;
            StorageReference riversRef = storageReference.child("images/" +randomKey);
            riversRef.putFile(imageUri);
        }
    }

    private void getCurrentData() {

        Intent intent = getIntent();

        String email = intent.getStringExtra("email");
        String name = intent.getStringExtra("name");
        String age = intent.getStringExtra("age");
        String image = intent.getStringExtra("image");
        String phone = intent.getStringExtra("phone");

        if(image.equals("default")){
            storageReference.child("default_profile_img/default_img.png")
                    .getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    Picasso.get().load(uri).into(profile_image);
                }
            });
        }

        profile_email.setText(email);
        profile_name.setText(name);
        profile_age.setText(age);
        profile_phone_num.setText(phone);

    }
}