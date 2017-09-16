package com.example.mannas.capstone;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.mannas.capstone.connection.BroadcastManager;
import com.example.mannas.capstone.connection.ConnectionListener;
import com.example.mannas.capstone.data.ContentProvider.Contract;
import com.example.mannas.capstone.data.Loaders.BookDetailLoader;
import com.example.mannas.capstone.data.Loaders.SubjectLoader;
import com.example.mannas.capstone.data.SharedPrefirencesManager;
import com.example.mannas.capstone.data.Util.Work;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

/**
 * An activity representing a list of Books. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link BookDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BookListActivity extends AppCompatActivity
implements LoaderManager.LoaderCallbacks<SubjectLoader.SubjectResponse>,
        ConnectionListener
{
    public static final String SUBJECT_KEY = "sub_key";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    protected boolean mTwoPane;
    protected Integer mSubjectRecyclerPage =0;
    protected String mSubjectName ;
    protected final Integer mSubjectLoaderID_initial = 0
            ,mSubjectLoaderID_more =1
            ,mSubjectLoaderID_new =2;
    Integer counter =0;

    protected SimpleItemRecyclerViewAdapter adapter;

    @BindView(R.id.activity_main_book_list) CoordinatorLayout activity_main_book_list;

    @BindView(R.id.content_master) View mainFragment_content;
        @BindView(R.id.wraper_subjects) View wraper_subjects;
        @BindView(R.id.empty_subject_recycler) View empty_subject_recycler;
        @BindView(R.id.wraper_recycler) View wraper_recycler;
    @BindView(R.id.loading_indicator) View loadingIndicator;

    @BindView(R.id.offline_sign) View offline_sign;
    @BindView(R.id.subjects_tag_view) TagContainerLayout subjects_tag_view;
    @BindView(R.id.load_more) Button moreBtn;

    Snackbar snackbar;


    AsyncTask<Void,Void,ArrayList<String>> subjectsAsyncTask = new SubjectsAsyncTask();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_book_list);

        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        View recyclerView = findViewById(R.id.main_recycler);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);

//        loadingIndicator = findViewById(R.id.loading_indicator);
//        offline_sign = findViewById(R.id.offline_sign);
//        subjects_tag_view = (TagContainerLayout) findViewById(R.id.subjects_tag_view);
        if(getIntent()!=null&& getIntent().getExtras()!=null && getIntent().getExtras().getString(SUBJECT_KEY)!=null){
            mSubjectName =  getIntent().getExtras().getString(SUBJECT_KEY);
            if(mSubjectName!=null) {
                mSubjectName = mSubjectName.replace(' ', '+');
                mSubjectName = mSubjectName.toLowerCase();
                getSupportLoaderManager().restartLoader(mSubjectLoaderID_initial,null,this).forceLoad();

            }
        }else{
            mSubjectName = SharedPrefirencesManager.getMainRecyclerSubjectName(this);
            getSupportLoaderManager().restartLoader(mSubjectLoaderID_initial,null,this).forceLoad();
        }

        if (findViewById(R.id.detail) != null) {
            mTwoPane = true;
        }
        setSearchView();
        BroadcastManager.getInstance().RegisterListener(this);
        setOfflineSign();
        setSubjects_tag_view();
        MobileAds.initialize(this,getResources().getString(R.string.banner_ad_app_id));
        setAdsView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.downloads:
                Intent i = new Intent(this,DownloadsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BroadcastManager.getInstance().UnRegisterListener(this);
        if(!subjectsAsyncTask.isCancelled()){
            subjectsAsyncTask.cancel(true);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        counter++;
        if(counter%2==0) {
            subjectsAsyncTask = new SubjectsAsyncTask();
            subjectsAsyncTask.execute();
        }
    }

    protected void setSearchView(){
        final SearchView searchView = (SearchView) findViewById(R.id.search);
        // Sets searchable configuration defined in searchable.xml for this SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent i = new Intent(searchView.getContext() ,SearchResultActivity.class);
                i.putExtra(SearchResultActivity.QUERY_KEY,query);
                searchView.getContext().startActivity(i);
                //searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                //filterSearchFor(query);
                return true;
            }
        });
    }
    protected void setOfflineSign(){
        Boolean isOffline;
        ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connManager.getActiveNetworkInfo();
        if(info==null){
            isOffline= true;
        }else {
            isOffline = !info.isConnected();
        }
        OnConnectionStateChanged(isOffline);
    }
    protected void setSubjects_tag_view(){
        subjectsAsyncTask.execute();

        wraper_subjects.setVisibility(View.VISIBLE);

        subjects_tag_view.setGravity(Gravity.CENTER);
        subjects_tag_view.setBackgroundColor(Color.TRANSPARENT);
        subjects_tag_view.setBorderColor(Color.TRANSPARENT);
        subjects_tag_view.setBorderRadius(1.0f);

        subjects_tag_view.setOnTagClickListener(new TagView.OnTagClickListener() {
            @Override
            public void onTagClick(int position, String text) {
                //String sub = mItem.subject.get(position);

                mSubjectName = text.replace(' ','+');
                mSubjectName= mSubjectName.toLowerCase();
                SharedPrefirencesManager.setMainRecyclerSubjectName(BookListActivity.this,mSubjectName);
                getSupportLoaderManager().restartLoader(mSubjectLoaderID_new,null,BookListActivity.this).forceLoad();
            }
            @Override
            public void onTagLongClick(int position, String text) {

            }
            @Override
            public void onTagCrossClick(int position) {

            }
        });
    }
    protected void setAdsView(){
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }
    protected void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SimpleItemRecyclerViewAdapter(null);
        recyclerView.setAdapter(adapter);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this,1,false));
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(),1,1,false));
        if(moreBtn!=null)
            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportLoaderManager().restartLoader(mSubjectLoaderID_more,null,BookListActivity.this).forceLoad();
                }
            });
    }

    protected void stateLoading(){
        moreBtn.setEnabled(false);
        mainFragment_content.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }
    protected void stateLoadingMore(){
        moreBtn.setEnabled(false);
    }
    protected void stateFinishedLoadingMore(){
        moreBtn.setEnabled(true);
    }
    protected void stateFinishedLoading(){
        moreBtn.setEnabled(true);
        mainFragment_content.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }
    protected void stateRecyclerEmpty(){

        wraper_recycler.setVisibility(View.GONE);
        empty_subject_recycler.setVisibility(View.VISIBLE);
    }
    protected void stateRecyclerNonEmpty(){
        wraper_recycler.setVisibility(View.VISIBLE);
        empty_subject_recycler.setVisibility(View.GONE);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SimpleItemRecyclerViewAdapter.DATA_SET_KEY,adapter.getDataSet());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        ArrayList<Work> ls = savedInstanceState.getParcelableArrayList(SimpleItemRecyclerViewAdapter.DATA_SET_KEY);
        if(adapter!=null)
            adapter.changeDataSet(ls);
        else
            getSupportLoaderManager().restartLoader(mSubjectLoaderID_initial,null,this);
    }

    @Override
    public Loader<SubjectLoader.SubjectResponse> onCreateLoader(int id, Bundle args) {

        if(id == mSubjectLoaderID_initial){
            stateLoading();
            mSubjectRecyclerPage = 0;

            return new SubjectLoader(this, mSubjectName,0 );
        }else if(id == mSubjectLoaderID_more){

            stateLoadingMore();
            mSubjectRecyclerPage ++;

            return new SubjectLoader(this,mSubjectName,mSubjectRecyclerPage);

        }else if(id == mSubjectLoaderID_new){
            stateLoading();
            mSubjectRecyclerPage =0;
            return new SubjectLoader(this, mSubjectName,0 );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<SubjectLoader.SubjectResponse> loader, SubjectLoader.SubjectResponse data) {
        if(loader!=null&& data!=null && data.works !=null && data.works.size()>0){
            if(loader.getId()== mSubjectLoaderID_initial  ){
                adapter.addToDataSet(data.works);
                stateRecyclerNonEmpty();
            }
            else if(loader.getId() == mSubjectLoaderID_more) {
                stateFinishedLoadingMore();
                adapter.addToDataSet(data.works);
            }
            else if(loader.getId() == mSubjectLoaderID_new){
                stateRecyclerNonEmpty();
                adapter.changeDataSet(data.works);
            }
        }else  {
            stateRecyclerEmpty();
        }
        stateFinishedLoading();
    }

    @Override
    public void onLoaderReset(Loader<SubjectLoader.SubjectResponse> loader) {

    }

    @Override
    public void OnConnectionStateChanged(Boolean isOffline) {
        if(offline_sign!=null)
            offline_sign.setVisibility(isOffline?View.VISIBLE : View.GONE);

        if(isOffline){
            if(snackbar!=null)
                snackbar.dismiss();
            snackbar = Snackbar.make(activity_main_book_list, "Check your internet connection !", Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SimpleItemRecyclerViewAdapter.ViewHolder> {
        public static final String DATA_SET_KEY = "dataSet";

        ArrayList<Work> works;



        public SimpleItemRecyclerViewAdapter(ArrayList<Work> works ) {
            this.works = works;

        }

        public void changeDataSet(ArrayList<Work> newWorks){
            works=newWorks;
            notifyDataSetChanged();

        }

        public void addToDataSet(ArrayList<Work> newWorks){
            if(newWorks!=null){
                if(works!=null){
                    works.addAll(newWorks);
                    notifyDataSetChanged();
                }
                else{
                    works = new ArrayList<>();
                    works.addAll(newWorks);
                    notifyDataSetChanged();
                }
            }
        }
        public  ArrayList<Work> getDataSet(){
            return works;
        }
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_book_list_item, parent, false);
            return new ViewHolder(view);
        }


        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {



            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
//                        arguments.putString(BookDetailFragment.ARG_ITEM_KEY, holder.mItem.id);
                        arguments.putParcelable(BookDetailFragment.ARG_ITEM_KEY,works.get(position));
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BookDetailActivity.class);
//                        intent.putExtra(BookDetailFragment.ARG_ITEM_KEY, holder.mItem.id);
                        intent.putExtra(BookDetailFragment.ARG_ITEM_KEY,works.get(position));
                        context.startActivity(intent);
                    }
                }
            });

            try{
                holder.mTitle.setText(works.get(position).title);
            }
            catch (Exception e){
                holder.mTitle.setText("N/A");
            }

            try{
                holder.mAuthor.setText(works.get(position).authors.get(0).name);
            }
            catch (Exception e){
                holder.mAuthor.setText("N/A");
            }

            Picasso.with(holder.itemView.getContext()).load(works.get(position).getCoverUrl('M'))
                    .placeholder(R.drawable.ic_book).error(R.drawable.ic_book)
                    .into(holder.mPoster);

            holder.btn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final MaterialDialog builder = new MaterialDialog.Builder(holder.itemView.getContext())
                            .title("Getting Links...").positiveText("Cancel")
                            .content("please wait").cancelable(false)
                            .progress(true, 100).show();
                    final BookDetailLoader loader = new BookDetailLoader(holder.itemView.getContext(),works.get(position));
                    final MaterialDialog.Builder d = new MaterialDialog.Builder(holder.itemView.getContext());

                    loader.forceLoad();

                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialogInterface) {
                            loader.cancelLoad();
                        }
                    });
                    loader.registerListener(0, new Loader.OnLoadCompleteListener<BookDetailLoader.BookDetailResponse>() {
                        @Override
                        public void onLoadComplete(Loader<BookDetailLoader.BookDetailResponse> loader, final BookDetailLoader.BookDetailResponse data) {

                            int resID =  (data.ebooks!=null&&data.ebooks.size()>0)?
                                    R.layout.download_extension_select_dialog :
                                    R.layout.download_extension_select_dialog_error ;

                            View dView = LayoutInflater.from(holder.itemView.getContext()).inflate(resID,null,false);
                            if(resID==R.layout.download_extension_select_dialog){
                                View.OnClickListener listener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Boolean isCase = false;
                                        switch (view.getId()){
                                            case R.id.pdf:
                                                ExternalMemoryManager.downloadFile(view.getContext(),".pdf",works.get(position),data.ebooks.get(0).formats.pdf.url);
                                                isCase= true;
                                                break;
                                            case R.id.epub:
                                                ExternalMemoryManager.downloadFile(view.getContext(),".epub",works.get(position),data.ebooks.get(0).formats.epub.url);
                                                isCase= true;
                                                break;
                                            case R.id.txt:
                                                ExternalMemoryManager.downloadFile(view.getContext(),".txt",works.get(position),data.ebooks.get(0).formats.text.url);
                                                isCase= true;
                                                break;
                                            default:
                                        }
                                        if(isCase){
                                            if(snackbar!=null)
                                                snackbar.dismiss();
                                            snackbar = Snackbar.make(activity_main_book_list, "Starting Download", Snackbar.LENGTH_LONG);
                                            snackbar.show();
                                        }
                                    }
                                };
                                dView.findViewById(R.id.pdf).setOnClickListener(listener);
                                dView.findViewById(R.id.epub).setOnClickListener(listener);
                                dView.findViewById(R.id.txt).setOnClickListener(listener);
                                d.positiveText("Cancel");
                            }else{
                                d.negativeText("OK");
                            }
                            d.customView(dView,true);
                            builder.dismiss();
                            d.show();
                        }
                    });

                }
            });


        }

        @Override
        public int getItemCount() {
            Integer i=works==null?0:works.size();;
            return i;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView mTitle;
            public final TextView mAuthor;
            public final ImageView mPoster;
            public final ImageView btn_download;
            public ViewHolder(View itemView ) {
                super(itemView);
                this.mTitle = itemView.findViewById(R.id.title_book_card);
                this.mAuthor = itemView.findViewById(R.id.author_book_card);
                this.btn_download = itemView.findViewById(R.id.download);
                this.mPoster = itemView.findViewById(R.id.poster);
            }
        }

    }

    class SubjectsAsyncTask extends AsyncTask<Void, Void, ArrayList<String>> {
    @Override
    protected ArrayList<String> doInBackground(Void... voids) {
        String response;
        String []projection = {Contract.SubjectList.Columns.SubjectList};
        Cursor c = getApplicationContext().getContentResolver().query(Contract.SubjectList.uri,
                projection,
                Contract.SubjectList.Columns.ID +" = \" "+ "1"+" \" ;"
                ,null,null);
        if( c!=null && c.moveToFirst()){
            response = c.getString(c.getColumnIndex(projection[0]));
            c.close();

            Gson g = new Gson();
            ArrayList<String> ls = g.fromJson(response,ArrayList.class);
            return ls;
        }
        ArrayList <String> ls2 = new ArrayList<>();
        ls2.add("In libirary");
        ls2.add("Readers");
        ls2.add("Math");
        ls2.add("Fiction");
        ls2.add("OverDrive");
        return ls2;
    }

    @Override
    protected void onPostExecute(ArrayList<String> strings) {
        super.onPostExecute(strings);
        if(subjects_tag_view!=null && strings!=null)
            subjects_tag_view.setTags(strings);
    }
};

}
