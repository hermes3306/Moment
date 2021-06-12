package com.jason.moment.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.icu.util.Output;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import static android.app.Activity.RESULT_OK;

public class MediaUtil {
    static MediaUtil instance;
    final String TAG = "MapUtil";
    private static final int BUFFER_SIZE = 4096;

    public static MediaUtil getInstance() {
        if(instance==null) instance = new MediaUtil();
        return instance;
    }

    public void getImageFromAlbum(Activity activity) {
        Log.d(TAG,"-- getImageFromAlbum()");
        try {
//            Intent i = new Intent(Intent.ACTION_PICK,
//                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Intent i = new Intent(Intent.ACTION_PICK);
            i.setType("image/*");

            activity.startActivityForResult(i, Config.CALL_RESULT_LOAD_IMAGE);
            Log.d(TAG,"-- activity.startActivityFroResult called");
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG, "-- Err: " + sw.toString());
        }
    }

    public Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d(TAG, "  ERR:" + sw.toString());
            return null;
        }
    }
    private Bitmap decodeFile(File f) {
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);
            // The new size we want to scale to
            int REQUIRED_SIZE= Config.PIC_REQUIRED_SIZE;
            // Find the correct scale value. It should be the power of 2.
            int scale = 1;
            while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
                    o.outHeight / scale / 2 >= REQUIRED_SIZE) {
                scale *= 2;
            }
            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.e(TAG,"Err:" + sw.toString());
        }
        return null;
    }

    static int mDegree=-1;
    public void showImageRotated(ImageView iv_pic, String picturePath) {
        if(mDegree == -1) mDegree = 90;
        else mDegree += 90;
        showImageRotated(iv_pic, picturePath,mDegree);
    }

    public void showImageRotated(ImageView iv_pic, String picturePath, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        File mediaFile = new File(Config.PIC_SAVE_DIR, picturePath);
        Bitmap bitmap = decodeFile(mediaFile);
        bitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(),matrix,true);
        iv_pic.setImageBitmap(bitmap);
    }

    public void showImage(ImageView iv_pic, String picturePath) {
        File mediaFile = new File(Config.PIC_SAVE_DIR, picturePath);
        Bitmap bitmap = decodeFile(mediaFile);
        iv_pic.setImageBitmap(bitmap);
    }

    public void savePicFromView(View view) {
        View content = view;
        content.setDrawingCacheEnabled(true);
        Bitmap bitmap = content.getDrawingCache();
        File cachePath = new File(Config.PIC_SAVE_DIR, Config.getTmpPicName());
        try {
            cachePath.createNewFile();
            FileOutputStream ostream = new FileOutputStream(cachePath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, ostream);
            ostream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String savePicFromUri(Context ctx, Uri imageUri) {
        String file_name = Config.getTmpPicName();
        try {
            InputStream InputStream = ctx.getContentResolver().openInputStream(imageUri);
            File save_file = new File(Config.PIC_SAVE_DIR, file_name);
            FileOutputStream outputStream = new FileOutputStream(save_file);

            int bytesRead = -1;
            byte[] buffer = new byte[BUFFER_SIZE];
            while ((bytesRead = InputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            InputStream.close();
            Log.d(TAG, "-- " + file_name + " saved successfully");
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d(TAG,"-- ERR:" + sw.toString());
        }
        return file_name;
    }

    private void cp(InputStream is, OutputStream os) throws Exception{
        int bytesRead = -1;
        byte[] buffer = new byte[BUFFER_SIZE];
        while ((bytesRead = is.read(buffer)) != -1) {
            os.write(buffer, 0, bytesRead);
        }
        os.close();
        is.close();
        Log.d(TAG, "-- Copy stream done successfully!");
    }

    public void saveImageInGallery(Context _ctx, String filepath) {
        File input_file = new File(Config.PIC_SAVE_DIR, filepath);
        File save_file = new File(Config.PIC_GALLERY_DIR, filepath);
        Log.d(TAG, "-- " + input_file.getAbsolutePath() + " will be copied!");
        try {
            InputStream is = new FileInputStream(input_file);
            OutputStream os = new FileOutputStream(save_file);
            cp(is,os);

            Log.d(TAG, "-- " + save_file.getAbsolutePath() + " saved successfully");
            Toast.makeText(_ctx, save_file.getAbsolutePath() +  " copied", Toast.LENGTH_SHORT).show();
        }catch(Exception e) {
            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            Log.d(TAG,"-- ERR:" + sw.toString());
        }
    }

    public void updateGallery(Context ctx, String filepath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(Config.PIC_SAVE_DIR, filepath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
        Log.d(TAG, "-- UpdateGallery called");
    }

    public void onActivityResult(Context ctx, int requestCode, Intent data, ImageView image_view) {
        Log.d(TAG,"-- onActivityResult Called in the MediaUtil! ");
        if (requestCode == Config.CALL_RESULT_LOAD_IMAGE) {
            final Uri imageUri = data.getData();
            try {
                final InputStream imageStream = ctx.getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                image_view.setImageBitmap(selectedImage);

                String file_name = savePicFromUri(ctx, imageUri);
                showImage(image_view, file_name);
                CloudUtil.getInstance().Upload(file_name);

                //savePicFromView(image_view);
                Log.d(TAG, "-- success! ");
            }catch(Exception e) {
                StringWriter sw = new StringWriter();
                e.printStackTrace(new PrintWriter(sw));
                Log.d(TAG,"-- ERR:" + sw.toString());
            }
        }

    }

}
