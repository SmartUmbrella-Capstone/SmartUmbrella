package com.example.smartumbrella;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap gMap;
    private ArrayList<LatLng> markList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("구글 지도 활용");

        // SupportMapFragment 사용
        SupportMapFragment mapFrag = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFrag != null) {
            mapFrag.getMapAsync(this);
        }

        markList = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap map) {
        gMap = map;
        gMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        gMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(37.568256, 126.897240), 15));
        gMap.getUiSettings().setZoomControlsEnabled(true);

        gMap.setOnMapClickListener(latLng -> {
            // 기본 마커 추가
            gMap.addMarker(new MarkerOptions().position(latLng).title("마커 위치"));
            markList.add(latLng); // 위치 리스트에 추가
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 1, 0, "위성 지도");
        menu.add(0, 2, 0, "일반 지도");
        menu.add(0, 3, 0, "바로전 마크 지우기");
        menu.add(0, 4, 0, "모든 마크 지우기");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                return true;
            case 2:
                gMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                return true;
            case 3:
                if (!markList.isEmpty()) {
                    markList.remove(markList.size() - 1);
                }
                gMap.clear();
                for (LatLng latLng : markList) {
                    gMap.addMarker(new MarkerOptions().position(latLng).title("마커 위치"));
                }
                return true;
            case 4:
                gMap.clear();
                markList.clear(); // 모든 마크 지우기 후 리스트도 비우기
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        // 필요 시 구현
    }
}
