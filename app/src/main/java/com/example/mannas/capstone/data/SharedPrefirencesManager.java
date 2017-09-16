package com.example.mannas.capstone.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.mannas.capstone.data.Util.Work;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Mannas on 8/28/2017.
 */

public class SharedPrefirencesManager {
    public static final String MAIN_RECYCLER_SUBJECT_KEY ="mainRecSub";
    public static final String LAST_WORKS ="lastWorks";

    private static SharedPreferences sharedPreferences;

    private SharedPrefirencesManager(){}

    public static SharedPreferences get(Context c){
        if(sharedPreferences==null){
            sharedPreferences = c.getSharedPreferences("pref",Context.MODE_PRIVATE);
        }
        return sharedPreferences;
    }

    public static String getMainRecyclerSubjectName(Context c) {
        return get(c).getString(MAIN_RECYCLER_SUBJECT_KEY ,"love");
    }

    public static void setMainRecyclerSubjectName(Context c, String s){
         get(c).edit().putString(MAIN_RECYCLER_SUBJECT_KEY,s).apply();
    }

    public static void setLastLoadedSubjectWorks(Context c,ArrayList<Work> works){
        get(c).edit().putString(LAST_WORKS ,
                (new Gson().toJson(works,ArrayList.class))
                ).apply();
    }
    public static ArrayList<Work> getLastLoadedSubjectWorks(Context c){
        return  (new Gson().fromJson(get(c).getString(LAST_WORKS,null) ,
                new TypeToken<List<Work>>(){}.getType()
                ));
    }



}
