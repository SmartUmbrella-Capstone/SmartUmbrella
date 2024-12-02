package com.example.smartumbrella;

import android.database.Cursor;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private RecyclerView locationList;
    private LocationAdapter adapter;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 활성화
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // DatabaseHelper 초기화
        dbHelper = new DatabaseHelper(this);

        // 지도 초기화
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // RecyclerView 설정
        locationList = findViewById(R.id.location_list);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        // 데이터베이스에서 위치 로그 가져오기
        List<LocationLog> locationLogs = getLocationLogs();

        // 어댑터 설정 및 클릭 이벤트 처리
        adapter = new LocationAdapter(locationLogs, locationLog -> {
            // 클릭된 항목의 위치를 지도에 표시
            if (mMap != null) {
                LatLng position = new LatLng(locationLog.getLatitude(), locationLog.getLongitude());
                mMap.clear(); // 기존 마커 삭제
                mMap.addMarker(new MarkerOptions().position(position).title("Alert at: " + locationLog.getTimestamp()));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15.0f));
            }
        });
        locationList.setAdapter(adapter);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        // 데이터베이스에서 위치 로그 가져오기
        List<LocationLog> locationLogs = getLocationLogs();

        // 지도에 마커 추가
        for (LocationLog log : locationLogs) {
            LatLng position = new LatLng(log.getLatitude(), log.getLongitude());
            mMap.addMarker(new MarkerOptions().position(position).title("Alert at: " + log.getTimestamp()));
        }

        // 첫 번째 위치로 카메라 이동
        if (!locationLogs.isEmpty()) {
            LatLng firstPosition = new LatLng(locationLogs.get(0).getLatitude(), locationLogs.get(0).getLongitude());
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(firstPosition, 15.0f));
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // 뒤로가기 버튼 클릭 시 동작
        return true;
    }

    // 데이터베이스에서 위치 로그 가져오기
    private List<LocationLog> getLocationLogs() {
        List<LocationLog> logs = new ArrayList<>();
        Cursor cursor = dbHelper.getLocationLogs();

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow("timestamp"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("longitude"));
                logs.add(new LocationLog(timestamp, latitude, longitude));
            }
            cursor.close();
        }
        return logs;
    }
}
