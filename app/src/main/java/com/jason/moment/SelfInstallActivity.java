package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SelfInstallActivity extends AppCompatActivity {
    class PInfo {
        private String appname = "";
        private String pname = "";
        private String versionName = "";
        private int versionCode = 0;
        //private Drawable icon;
        /*private void prettyPrint() {
            //Log.v(appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
        }*/
    }
    public int VersionCode;
    public String VersionName="";
    public String ApkName ;
    public String AppName ;
    public String BuildVersionPath="";
    public String urlpath ;
    public String PackageName;
    public String InstallAppPackageName;
    public String Text="";

    TextView tvApkStatus;
    Button btnCheckUpdates;
    TextView tvInstallVersion;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_install);

        //Text= "Old".toString();
        Text= "New".toString();


        ApkName = "moment.apk";//"Test1.apk";// //"DownLoadOnSDcard_01.apk"; //
        AppName = "moment";//"Test1"; //cd A

        BuildVersionPath = "http://ezehub.club/moment/Version.txt".toString();
        PackageName = "package:com.jason.moment".toString();
        urlpath = "http://ezehub.club/moment/"+ Text.toString()+"_Apk/" + ApkName.toString();

        tvApkStatus =(TextView)findViewById(R.id.tvApkStatus);
        tvApkStatus.setText(Text+" Apk Download.".toString());


        tvInstallVersion = (TextView)findViewById(R.id.tvInstallVersion);
        String temp = getInstallPackageVersionInfo(AppName.toString());
        tvInstallVersion.setText("" +temp.toString());

        btnCheckUpdates =(Button)findViewById(R.id.btnCheckUpdates);
        btnCheckUpdates.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                GetVersionFromServer(BuildVersionPath);

                if(checkInstalledApp(AppName.toString()) == true)
                {
                    Toast.makeText(getApplicationContext(), "--Application Found " + AppName.toString(), Toast.LENGTH_SHORT).show();


                }else{
                    Log.d("IS", "--" + AppName.toString());
                    Toast.makeText(getApplicationContext(), "--Application Not Found. "+ AppName.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

    }// On Create END.

    private Boolean checkInstalledApp(String appName){
        return getPackages(appName);
    }

    // Get Information about Only Specific application which is Install on Device.
    public String getInstallPackageVersionInfo(String appName)
    {
        String InstallVersion = "";
        ArrayList<PInfo> apps = getInstalledApps(false); /* false = no system packages */
        final int max = apps.size();
        for (int i=0; i<max; i++)
        {
            //apps.get(i).prettyPrint();
            if(apps.get(i).appname.toString().equals(appName.toString()))
            {
                InstallVersion = "Install Version Code: "+ apps.get(i).versionCode+
                        " Version Name: "+ apps.get(i).versionName.toString();
                break;
            }
        }

        return InstallVersion.toString();
    }
    private Boolean getPackages(String appName)
    {
        Boolean isInstalled = false;
        ArrayList<PInfo> apps = getInstalledApps(false); /* false = no system packages */
        final int max = apps.size();
        for (int i=0; i<max; i++)
        {
            //apps.get(i).prettyPrint();
            Log.e("IS", "-- " + apps.get(i).appname.toString());
            if(apps.get(i).appname.toString().equals(appName.toString()))
            {
                /*if(apps.get(i).versionName.toString().contains(VersionName.toString()) == true &&
                        VersionCode == apps.get(i).versionCode)
                {
                    isInstalled = true;
                    Toast.makeText(getApplicationContext(),
                            "Code Match", Toast.LENGTH_SHORT).show();
                    openMyDialog();
                }*/
                if(VersionCode <= apps.get(i).versionCode)
                {
                    isInstalled = true;

                    /*Toast.makeText(getApplicationContext(),
                            "Install Code is Less.!", Toast.LENGTH_SHORT).show();*/

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    //SelfInstall01Activity.this.finish(); Close The App.

                                    DownloadOnSDcard();
                                    InstallApplication();
                                    UnInstallApplication(PackageName.toString());

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked

                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("New Apk Available..").setPositiveButton("Yes Proceed", dialogClickListener)
                            .setNegativeButton("No.", dialogClickListener).show();

                }
                if(VersionCode > apps.get(i).versionCode)
                {
                    isInstalled = true;
                    /*Toast.makeText(getApplicationContext(),
                            "Install Code is better.!", Toast.LENGTH_SHORT).show();*/

                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which)
                            {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Yes button clicked
                                    //SelfInstall01Activity.this.finish(); Close The App.

                                    DownloadOnSDcard();
                                    InstallApplication();
                                    UnInstallApplication(PackageName.toString());

                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    //No button clicked

                                    break;
                            }
                        }
                    };

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage("NO need to Install.").setPositiveButton("Install Forcely", dialogClickListener)
                            .setNegativeButton("Cancel.", dialogClickListener).show();
                }
            }
        }

        return isInstalled;
    }
    private ArrayList<PInfo> getInstalledApps(boolean getSysPackages)
    {
        ArrayList<PInfo> res = new ArrayList<PInfo>();
        List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);

        for(int i=0;i<packs.size();i++)
        {
            PackageInfo p = packs.get(i);
            if ((!getSysPackages) && (p.versionName == null)) {
                continue ;
            }
            PInfo newInfo = new PInfo();
            newInfo.appname = p.applicationInfo.loadLabel(getPackageManager()).toString();
            newInfo.pname = p.packageName;
            newInfo.versionName = p.versionName;
            newInfo.versionCode = p.versionCode;
            //newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
            res.add(newInfo);
        }
        return res;
    }


    public void UnInstallApplication(String packageName)// Specific package Name Uninstall.
    {
        //Uri packageURI = Uri.parse("package:com.CheckInstallApp");
        Uri packageURI = Uri.parse(packageName.toString());
        Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageURI);
        startActivity(uninstallIntent);
    }
    public void InstallApplication()
    {
        Uri packageURI = Uri.parse(PackageName.toString());
        Intent intent = new Intent(android.content.Intent.ACTION_VIEW, packageURI);

//      Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

        //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //intent.setFlags(Intent.ACTION_PACKAGE_REPLACED);

        //intent.setAction(Settings. ACTION_APPLICATION_SETTINGS);

        intent.setDataAndType
                (Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/download/"  + ApkName.toString())),
                        "application/vnd.android.package-archive");

        // Not open this Below Line Because...
        ////intent.setClass(this, Project02Activity.class); // This Line Call Activity Recursively its dangerous.

        startActivity(intent);
    }
    public void GetVersionFromServer(String BuildVersionPath)
    {
            VersionCode = 1;
            VersionName = "Moment";
    }// Method End.

    // Download On My Mobile SDCard or Emulator.
    public void DownloadOnSDcard()
    {
        try{
            URL url = new URL(urlpath.toString()); // Your given URL.

            HttpURLConnection c = (HttpURLConnection) url.openConnection();
            c.setRequestMethod("GET");
            c.setDoOutput(true);
            c.connect(); // Connection Complete here.!

            //Toast.makeText(getApplicationContext(), "HttpURLConnection complete.", Toast.LENGTH_SHORT).show();

            String PATH = Environment.getExternalStorageDirectory() + "/download/";
            File file = new File(PATH); // PATH = /mnt/sdcard/download/
            if (!file.exists()) {
                file.mkdirs();
            }
            File outputFile = new File(file, ApkName.toString());
            FileOutputStream fos = new FileOutputStream(outputFile);

            //      Toast.makeText(getApplicationContext(), "SD Card Path: " + outputFile.toString(), Toast.LENGTH_SHORT).show();

            InputStream is = c.getInputStream(); // Get from Server and Catch In Input Stream Object.

            byte[] buffer = new byte[1024];
            int len1 = 0;
            while ((len1 = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len1); // Write In FileOutputStream.
            }
            fos.close();
            is.close();//till here, it works fine - .apk is download to my sdcard in download file.
            // So please Check in DDMS tab and Select your Emulator.

            //Toast.makeText(getApplicationContext(), "Download Complete on SD Card.!", Toast.LENGTH_SHORT).show();
            //download the APK to sdcard then fire the Intent.
        }
        catch (IOException e)
        {
            Toast.makeText(getApplicationContext(), "Error! " +
                    e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}