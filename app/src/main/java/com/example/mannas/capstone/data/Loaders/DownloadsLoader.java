package com.example.mannas.capstone.data.Loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.example.mannas.capstone.ExternalMemoryManager;

import java.util.ArrayList;

/**
 * Created by Mannas on 9/14/2017.
 */

public class DownloadsLoader extends AsyncTaskLoader<ArrayList<String>> {

    public DownloadsLoader(Context context) {
        super(context);
    }

    @Override
    public ArrayList<String> loadInBackground() {
        ArrayList<String> ls = ExternalMemoryManager.getDownloadsFolderFilesPaths();
        ArrayList<String> books = new ArrayList<>();
        if(ls!=null && ls.size()>0)
            for(int i=0;i<ls.size();i++){
                if(isBook(ls.get(i)))
                    books.add(ls.get(i));
            }
        else
            return null;
        return books;
    }
    private Boolean isBook(String name){
        switch (name.substring(name.lastIndexOf('.'))){
            case ".pdf":
                return true;
            case ".txt":
                return true;
            case ".epub":
                return true;
            default:
                return false;
        }
    }
}
