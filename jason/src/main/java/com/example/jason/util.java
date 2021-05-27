package com.example.jason;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;

public class util {
    public static void F100_alert(Context context, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title).setMessage(message);
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


}