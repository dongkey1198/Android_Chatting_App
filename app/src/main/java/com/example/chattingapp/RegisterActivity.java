package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaCodec;
import android.os.Bundle;

import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.regex.Pattern;

//2020-01-06
//파이어베이스 Real Time Database 와 Authentication 을 이용하여 Register page 및 기능 구현

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{

    private FirebaseAuth mAuth;

    private EditText et_email, et_pwd, et_pwd_check, et_name, et_age;
    private Button login_button, register_button;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Edit Texts
        et_email = (EditText) findViewById(R.id.register_email);
        et_pwd = (EditText) findViewById(R.id.register_password);
        et_pwd_check = (EditText) findViewById(R.id.register_password_check);
        et_name = (EditText) findViewById(R.id.register_name);
        et_age = (EditText) findViewById(R.id.register_age);

        // Buttons
        register_button = (Button) findViewById(R.id.register_button);
        register_button.setOnClickListener(this);

        login_button = (Button) findViewById(R.id.login_button);
        login_button.setOnClickListener(this);

        // Progress Bar
        progressBar = (ProgressBar)findViewById(R.id.register_progressbar);

    }// End of OnCreate

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.register_button:
                registerNewUser();
                break;

            case R.id.login_button:
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }
    }

    private void registerNewUser() {

        String email = et_email.getText().toString().trim();
        String name = et_name.getText().toString().trim();
        String age = et_age.getText().toString().trim();
        String pwd = et_pwd.getText().toString().trim();
        String pwd2 = et_pwd_check.getText().toString().trim();

        if(email.isEmpty()){
            et_email.setError("이메일을 입력하세요!");
            et_email.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_email.setError("형식이 올바르지 않습니다.");
            et_email.requestFocus();
            return;
        }
        if(name.isEmpty()){
            et_name.setError("이름을 입력하세요!");
            et_name.requestFocus();
            return;
        }
        if(age.isEmpty()){
            et_age.setError("나이를 입력하세요!");
            et_age.requestFocus();
            return;
        }
        if(pwd.isEmpty()){
            et_pwd.setError("비밀번호를 입력하세요!");
            et_pwd.requestFocus();
            return;
        }
        if(pwd2.isEmpty()){
            et_pwd_check.setError("비밀번호를 입력하세요!");
            et_pwd_check.requestFocus();
            return;
        }
        if(!pwd.equals(pwd2)){
            et_pwd.setError("입력한 비밀번호가 일치하지 않습니다.");
            et_pwd.requestFocus();
            return;
        }
        if(pwd.length()<6){
            et_pwd.setError("비밀번호는 6자리 이상이어야 합니다");
            et_pwd.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.createUserWithEmailAndPassword(email, pwd)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            User user = new User(name, age, email);
                            FirebaseDatabase.getInstance().getReference("Users")
                                    .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                    .setValue(user).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful()){
                                        Toast.makeText(RegisterActivity.this, "회원가입이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);

                                        //회원가입 성공시 로그인 페이지로 이동
                                        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                        intent.putExtra("id", email);
                                        intent.putExtra("pwd", pwd);
                                        startActivity(intent);
                                    }
                                    else{
                                        Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                        }
                        else{
                            Toast.makeText(RegisterActivity.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });


    }

}

