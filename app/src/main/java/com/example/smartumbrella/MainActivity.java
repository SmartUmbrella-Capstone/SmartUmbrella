package com.example.smartumbrella;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 버튼 초기화
        Button buttonMap = findViewById(R.id.button1);
        Button buttonSettings = findViewById(R.id.button2);
        Button buttonCancel = findViewById(R.id.buttonCancel); // 연결 취소 버튼

        // 지도 버튼 클릭 리스너
        buttonMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MapActivity.class);
                startActivity(intent);
            }
        });

        // 사용자 설정 버튼 클릭 리스너
        buttonSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SettingActivity.class);
                startActivity(intent);
            }
        });

        // 연결 취소 버튼 클릭 리스너
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDisconnectDialog(); // 연결 해제 확인 다이얼로그 표시
            }
        });
    }

    // 연결 해제 확인 다이얼로그
    private void showDisconnectDialog() {
        new AlertDialog.Builder(this)
                .setTitle("연결 해제")
                .setMessage("정말 연결을 해제하시겠습니까?")
                .setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // SubMainActivity로 이동
                        Intent intent = new Intent(MainActivity.this, SubMainActivity.class);
                        startActivity(intent);
                        finish(); // MainActivity 종료
                    }
                })
                .setNegativeButton("아니오", null) // 액션 없이 다이얼로그 닫기
                .show();
    }
}
