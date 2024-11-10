package com.example.smartumbrella;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.appcompat.app.AppCompatActivity;
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
    private RecyclerView locationList;
    private LocationAdapter adapter;
    private double latitude, longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 활성화

        // GPS 위치 받기
        latitude = getIntent().getDoubleExtra("latitude", 37.5665);  // 서울의 기본값
        longitude = getIntent().getDoubleExtra("longitude", 126.9780);  // 서울의 기본값

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // RecyclerView 설정
        locationList = findViewById(R.id.location_list);
        locationList.setLayoutManager(new LinearLayoutManager(this));

        // 샘플 위치 데이터 추가
        List<String> locations = new ArrayList<>();
        locations.add("Location 1: 명지전문대");
        locations.add("Location 2: 세절역");
        locations.add("Location 3: 명지대학교");
        locations.add("Location 4: 충암고등학교");
        locations.add("Location 5: 홍대입구역");

        // 어댑터 설정
        adapter = new LocationAdapter(locations);
        locationList.setAdapter(adapter);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions().position(location).title("Last Known Location"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15.0f));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // 뒤로가기 버튼 클릭 시 동작
        return true;
    }
}
