package com.example.chattingapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button login;
    private Button register;
    private TextView find_pwd;
    private EditText et_id, et_pwd;
    private ProgressBar progressBar;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        find_pwd = (TextView)findViewById(R.id.find_pwd);
        et_id = (EditText)findViewById(R.id.login_id);
        et_pwd = (EditText)findViewById(R.id.login_password);
        progressBar = (ProgressBar)findViewById(R.id.login_progressbar);

        login = (Button) findViewById(R.id.login_button);
        login.setOnClickListener(this);

        register = (Button) findViewById(R.id.register_button);
        register.setOnClickListener(this);

        find_pwd.setOnClickListener(this);

        getLoginInfo();

    }// End of OnCreate

    private void getLoginInfo() {
        //회원 가입이 성공할경우 intent객체를 통해 돌아온 데이터들을 추출하여
        //id와 password 정보를 입력한다.
        Intent intent = getIntent();

        String id = intent.getStringExtra("id");
        String pwd = intent.getStringExtra("pwd");

        et_id.setText(id);
        et_pwd.setText(pwd);
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.register_button:
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class),0);
                break;

            case R.id.login_button:
                userLogin();
                break;

            case R.id.find_pwd:
                startActivity(new Intent(LoginActivity.this, ResetPwdActivity.class));
                break;
        }
    }

    private void userLogin() {

        String email = et_id.getText().toString().trim();
        String password = et_pwd.getText().toString().trim();

        if(email.isEmpty()){
            et_id.setError("아이디를 입력하세요!");
            et_id.requestFocus();
            return;
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            et_id.setError("형식이 올바르지 않습니다.");
            et_id.requestFocus();
            return;
        }
        if(password.isEmpty()){
            et_pwd.setError("비밀번호를 입력하세요!");
            et_pwd.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()){

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                    if(user.isEmailVerified()){
                        Toast.makeText(LoginActivity.this, "성공적으로 로그인 하였습니다.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                    else{
                        user.sendEmailVerification();
                        Toast.makeText(LoginActivity.this, "이메일 인증이 필요합니다.", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.GONE);
                    }

                }
                else{
                    Toast.makeText(LoginActivity.this, "입력하신 정보가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            }
        });
    }
}
