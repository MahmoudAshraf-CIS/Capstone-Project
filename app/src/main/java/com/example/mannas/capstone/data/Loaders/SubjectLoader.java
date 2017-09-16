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
 *
 *   loads {@link SubjectResponse }
 *   which holds {@link ArrayList<Work>}
 *   each {@link Work} is a Book
 */

public class SubjectLoader extends AsyncTaskLoader<SubjectLoader.SubjectResponse>{
    public static Integer lastUsedOffset=0;
    public static Integer lastUsedLimit=0;


    private Boolean includeFullDetail ,onlyEbooks;
    private Integer limit ,offset ;
    private String subjectName;
    private String LOG_TAG = SubjectLoader.class.getName();
    private String ERR_DOWNLOADING = "Error downloading", ERR_PARSING ="Error Parsing";



    public SubjectLoader(Context context,String subjectName,Integer page){
        super(context);
        includeFullDetail = onlyEbooks = true;
        this.subjectName = subjectName;
        limit =20;
        offset = page * limit;
    }

    public SubjectLoader(Context context, Boolean includeFullDetail, Boolean onlyEbooks, String subjectName,Integer page) {
        super(context);
        this.includeFullDetail = includeFullDetail;
        this.onlyEbooks = onlyEbooks;
        this.subjectName = subjectName;
        limit =20;
        offset = page * limit;
    }

    @Override
    public SubjectResponse loadInBackground() {
        String response="";
        Boolean fromCache = false;
        try {
            String url =getUrl();
            response = download(url);
        } catch (IOException e) {
            Log.e(LOG_TAG,ERR_DOWNLOADING);
            e.printStackTrace();
            fromCache = true;
            String []projection = {Contract.SubjectResponse.Columns.json};
            Cursor c = getContext().getContentResolver().query(Contract.SubjectResponse.uri,
                    projection,
                    Contract.SubjectResponse.Columns.subject_name +" = \" " + subjectName
                    +"  \" AND "+
                    Contract.SubjectResponse.Columns.offset       +" = " + offset.toString()+" ;"
                    ,null,null);
            if( c!=null && c.moveToFirst()){
                response = c.getString(c.getColumnIndex(projection[0]));
                c.close();
            }
        }
        if(!fromCache)
         cache(subjectName,offset , response);

        try {
            SubjectResponse r = parse(response);
            cacheInSharedPrefirences(r.works);
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
        if(response!=null && response.body()!=null){
            return response.body().string();
        }

        return null;
    }

    private void cache(final String subjectName ,final Integer offset , final String json){
        if(json!=null &&!json.equals(""))
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... voids) {
                    //cache in DataBase for app
                    ContentValues v = new ContentValues(1);
                    v.put(Contract.SubjectResponse.Columns.subject_name,subjectName );
                    v.put(Contract.SubjectResponse.Columns.offset,offset );
                    v.put(Contract.SubjectResponse.Columns.json,json );
                    getContext().getContentResolver().insert(Contract.SubjectResponse.uri, v);

                    return null;
                }
            }.execute();
    }
    private void cacheInSharedPrefirences(final ArrayList<Work> works){
        if(works!=null && works.size()>0)
            new AsyncTask<Void,Void,Void>(){
                @Override
                protected Void doInBackground(Void... voids) {

                    //cache in SharedPrefirences for widget
                    SharedPrefirencesManager.setLastLoadedSubjectWorks(getContext(),works );
                    return null;
                }
            }.execute();
    }

    private SubjectResponse parse(String json)throws Exception{
        return new Gson().fromJson(json,SubjectResponse.class);
    }

    private String getUrl(){
        //http://openlibrary.org/subjects/love.json?details=true&ebooks=true&limit=10&offset=0
        return "http://openlibrary.org/subjects/"+
                subjectName+".json?"+
                (includeFullDetail?"details=true":"details=false")+
                (onlyEbooks?"&ebooks=true":"&ebooks=false")+
                "&limit="+ limit.toString()
                +"&offset="+offset.toString();
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


    public static final String response_example = "{\n" +
            "    \"publishers\": [\n" +
            "        {\n" +
            "            \"count\": 106,\n" +
            "            \"name\": \"Bantam Books\",\n" +
            "            \"key\": \"/publishers/Bantam_Books\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 59,\n" +
            "            \"name\": \"Sine nomine\",\n" +
            "            \"key\": \"/publishers/Sine_nomine\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 58,\n" +
            "            \"name\": \"Scholastic\",\n" +
            "            \"key\": \"/publishers/Scholastic\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 53,\n" +
            "            \"name\": \"HarperCollins\",\n" +
            "            \"key\": \"/publishers/HarperCollins\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 52,\n" +
            "            \"name\": \"Delacorte Press\",\n" +
            "            \"key\": \"/publishers/Delacorte_Press\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 52,\n" +
            "            \"name\": \"Harper & Row\",\n" +
            "            \"key\": \"/publishers/Harper_&_Row\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 50,\n" +
            "            \"name\": \"Pocket Books\",\n" +
            "            \"key\": \"/publishers/Pocket_Books\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 44,\n" +
            "            \"name\": \"Simon Pulse\",\n" +
            "            \"key\": \"/publishers/Simon_Pulse\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 38,\n" +
            "            \"name\": \"Tandem Library\",\n" +
            "            \"key\": \"/publishers/Tandem_Library\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 37,\n" +
            "            \"name\": \"Simon & Schuster\",\n" +
            "            \"key\": \"/publishers/Simon_&_Schuster\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 36,\n" +
            "            \"name\": \"Oxford University Press\",\n" +
            "            \"key\": \"/publishers/Oxford_University_Press\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 35,\n" +
            "            \"name\": \"HarperTeen\",\n" +
            "            \"key\": \"/publishers/HarperTeen\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 34,\n" +
            "            \"name\": \"Doubleday\",\n" +
            "            \"key\": \"/publishers/Doubleday\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 34,\n" +
            "            \"name\": \"Macmillan\",\n" +
            "            \"key\": \"/publishers/Macmillan\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 34,\n" +
            "            \"name\": \"St. Martin's Press\",\n" +
            "            \"key\": \"/publishers/St._Martin's_Press\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 34,\n" +
            "            \"name\": \"Thorndike Press\",\n" +
            "            \"key\": \"/publishers/Thorndike_Press\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 32,\n" +
            "            \"name\": \"Thomas Nelson\",\n" +
            "            \"key\": \"/publishers/Thomas_Nelson\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 31,\n" +
            "            \"name\": \"Bantam\",\n" +
            "            \"key\": \"/publishers/Bantam\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 29,\n" +
            "            \"name\": \"Random House\",\n" +
            "            \"key\": \"/publishers/Random_House\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 28,\n" +
            "            \"name\": \"Harvest House Publishers\",\n" +
            "            \"key\": \"/publishers/Harvest_House_Publishers\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 27,\n" +
            "            \"name\": \"Gallimard\",\n" +
            "            \"key\": \"/publishers/Gallimard\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 26,\n" +
            "            \"name\": \"Paulist Press\",\n" +
            "            \"key\": \"/publishers/Paulist_Press\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 26,\n" +
            "            \"name\": \"Penguin Books\",\n" +
            "            \"key\": \"/publishers/Penguin_Books\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 25,\n" +
            "            \"name\": \"Hyperion\",\n" +
            "            \"key\": \"/publishers/Hyperion\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 24,\n" +
            "            \"name\": \"Cambridge University Press\",\n" +
            "            \"key\": \"/publishers/Cambridge_University_Press\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"subject_type\": \"subject\",\n" +
            "    \"name\": \"Love\",\n" +
            "    \"places\": [\n" +
            "        {\n" +
            "            \"count\": 125,\n" +
            "            \"name\": \"United States\",\n" +
            "            \"key\": \"/subjects/place:united_states\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 58,\n" +
            "            \"name\": \"France\",\n" +
            "            \"key\": \"/subjects/place:france\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 53,\n" +
            "            \"name\": \"England\",\n" +
            "            \"key\": \"/subjects/place:england\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 28,\n" +
            "            \"name\": \"China\",\n" +
            "            \"key\": \"/subjects/place:china\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 28,\n" +
            "            \"name\": \"Italy\",\n" +
            "            \"key\": \"/subjects/place:italy\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 26,\n" +
            "            \"name\": \"India\",\n" +
            "            \"key\": \"/subjects/place:india\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 25,\n" +
            "            \"name\": \"Europe\",\n" +
            "            \"key\": \"/subjects/place:europe\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 24,\n" +
            "            \"name\": \"Great Britain\",\n" +
            "            \"key\": \"/subjects/place:great_britain\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 24,\n" +
            "            \"name\": \"New York (N.Y.)\",\n" +
            "            \"key\": \"/subjects/place:new_york_(n.y.)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 23,\n" +
            "            \"name\": \"Japan\",\n" +
            "            \"key\": \"/subjects/place:japan\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 20,\n" +
            "            \"name\": \"Greece\",\n" +
            "            \"key\": \"/subjects/place:greece\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 20,\n" +
            "            \"name\": \"London\",\n" +
            "            \"key\": \"/subjects/place:london\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 16,\n" +
            "            \"name\": \"New York\",\n" +
            "            \"key\": \"/subjects/place:new_york\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 16,\n" +
            "            \"name\": \"Spain\",\n" +
            "            \"key\": \"/subjects/place:spain\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 15,\n" +
            "            \"name\": \"Portugal\",\n" +
            "            \"key\": \"/subjects/place:portugal\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 14,\n" +
            "            \"name\": \"Taiwan\",\n" +
            "            \"key\": \"/subjects/place:taiwan\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 13,\n" +
            "            \"name\": \"California\",\n" +
            "            \"key\": \"/subjects/place:california\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"Canada\",\n" +
            "            \"key\": \"/subjects/place:canada\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"London (England)\",\n" +
            "            \"key\": \"/subjects/place:london_(england)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"Rome\",\n" +
            "            \"key\": \"/subjects/place:rome\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 11,\n" +
            "            \"name\": \"Paris\",\n" +
            "            \"key\": \"/subjects/place:paris\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"Argentina\",\n" +
            "            \"key\": \"/subjects/place:argentina\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"Brazil\",\n" +
            "            \"key\": \"/subjects/place:brazil\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Australia\",\n" +
            "            \"key\": \"/subjects/place:australia\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Egypt\",\n" +
            "            \"key\": \"/subjects/place:egypt\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"people\": [\n" +
            "        {\n" +
            "            \"count\": 67,\n" +
            "            \"name\": \"Plato\",\n" +
            "            \"key\": \"/subjects/person:plato\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 57,\n" +
            "            \"name\": \"Jesus Christ\",\n" +
            "            \"key\": \"/subjects/person:jesus_christ\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 50,\n" +
            "            \"name\": \"Socrates\",\n" +
            "            \"key\": \"/subjects/person:socrates\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 30,\n" +
            "            \"name\": \"Thomas Aquinas, Saint (1225?-1274)\",\n" +
            "            \"key\": \"/subjects/person:thomas_aquinas_saint_(1225-1274)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 20,\n" +
            "            \"name\": \"Augustine Saint, Bishop of Hippo\",\n" +
            "            \"key\": \"/subjects/person:augustine_saint_bishop_of_hippo\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 14,\n" +
            "            \"name\": \"William Shakespeare (1564-1616)\",\n" +
            "            \"key\": \"/subjects/person:william_shakespeare_(1564-1616)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"Sigmund Freud (1856-1939)\",\n" +
            "            \"key\": \"/subjects/person:sigmund_freud_(1856-1939)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 11,\n" +
            "            \"name\": \"Vātsyāyana\",\n" +
            "            \"key\": \"/subjects/person:vātsyāyana\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Julian of Norwich (b. 1343)\",\n" +
            "            \"key\": \"/subjects/person:julian_of_norwich_(b._1343)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Mary Blessed Virgin, Saint\",\n" +
            "            \"key\": \"/subjects/person:mary_blessed_virgin_saint\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"Dante Alighieri (1265-1321)\",\n" +
            "            \"key\": \"/subjects/person:dante_alighieri_(1265-1321)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"León Hebreo (b. ca. 1460)\",\n" +
            "            \"key\": \"/subjects/person:león_hebreo_(b._ca._1460)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"God\",\n" +
            "            \"key\": \"/subjects/person:god\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Sorin Cerin\",\n" +
            "            \"key\": \"/subjects/person:sorin_cerin\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Søren Kierkegaard (1813-1855)\",\n" +
            "            \"key\": \"/subjects/person:søren_kierkegaard_(1813-1855)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 6,\n" +
            "            \"name\": \"François de Salignac de La Mothe- Fénelon (1651-1715)\",\n" +
            "            \"key\": \"/subjects/person:françois_de_salignac_de_la_mothe-_fénelon_(1651-1715)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 6,\n" +
            "            \"name\": \"Lysias\",\n" +
            "            \"key\": \"/subjects/person:lysias\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 6,\n" +
            "            \"name\": \"Marsilio Ficino (1433-1499)\",\n" +
            "            \"key\": \"/subjects/person:marsilio_ficino_(1433-1499)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 5,\n" +
            "            \"name\": \"Francis de Sales, Saint (1567-1622)\",\n" +
            "            \"key\": \"/subjects/person:francis_de_sales_saint_(1567-1622)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 5,\n" +
            "            \"name\": \"John of the Cross Saint (1542-1591)\",\n" +
            "            \"key\": \"/subjects/person:john_of_the_cross_saint_(1542-1591)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 5,\n" +
            "            \"name\": \"Plotinus\",\n" +
            "            \"key\": \"/subjects/person:plotinus\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"Aristotle\",\n" +
            "            \"key\": \"/subjects/person:aristotle\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"Bernard of Clairvaux, Saint (1090 or 91-1153)\",\n" +
            "            \"key\": \"/subjects/person:bernard_of_clairvaux_saint_(1090_or_91-1153)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"Georg Wilhelm Friedrich Hegel (1770-1831)\",\n" +
            "            \"key\": \"/subjects/person:georg_wilhelm_friedrich_hegel_(1770-1831)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"Ibn Ḥazm, ʻAlī ibn Aḥmad (994-1064)\",\n" +
            "            \"key\": \"/subjects/person:ibn_ḥazm_ʻalī_ibn_aḥmad_(994-1064)\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"publishing_history\": [\n" +
            "        [\n" +
            "            1471,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1492,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1494,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1516,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1519,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1525,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1536,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1545,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1549,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1552,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1554,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1555,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1558,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1559,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1561,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1563,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1564,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1566,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1568,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1569,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1572,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1581,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1587,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1588,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1592,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1594,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1595,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1600,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1602,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1603,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1606,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1607,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1610,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1613,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1615,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1616,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1620,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1624,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1629,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1630,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1631,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1632,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1634,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1637,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1640,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1641,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1643,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1644,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1645,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1649,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1650,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1651,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1652,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1655,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1656,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1657,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1659,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1660,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1661,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1662,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1663,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1664,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1665,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1667,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1668,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1669,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1670,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1671,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1672,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1673,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1675,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1677,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1678,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1679,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1680,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1682,\n" +
            "            5\n" +
            "        ],\n" +
            "        [\n" +
            "            1683,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1684,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1686,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1687,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1688,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1689,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1691,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1692,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1693,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1694,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1696,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1697,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1698,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1699,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1700,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1701,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1708,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1709,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1711,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1712,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1713,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1716,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1717,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1719,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1721,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1723,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1725,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1726,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1730,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1732,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1735,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1737,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1738,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1739,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1744,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1746,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1747,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1748,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1750,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1752,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1753,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1754,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1756,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1758,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1759,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1762,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1764,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1765,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1767,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1768,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1769,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1770,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1772,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1773,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1775,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1777,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1779,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1782,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1784,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1786,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1787,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1788,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1789,\n" +
            "            5\n" +
            "        ],\n" +
            "        [\n" +
            "            1790,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1791,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1792,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1793,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1794,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1795,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1796,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1798,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1799,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1800,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1801,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1802,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1805,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1806,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1808,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1810,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1811,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1812,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1813,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1815,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1816,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1817,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1818,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1821,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1822,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1824,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1825,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1827,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1829,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1830,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1833,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1834,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1835,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1836,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1837,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1838,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1839,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1840,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1841,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1842,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1843,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1844,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1845,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1846,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1847,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1848,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1849,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1850,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1851,\n" +
            "            5\n" +
            "        ],\n" +
            "        [\n" +
            "            1853,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1856,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1857,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1858,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1859,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1860,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1861,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1862,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1863,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1864,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1865,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1866,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1867,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1868,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1869,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1870,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1871,\n" +
            "            14\n" +
            "        ],\n" +
            "        [\n" +
            "            1872,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1873,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1874,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1875,\n" +
            "            8\n" +
            "        ],\n" +
            "        [\n" +
            "            1876,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1877,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1878,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1879,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1880,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1881,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            1882,\n" +
            "            2\n" +
            "        ],\n" +
            "        [\n" +
            "            1883,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1884,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1885,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1886,\n" +
            "            11\n" +
            "        ],\n" +
            "        [\n" +
            "            1887,\n" +
            "            4\n" +
            "        ],\n" +
            "        [\n" +
            "            1888,\n" +
            "            3\n" +
            "        ],\n" +
            "        [\n" +
            "            1889,\n" +
            "            8\n" +
            "        ],\n" +
            "        [\n" +
            "            1890,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1891,\n" +
            "            12\n" +
            "        ],\n" +
            "        [\n" +
            "            1892,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1893,\n" +
            "            13\n" +
            "        ],\n" +
            "        [\n" +
            "            1894,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1895,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1896,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1897,\n" +
            "            10\n" +
            "        ],\n" +
            "        [\n" +
            "            1898,\n" +
            "            8\n" +
            "        ],\n" +
            "        [\n" +
            "            1899,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1900,\n" +
            "            29\n" +
            "        ],\n" +
            "        [\n" +
            "            1901,\n" +
            "            11\n" +
            "        ],\n" +
            "        [\n" +
            "            1902,\n" +
            "            11\n" +
            "        ],\n" +
            "        [\n" +
            "            1903,\n" +
            "            5\n" +
            "        ],\n" +
            "        [\n" +
            "            1904,\n" +
            "            5\n" +
            "        ],\n" +
            "        [\n" +
            "            1905,\n" +
            "            10\n" +
            "        ],\n" +
            "        [\n" +
            "            1906,\n" +
            "            7\n" +
            "        ],\n" +
            "        [\n" +
            "            1907,\n" +
            "            10\n" +
            "        ],\n" +
            "        [\n" +
            "            1908,\n" +
            "            6\n" +
            "        ],\n" +
            "        [\n" +
            "            1909,\n" +
            "            12\n" +
            "        ],\n" +
            "        [\n" +
            "            1910,\n" +
            "            9\n" +
            "        ],\n" +
            "        [\n" +
            "            1911,\n" +
            "            22\n" +
            "        ],\n" +
            "        [\n" +
            "            1912,\n" +
            "            14\n" +
            "        ],\n" +
            "        [\n" +
            "            1913,\n" +
            "            9\n" +
            "        ],\n" +
            "        [\n" +
            "            1914,\n" +
            "            13\n" +
            "        ],\n" +
            "        [\n" +
            "            1915,\n" +
            "            12\n" +
            "        ],\n" +
            "        [\n" +
            "            1916,\n" +
            "            9\n" +
            "        ],\n" +
            "        [\n" +
            "            1917,\n" +
            "            11\n" +
            "        ],\n" +
            "        [\n" +
            "            1918,\n" +
            "            13\n" +
            "        ],\n" +
            "        [\n" +
            "            1919,\n" +
            "            10\n" +
            "        ],\n" +
            "        [\n" +
            "            1920,\n" +
            "            8\n" +
            "        ],\n" +
            "        [\n" +
            "            1921,\n" +
            "            10\n" +
            "        ],\n" +
            "        [\n" +
            "            1922,\n" +
            "            16\n" +
            "        ],\n" +
            "        [\n" +
            "            1923,\n" +
            "            13\n" +
            "        ],\n" +
            "        [\n" +
            "            1924,\n" +
            "            24\n" +
            "        ],\n" +
            "        [\n" +
            "            1925,\n" +
            "            13\n" +
            "        ],\n" +
            "        [\n" +
            "            1926,\n" +
            "            18\n" +
            "        ],\n" +
            "        [\n" +
            "            1927,\n" +
            "            19\n" +
            "        ],\n" +
            "        [\n" +
            "            1928,\n" +
            "            12\n" +
            "        ],\n" +
            "        [\n" +
            "            1929,\n" +
            "            15\n" +
            "        ],\n" +
            "        [\n" +
            "            1930,\n" +
            "            28\n" +
            "        ],\n" +
            "        [\n" +
            "            1931,\n" +
            "            19\n" +
            "        ],\n" +
            "        [\n" +
            "            1932,\n" +
            "            14\n" +
            "        ],\n" +
            "        [\n" +
            "            1933,\n" +
            "            15\n" +
            "        ],\n" +
            "        [\n" +
            "            1934,\n" +
            "            14\n" +
            "        ],\n" +
            "        [\n" +
            "            1935,\n" +
            "            17\n" +
            "        ],\n" +
            "        [\n" +
            "            1936,\n" +
            "            17\n" +
            "        ],\n" +
            "        [\n" +
            "            1937,\n" +
            "            21\n" +
            "        ],\n" +
            "        [\n" +
            "            1938,\n" +
            "            19\n" +
            "        ],\n" +
            "        [\n" +
            "            1939,\n" +
            "            30\n" +
            "        ],\n" +
            "        [\n" +
            "            1940,\n" +
            "            12\n" +
            "        ],\n" +
            "        [\n" +
            "            1941,\n" +
            "            19\n" +
            "        ],\n" +
            "        [\n" +
            "            1942,\n" +
            "            21\n" +
            "        ],\n" +
            "        [\n" +
            "            1943,\n" +
            "            16\n" +
            "        ],\n" +
            "        [\n" +
            "            1944,\n" +
            "            21\n" +
            "        ],\n" +
            "        [\n" +
            "            1945,\n" +
            "            16\n" +
            "        ],\n" +
            "        [\n" +
            "            1946,\n" +
            "            18\n" +
            "        ],\n" +
            "        [\n" +
            "            1947,\n" +
            "            26\n" +
            "        ],\n" +
            "        [\n" +
            "            1948,\n" +
            "            18\n" +
            "        ],\n" +
            "        [\n" +
            "            1949,\n" +
            "            19\n" +
            "        ],\n" +
            "        [\n" +
            "            1950,\n" +
            "            24\n" +
            "        ],\n" +
            "        [\n" +
            "            1951,\n" +
            "            20\n" +
            "        ],\n" +
            "        [\n" +
            "            1952,\n" +
            "            34\n" +
            "        ],\n" +
            "        [\n" +
            "            1953,\n" +
            "            32\n" +
            "        ],\n" +
            "        [\n" +
            "            1954,\n" +
            "            11\n" +
            "        ],\n" +
            "        [\n" +
            "            1955,\n" +
            "            32\n" +
            "        ],\n" +
            "        [\n" +
            "            1956,\n" +
            "            23\n" +
            "        ],\n" +
            "        [\n" +
            "            1957,\n" +
            "            45\n" +
            "        ],\n" +
            "        [\n" +
            "            1958,\n" +
            "            24\n" +
            "        ],\n" +
            "        [\n" +
            "            1959,\n" +
            "            43\n" +
            "        ],\n" +
            "        [\n" +
            "            1960,\n" +
            "            47\n" +
            "        ],\n" +
            "        [\n" +
            "            1961,\n" +
            "            44\n" +
            "        ],\n" +
            "        [\n" +
            "            1962,\n" +
            "            53\n" +
            "        ],\n" +
            "        [\n" +
            "            1963,\n" +
            "            61\n" +
            "        ],\n" +
            "        [\n" +
            "            1964,\n" +
            "            58\n" +
            "        ],\n" +
            "        [\n" +
            "            1965,\n" +
            "            52\n" +
            "        ],\n" +
            "        [\n" +
            "            1966,\n" +
            "            61\n" +
            "        ],\n" +
            "        [\n" +
            "            1967,\n" +
            "            73\n" +
            "        ],\n" +
            "        [\n" +
            "            1968,\n" +
            "            51\n" +
            "        ],\n" +
            "        [\n" +
            "            1969,\n" +
            "            75\n" +
            "        ],\n" +
            "        [\n" +
            "            1970,\n" +
            "            83\n" +
            "        ],\n" +
            "        [\n" +
            "            1971,\n" +
            "            65\n" +
            "        ],\n" +
            "        [\n" +
            "            1972,\n" +
            "            86\n" +
            "        ],\n" +
            "        [\n" +
            "            1973,\n" +
            "            96\n" +
            "        ],\n" +
            "        [\n" +
            "            1974,\n" +
            "            90\n" +
            "        ],\n" +
            "        [\n" +
            "            1975,\n" +
            "            87\n" +
            "        ],\n" +
            "        [\n" +
            "            1976,\n" +
            "            94\n" +
            "        ],\n" +
            "        [\n" +
            "            1977,\n" +
            "            87\n" +
            "        ],\n" +
            "        [\n" +
            "            1978,\n" +
            "            94\n" +
            "        ],\n" +
            "        [\n" +
            "            1979,\n" +
            "            94\n" +
            "        ],\n" +
            "        [\n" +
            "            1980,\n" +
            "            102\n" +
            "        ],\n" +
            "        [\n" +
            "            1981,\n" +
            "            104\n" +
            "        ],\n" +
            "        [\n" +
            "            1982,\n" +
            "            112\n" +
            "        ],\n" +
            "        [\n" +
            "            1983,\n" +
            "            142\n" +
            "        ],\n" +
            "        [\n" +
            "            1984,\n" +
            "            153\n" +
            "        ],\n" +
            "        [\n" +
            "            1985,\n" +
            "            114\n" +
            "        ],\n" +
            "        [\n" +
            "            1986,\n" +
            "            124\n" +
            "        ],\n" +
            "        [\n" +
            "            1987,\n" +
            "            158\n" +
            "        ],\n" +
            "        [\n" +
            "            1988,\n" +
            "            165\n" +
            "        ],\n" +
            "        [\n" +
            "            1989,\n" +
            "            165\n" +
            "        ],\n" +
            "        [\n" +
            "            1990,\n" +
            "            194\n" +
            "        ],\n" +
            "        [\n" +
            "            1991,\n" +
            "            182\n" +
            "        ],\n" +
            "        [\n" +
            "            1992,\n" +
            "            197\n" +
            "        ],\n" +
            "        [\n" +
            "            1993,\n" +
            "            191\n" +
            "        ],\n" +
            "        [\n" +
            "            1994,\n" +
            "            220\n" +
            "        ],\n" +
            "        [\n" +
            "            1995,\n" +
            "            218\n" +
            "        ],\n" +
            "        [\n" +
            "            1996,\n" +
            "            254\n" +
            "        ],\n" +
            "        [\n" +
            "            1997,\n" +
            "            229\n" +
            "        ],\n" +
            "        [\n" +
            "            1998,\n" +
            "            279\n" +
            "        ],\n" +
            "        [\n" +
            "            1999,\n" +
            "            308\n" +
            "        ],\n" +
            "        [\n" +
            "            2000,\n" +
            "            259\n" +
            "        ],\n" +
            "        [\n" +
            "            2001,\n" +
            "            236\n" +
            "        ],\n" +
            "        [\n" +
            "            2002,\n" +
            "            238\n" +
            "        ],\n" +
            "        [\n" +
            "            2003,\n" +
            "            246\n" +
            "        ],\n" +
            "        [\n" +
            "            2004,\n" +
            "            241\n" +
            "        ],\n" +
            "        [\n" +
            "            2005,\n" +
            "            243\n" +
            "        ],\n" +
            "        [\n" +
            "            2006,\n" +
            "            243\n" +
            "        ],\n" +
            "        [\n" +
            "            2007,\n" +
            "            267\n" +
            "        ],\n" +
            "        [\n" +
            "            2008,\n" +
            "            232\n" +
            "        ],\n" +
            "        [\n" +
            "            2009,\n" +
            "            225\n" +
            "        ],\n" +
            "        [\n" +
            "            2010,\n" +
            "            243\n" +
            "        ],\n" +
            "        [\n" +
            "            2011,\n" +
            "            262\n" +
            "        ],\n" +
            "        [\n" +
            "            2012,\n" +
            "            246\n" +
            "        ],\n" +
            "        [\n" +
            "            2013,\n" +
            "            72\n" +
            "        ],\n" +
            "        [\n" +
            "            2014,\n" +
            "            29\n" +
            "        ],\n" +
            "        [\n" +
            "            2016,\n" +
            "            1\n" +
            "        ],\n" +
            "        [\n" +
            "            2017,\n" +
            "            3\n" +
            "        ]\n" +
            "    ],\n" +
            "    \"times\": [\n" +
            "        {\n" +
            "            \"count\": 62,\n" +
            "            \"name\": \"20th century\",\n" +
            "            \"key\": \"/subjects/time:20th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 38,\n" +
            "            \"name\": \"19th century\",\n" +
            "            \"key\": \"/subjects/time:19th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 32,\n" +
            "            \"name\": \"17th century\",\n" +
            "            \"key\": \"/subjects/time:17th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 19,\n" +
            "            \"name\": \"Middle Ages, 600-1500\",\n" +
            "            \"key\": \"/subjects/time:middle_ages_600-1500\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 15,\n" +
            "            \"name\": \"To 1500\",\n" +
            "            \"key\": \"/subjects/time:to_1500\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"18th century\",\n" +
            "            \"key\": \"/subjects/time:18th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 11,\n" +
            "            \"name\": \"Early church, ca. 30-600\",\n" +
            "            \"key\": \"/subjects/time:early_church_ca._30-600\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"21st century\",\n" +
            "            \"key\": \"/subjects/time:21st_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Early works to 1800\",\n" +
            "            \"key\": \"/subjects/time:early_works_to_1800\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"16th century\",\n" +
            "            \"key\": \"/subjects/time:16th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"1898-1951\",\n" +
            "            \"key\": \"/subjects/time:1898-1951\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 6,\n" +
            "            \"name\": \"Civil War, 1861-1865\",\n" +
            "            \"key\": \"/subjects/time:civil_war_1861-1865\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 5,\n" +
            "            \"name\": \"2008\",\n" +
            "            \"key\": \"/subjects/time:2008\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"2009\",\n" +
            "            \"key\": \"/subjects/time:2009\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"21st Century\",\n" +
            "            \"key\": \"/subjects/time:21st_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 4,\n" +
            "            \"name\": \"Now\",\n" +
            "            \"key\": \"/subjects/time:now\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"14th Febuary\",\n" +
            "            \"key\": \"/subjects/time:14th_febuary\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"1817-1860\",\n" +
            "            \"key\": \"/subjects/time:1817-1860\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"1950-\",\n" +
            "            \"key\": \"/subjects/time:1950-\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"2000\",\n" +
            "            \"key\": \"/subjects/time:2000\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"2014\",\n" +
            "            \"key\": \"/subjects/time:2014\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"Elizabeth, 1558-1603\",\n" +
            "            \"key\": \"/subjects/time:elizabeth_1558-1603\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"January\",\n" +
            "            \"key\": \"/subjects/time:january\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"Late 20th century\",\n" +
            "            \"key\": \"/subjects/time:late_20th_century\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 3,\n" +
            "            \"name\": \"Medieval, 500-1500\",\n" +
            "            \"key\": \"/subjects/time:medieval_500-1500\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"languages\": [\n" +
            "        {\n" +
            "            \"count\": 4677,\n" +
            "            \"name\": \"eng\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 522,\n" +
            "            \"name\": \"fre\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 347,\n" +
            "            \"name\": \"spa\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 258,\n" +
            "            \"name\": \"chi\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 225,\n" +
            "            \"name\": \"ger\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 217,\n" +
            "            \"name\": \"ita\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 101,\n" +
            "            \"name\": \"jpn\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 95,\n" +
            "            \"name\": \"ara\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 72,\n" +
            "            \"name\": \"rus\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 61,\n" +
            "            \"name\": \"por\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 38,\n" +
            "            \"name\": \"heb\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 35,\n" +
            "            \"name\": \"pol\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 32,\n" +
            "            \"name\": \"kor\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 31,\n" +
            "            \"name\": \"lat\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 25,\n" +
            "            \"name\": \"dut\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 24,\n" +
            "            \"name\": \"gre\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 19,\n" +
            "            \"name\": \"vie\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 17,\n" +
            "            \"name\": \"hin\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 15,\n" +
            "            \"name\": \"hun\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 15,\n" +
            "            \"name\": \"und\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 13,\n" +
            "            \"name\": \"dan\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"swe\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"tur\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"per\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"cat\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"subjects\": [\n" +
            "        {\n" +
            "            \"count\": 1843,\n" +
            "            \"name\": \"Accessible book\",\n" +
            "            \"key\": \"/subjects/accessible_book\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 1618,\n" +
            "            \"name\": \"Protected DAISY\",\n" +
            "            \"key\": \"/subjects/protected_daisy\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 1053,\n" +
            "            \"name\": \"In library\",\n" +
            "            \"key\": \"/subjects/in_library\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 1044,\n" +
            "            \"name\": \"Christianity\",\n" +
            "            \"key\": \"/subjects/christianity\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 1020,\n" +
            "            \"name\": \"Fiction\",\n" +
            "            \"key\": \"/subjects/fiction\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 942,\n" +
            "            \"name\": \"Religious aspects of Love\",\n" +
            "            \"key\": \"/subjects/religious_aspects_of_love\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 622,\n" +
            "            \"name\": \"Marriage\",\n" +
            "            \"key\": \"/subjects/marriage\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 568,\n" +
            "            \"name\": \"Man-woman relationships\",\n" +
            "            \"key\": \"/subjects/man-woman_relationships\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 559,\n" +
            "            \"name\": \"Religious aspects\",\n" +
            "            \"key\": \"/subjects/religious_aspects\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 526,\n" +
            "            \"name\": \"Interpersonal relations\",\n" +
            "            \"key\": \"/subjects/interpersonal_relations\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 476,\n" +
            "            \"name\": \"God\",\n" +
            "            \"key\": \"/subjects/god\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 405,\n" +
            "            \"name\": \"Sex\",\n" +
            "            \"key\": \"/subjects/sex\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 347,\n" +
            "            \"name\": \"Internet Archive Wishlist\",\n" +
            "            \"key\": \"/subjects/internet_archive_wishlist\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 265,\n" +
            "            \"name\": \"History\",\n" +
            "            \"key\": \"/subjects/history\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 265,\n" +
            "            \"name\": \"Juvenile fiction\",\n" +
            "            \"key\": \"/subjects/juvenile_fiction\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 244,\n" +
            "            \"name\": \"Miscellanea\",\n" +
            "            \"key\": \"/subjects/miscellanea\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 224,\n" +
            "            \"name\": \"Intimacy (Psychology)\",\n" +
            "            \"key\": \"/subjects/intimacy_(psychology)\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 219,\n" +
            "            \"name\": \"Early works to 1800\",\n" +
            "            \"key\": \"/subjects/early_works_to_1800\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 212,\n" +
            "            \"name\": \"love\",\n" +
            "            \"key\": \"/subjects/love\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 200,\n" +
            "            \"name\": \"Christian life\",\n" +
            "            \"key\": \"/subjects/christian_life\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 198,\n" +
            "            \"name\": \"Women\",\n" +
            "            \"key\": \"/subjects/women\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 184,\n" +
            "            \"name\": \"Friendship\",\n" +
            "            \"key\": \"/subjects/friendship\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 172,\n" +
            "            \"name\": \"Psychology\",\n" +
            "            \"key\": \"/subjects/psychology\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 171,\n" +
            "            \"name\": \"Psychological aspects\",\n" +
            "            \"key\": \"/subjects/psychological_aspects\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"key\": \"/subjects/love\",\n" +
            "    \"authors\": [\n" +
            "        {\n" +
            "            \"count\": 85,\n" +
            "            \"name\": \"Erich Fromm\",\n" +
            "            \"key\": \"/authors/OL119716A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 43,\n" +
            "            \"name\": \"Plato\",\n" +
            "            \"key\": \"/authors/OL189658A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 21,\n" +
            "            \"name\": \"Francine Pascal\",\n" +
            "            \"key\": \"/authors/OL716700A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 20,\n" +
            "            \"name\": \"Francis de Sales\",\n" +
            "            \"key\": \"/authors/OL116100A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 20,\n" +
            "            \"name\": \"Ruoquan Wu\",\n" +
            "            \"key\": \"/authors/OL5638565A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 17,\n" +
            "            \"name\": \"Gary D. Chapman\",\n" +
            "            \"key\": \"/authors/OL189792A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 15,\n" +
            "            \"name\": \"Hans Urs von Balthasar\",\n" +
            "            \"key\": \"/authors/OL38332A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 13,\n" +
            "            \"name\": \"Julian of Norwich\",\n" +
            "            \"key\": \"/authors/OL60439A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"Gary Smalley\",\n" +
            "            \"key\": \"/authors/OL22636A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 12,\n" +
            "            \"name\": \"Katherine A. Applegate\",\n" +
            "            \"key\": \"/authors/OL2703454A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 11,\n" +
            "            \"name\": \"Gregory J. P. Godek\",\n" +
            "            \"key\": \"/authors/OL67617A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"Leo F. Buscaglia\",\n" +
            "            \"key\": \"/authors/OL582115A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 10,\n" +
            "            \"name\": \"Lauren Kate\",\n" +
            "            \"key\": \"/authors/OL6686660A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Rougemont, Denis de\",\n" +
            "            \"key\": \"/authors/OL153000A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"Daphne Rose Kingma\",\n" +
            "            \"key\": \"/authors/OL30813A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 9,\n" +
            "            \"name\": \"William Shakespeare\",\n" +
            "            \"key\": \"/authors/OL9388A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"Max Lucado\",\n" +
            "            \"key\": \"/authors/OL21341A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 8,\n" +
            "            \"name\": \"Jules Gay\",\n" +
            "            \"key\": \"/authors/OL4447977A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"ʻAlī ibn Aḥmad Ibn Ḥazm\",\n" +
            "            \"key\": \"/authors/OL108297A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Emanuel Swedenborg\",\n" +
            "            \"key\": \"/authors/OL24981A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Henry Drummond\",\n" +
            "            \"key\": \"/authors/OL2791618A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Iyanla Vanzant\",\n" +
            "            \"key\": \"/authors/OL30875A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Dandi Daley Mackall\",\n" +
            "            \"key\": \"/authors/OL32072A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Claudia Gray\",\n" +
            "            \"key\": \"/authors/OL3352909A\"\n" +
            "        },\n" +
            "        {\n" +
            "            \"count\": 7,\n" +
            "            \"name\": \"Richard Carlson\",\n" +
            "            \"key\": \"/authors/OL34615A\"\n" +
            "        }\n" +
            "    ],\n" +
            "    \"ebook_count\": 7212,\n" +
            "    \"works\": [\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 474323,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"printdisabled\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cornell\",\n" +
            "                \"Wellesley_College_Library\",\n" +
            "                \"inlibrary\",\n" +
            "                \"robarts\",\n" +
            "                \"librarygenesis\",\n" +
            "                \"china\",\n" +
            "                \"browserlending\",\n" +
            "                \"blc\",\n" +
            "                \"19thcennov\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 332,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Wuthering Heights\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL7947975M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL24279932M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL15035771W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Emily Brontë\",\n" +
            "                    \"key\": \"/authors/OL4327048A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"wutheringheight00bron\",\n" +
            "            \"lending_identifier\": \"wutheringheightsbron00bron\",\n" +
            "            \"subject\": [\n" +
            "                \"Heathcliff (Fictitious character)\",\n" +
            "                \"In library\",\n" +
            "                \"Man-woman relationships\",\n" +
            "                \"Popular Print Disabled Books\",\n" +
            "                \"Readers\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Fiction in English\",\n" +
            "                \"Fiction\",\n" +
            "                \"England in fiction\",\n" +
            "                \"OverDrive\",\n" +
            "                \"Orphans in fiction\",\n" +
            "                \"Foundlings\",\n" +
            "                \"Foundlings in fiction\",\n" +
            "                \"Vocabulary\",\n" +
            "                \"Revenge\",\n" +
            "                \"Man-woman relationships in fiction\",\n" +
            "                \"Study and teaching\",\n" +
            "                \"Tragedy in fiction\",\n" +
            "                \"Landscape in literature in fiction\",\n" +
            "                \"Large type books\",\n" +
            "                \"Country life in fiction\",\n" +
            "                \"Classic Literature\",\n" +
            "                \"Landscape in literature\",\n" +
            "                \"Love in fiction\",\n" +
            "                \"Open Library Staff Picks\",\n" +
            "                \"SAT (Educational test)\",\n" +
            "                \"Rejection (Psychology)\",\n" +
            "                \"English language\",\n" +
            "                \"Slavery\",\n" +
            "                \"Country life\",\n" +
            "                \"Triangles (Interpersonal relations)\",\n" +
            "                \"Readers (Adult)\",\n" +
            "                \"Revenge in fiction\",\n" +
            "                \"Study guides\",\n" +
            "                \"Rural families\",\n" +
            "                \"Rural families in fiction\",\n" +
            "                \"Social life and customs\",\n" +
            "                \"Examinations\",\n" +
            "                \"Love\",\n" +
            "                \"Romantic fiction\",\n" +
            "                \"Reading books\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Slavery in fiction\",\n" +
            "                \"Orphans\",\n" +
            "                \"Vietnamese language books\",\n" +
            "                \"open_syllabus_project\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 6488532,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"uoftpreservation\",\n" +
            "                \"kellylibrary\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cornell\",\n" +
            "                \"pimslibrary\",\n" +
            "                \"inlibrary\",\n" +
            "                \"cdl\",\n" +
            "                \"robarts\",\n" +
            "                \"printdisabled\",\n" +
            "                \"librarygenesis\",\n" +
            "                \"china\",\n" +
            "                \"browserlending\",\n" +
            "                \"europeanlibraries\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 251,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"The Dialogues of Plato\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL23294348M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL3384453M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL15100036W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Plato\",\n" +
            "                    \"key\": \"/authors/OL189658A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"theworksofplato01platiala\",\n" +
            "            \"lending_identifier\": \"dialoguesofplato96plat\",\n" +
            "            \"subject\": [\n" +
            "                \"In library\",\n" +
            "                \"Bibliography\",\n" +
            "                \"Utopias\",\n" +
            "                \"Platao\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Rhetoric\",\n" +
            "                \"Methodology\",\n" +
            "                \"OverDrive\",\n" +
            "                \"Dialogen\",\n" +
            "                \"History & criticism\",\n" +
            "                \"Ancient & Classical\",\n" +
            "                \"Filosofie\",\n" +
            "                \"History & Surveys\",\n" +
            "                \"Literatura griega\",\n" +
            "                \"Philosophie ancienne\",\n" +
            "                \"Philosophy\",\n" +
            "                \"Grieks\",\n" +
            "                \"Philosophers\",\n" +
            "                \"Ouvrages avant 1800\",\n" +
            "                \"Greek Dialogues\",\n" +
            "                \"Political science\",\n" +
            "                \"Ethics\",\n" +
            "                \"Ontology\",\n" +
            "                \"Platonists\",\n" +
            "                \"Death and burial\",\n" +
            "                \"Filosofía antigua\",\n" +
            "                \"Trials, litigation\",\n" +
            "                \"Klassieke oudheid\",\n" +
            "                \"Ancient Philosophy\",\n" +
            "                \"Poetics\",\n" +
            "                \"Vertus\",\n" +
            "                \"Ancient Rhetoric\",\n" +
            "                \"Socrates\",\n" +
            "                \"Greek poetry\",\n" +
            "                \"open_syllabus_project\",\n" +
            "                \"Filosofía griega\",\n" +
            "                \"Pleasure\",\n" +
            "                \"Friendship\",\n" +
            "                \"Male Homosexuality\",\n" +
            "                \"Language and languages\",\n" +
            "                \"History\",\n" +
            "                \"Imaginary conversations\",\n" +
            "                \"The Holy\",\n" +
            "                \"Aesthetics\",\n" +
            "                \"Love\",\n" +
            "                \"Electronic books\",\n" +
            "                \"History and criticism\",\n" +
            "                \"Platonismo\",\n" +
            "                \"Jowett, Benjamin, 1817-1893\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Colecciones de escritos\",\n" +
            "                \"Nonfiction\",\n" +
            "                \"Colecciones\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Theory of Knowledge\",\n" +
            "                \"Sophists (Greek philosophy)\",\n" +
            "                \"Biography\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 295772,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"university_of_alberta_libraries\",\n" +
            "                \"kellylibrary\",\n" +
            "                \"browserlending\",\n" +
            "                \"americana\",\n" +
            "                \"ubclibrary\",\n" +
            "                \"cdl\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"microfilm\",\n" +
            "                \"university_of_alberta_libraries_microfilm\",\n" +
            "                \"universityofottawa\",\n" +
            "                \"inlibrary\",\n" +
            "                \"robarts\",\n" +
            "                \"printdisabled\",\n" +
            "                \"china\",\n" +
            "                \"additional_collections\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 198,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Cyrano de Bergerac\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL3684423M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL24204534M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL551668W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Edmond Rostand\",\n" +
            "                    \"key\": \"/authors/OL39281A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"cyranodeberger00rost\",\n" +
            "            \"lending_identifier\": \"cyranodebergera00rost\",\n" +
            "            \"subject\": [\n" +
            "                \"Fa guo xiao shuo\",\n" +
            "                \"In library\",\n" +
            "                \"French Authors\",\n" +
            "                \"French drama\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Fiction\",\n" +
            "                \"OverDrive\",\n" +
            "                \"1619-1655\",\n" +
            "                \"French language\",\n" +
            "                \"Translations from French\",\n" +
            "                \"Théâtre\",\n" +
            "                \"Large type books\",\n" +
            "                \"In literature\",\n" +
            "                \"Cyrano de Bergerac, 1619-1655\",\n" +
            "                \"Cyrano de Bergerac, Savinien, 1619-1655\",\n" +
            "                \"English drama\",\n" +
            "                \"Rejection (Psychology)\",\n" +
            "                \"Drama\",\n" +
            "                \"Adventure stories\",\n" +
            "                \"History\",\n" +
            "                \"Social life and customs\",\n" +
            "                \"Classic Literature\",\n" +
            "                \"Love\",\n" +
            "                \"Translations into English\",\n" +
            "                \"Protected DAISY\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 104356,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"printdisabled\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cornell\",\n" +
            "                \"inlibrary\",\n" +
            "                \"cdl\",\n" +
            "                \"robarts\",\n" +
            "                \"librarygenesis\",\n" +
            "                \"china\",\n" +
            "                \"browserlending\",\n" +
            "                \"trinitycollege\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 140,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Symposion\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL7355485M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL7355041M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL51950W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Plato\",\n" +
            "                    \"key\": \"/authors/OL189658A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"banquetofplato00platrich\",\n" +
            "            \"lending_identifier\": \"symposiumtransla00plat\",\n" +
            "            \"subject\": [\n" +
            "                \"great_books_of_the_western_world\",\n" +
            "                \"Religious aspects of Love\",\n" +
            "                \"Ouvrages avant 1800\",\n" +
            "                \"Accessible book\",\n" +
            "                \"In library\",\n" +
            "                \"Christianity\",\n" +
            "                \"Platonic love\",\n" +
            "                \"Socrate\",\n" +
            "                \"Philosophy\",\n" +
            "                \"Rhetoric\",\n" +
            "                \"Soul\",\n" +
            "                \"Ancient Rhetoric\",\n" +
            "                \"Filosofia antiga\",\n" +
            "                \"Amour\",\n" +
            "                \"Mythology\",\n" +
            "                \"Imaginary conversations\",\n" +
            "                \"Love\",\n" +
            "                \"Amor (filosofia)\",\n" +
            "                \"Influence\",\n" +
            "                \"Immortality (Philosophy)\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Theory of Knowledge\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 5991932,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"lendinglibrary\",\n" +
            "                \"browserlending\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cornell\",\n" +
            "                \"microfilm\",\n" +
            "                \"emmanuelcollege\",\n" +
            "                \"cdl\",\n" +
            "                \"inlibrary\",\n" +
            "                \"robarts\",\n" +
            "                \"printdisabled\",\n" +
            "                \"librarygenesis\",\n" +
            "                \"china\",\n" +
            "                \"friendsofthesanfranciscopubliclibrary\",\n" +
            "                \"additional_collections\",\n" +
            "                \"trinitycollege\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 124,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Phaedo\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL20423446M\",\n" +
            "            \"lendinglibrary\": true,\n" +
            "            \"lending_edition\": \"OL24220028M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL51949W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Plato\",\n" +
            "                    \"key\": \"/authors/OL189658A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"phaedoliterally00platuoft\",\n" +
            "            \"lending_identifier\": \"phaedopl00plat\",\n" +
            "            \"subject\": [\n" +
            "                \"In library\",\n" +
            "                \"Immortalité (Philosophie)\",\n" +
            "                \"Love\",\n" +
            "                \"Âme\",\n" +
            "                \"Ouvrages avant 1800\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Phaedo (Plato)\",\n" +
            "                \"Immortality (Philosophy)\",\n" +
            "                \"Ancient Rhetoric\",\n" +
            "                \"Fiction\",\n" +
            "                \"great_books_of_the_western_world\",\n" +
            "                \"Immortality\",\n" +
            "                \"OverDrive\",\n" +
            "                \"Biblioteca Nacional (Spain)\",\n" +
            "                \"open_syllabus_project\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Classic Literature\",\n" +
            "                \"Lending library\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": false,\n" +
            "            \"cover_id\": 230682,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"pimslibrary\",\n" +
            "                \"robarts\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 111,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Ars amatoria\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL3569091M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL97774W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Ovid\",\n" +
            "                    \"key\": \"/authors/OL18770A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"dearteamatoriali00oviduoft\",\n" +
            "            \"lending_identifier\": \"\",\n" +
            "            \"subject\": [\n" +
            "                \"Man-woman relationships\",\n" +
            "                \"Readers\",\n" +
            "                \"Latin Erotic poetry\",\n" +
            "                \"Seduction\",\n" +
            "                \"Poetry\",\n" +
            "                \"Anthologies\",\n" +
            "                \"Incantations\",\n" +
            "                \"Latin language\",\n" +
            "                \"Poesía\",\n" +
            "                \"Translations into French\",\n" +
            "                \"Love poetry\",\n" +
            "                \"Problems, exercises\",\n" +
            "                \"Latin poetry\",\n" +
            "                \"Translations into Italian\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Traducciones al español\",\n" +
            "                \"Separation (Psychology)\",\n" +
            "                \"Translations into German\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Separación (Psicología)\",\n" +
            "                \"Translations into Spanish\",\n" +
            "                \"Seducation\",\n" +
            "                \"Latin literature\",\n" +
            "                \"Love\",\n" +
            "                \"Translations into Russian\",\n" +
            "                \"Latin Didactic poetry\",\n" +
            "                \"Latin Love poetry\",\n" +
            "                \"Translations into English\",\n" +
            "                \"Poesía amorosa latina\",\n" +
            "                \"Poesía erótica latina\",\n" +
            "                \"Seducción\",\n" +
            "                \"Poesía didáctica latina\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": false,\n" +
            "            \"cover_id\": 2114293,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"kellylibrary\",\n" +
            "                \"prscr\",\n" +
            "                \"unclibraries\",\n" +
            "                \"americana\",\n" +
            "                \"europeanlibraries\",\n" +
            "                \"thomasfisher\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 66,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Introduction à la vie dévote\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL9521902M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL1140014W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Francis de Sales\",\n" +
            "                    \"key\": \"/authors/OL116100A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"introductiontodefran\",\n" +
            "            \"lending_identifier\": \"\",\n" +
            "            \"subject\": [\n" +
            "                \"Bible\",\n" +
            "                \"Devotional exercises\",\n" +
            "                \"Religious aspects of Love\",\n" +
            "                \"Prayers and devotions\",\n" +
            "                \"Meditations\",\n" +
            "                \"God\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Christian life\",\n" +
            "                \"Maxims\",\n" +
            "                \"Worship and love\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Devotional literature\",\n" +
            "                \"Méditations\",\n" +
            "                \"Spiritual life\",\n" +
            "                \"Spanish\",\n" +
            "                \"Mystics\",\n" +
            "                \"Love\",\n" +
            "                \"Catholic authors\",\n" +
            "                \"Catholic Church\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 1801119,\n" +
            "            \"ia_collection\": [\n" +
            "                \"printdisabled\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cdl\",\n" +
            "                \"browserlending\",\n" +
            "                \"inlibrary\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 62,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Phaedrus\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL8471806M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL6215336M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL51943W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Plato\",\n" +
            "                    \"key\": \"/authors/OL189658A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"phaedrusofplato00plat\",\n" +
            "            \"lending_identifier\": \"phaedrus00plat\",\n" +
            "            \"subject\": [\n" +
            "                \"Rhetoric\",\n" +
            "                \"Love\",\n" +
            "                \"Ancient Philosophy\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Love.\",\n" +
            "                \"Soul\",\n" +
            "                \"Ancient Rhetoric\",\n" +
            "                \"great_books_of_the_western_world\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"open_syllabus_project\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Theory of Knowledge\",\n" +
            "                \"In library\",\n" +
            "                \"Inspiration\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": true,\n" +
            "            \"cover_id\": 107854,\n" +
            "            \"ia_collection\": [\n" +
            "                \"printdisabled\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cdl\",\n" +
            "                \"inlibrary\",\n" +
            "                \"china\",\n" +
            "                \"librarygenesis\",\n" +
            "                \"browserlending\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 55,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Novels\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL118081M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"OL1270593M\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL88879W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"E. M. Forster\",\n" +
            "                    \"key\": \"/authors/OL6898863A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"roomwithview00forsrich\",\n" +
            "            \"lending_identifier\": \"roomwithview00fors_1\",\n" +
            "            \"subject\": [\n" +
            "                \"Movie novels\",\n" +
            "                \"In library\",\n" +
            "                \"British in fiction\",\n" +
            "                \"love\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Fiction in English\",\n" +
            "                \"British\",\n" +
            "                \"Fiction\",\n" +
            "                \"romance\",\n" +
            "                \"England in fiction\",\n" +
            "                \"Young women in fiction\",\n" +
            "                \"Historical fiction\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Young women\",\n" +
            "                \"Travel\"\n" +
            "            ]\n" +
            "        },\n" +
            "        {\n" +
            "            \"printdisabled\": false,\n" +
            "            \"cover_id\": 6981572,\n" +
            "            \"ia_collection\": [\n" +
            "                \"toronto\",\n" +
            "                \"china\",\n" +
            "                \"americana\",\n" +
            "                \"internetarchivebooks\",\n" +
            "                \"cdl\",\n" +
            "                \"robarts\"\n" +
            "            ],\n" +
            "            \"has_fulltext\": true,\n" +
            "            \"edition_count\": 54,\n" +
            "            \"checked_out\": false,\n" +
            "            \"title\": \"Catullus\",\n" +
            "            \"public_scan\": true,\n" +
            "            \"cover_edition_key\": \"OL17793209M\",\n" +
            "            \"lendinglibrary\": false,\n" +
            "            \"lending_edition\": \"\",\n" +
            "            \"first_publish_year\": null,\n" +
            "            \"key\": \"/works/OL15229223W\",\n" +
            "            \"authors\": [\n" +
            "                {\n" +
            "                    \"name\": \"Gaius Valerius Catullus\",\n" +
            "                    \"key\": \"/authors/OL16144A\"\n" +
            "                },\n" +
            "                {\n" +
            "                    \"name\": \"Gaius Valerius Catullus\",\n" +
            "                    \"key\": \"/authors/OL6898449A\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"ia\": \"catulluseditedby00catuuoft\",\n" +
            "            \"lending_identifier\": \"\",\n" +
            "            \"subject\": [\n" +
            "                \"Love\",\n" +
            "                \"Readers\",\n" +
            "                \"Latin Love poetry\",\n" +
            "                \"Latin poetry\",\n" +
            "                \"Accessible book\",\n" +
            "                \"Translations into English\",\n" +
            "                \"Poetry\",\n" +
            "                \"Latin Elegiac poetry\",\n" +
            "                \"Latin language\",\n" +
            "                \"Protected DAISY\",\n" +
            "                \"Early works to 1800\",\n" +
            "                \"Epigrams\",\n" +
            "                \"Latin Epigrams\",\n" +
            "                \"Textual Criticism\"\n" +
            "            ]\n" +
            "        }\n" +
            "    ],\n" +
            "    \"work_count\": 7212\n" +
            "}";
}
