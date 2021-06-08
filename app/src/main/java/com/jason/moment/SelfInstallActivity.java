package com.jason.moment;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jason.moment.util.CloudUtil;
import com.jason.moment.util.Config;
import com.jason.moment.util.UpdateApp;

public class SelfInstallActivity extends AppCompatActivity {
    class PInfo {
        private String appname = "";
        private String pname = "";
        private String versionName = "";
        private int versionCode = 0;
        //private Drawable icon;
        private void prettyPrint() {
            //Log.d(TAG, "--" + appname + "\t" + pname + "\t" + versionName + "\t" + versionCode);
        }
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
    Button btnInstall;
    Button btnTest;
    TextView tvInstallVersion;
    Context _ctx;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        // APK설치할 경우, 경고 무시하는 방법
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self_install);
        _ctx = this;

        //Text= "Old".toString();
        Text= "New".toString();

        ApkName = "moment.apk";
        AppName = "moment";

        BuildVersionPath = Config._apkver;
        PackageName = "package:com.jason.moment".toString();        //"package:com.Test1".toString();
        urlpath = Config._apkurl;

        tvApkStatus =(TextView)findViewById(R.id.tvApkStatus);
        tvApkStatus.setText(Text+" Apk Download.".toString());

        tvInstallVersion = (TextView)findViewById(R.id.tvInstallVersion);
        String temp = getInstallPackageVersionInfo(AppName.toString());
        tvInstallVersion.setText("" +temp.toString());

        btnCheckUpdates =(Button)findViewById(R.id.btnInstall);
        btnCheckUpdates.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                GetVersionFromServer(_ctx, BuildVersionPath);

                Log.d(TAG,"--VersionName:" + VersionName);
                Log.d(TAG,"--VersionCode:" + VersionCode);

                if(checkInstalledApp(AppName.toString()) == true)
                {
                    Toast.makeText(getApplicationContext(), "Application Found " + AppName.toString(), Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(), "Application Not Found. "+ AppName.toString(), Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnInstall =(Button)findViewById(R.id.btnInstall);
        btnInstall.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                GetVersionFromServer(_ctx, BuildVersionPath);

                Log.d(TAG,"--VersionName:" + VersionName);
                Log.d(TAG,"--VersionCode:" + VersionCode);

                Download();
                InstallApplication();
            }
        });

        btnTest =(Button)findViewById(R.id.btnTest);
        btnTest.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View arg0)
            {
                UpdateApp atualizaApp = new UpdateApp();
                atualizaApp.setContext(getApplicationContext());
                atualizaApp.execute(Config._apkurl);
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
            apps.get(i).prettyPrint();
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

                                    Download();
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

                                    Download();
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
        Log.d(TAG, "-- PackageName:" + PackageName);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType
                (Uri.fromFile(new File(Config.APK_SAVE_DIR, Config._apkname)),
                        "application/vnd.android.package-archive");

        Log.d(TAG, "-- Uri to install:" + new File(Config.APK_SAVE_DIR, Config._apkname));
        Log.d(TAG, "-- type:" + "application/vnd.android.package-archive");

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void GetVersionFromServer(Context context, String fileURL) {
        new AsyncTask<Void, Void, Void>() {
            final ProgressDialog asyncDialog = new ProgressDialog(context);
            @Override
            protected Void doInBackground(Void... voids) {

                URL u;
                try {
                    u = new URL(BuildVersionPath.toString());

                    HttpURLConnection c = (HttpURLConnection) u.openConnection();
                    c.setRequestMethod("GET");
                    c.setDoOutput(true);
                    c.connect();

                    //Toast.makeText(getApplicationContext(), "HttpURLConnection Complete.!", Toast.LENGTH_SHORT).show();

                    InputStream in = c.getInputStream();

                    ByteArrayOutputStream baos = new ByteArrayOutputStream();

                    byte[] buffer = new byte[1024]; //that stops the reading after 1024 chars..
                    //in.read(buffer); //  Read from Buffer.
                    //baos.write(buffer); // Write Into Buffer.

                    int len1 = 0;
                    while ((len1 = in.read(buffer)) != -1) {
                        baos.write(buffer, 0, len1); // Write Into ByteArrayOutputStream Buffer.
                    }
                    String temp = "";
                    String s = baos.toString();// baos.toString(); contain Version Code = 2; \n Version name = 2.1;

                    for (int i = 0; i < s.length(); i++) {
                        i = s.indexOf("=") + 1;
                        while (s.charAt(i) == ' ') // Skip Spaces
                        {
                            i++; // Move to Next.
                        }
                        while (s.charAt(i) != ';' && (s.charAt(i) >= '0' && s.charAt(i) <= '9' || s.charAt(i) == '.')) {
                            temp = temp.toString().concat(Character.toString(s.charAt(i)));
                            i++;
                        }
                        //
                        s = s.substring(i); // Move to Next to Process.!
                        temp = temp + " "; // Separate w.r.t Space Version Code and Version Name.

                        asyncDialog.setProgress(i);

                    }
                    String[] fields = temp.split(" ");// Make Array for Version Code and Version Name.

                    VersionCode = Integer.parseInt(fields[0].toString());// .ToString() Return String Value.
                    VersionName = fields[1].toString();

                    Log.d(TAG, "--VersionCode:" + VersionCode);
                    Log.d(TAG, "--VersionName:" + VersionName);

                    baos.close();
                } catch (MalformedURLException e) {
                    Toast.makeText(getApplicationContext(), "Error." + e.getMessage(), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Error." + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                return null;
            }


            @Override
            protected void onPreExecute() {
                asyncDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                asyncDialog.setMessage("Downloading...");
                asyncDialog.show();
                super.onPreExecute();
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                asyncDialog.dismiss();
                super.onPostExecute(aVoid);
                Toast.makeText(context, "Download success", Toast.LENGTH_LONG).show();
            }

        }.execute();;
    }


    // Download On My Mobile SDCard or Emulator.
    public void Download()
    {
        try {
            Log.d(TAG,"-- before download APK");
            CloudUtil.getInstance().DownloadAsync(this, Config.APK_SAVE_DIR, Config._apkurl);
            Log.d(TAG,"-- after download APK");
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d(TAG, sw.toString());
        }
    }

    String TAG = "SelfInstallActivity";

}