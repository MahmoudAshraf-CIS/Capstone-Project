package com.example.mannas.capstone.data.Loaders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mannas.capstone.data.ContentProvider.Contract;
import com.example.mannas.capstone.data.SharedPrefirencesManager;
import com.example.mannas.capstone.data.Util.Author;
import com.example.mannas.capstone.data.Util.Person;
import com.example.mannas.capstone.data.Util.Place;
import com.example.mannas.capstone.data.Util.Publisher;
import com.example.mannas.capstone.data.Util.Subject;
import com.example.mannas.capstone.data.Util.Work;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mannas on 8/23/2017.
 * <p>
 * loads {@link SubjectResponse }
 * which holds {@link ArrayList<Work>}
 * each {@link Work} is a Book
 */

public class SubjectLoader extends AsyncTaskLoader<SubjectLoader.SubjectResponse> {



    private Boolean includeFullDetail, onlyEbooks;
    private Integer limit, offset;
    private String subjectName;

    private String LOG_TAG = SubjectLoader.class.getName();
    private String ERR_DOWNLOADING = "Error downloading", ERR_PARSING = "Error Parsing";

    public SubjectLoader(Context context, String subjectName, Integer page) {
        super(context);
        includeFullDetail = onlyEbooks = true;
        this.subjectName = subjectName;
        limit = 20;
        offset = page * limit;
    }

    public SubjectLoader(Context context, Boolean includeFullDetail, Boolean onlyEbooks, String subjectName, Integer page) {
        super(context);
        this.includeFullDetail = includeFullDetail;
        this.onlyEbooks = onlyEbooks;
        this.subjectName = subjectName;
        limit = 20;
        offset = page * limit;
    }

    @Override
    public SubjectResponse loadInBackground() {
        String response = "";
        Boolean fromCache = false;
        try {
            String url = getUrl();
            response = download(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, ERR_DOWNLOADING);
            e.printStackTrace();
            fromCache = true;
            String[] projection = {Contract.SubjectResponse.Columns.json};
            Cursor c = getContext().getContentResolver().query(Contract.SubjectResponse.uri,
                    projection,
                    Contract.SubjectResponse.Columns.subject_name + " = \" " + subjectName
                            + "  \" AND " +
                            Contract.SubjectResponse.Columns.offset + " = " + offset.toString() + " ;"
                    , null, null);
            if (c != null && c.moveToFirst()) {
                response = c.getString(c.getColumnIndex(projection[0]));
                c.close();
            }
        }
        if (!fromCache)
            cache(subjectName, offset, response);

        try {
            SubjectResponse r = parse(response);
            if(r==null){
                Log.e(LOG_TAG,ERR_PARSING);
                return null;
            }
            cacheInSharedPrefirences(r.works , subjectName);
            return r;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String download(String Url) throws IOException {

//        return response_example;
        Request request = new Request.Builder().url(Url).build();
        Response response = (new OkHttpClient()).newCall(request).execute();
        if (response != null && response.body() != null) {
            return response.body().string();
        }

        return null;
    }

    private void cache(final String subjectName, final Integer offset, final String json) {
        if (json != null && !json.equals(""))
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    //cache in DataBase for app
                    ContentValues v = new ContentValues(1);
                    v.put(Contract.SubjectResponse.Columns.subject_name, subjectName);
                    v.put(Contract.SubjectResponse.Columns.offset, offset);
                    v.put(Contract.SubjectResponse.Columns.json, json);
                    getContext().getContentResolver().insert(Contract.SubjectResponse.uri, v);

                    return null;
                }
            }.execute();
    }

    private void cacheInSharedPrefirences(final ArrayList<Work> works , String subjectName) {
        SharedPrefirencesManager.setMainRecyclerSubjectName(getContext(),subjectName);
        if (works != null && works.size() > 0)
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {

                    //cache in SharedPrefirences for widget
                    SharedPrefirencesManager.setLastLoadedSubjectWorks(getContext(), works);
                    return null;
                }
            }.execute();
    }

    private SubjectResponse parse(String json) throws Exception {
        return new Gson().fromJson(json, SubjectResponse.class);
    }

    private String getUrl() {
        //http://openlibrary.org/subjects/love.json?details=true&ebooks=true&limit=10&offset=0
        return "http://openlibrary.org/subjects/" +
                subjectName + ".json?" +
                (includeFullDetail ? "details=true" : "details=false") +
                (onlyEbooks ? "&ebooks=true" : "&ebooks=false") +
                "&limit=" + limit.toString()
                + "&offset=" + offset.toString();
    }

    public static class SubjectResponse {

        public ArrayList<Publisher> publishers;
        public String subject_type;
        public String name;
        public ArrayList<Place> places;
        public ArrayList<Person> people;
        public ArrayList<Subject> subjects;
        public String key;
        public ArrayList<Author> authors;
        public Integer ebook_count;

        public ArrayList<Work> works;
        public Integer work_count;
    }
}
