package com.example.chattingapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.remoteconfig.FirebaseRemoteConfig;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private Button login;
    private Button register;
    private TextView find_pwd;
    private EditText et_id, et_pwd;

    private FirebaseRemoteConfig mFirebaseRemoteConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        String splash_background = mFirebaseRemoteConfig.getString("splash_background");

        //StatusBar의 색상을 변경하는 코드이다 버전 lollipop부터
        //사용가능하기때문에 이런식으로 버전에 해당할경우 이코드를 적용시키도록 설정한다.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(Color.parseColor(splash_background));
        }

        login = (Button) findViewById(R.id.login_button);
        register = (Button) findViewById(R.id.register_button);
        find_pwd = (TextView)findViewById(R.id.find_pwd);
        et_id = (EditText)findViewById(R.id.login_id);
        et_pwd = (EditText)findViewById(R.id.login_password);

        //회원 가입이 성공할경우 intent객체를 통해 돌아온 데이터들을 추출하여
        //id와 password 정보를 입력한다.
        Intent intent = getIntent();

        String id = intent.getStringExtra("id");
        String pwd = intent.getStringExtra("pwd");

        et_id.setText(id);
        et_pwd.setText(pwd);

        register.setOnClickListener(this);
    }// End of OnCreate

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.register_button:
                startActivityForResult(new Intent(LoginActivity.this, RegisterActivity.class),0);
                break;
        }
    }
}
