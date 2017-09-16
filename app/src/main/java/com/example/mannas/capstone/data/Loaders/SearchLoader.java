package com.example.mannas.capstone.data.Loaders;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mannas.capstone.data.Util.Doc;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mannas on 8/24/2017.
 */

public class SearchLoader extends AsyncTaskLoader<SearchLoader.SearchResult> {
    private String LOG_TAG = SubjectLoader.class.getName();
    private String ERR_DOWNLOADING = "Error downloading",
            ERR_PARSING ="Error Parsing";


    String query;
    Integer page;

    public SearchLoader(Context context, String query, Integer page) {
        super(context);
        this.query = query.replace(' ','+');
        this.page = ( page <= 1) ? 1 : page;
    }

    @Override
    public SearchResult loadInBackground() {
        String response="";
        try {
            response = download(getUrl());
        } catch (IOException e) {
            Log.e(LOG_TAG,ERR_DOWNLOADING);
            e.printStackTrace();
        }

        try {
            return parse(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String download(String Url) throws IOException {
        Request request = new Request.Builder().url(Url).build();
        Response response = (new OkHttpClient()).newCall(request).execute();
        if(response!=null && response.body()!=null){
            String s = response.body().string();
            return s;
        }

        return null;
    }

    private SearchResult parse(String json) throws Exception{
        return new Gson().fromJson(json,SearchResult.class);
    }

    private String getUrl(){
        //http://openlibrary.org/search.json?q=the+lord&page=2
        return "http://openlibrary.org/search.json?q=" +
                query +
                "&page=" +
                page.toString();
    }

    public static class SearchResult implements Parcelable {
        public Integer start;
        public Integer num_found;
        public ArrayList<Doc> docs;

        protected SearchResult(Parcel in) {
            start = in.readByte() == 0x00 ? null : in.readInt();
            num_found = in.readByte() == 0x00 ? null : in.readInt();
            if (in.readByte() == 0x01) {
                docs = new ArrayList<Doc>();
                in.readList(docs, Doc.class.getClassLoader());
            } else {
                docs = null;
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            if (start == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeInt(start);
            }
            if (num_found == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeInt(num_found);
            }
            if (docs == null) {
                dest.writeByte((byte) (0x00));
            } else {
                dest.writeByte((byte) (0x01));
                dest.writeList(docs);
            }
        }

        @SuppressWarnings("unused")
        public static final Parcelable.Creator<SearchResult> CREATOR = new Parcelable.Creator<SearchResult>() {
            @Override
            public SearchResult createFromParcel(Parcel in) {
                return new SearchResult(in);
            }

            @Override
            public SearchResult[] newArray(int size) {
                return new SearchResult[size];
            }
        };
    }

}
