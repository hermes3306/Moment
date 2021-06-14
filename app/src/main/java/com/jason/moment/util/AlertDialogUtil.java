package com.jason.moment.util;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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

        AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
        AlertDialog alert = builder.create();

        LayoutInflater factory = LayoutInflater.from(_ctx);
        final View view = factory.inflate(R.layout.layout_scroll_linearlayout, null);
        LinearLayout ll = view.findViewById(R.id.linearLayout);
        TextView view_title = view.findViewById(R.id.view_title);

        String range[] = MyActiviySummary.getInstance(_ctx).getStringRange_by_dist(distanceKm);
        view_title.setText(String.format("%s-%sKM 구간 기록", range[0], range[1] ));

        final TextView[] tvs1 = new TextView[asl.size()];
        final TextView[] tvs2 = new TextView[asl.size()];
        final TextView[] tvs3 = new TextView[asl.size()];
        final TextView[] tvs4 = new TextView[asl.size()];
        for(int i=0;i<asl.size();i++) {
            ActivityStat as = ActivityStat.fromActivitySummary(asl.get(i));

            final TextView tv1 = new TextView(_ctx);
            final String activity_file_name = as._fname;


            //Drawable img = _ctx.getResources().getDrawable(R.drawable.file48);
            int img = 0;
            switch( i+1 ) {
                case 1: img = R.drawable.num_1; break;
                case 2: img = R.drawable.num_2; break;
                case 3: img = R.drawable.num_3; break;
                case 4: img = R.drawable.num_4; break;
                case 5: img = R.drawable.num_5; break;
                case 6: img = R.drawable.num_6; break;
                case 7: img = R.drawable.num_7; break;
                case 8: img = R.drawable.num_8; break;
                case 9: img = R.drawable.num_9; break;
                default :  img = R.drawable.running_new; break;
            }

            if(i>=9) tv1.setText("  " + String.format("%d",i+1) + ". " + as.name + "\n" + "(" + as.date_str2 + ")");
            else tv1.setText("  " + as.name + "\n" + "(" + as.date_str2 + ")");
            tv1.setTextColor(Color.GRAY);

            Drawable img_drawable = _ctx.getResources().getDrawable(img);
            tv1.setCompoundDrawablesWithIntrinsicBounds(img_drawable, null, null, null);

            tv1.setTextSize(16);
            tv1.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(_ctx, MyReportActivity.class);
                    intent.putExtra("activity_file_name", activity_file_name);
                    _ctx.startActivity(intent);
                    alert.dismiss();
                }
            });
            ll.addView(tv1);
            tvs1[i] = tv1;


            final TextView tv2 = new TextView(_ctx);
            tv2.setText("-");
            tv2.setTextColor(Color.GRAY);
            tv2.setTextSize(5);
            tv2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(_ctx, MyReportActivity.class);
                    intent.putExtra("activity_file_name", activity_file_name);
                    _ctx.startActivity(intent);
                    alert.dismiss();
                }
            });
            ll.addView(tv2);
            tvs2[i] = tv2;

            final TextView tv3 = new TextView(_ctx);
            tv3.setText("\t" + String.format("%.1f",as.distanceKm) + "\t" + as.durationM + "\t" + as.minperKms + "\t" + as.calories);
            tv3.setTextColor(Color.GRAY);
            tv3.setTextSize(18);
            tv3.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(_ctx, MyReportActivity.class);
                    intent.putExtra("activity_file_name", activity_file_name);
                    _ctx.startActivity(intent);
                    alert.dismiss();
                }
            });
            ll.addView(tv3);
            tvs3[i] = tv3;

            final TextView tv4 = new TextView(_ctx);
            tv4.setText("\t\t\t" + "KM" + "\t" + "TIME" + "\t" + "MiN/KM"+ "\t" + "Calories\n");

            tv4.setTextColor(Color.GRAY);
            tv4.setTextSize(15);
            tv4.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(_ctx, MyReportActivity.class);
                    intent.putExtra("activity_file_name", activity_file_name);
                    _ctx.startActivity(intent);
                    alert.dismiss();
                }
            });
            ll.addView(tv4);
            tvs4[i] = tv4;
        }
        alert.setView(view);
        alert.show();
    }


}
