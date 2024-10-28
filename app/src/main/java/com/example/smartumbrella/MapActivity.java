package com.example.smartumbrella;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import androidx.annotation.NonNull;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        // Toolbar 설정
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);  // 뒤로가기 버튼 활성화

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().
                findFragmentById(R.id.map);
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
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng seoul = new LatLng( 37.585187047530816, 126.92491924526931);
        googleMap.addMarker(new MarkerOptions().position(seoul).title("Maker in Seoul"));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(seoul));
        float zoomLevel = 15.0f; // Adjust this value for the desired zoom level
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, zoomLevel));

    }
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();  // 뒤로가기 버튼 클릭 시 동작
        return true;
    }
}