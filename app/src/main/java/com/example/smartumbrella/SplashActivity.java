package com.example.smartumbrella;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // 스플래시 화면 레이아웃

        // 3초 후에 SubMainActivity로 이동
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashActivity.this, SubMainActivity.class);
                startActivity(intent);
                finish();
            }
        }, 3000);
    }
}
