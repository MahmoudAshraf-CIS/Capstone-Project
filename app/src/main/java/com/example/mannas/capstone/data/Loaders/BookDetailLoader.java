package com.example.mannas.capstone.data.Loaders;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.example.mannas.capstone.data.ContentProvider.Contract;
import com.example.mannas.capstone.data.Util.Author;
import com.example.mannas.capstone.data.Util.Cover;
import com.example.mannas.capstone.data.Util.Ebook;
import com.example.mannas.capstone.data.Util.Person;
import com.example.mannas.capstone.data.Util.Place;
import com.example.mannas.capstone.data.Util.Publisher;
import com.example.mannas.capstone.data.Util.Subject;
import com.example.mannas.capstone.data.Util.Work;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Mannas on 8/23/2017.
 */

public class BookDetailLoader extends AsyncTaskLoader<BookDetailLoader.BookDetailResponse> {

    final String res = "{\"OLID:OL123M\": {\"publishers\": [{\"name\": \"Amrit Book Co.\"}], \"pagination\": \"142 p.\", \"identifiers\": {\"lccn\": [\"sa 65000036\"], \"openlibrary\": [\"OL123M\"], \"oclc\": [\"11322102\"]}, \"classifications\": {\"lc_classifications\": [\"PZ4.B624 St\", \"PR9499.3.B5 St\"]}, \"title\": \"Stories of Indian life.\", \"url\": \"https://openlibrary.org/books/OL123M/Stories_of_Indian_life.\", \"number_of_pages\": 142, \"subject_places\": [{\"url\": \"https://openlibrary.org/subjects/place:india\", \"name\": \"India\"}], \"subjects\": [{\"url\": \"https://openlibrary.org/subjects/social_life_and_customs\", \"name\": \"Social life and customs\"}, {\"url\": \"https://openlibrary.org/subjects/fiction\", \"name\": \"Fiction\"}], \"publish_date\": \"1964\", \"key\": \"/books/OL123M\", \"authors\": [{\"url\": \"https://openlibrary.org/authors/OL105A/Anil_Chandra_Biswas\", \"name\": \"Anil Chandra Biswas\"}], \"publish_places\": [{\"name\": \"New Delhi\"}]}}";
    private Work work;
    private String LOG_TAG = BookDetailLoader.class.getName();
    private String ERR_DOWNLOADING = "Error downloading",
            ERR_PARSING = "Error Parsing";

    public BookDetailLoader(Context context, Work work) {
        super(context);
        this.work = work;
    }

    public BookDetailLoader(Context context, String OLID) {
        super(context);
        this.work = new Work();
        work.cover_edition_key = OLID;
    }

    @Override
    public BookDetailResponse loadInBackground() {
        if (work.cover_edition_key != null && !work.cover_edition_key.equals("")) {
            String response = "";
            Boolean fromCache = false;
            try {
                response = download(getUrl());
            } catch (IOException e) {
                Log.e(LOG_TAG, ERR_DOWNLOADING);
                e.printStackTrace();

                fromCache = true;
                String[] projection = {Contract.BookDetailResponse.Columns.json};
                Cursor c = getContext().getContentResolver().query(Contract.BookDetailResponse.uri,
                        projection,
                        Contract.BookDetailResponse.Columns.OLID + " = \" " + work.cover_edition_key + "\" ", null, null);
                if (c != null && c.moveToFirst()) {
                    response = c.getString(c.getColumnIndex(projection[0]));
                    c.close();
                }
            }
            if (!fromCache)
                cache(response, work.cover_edition_key);
            try {
                return parse(response);
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(ERR_PARSING, "Invalid json entry");
            }
        }
        return null;
    }

    private String download(String Url) throws IOException {
//        return res;
        Request request = new Request.Builder().url(Url).build();
        Response response = (new OkHttpClient()).newCall(request).execute();
        if (response != null && response.body() != null)
            return response.body().string();
        return null;
    }

    private void cache(final String OLID, final String json) {
        if (json != null && !json.equals(""))
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... voids) {
                    ContentValues v = new ContentValues(1);
                    v.put(Contract.BookDetailResponse.Columns.json, json);
                    v.put(Contract.BookDetailResponse.Columns.OLID, OLID);
                    getContext().getContentResolver().insert(Contract.BookDetailResponse.uri, v);
                    return null;
                }
            }.execute();
    }

    private BookDetailResponse parse(String json) throws JSONException {

        JSONObject resp = new JSONObject(json);
        if (resp.has("OLID:" + work.cover_edition_key)) {
             JSONObject olid = resp.getJSONObject("OLID:" + work.cover_edition_key);
             String olid_str = olid.toString();

            return new Gson().fromJson(olid_str, BookDetailResponse.class);
        }

        return null;
    }

    private String getUrl() {
        //https://openlibrary.org/api/books?bibkeys=OLID:OL123M&format=json&jscmd=data

        return "https://openlibrary.org/api/books?bibkeys=OLID:" +
                work.cover_edition_key +
                "&format=json&jscmd=data";
    }

    public static class BookDetailResponse {
        public ArrayList<Publisher> publishers;
        public String pagination;
        public String title;
        public String url;
        public String notes;
        public Integer number_of_pages;
        public Cover cover;
        public ArrayList<Subject> subjects;
        public ArrayList<Person> subject_people;
        public String key;
        public ArrayList<Author> authors;
        public String publish_date;
        public String by_statement;
        public ArrayList<Place> publish_places;
        public ArrayList<Ebook> ebooks;
    }
}
