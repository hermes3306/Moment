package com.jason.moment.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jason.moment.MyReportActivity;
import com.jason.moment.R;
import com.jason.moment.util.db.MyActiviySummary;

import java.util.ArrayList;

public class AlertDialogUtil {
    static AlertDialogUtil instance;
    public static AlertDialogUtil getInstance() {
        if(instance==null) instance = new AlertDialogUtil();
        return instance;
    }

    public void chooseRank(Context _ctx, double distanceKm) {
        ArrayList<ActivitySummary> asl = MyActiviySummary.getInstance(_ctx).query_rank_speed_by_dist(distanceKm);
        for(int i=0;i<asl.size();i++) Log.d("TAG", "-- " + i + ":" + asl.get(i).toString());

        AlertDialog.Builder alertadd = new AlertDialog.Builder(_ctx);
        LayoutInflater factory = LayoutInflater.from(_ctx);
        final View view = factory.inflate(R.layout.layout_scroll_linearlayout, null);
        LinearLayout ll = view.findViewById(R.id.linearLayout);

        final TextView[] tvs = new TextView[asl.size()];
        for(int i=0;i<asl.size();i++) {
            final TextView tv = new TextView(_ctx);
            final String activity_file_name = asl.get(i).name;
            tv.setText(asl.get(i).toString());
            tv.setTextColor(Color.GRAY);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(_ctx, MyReportActivity.class);
                    intent.putExtra("activity_file_name", activity_file_name);
                    _ctx.startActivity(intent);
                }
            });
            ll.addView(tv);
            tvs[i] = tv;
        }
        alertadd.setView(view);
//        alertadd.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
//            public void onClick(DialogInterface dlg, int sumthin) {
//            }
//        });
        alertadd.show();

    }


}
