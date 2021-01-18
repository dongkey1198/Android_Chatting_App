package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
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
import com.google.android.gms.tasks.OnCompleteListener;
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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.EventListener;
import java.util.HashMap;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingActivity extends AppCompatActivity implements View.OnClickListener{


    private static final int CAMERA_PERM_CODE = 101;
    public static final int CAMERA_REQUEST_CODE = 102;
    public static final int GALLERY_REQUEST_CODE = 103;
    public static final int REQUEST_CODE_STORAGE_PERMISSION = 104;

    private String currentPhotoPath;

    private TextView profile_email;
    private EditText profile_name, profile_age, profile_phone_num;
    private CircleImageView profile_image;
    private Button back_button, profile_edit_button;
    private Dialog dialog;

    private FirebaseUser fuser;
    private StorageReference storageReference;

    private String currentUri;
    private String mUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        profile_email = (TextView)findViewById(R.id.profile_email);
        profile_name = (EditText)findViewById(R.id.profile_name);
        profile_age = (EditText)findViewById(R.id.profile_age);
        profile_phone_num = (EditText)findViewById(R.id.profile_phone);


        storageReference = FirebaseStorage.getInstance().getReference();

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

        //customDialog 보여주기
        dialog.show();

        //디바이스 앨범에서 사진읽어오기
        ImageView album = (ImageView)dialog.findViewById(R.id.album);
        album.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                }
                else{
                    selectImage();
                }
            }
        });

        // 카메라를 통해 사진 읽어오기
        ImageView camera = (ImageView)dialog.findViewById(R.id.camera);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askCameraPermissions();
            }
        });

    }

    private void selectImage() {
        Intent gallary = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if(gallary.resolveActivity(getPackageManager()) != null){
            startActivityForResult(gallary, GALLERY_REQUEST_CODE);
        }
    }

    private void askCameraPermissions() {
        if(ContextCompat.checkSelfPermission(SettingActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(SettingActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERM_CODE);
        }
        else{
            dispatchTakePictureIntent();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == CAMERA_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){

                File f = new File(currentPhotoPath);
                profile_image.setImageURI(Uri.fromFile(f));
                Uri contentUri = Uri.fromFile(f);
                //imageUri = contentUri;
                Log.d("Mylog", "Absolute Url of Captured Image: " + Uri.fromFile(f));

                uploadImageToFirebase(f.getName(), contentUri);

            }
        }
        else if(requestCode == GALLERY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK){
                Uri contentUri = data.getData();
                //imageUri = contentUri;
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                String imageFileName = "JPEG_" + timeStamp + "." + getFileExt(contentUri);
                profile_image.setImageURI(contentUri);

                Log.d("Mylog", "Absolute Url of Gallary Image: " + imageFileName);

                uploadImageToFirebase(imageFileName, contentUri);
            }
        }
        dialog.dismiss();
    }

    private String getFileExt(Uri contentUri) {
        ContentResolver c = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(c.getType(contentUri));
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == CAMERA_PERM_CODE ){
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                dispatchTakePictureIntent();
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

    private void uploadImageToFirebase(String imageFileName, Uri contentUri) {
        //mUri = contentUri.toString();
        mUri = contentUri.toString();
        StorageReference image = storageReference.child("images/" + imageFileName);
        image.putFile(contentUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                image.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Log.d("Mylog", "Successfully uploaded an image on Firebase Storage" + uri.toString());

                    }
                });
            }
        });
    }

    //사용자 정보 업데이트
    private void upDateUserInfo() {

        if(mUri == null){
            mUri = currentUri;
        }
        else{

            fuser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users");
            Log.d("mylog", "imguri: " + mUri);

            HashMap<String, Object> hashMap = new HashMap<>();
            hashMap.put("imageURL", mUri);
            hashMap.put("name", profile_name.getText().toString());
            hashMap.put("age", profile_age.getText().toString());
            hashMap.put("phone_num", profile_phone_num.getText().toString());

            reference.child(fuser.getUid()).updateChildren(hashMap);
            Toast.makeText(SettingActivity.this, "프로필 수정 완료", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    // 기존의 사용자 정보를 ProfileFragment 로 부터 전달받아 화면에 출력한다.
    private void getCurrentData() {

        Intent intent = getIntent();

        String email = intent.getStringExtra("email");
        String name = intent.getStringExtra("name");
        String age = intent.getStringExtra("age");
        String image = intent.getStringExtra("image");
        String phone = intent.getStringExtra("phone");

        currentUri = image;

        if(image.equals("default")){
                profile_image.setImageResource(R.drawable.default_img);
        }
        else{
            Glide.with(SettingActivity.this).load(image).into(profile_image);
        }
        profile_email.setText(email);
        profile_name.setText(name);
        profile_age.setText(age);
        profile_phone_num.setText(phone);
    }
}