package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;


public class ResetPwdActivity extends AppCompatActivity {

    private EditText login_id;
    private Button reset_pwd_button, back_button;

    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        login_id = (EditText)findViewById(R.id.login_id);
        reset_pwd_button = (Button)findViewById(R.id.reset_pwd_button);
        back_button = (Button)findViewById(R.id.back_button);

        auth = FirebaseAuth.getInstance();

        getEmailAddress();

        reset_pwd_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });

        back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void getEmailAddress() {
        Intent intent = getIntent();
        String email = intent.getStringExtra("email");
        login_id.setText(email);
    }

    private void resetPassword() {

        String email = login_id.getText().toString().trim();

        if(email.isEmpty()){
            login_id.setError("이메일을 입력하세요");
            login_id.requestFocus();
            return;
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            login_id.setError("형식이 올바르지 않습니다.");
            login_id.requestFocus();
            return;
        }

        auth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(ResetPwdActivity.this, "비밀번호 변경을 위해 이메일을 확인하세요", Toast.LENGTH_SHORT).show();
                finish();
            }
        });


    }
}