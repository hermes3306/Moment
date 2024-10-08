package com.jason.moment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.jason.moment.util.ActivityStat;
import com.jason.moment.util.AlertDialogUtil;
import com.jason.moment.util.C;
import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.DateUtil;
import com.jason.moment.util.MP3;
import com.jason.moment.util.MapUtil;
import com.jason.moment.util.MediaUtil;
import com.jason.moment.util.MyActivity;
import com.jason.moment.util.MyActivityUtil;
import com.jason.moment.util.PermissionUtil;
import com.jason.moment.util.Progress;
import com.jason.moment.util.StartupBatch;
import com.jason.moment.util.StringUtil;
import com.jason.moment.util.db.MyActiviySummary;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import com.jason.moment.util.StravaUploader;

public class FileActivity extends AppCompatActivity implements View.OnClickListener{
    public static String TAG = "FileActivity";
    Context _ctx=null;
    GoogleMap _googleMap;
    ArrayList<String> media_list = null;
    ArrayList<String> pic_list = null;
    StravaUploader stravaUploader = null;

    public String activity_filename = null;

    public static int position = 0;
    public static int filetype = -1;

    public static ArrayList<Marker> markers = null;
    public static ArrayList<MyActivity> mActivityList = new ArrayList<MyActivity>();
    public static boolean tog_add = true;

    public static int marker_pos = 0;
    public static int marker_pos_prev =0;
    public static Polyline line_prev = null;
    public float myzoom = 16f;
    public static Marker last_marker=null;
    public static Marker bef_last_marker=null;
    static ActivityStat activityStat = null;

    public static final int REQUEST_ACTIVITY_FILE_LIST = 0x0001;
    File[] _file_list = null;
    File _file = null;
    MyActivity lastActivity = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        PermissionUtil.getInstance().setPermission(this);
        super.onCreate(savedInstanceState);
        _ctx = this;
        setContentView(R.layout.activity_file);
        stravaUploader = new StravaUploader(_ctx);

        Intent intent = getIntent();
        position = intent.getExtras().getInt("pos");
        filetype = intent.getExtras().getInt("filetype");
        _file_list = MyActivityUtil.getFiles(filetype);

        if(_file_list == null) {
            Toast.makeText(getApplicationContext(),"No files found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        } else if(_file_list.length==0) {
            Toast.makeText(getApplicationContext(), "No files found!", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        _file = _file_list[0];

        final Context _ctx = this;
        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        MapsInitializer.initialize(this);
        mMapView.onCreate(savedInstanceState);  // check required ....
        mMapView.onResume();

        mMapView.getMapAsync(new OnMapReadyCallback() {
            final TextView tv_activity_name = (TextView)findViewById(R.id.name);
            final TextView tv_date_str = (TextView)findViewById(R.id.date_str);

            final TextView memo = findViewById(R.id.memo);
            final TextView weather = findViewById(R.id.weather);
            final TextView co_runner = findViewById(R.id.tv_co_runner);
            final ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
            final ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
            final TextView tv_distance = (TextView) findViewById(R.id.tv_distance);
            final TextView tv_duration = (TextView) findViewById(R.id.tv_duration);
            final TextView tv_minperkm = (TextView) findViewById(R.id.tv_minperkm);
            final TextView tv_carolies = (TextView) findViewById(R.id.tv_carolies);
            final TextView tv_rank = (TextView) findViewById(R.id.tv_rank);
            final TextView tv_rank_range = (TextView) findViewById(R.id.tv_rank_range);
            final TextView tv_medias = (TextView) findViewById(R.id.medias);
            final TextView tv_file_name = (TextView) findViewById(R.id.tv_file_name);
            final TextView tv_file_information = (TextView) findViewById(R.id.tv_file_information);
            final TextView tv_white_km = (TextView) findViewById(R.id.tv_white_km);
            final TextView tv_white_avg = (TextView) findViewById(R.id.tv_white_avg);
            final TextView tv_white_duration = (TextView) findViewById(R.id.tv_white_duration);
            final TextView tv_co_runner = (TextView) findViewById(R.id.tv_co_runner);

            final ImageButton imbt_marker = (ImageButton) findViewById(R.id.imbt_marker);
            final ImageButton imbt_navi = (ImageButton) findViewById(R.id.imbt_navi);
            final ImageButton imbt_trash = (ImageButton) findViewById(R.id.imbt_trash);
            final ImageButton imbt_pop_menu = (ImageButton) findViewById(R.id.imbt_pop_menu);
            final ImageButton imbt_hide_arrow = (ImageButton) findViewById(R.id.imbt_hide_arrow);
            final ImageButton imbt_up = (ImageButton) findViewById(R.id.imbt_up);
            final ImageButton imbt_picture_view = (ImageButton) findViewById(R.id.imbt_picture_view);
            final ImageView iv_main_picture = (ImageView) findViewById(R.id.iv_main_picture);
            ImageButton imbt_satellite_off = (ImageButton)findViewById(R.id.imbt_satellite_off);
            ImageButton imbt_satellite_on = (ImageButton)findViewById(R.id.imbt_satellite_on);
            MapView mapView = (MapView)findViewById(R.id.mapView);
            LinearLayout ll_stat01 = (LinearLayout) findViewById(R.id.ll_stat01);
            LinearLayout ll_stat02 = (LinearLayout) findViewById(R.id.ll_stat02);
            LinearLayout ll_dashboard01 = (LinearLayout) findViewById(R.id.ll_dashboard01);
            LinearLayout ll_dashboard02 = (LinearLayout) findViewById(R.id.ll_dashboard02);

            final File[] flist = MyActivityUtil.getFiles(filetype);

            public void GO(final GoogleMap googleMap, File myfile) {

                if(myfile != null) {
                    mActivityList = MyActivityUtil.deserialize(myfile);
                    _file = myfile;
                    activity_filename = myfile.getName();

                    // media_list checkup
                    media_list = MyActivityUtil.deserializeMediaInfoFromCSV(activity_filename);
                    pic_list = new ArrayList<>();
                    if (media_list == null) {
                        media_list = null;
                        pic_list = null;
                    } else if(media_list.size()==0) {
                        media_list = null;
                        pic_list = null;
                    }else {
                        for(int i=0;i<media_list.size();i++) {
                            if(media_list.get(i).endsWith(Config._pic_ext))  pic_list.add(media_list.get(i));
                        }
                        if(pic_list.size()==0) pic_list=null;
                    }
                }

                if(mActivityList==null) {
                    Log.e(TAG, "-- " + myfile + " failed to be deserialized");
                    return;
                } else if(mActivityList.size()==0) {
                    Log.e(TAG, "-- " + myfile + " serialized successfully but the size is 0");
                    return;
                } else {
                    Log.d(TAG, "-- " + myfile + " is deserialized successfully! with # of " + mActivityList.size());
                }

                if(mActivityList.size()>1) {
                    marker_pos = mActivityList.size()-1;
                }

                MyActivity ta = mActivityList.get(0);
                String date_str = ta.cr_date + " " + ta.cr_time;
                try {
                    activityStat = ActivityStat.getActivityStat(mActivityList);
                }catch(Exception e) {
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    Log.e(TAG, "-- " + sw.toString());
                    myfile.delete();
                }

                if(activityStat !=null) {
                    String _minDist = String.format("%.1f", activityStat.distanceKm);
                    String sinfo = "" + date_str;

                    if(pic_list!=null) {
                        imbt_picture_view.setVisibility(View.VISIBLE);
                    }else {
                        imbt_picture_view.setVisibility(View.GONE);
                        iv_main_picture.setVisibility(View.GONE);
                        mMapView.setVisibility(View.VISIBLE);
                    }

                    tv_activity_name.setText(activityStat.name);
                    tv_date_str.setText(activityStat.date_str);

                    tv_distance.setText(_minDist);
                    tv_duration.setText(activityStat.durationM);
                    tv_minperkm.setText(activityStat.minperKms);
                    tv_carolies.setText("   " + activityStat.calories);

                    tv_co_runner.setText(MyActivityUtil.getRunnerInfo(activity_filename));
                    tv_file_name.setText(activity_filename);

                    tv_white_km.setText(_minDist);
                    tv_white_avg.setText(activityStat.minperKms);
                    tv_white_duration.setText(activityStat.durationM);
                    if(media_list!=null) {
                        tv_medias.setText("" + media_list.size() + "의 사진/동영상이 있습니다.");
                    }else {
                        tv_medias.setText("사진/동영상이 없습니다.");
                    }

                    try {
                        //int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm);
                        int rank = MyActiviySummary.getInstance(_ctx).rank(activityStat.minperKm, activityStat.distanceKm);
                        tv_rank.setText("" + rank + "번째로 빠릅니다.");
                        String range[] = MyActiviySummary.getInstance(_ctx).getStringRange_by_dist(activityStat.distanceKm);
                        tv_rank_range.setText(range[0] + "-" + range[1] + "KM 운동을 비교해 보세요.");
                    }catch(Exception e) {
                        tv_rank.setText("-" + "번째로 빠릅니다.");
                        tv_rank_range.setText("전체 운동을 비교해 보세요.");
                        StringWriter sw = new StringWriter();
                        e.printStackTrace(new PrintWriter(sw));
                        Log.e(TAG, sw.toString());
                    }
                } else {
                    myfile.delete();
                    if(position+1 < flist.length) {
                        GO(googleMap, flist[++position]);
                        return;
                    }
                }
                mMapView.setVisibility(View.VISIBLE);
                iv_main_picture.setVisibility(View.GONE);
                int width = mMapView.getWidth();
                int height = mMapView.getHeight();
                MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);

                iv_main_picture.setVisibility(View.GONE);
                if(C.satellite = false) {
                    ll_stat01.setVisibility(View.GONE);
                    ll_stat02.setVisibility(View.GONE);
                    ll_dashboard01.setVisibility(View.VISIBLE);
                    ll_dashboard02.setVisibility(View.VISIBLE);
                } else {
                    ll_stat01.setVisibility(View.VISIBLE);
                    ll_stat02.setVisibility(View.VISIBLE);
                    ll_dashboard01.setVisibility(View.GONE);
                    ll_dashboard02.setVisibility(View.GONE);
                }

            }

            public void alertDeleteDialog(File file) {
                AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
                builder.setTitle("파일을 삭제하시겠습니까?");
                builder.setMessage("파일을 삭제하시겠습니까?");
                builder.setPositiveButton("삭제",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                file.delete();
                                File[] flist = MyActivityUtil.getFiles(filetype);
                                if(flist==null) {
                                    Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                                    return;
                                }
                                if (position >= 0 && position < flist.length-1) {}
                                else position=0;
                                GO(_googleMap, flist[position]);
                            }
                        });
                builder.setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                builder.show();
            }

            @SuppressLint("MissingPermission")
            @Override
            public void onMapReady(final GoogleMap googleMap) {
                _googleMap = googleMap;
                C.getInstance().setGoogleMap(_ctx, googleMap);

                GO(googleMap, _file);

                imbt_prev.setOnClickListener(new View.OnClickListener(){
                    public void onClick(View view) {

                        File[] flist = MyActivityUtil.getFiles(filetype);
                        if(flist==null) {
                            Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                            return;
                        }

                        if (position > 0 && position <= flist.length) position--;
                        else position=flist.length-1;
                        GO(googleMap, flist[position]);
                    }
                });

                imbt_next.setOnClickListener(new View.OnClickListener(){
                    public void onClick (View view) {
                        File[] flist = MyActivityUtil.getFiles(filetype);
                        if(flist==null) {
                            Toast.makeText(getApplicationContext(),"No more files!",Toast.LENGTH_LONG).show();
                            return;
                        }
                        if (position >= 0 && position < flist.length-1) position++;
                        else position=0;
                        GO(googleMap, flist[position]);
                    }
                });

                imbt_marker.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        C.nomarkers = !C.nomarkers;
                        int width = mMapView.getWidth();
                        int height = mMapView.getHeight();
                        MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);
                    }
                });

                imbt_navi.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        C.notrack = !C.notrack;
                        int width = mMapView.getWidth();
                        int height = mMapView.getHeight();
                        MapUtil.DRAW(_ctx,googleMap,width,height,mActivityList);
                    }
                });

                imbt_pop_menu.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.VISIBLE);
                        imbt_navi.setVisibility(View.VISIBLE);
                        imbt_trash.setVisibility(View.VISIBLE);
                        imbt_up.setVisibility(View.VISIBLE);
                        imbt_hide_arrow.setVisibility(View.VISIBLE);
                        imbt_pop_menu.setVisibility(View.GONE);
                    }
                });

                imbt_trash.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);

                        File[] flist = MyActivityUtil.getFiles(filetype);
                        try {
                            alertDeleteDialog(flist[position]);
                            flist = MyActivityUtil.getFiles(filetype);
                            if (flist.length > 1) position=position;
                            else position=0;
                        }catch(Exception e) {
                            StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            Log.e(TAG,"Err:" + sw.toString());
                        }
                        GO(googleMap, flist[position]);
                    }
                });

                imbt_up.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        CloudUtil.getInstance().Upload(_file_list[position].getName());
                    }
                });

                imbt_hide_arrow.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imbt_marker.setVisibility(View.GONE);
                        imbt_navi.setVisibility(View.GONE);
                        imbt_trash.setVisibility(View.GONE);
                        imbt_up.setVisibility(View.GONE);
                        imbt_hide_arrow.setVisibility(View.GONE);
                        imbt_pop_menu.setVisibility(View.VISIBLE);
                        hide_arrow = !hide_arrow;
                        if(hide_arrow) {
                            imbt_next.setVisibility(View.GONE);
                            imbt_prev.setVisibility(View.GONE);
                        }else{
                            imbt_next.setVisibility(View.VISIBLE);
                            imbt_prev.setVisibility(View.VISIBLE);
                            imbt_prev.setVisibility(View.VISIBLE);
                        }
                    }
                });
            } /* on  MapReady */

        });
    } /* onCreate */

    static boolean hide_arrow = false;
    public static Date getStartTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(0));
        return date;
    }

    public static Date getEndTimeDate(ArrayList<MyActivity> list) {
        if(list == null) return null;
        if(list.size()==0) return null;
        Date date = StringUtil.StringToDate(list.get(list.size()-1));
        return date;
    }

    static int pic_pos=0;
    static int mapViewHeight;
    @Override
    public void onClick(View v) {
        ImageButton imbt_prev = (ImageButton) findViewById(R.id.imbt_prev);
        ImageButton imbt_next = (ImageButton) findViewById(R.id.imbt_next);
        ImageButton imbt_satellite_off = (ImageButton)findViewById(R.id.imbt_satellite_off);
        ImageButton imbt_satellite_on = (ImageButton)findViewById(R.id.imbt_satellite_on);
        MapView mapView = (MapView)findViewById(R.id.mapView);
        LinearLayout ll_stat01 = (LinearLayout) findViewById(R.id.ll_stat01);
        LinearLayout ll_stat02 = (LinearLayout) findViewById(R.id.ll_stat02);
        LinearLayout ll_dashboard01 = (LinearLayout) findViewById(R.id.ll_dashboard01);
        LinearLayout ll_dashboard02 = (LinearLayout) findViewById(R.id.ll_dashboard02);
        final MapView mMapView = (MapView) findViewById(R.id.mapView);
        final ImageView iv_main_picture = (ImageView) findViewById(R.id.iv_main_picture);

        switch (v.getId()) {
            case R.id.name:
                AlertDialogUtil.getInstance().show_activity_file_stat(_ctx,this);
                break;
            case R.id.iv_main_picture:
            case R.id.imbt_picture_view:
                v.setVisibility(View.GONE);
                mMapView.setVisibility(View.GONE);
                iv_main_picture.setVisibility(View.VISIBLE);
                ll_stat01.setVisibility(View.GONE);
                ll_stat02.setVisibility(View.GONE);
                ll_dashboard01.setVisibility(View.VISIBLE);
                ll_dashboard02.setVisibility(View.VISIBLE);
                if(pic_list.size() <= pic_pos) pic_pos = 0;
                MediaUtil.getInstance().showImage(iv_main_picture, pic_list.get(pic_pos));
                if(pic_pos+1 < pic_list.size()) pic_pos++;
                else pic_pos=0;
                break;
            case R.id.tv_rank:
            case R.id.tv_rank_range:
                Log.e(TAG, "-- " + _file_list[position]);
                ArrayList <MyActivity> mal = MyActivityUtil.deserialize(_file_list[position]);
                ActivityStat as = ActivityStat.getActivityStat(mal);
                double distanceKm = as.distanceKm;
                AlertDialogUtil.getInstance().chooseRank(_ctx, distanceKm);
                break;

            case R.id.tv_activity_progress:
                Log.e(TAG, "-- " + _file_list[position]);
                mal = MyActivityUtil.deserialize(_file_list[position]);
                ArrayList<Progress> plist = MyActivityUtil.getProgress(mal);
                for(int i=0;i<plist.size();i++) {
                    Log.d(TAG, "-- " + plist.get(i));
                }
                AlertDialogUtil.getInstance().showProgress(_ctx, plist);
                break;
            case R.id.media_information:
            case R.id.medias:
                AlertDialogUtil.getInstance().showMedias(_ctx,media_list,0);
                break;
            case R.id.tv_file_information:
                File f = new File(Config.CSV_SAVE_DIR, activity_filename);
                String f_name = AlertDialogUtil.getInstance().Rename(_ctx,f);
                final TextView tv_file_name = (TextView) findViewById(R.id.tv_file_name);
                activity_filename = f_name;
                tv_file_name.setText(f_name);
                break;
            case R.id.tv_file_name:
                Intent detailMaps = new Intent(FileActivity.this, DetailMapsActivity.class);
                detailMaps.putExtra("activity_filename", activity_filename);
                startActivity(detailMaps);
                break;

            case R.id.tv_strava_info:
            case R.id.tv_strava:
                uploadToStrava();
                break;

            case R.id.imbt_satellite_on:
                C.satellite = false;
                mMapView.setVisibility(View.VISIBLE);
                iv_main_picture.setVisibility(View.GONE);
                _googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                v.setVisibility(View.GONE);
                imbt_satellite_off.setVisibility(View.VISIBLE);
                ll_stat01.setVisibility(View.VISIBLE);
                ll_stat02.setVisibility(View.VISIBLE);
                ll_dashboard01.setVisibility(View.GONE);
                ll_dashboard02.setVisibility(View.GONE);
                break;
            case R.id.imbt_satellite_off:
                C.satellite= true;
                mMapView.setVisibility(View.VISIBLE);
                iv_main_picture.setVisibility(View.GONE);
                _googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                v.setVisibility(View.GONE);
                imbt_satellite_on.setVisibility(View.VISIBLE);
                ll_stat01.setVisibility(View.GONE);
                ll_stat02.setVisibility(View.GONE);
                ll_dashboard01.setVisibility(View.VISIBLE);
                ll_dashboard02.setVisibility(View.VISIBLE);
                break;
            case R.id.imSetting:
                Log.d(TAG, "-- Setting Activities!");
                Intent configIntent = new Intent(FileActivity.this, ConfigActivity.class);
                configIntent.putExtra("1", 1);
                startActivityForResult(configIntent, Config.CALL_SETTING_ACTIVITY);
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_mapsactivity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case R.id.action_settings:
                Log.d(TAG,"-- Setting Activities!");
                Intent configIntent = new Intent(_ctx, ConfigActivity.class);
                startActivity(configIntent);
                return true;

            case R.id.download_activities:
                new CloudUtil().DownloadAll(_ctx, Config._default_ext);
                return true;

            case R.id.download_images:
                new CloudUtil().DownloadAll(_ctx, Config._img);
                return true;

            case R.id.download_videos:
                new CloudUtil().DownloadAll(_ctx, Config._mov);
                return true;

            case R.id.download_musics:
                new CloudUtil().DownloadAll(_ctx, Config._mp3);
                return true;

            case R.id.upload_activities:
                new CloudUtil().UploadAll(_ctx, Config._default_ext);
                return true;

            case R.id.upload_images:
                new CloudUtil().UploadAll(_ctx, Config._img);
                return true;

            case R.id.upload_videos:
                new CloudUtil().UploadAll(_ctx, Config._mov);
                return true;

            case R.id.upload_musics:
                new CloudUtil().UploadAll(_ctx, Config._mp3);
                return true;

            case R.id.playMp3:
                MP3.shuffleAndPlay(_ctx);
                return true;

            case R.id.stopMp3:
                MP3.stop();
                return true;

            case R.id.mp3Player:
                MP3.showPlayer(_ctx);
                return true;

            case R.id.rebuild_rank:
                new StartupBatch(_ctx).rebuildActivitySummaries(_ctx);
                return true;

            case R.id.activityList:
                File dir = null;
                if(Config._default_ext == Config._csv) dir = Config.CSV_SAVE_DIR;
                else dir = Config.MNT_SAVE_DIR;
                File[] _flist = dir.listFiles();
                String[] fnamelist = new String[_flist.length];
                for(int i=0;i<_flist.length;i++) {
                    fnamelist[i] = _flist[i].getName().substring(0,_flist[i].getName().length()-4);
                }
                AlertDialog.Builder builder = new AlertDialog.Builder(_ctx )
                        .setItems(fnamelist, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(_ctx, MyReportActivity.class);
                                intent.putExtra("activity_file_name", fnamelist[i]);
                                startActivity(intent);
                            }
                        })
                        .setTitle("Choose an activity");
                AlertDialog mSportSelectDialog = builder.create();
                mSportSelectDialog.show();
                return true;

            case R.id.scrollpic_activity:
                Log.d(TAG,"-- Scroll Pic Activity!");
                Intent scrollPicIntent = new Intent(_ctx, Pic_Full_Screen_Activity.class);
                startActivityForResult(scrollPicIntent, Config.CALL_SCROLL_PIC_ACTIVITY);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void uploadToStrava() {
        // Generate GPX file from the activity data
        File gpxFile = generateGpxFile();

        if (gpxFile != null) {
            String dateTimeString = mActivityList.get(0).cr_date + " " + mActivityList.get(0).cr_time;
            SimpleDateFormat parser = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();
            try {
                date = parser.parse(dateTimeString);
            } catch(Exception ignored) {}

            // Get the activity name using the Date object
            String name = DateUtil.getActivityNameInEng(date);

            String description = "Uploaded from MOMENT";
            String activityType = "run"; // or "ride" for cycling, etc.

            stravaUploader.authenticate(gpxFile, name, description, activityType);
        } else {
            Toast.makeText(this, "Failed to generate GPX file", Toast.LENGTH_SHORT).show();
        }
    }

    public File generateGpxFile() {
        return stravaUploader.generateGpxFile(mActivityList);
    }



}
