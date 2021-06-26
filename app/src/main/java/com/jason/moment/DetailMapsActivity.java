package com.jason.moment;

import androidx.fragment.app.FragmentActivity;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodecInfo;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jason.moment.databinding.ActivityDetailMapsBinding;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.ActivitySummary;
import com.jason.moment.util.AddressUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.MyMediaInfo;
import com.jason.moment.util.PermissionUtil;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyMedia;

import java.util.ArrayList;
import java.util.Date;

public class DetailMapsActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener{

    private GoogleMap mMap;
    private ActivityDetailMapsBinding binding;
    private String activity_filename = null;
    private MyMediaInfo mm = null;
    Context _ctx = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        _ctx = this;
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        activity_filename = intent.getExtras().getString("activity_filename");
        mm = (MyMediaInfo)intent.getSerializableExtra("my_media_info");

        binding = ActivityDetailMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        C.getInstance().setGoogleMap(_ctx, mMap);

        // Add a marker in Sydney and move the camera
//        LatLng sydney = new LatLng(-34, 151);
//        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        if(activity_filename != null ) {
            ArrayList<MyActivity> mal = MyActivityUtil.deserialize(activity_filename);
            ActivityStat as = ActivityStat.getInstance().stat(mal);
            TextView tv_media_memo = findViewById(R.id.tv_media_memo);
            tv_media_memo.setText(as.name);
            TextView tv_media_cr_datetime = findViewById(R.id.tv_media_cr_datetime);
            tv_media_cr_datetime.setText(as.date_str);
            Display display = getWindowManager().getDefaultDisplay();
            MapUtil.DRAW(this, mMap, display, mal);
        } else if(mm != null) {
            LatLng ll = new LatLng(mm.getLatitude(), mm.getLongitude());
            Date date = StringUtil.StringToDate(mm.getCr_datetime(),"yyyy-MM-dd HH:mm:ss");
            MyActivity ma = new MyActivity(ll.latitude, ll.longitude, date);

            MapUtil.drawMarker(mMap, mm);
            MapUtil.moveCamera(mMap ,mm ,18f);

            TextView tv_media_memo = findViewById(R.id.tv_media_memo);
            tv_media_memo.setText(mm.getMemo());
            TextView tv_media_cr_datetime = findViewById(R.id.tv_media_cr_datetime);
            tv_media_cr_datetime.setText(mm.getCr_datetime());

        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_search_address:
                EditText et = findViewById(R.id.ed_address);
                String address = et.getText().toString();
                LatLng ll = AddressUtil.getLatLangFromAddress(_ctx, address);
                if(ll != null) {
                    if(ll.latitude != -10000) {
                        if(mm==null) {
                            MyMediaInfo mm2 = new MyMediaInfo();
                            mm2.setLatitude(ll.latitude);;
                            mm2.setLongitude(ll.longitude);
                            mm2.setMemo(address);
                            mm2.setName(address);
                            MapUtil.drawMarker(mMap, mm);
                            MapUtil.moveCamera(mMap, mm, 18f);
                            MapUtil.moveCamera(mMap, mm, 18f);
                        }else {
                            mm.setLatitude(ll.latitude);
                            mm.setLongitude(ll.longitude);
                            mm.setMemo(et.getText().toString());
                            address = AddressUtil.getAddress(_ctx, ll);
                            mm.setAddress(address);
                            MyMedia.getInstance(_ctx).ins(mm);
                            Toast.makeText(_ctx,"The address of ("+ address +") for this media saved!", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
            case 1:
                break;
        }
    }
}