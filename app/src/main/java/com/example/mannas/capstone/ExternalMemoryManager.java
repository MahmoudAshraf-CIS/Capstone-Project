package com.example.mannas.capstone;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.webkit.MimeTypeMap;

import com.example.mannas.capstone.data.Util.Work;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Mannas on 8/29/2017.
 */

public class ExternalMemoryManager {
    ExternalMemoryManager() {
    }

    public static String getDownloadFolderPath() {
        return Environment.getExternalStorageDirectory() + "/Download/Capstone";
    }

    public static String getOfflinePath(Work work, String extension) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/Download/Capstone/" + getFileName(work, extension));

        if (!direct.exists()) {
//            direct.mkdirs();
            return null;
        }

        return direct.getAbsolutePath();
    }

    public static String getOfflinePath(String OLID, String title, String extension) {
        File direct = new File(Environment.getExternalStorageDirectory()
                + "/Download/Capstone/" + getFileName(OLID, title, extension));

        if (!direct.exists()) {
//            direct.mkdirs();
            return null;
        }

        return direct.getAbsolutePath();
    }

    public static String getOfflinePath(String fileName) {
        return getDownloadFolderPath() + "/" + fileName;
    }

    public static ArrayList<String> getDownloadsFolderFilesPaths() {
        File direct = new File(getDownloadFolderPath());
        if (!direct.exists())
            return null;
        else {
            File[] files = direct.listFiles();
            ArrayList<String> paths = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                paths.add(files[i].getName());
            }
            return paths;
        }
    }

    public static void openFile(String path, Context context, CoordinatorLayout coordinatorLayout) {
        //// TODO: 8/29/2017 open file
//        Intent openFile = new Intent(Intent.ACTION_VIEW);
//        openFile.setData(Uri.parse(path));
//        try {
//            context.startActivity(openFile);
//        } catch (ActivityNotFoundException e) {
//            Log.e(ExternalMemoryManager.class.getName(), " Cannot open file.");
//            Snackbar.make(coordinatorLayout, "Cannot open file.", Snackbar.LENGTH_LONG).show();
//
//        }

        File file = new File(path);
        MimeTypeMap myMime = MimeTypeMap.getSingleton();

        String mimeType = myMime.getMimeTypeFromExtension(getExtension(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), mimeType);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Snackbar.make(coordinatorLayout,context.getResources().getString(R.string.no_handler_for_this_type_of_file), Snackbar.LENGTH_LONG).show();

        }

    }

    private static String getExtension(String name) {
        switch (name.substring(name.lastIndexOf('.'))) {
            case ".pdf":
                return "pdf";
            case ".txt":
                return "plain";
            case ".epub":
                return "plain";
            default:
                return null;
        }
    }

    private static String getFileName(Work work, String extension) {
        return work.cover_edition_key + "-" + work.title + extension;
    }

    private static String getFileName(String OLID, String title, String extension) {
        return OLID + "-" + title + extension;
    }

    private static Boolean isDownloadFolderExist() {
        File direct = new File(getDownloadFolderPath());
        if (!direct.exists()) {
            return direct.mkdirs();
        }
        return true;
    }

    public static Boolean downloadFile(Context c, String extension, String OLID, String title, String url) {
        if (isDownloadFolderExist()) {
            DownloadManager mgr;
            DownloadManager.Request request;
            mgr = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir("/Download/Capstone", getFileName(OLID, title, extension));
            mgr.enqueue(request);
            return true;
        } else {
            return false;
        }
    }

    public static Boolean downloadFile(Context c, String extension, Work work, String url) {
        if (isDownloadFolderExist()) {
            DownloadManager mgr;
            DownloadManager.Request request;
            mgr = (DownloadManager) c.getSystemService(Context.DOWNLOAD_SERVICE);
            request = new DownloadManager.Request(Uri.parse(url));
            request.setDestinationInExternalPublicDir("/Download/Capstone", getFileName(work, extension));
            mgr.enqueue(request);
            return true;
        } else {
            return false;
        }
    }

}
