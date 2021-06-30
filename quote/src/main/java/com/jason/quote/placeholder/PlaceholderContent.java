package com.jason.quote.placeholder;

import android.util.Log;

import com.jason.quote.util.Config;
import com.jason.quote.util.MyActivityUtil;
import com.jason.quote.util.PermissionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    public static final List<PlaceholderItem> ITEMS = new ArrayList<PlaceholderItem>();

    /**
     * A map of sample (placeholder) items, by ID.
     */
    public static final Map<String, PlaceholderItem> ITEM_MAP = new HashMap<String, PlaceholderItem>();

    private static final int COUNT = 25;
    private static File files[] = null;
    static {
        // Add some sample items.
        Log.d("----", Config.CSV_SAVE_DIR.getAbsolutePath() );
        File files[] = Config.CSV_SAVE_DIR.listFiles();
        for (int i = 0; i < files.length; i++) {
            addItem(createPlaceholderItem(files, i));
            Log.d("----", files[i].getName());
        }
    }

    private static void addItem(PlaceholderItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static PlaceholderItem createPlaceholderItem(File files[], int position) {
        String file_name = files[position].getName();
        String activity_info = MyActivityUtil.getActivityInfoFromFile(file_name);
        String content = activity_info;
        return new PlaceholderItem(String.valueOf(position), activity_info, file_name, content, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {

            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A placeholder item representing a piece of content.
     */
    public static class PlaceholderItem {
        public final String id;
        public final String act_info;
        public final String file_mame;
        public final String content;
        public final String details;

        public PlaceholderItem(String id, String act_info, String file_name, String content, String details) {
            this.id = id;
            this.act_info = act_info;
            this.file_mame = file_name;
            this.content = content;
            this.details = details;
        }


        @Override
        public String toString() {
            return content;
        }
    }
}