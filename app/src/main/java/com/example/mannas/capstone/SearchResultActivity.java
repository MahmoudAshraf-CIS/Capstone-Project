package com.example.mannas.capstone;

import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.example.mannas.capstone.connection.ConnectionListener;
import com.example.mannas.capstone.data.Loaders.BookDetailLoader;
import com.example.mannas.capstone.data.Loaders.SearchLoader;
import com.example.mannas.capstone.data.Util.Doc;
import com.example.mannas.capstone.data.Util.Work;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import co.lujun.androidtagview.TagContainerLayout;

/**
 * Created by Mannas on 8/30/2017.
 */

public class SearchResultActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<SearchLoader.SearchResult>,
        ConnectionListener {
    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    public static final String QUERY_KEY = "query";
    protected final Integer SearchReasultLoader_ID_initial = 1;
    protected final Integer SearchReasultLoader_ID_newQuery = 2;
    protected final Integer SearchReasultLoader_ID_more = 3;
    protected boolean mTwoPane;
    protected SearchResultActivity.SimpleItemRecyclerViewAdapter adapter;

    @BindView(R.id.activity_main_book_list)
    CoordinatorLayout SearchResultActivity;

    @BindView(R.id.content_master)
    View mainFragment_content;
    @BindView(R.id.wraper_subjects)
    View wraper_subjects;
    @BindView(R.id.nothing_to_show)
    View nothingToShow;
    @BindView(R.id.wraper_recycler)
    View wraper_recycler;
    @BindView(R.id.loading_indicator)
    View loadingIndicator;

    @BindView(R.id.offline_sign)
    View offline_sign;
    @BindView(R.id.subjects_tag_view)
    TagContainerLayout subjects_tag_view;
    @BindView(R.id.load_more)
    Button moreBtn;

    Integer page;
    String subject;
    Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_book_list);
        ButterKnife.bind(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getResources().getString(R.string.search_result));


        View recyclerView = findViewById(R.id.main_recycler);
        assert recyclerView != null;
        setupRecyclerView((RecyclerView) recyclerView);
        wraper_subjects.setVisibility(View.GONE);

        if (getIntent() != null && getIntent().getExtras() != null) {
            subject = getIntent().getExtras().getString(QUERY_KEY);
            if (subject != null) {
                subject = subject.replace(' ', '+');
                subject = subject.toLowerCase();
                page = 0;
                getSupportLoaderManager().restartLoader(SearchReasultLoader_ID_initial, null, this).forceLoad();
            } else {
                if (snackbar != null)
                    snackbar.dismiss();
                snackbar = Snackbar.make(SearchResultActivity,getResources().getString(R.string.not_valid_keyword)  , Snackbar.LENGTH_LONG);
                snackbar.show();
            }
        }

        if (findViewById(R.id.detail) != null) {
            mTwoPane = true;
        }
        setSearchView();
        setOfflineSign();
        setAdsView();
    }

    protected void setOfflineSign() {
        if (offline_sign != null) {
            Boolean isOffline;
            ConnectivityManager connManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo info = connManager.getActiveNetworkInfo();
            if (info == null) {
                isOffline = true;
            } else {
                isOffline = !info.isConnected();
            }
            OnConnectionStateChanged(isOffline);
        }
    }

    protected void setSearchView() {
        final SearchView searchView = (SearchView) findViewById(R.id.search);
        searchView.setQuery(subject, false);

        // Sets searchable configuration defined in searchable.xml for this SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query != null) {
                    subject = query.replace(' ', '+');
                    subject = subject.toLowerCase();
                    page = 0;
                    getSupportLoaderManager().restartLoader(SearchReasultLoader_ID_newQuery, null, SearchResultActivity.this).forceLoad();

                    ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                            .hideSoftInputFromWindow(searchView.getWindowToken(), 0);

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                return true;
            }
        });
    }

    protected void setAdsView() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        // Create an ad request. Check logcat output for the hashed device ID to
        // get test ads on a physical device. e.g.
        // "Use AdRequest.Builder.addTestDevice("ABCDEF012345") to get test ads on this device."
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(BookListActivity.SimpleItemRecyclerViewAdapter.DATA_SET_KEY, adapter.getDataSet());
        outState.putString("subject", subject);
        outState.putInt("page", page);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        subject = savedInstanceState.getString("subject");
        page = savedInstanceState.getInt("page");

        ArrayList<Doc> ls = savedInstanceState.getParcelableArrayList(BookListActivity.SimpleItemRecyclerViewAdapter.DATA_SET_KEY);
        if (adapter != null)
            adapter.changeDataSet(ls);
        else
            getSupportLoaderManager().restartLoader(SearchReasultLoader_ID_initial, null, this);
    }

    protected void setupRecyclerView(@NonNull RecyclerView recyclerView) {
        adapter = new SearchResultActivity.SimpleItemRecyclerViewAdapter(null);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1, 1, false));
        if (moreBtn != null)
            moreBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getSupportLoaderManager().restartLoader(SearchReasultLoader_ID_more, null, SearchResultActivity.this).forceLoad();
                }
            });
    }
//        mainFragment_content;
//          wraper_subjects;
//         nothingToShow;
//         wraper_recycler;
//        loadingIndicator;

    protected void stateLoading() {
        moreBtn.setEnabled(false);
        mainFragment_content.setVisibility(View.GONE);
        nothingToShow.setVisibility(View.GONE);

        loadingIndicator.setVisibility(View.VISIBLE);
    }

    protected void stateFinishedLoading() {
        moreBtn.setEnabled(true);
        mainFragment_content.setVisibility(View.VISIBLE);

        loadingIndicator.setVisibility(View.GONE);
    }

    protected void stateLoadingMore() {
        moreBtn.setEnabled(false);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    protected void stateFinishedLoadingMore() {
        moreBtn.setEnabled(true);
        loadingIndicator.setVisibility(View.GONE);
    }

    protected void stateRecyclerEmpty() {
        wraper_recycler.setVisibility(View.GONE);
        nothingToShow.setVisibility(View.VISIBLE);
    }

    protected void stateRecyclerNonEmpty() {
        wraper_recycler.setVisibility(View.VISIBLE);
        nothingToShow.setVisibility(View.GONE);
    }

    @Override
    public Loader<SearchLoader.SearchResult> onCreateLoader(int id, Bundle args) {

        if (id == SearchReasultLoader_ID_initial) {
            stateLoading();
            page = 0;
            return new SearchLoader(this, subject, page);
        } else if (id == SearchReasultLoader_ID_more) {
            stateLoadingMore();
            page++;
            return new SearchLoader(this, subject, page);
        } else if (id == SearchReasultLoader_ID_newQuery) {
            stateLoading();
            page = 0;
            return new SearchLoader(this, subject, page);
        }

        return null;
    }

    @Override
    public void onLoadFinished(Loader<SearchLoader.SearchResult> loader, SearchLoader.SearchResult data) {
        if (loader != null && data != null && data.docs != null && data.docs.size() > 0) {
            if (loader.getId() == SearchReasultLoader_ID_initial) {
                adapter.addToDataSet(data.docs);
                stateRecyclerNonEmpty();
            } else if (loader.getId() == SearchReasultLoader_ID_more) {
                stateFinishedLoadingMore();
                adapter.addToDataSet(data.docs);

            } else if (loader.getId() == SearchReasultLoader_ID_newQuery) {
                stateRecyclerNonEmpty();
                adapter.changeDataSet(data.docs);
            }
        } else {
            stateRecyclerEmpty();
        }
        stateFinishedLoading();
    }

    @Override
    public void onLoaderReset(Loader<SearchLoader.SearchResult> loader) {

    }


    @Override
    public void OnConnectionStateChanged(Boolean isOffline) {
        if (offline_sign != null)
            offline_sign.setVisibility(isOffline ? View.VISIBLE : View.GONE);

        if (isOffline) {
            if (snackbar != null)
                snackbar.dismiss();
            snackbar = Snackbar.make(SearchResultActivity, getResources().getString(R.string.check_your_internet_connection) , Snackbar.LENGTH_LONG);
            snackbar.show();
        }
    }

    public class SimpleItemRecyclerViewAdapter
            extends RecyclerView.Adapter<SearchResultActivity.SimpleItemRecyclerViewAdapter.ViewHolder> {
        public static final String DATA_SET_KEY = "dataSet";

        ArrayList<Doc> docs;

        public SimpleItemRecyclerViewAdapter(ArrayList<Doc> docs) {
            this.docs = docs;

        }

        public void changeDataSet(ArrayList<Doc> docs) {
            this.docs = docs;
            notifyDataSetChanged();
        }

        public void addToDataSet(ArrayList<Doc> docs) {
            if (this.docs != null)
                this.docs.addAll(docs);
            else {
                this.docs = new ArrayList<>();
                this.docs.addAll(docs);
            }
            notifyDataSetChanged();
        }

        public ArrayList<Doc> getDataSet() {
            return docs;
        }

        @Override
        public SearchResultActivity.SimpleItemRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_book_list_item, parent, false);
            return new SearchResultActivity.SimpleItemRecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SearchResultActivity.SimpleItemRecyclerViewAdapter.ViewHolder holder,  int position) {

            final String OLID = docs.get(position).edition_key.get(0);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTwoPane) {
                        Bundle arguments = new Bundle();
                        arguments.putString(BookDetailFragment.OLID_KEY, OLID);
                        arguments.putString(BookDetailFragment.TITLE_KEY, docs.get(holder.getAdapterPosition()).title);
                        BookDetailFragment fragment = new BookDetailFragment();
                        fragment.setArguments(arguments);
                        getSupportFragmentManager().beginTransaction().replace(R.id.detail, fragment).commit();
                    } else {
                        Context context = v.getContext();
                        Intent intent = new Intent(context, BookDetailActivity.class);
                        intent.putExtra(BookDetailFragment.OLID_KEY, OLID);
                        intent.putExtra(BookDetailFragment.TITLE_KEY, docs.get(holder.getAdapterPosition()).title);
                        context.startActivity(intent);
                    }
                }
            });

            holder.mTitle.setText(docs.get(position).title == null ?getResources().getString(R.string.na) : docs.get(position).title);
            if (docs.get(position).author_name != null && docs.get(position).author_name.size() > 0)
                holder.mAuthor.setText(docs.get(position).author_name.get(0));
            else
                holder.mAuthor.setText(getResources().getString(R.string.na));

            String url;
            if (docs.get(position).cover_i != null)
                url = "https://covers.openlibrary.org/w/id/" + docs.get(position).cover_i + "-S.jpg";
            else
                url = "https://covers.openlibrary.org/b/olid/" + OLID + "-S.jpg";


            //// TODO: 9/7/2017 follow the redirected link of the image
            Picasso.with(holder.itemView.getContext()).load(url)
                    .placeholder(R.drawable.ic_book).error(R.drawable.ic_book).into(holder.mPoster);
            if(!docs.get(holder.getAdapterPosition()).has_fulltext)
                holder.btn_download.setVisibility(View.GONE);
            else
               holder.btn_download.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final MaterialDialog builder = new MaterialDialog.Builder(holder.itemView.getContext())
                            .title(getResources().getString(R.string.getting_links)).positiveText(getResources().getString(R.string.cancel))
                            .content(getResources().getString(R.string.please_wait)).cancelable(false)
                            .progress(true, 100).show();
                    final BookDetailLoader loader = new BookDetailLoader(holder.itemView.getContext(), docs.get(holder.getAdapterPosition()).edition_key.get(0));
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

                            Boolean hasFormats =false , hasPdf=false,hasEpub=false,hasTxt=false;
                            int resID;
                            hasFormats = (data.ebooks != null && data.ebooks.size() > 0 && data.ebooks.get(0)!=null && data.ebooks.get(0).formats!=null);
                            if(hasFormats){
                                hasPdf  = data.ebooks.get(0).formats.pdf !=null &&data.ebooks.get(0).formats.pdf.url !=null;
                                hasEpub = data.ebooks.get(0).formats.epub !=null &&data.ebooks.get(0).formats.epub.url !=null;
                                hasTxt  = data.ebooks.get(0).formats.text !=null &&data.ebooks.get(0).formats.text.url !=null;
                                if(hasPdf || hasEpub || hasTxt)
                                    resID = R.layout.download_extension_select_dialog;
                                else
                                    resID = R.layout.download_extension_select_dialog_error;
                            }else{
                                resID = R.layout.download_extension_select_dialog_error;
                            }

                            View dView = LayoutInflater.from(holder.itemView.getContext()).inflate(resID, null, false);
                            if (resID == R.layout.download_extension_select_dialog) {
                                View pdf,epub,txt;
                                pdf = dView.findViewById(R.id.pdf);
                                epub= dView.findViewById(R.id.epub);
                                txt=  dView.findViewById(R.id.txt);

                                if( hasPdf )
                                    pdf.setVisibility(View.VISIBLE);
                                if( hasEpub )
                                    epub.setVisibility(View.VISIBLE);
                                if( hasTxt )
                                    txt.setVisibility(View.VISIBLE);
                                View.OnClickListener listener = new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Boolean isCase = false, success = false;
                                        switch (view.getId()) {
                                            case R.id.pdf:
                                                success = ExternalMemoryManager.downloadFile(view.getContext(), ".pdf", OLID,
                                                        docs.get(holder.getAdapterPosition()).title
                                                        , data.ebooks.get(0).formats.pdf.url);
                                                isCase = true;
                                                break;
                                            case R.id.epub:
                                                success = ExternalMemoryManager.downloadFile(view.getContext(), ".epub",
                                                        OLID,
                                                        docs.get(holder.getAdapterPosition()).title, data.ebooks.get(0).formats.epub.url);
                                                isCase = true;
                                                break;
                                            case R.id.txt:
                                                success = ExternalMemoryManager.downloadFile(view.getContext(), ".txt",
                                                        OLID,
                                                        docs.get(holder.getAdapterPosition()).title, data.ebooks.get(0).formats.text.url);
                                                isCase = true;
                                                break;
                                            default:
                                        }
                                        if (isCase) {
                                            if (snackbar != null)
                                                snackbar.dismiss();
                                            if (success) {
                                                snackbar = Snackbar.make(SearchResultActivity, getResources().getString(R.string.starting_download), Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                            } else {
                                                snackbar = Snackbar.make(SearchResultActivity, getResources().getString(R.string.failed_to_start_download), Snackbar.LENGTH_LONG);
                                                snackbar.show();
                                            }
                                        }
                                    }
                                };

                                pdf.setOnClickListener(listener);
                                epub.setOnClickListener(listener);
                                txt.setOnClickListener(listener);

                                d.positiveText(getResources().getString(R.string.cancel));
                            } else {
                                d.negativeText(getResources().getString(R.string.ok));
                            }
                            d.customView(dView, true);
                            builder.dismiss();
                            d.show();
                        }
                    });


                }
            });

        }

        @Override
        public int getItemCount() {
            Integer i = docs == null ? 0 : docs.size();
            return i;
        }


        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView mTitle;
            public final TextView mAuthor;
            public final ImageView mPoster;
            public final ImageView btn_download;

            public ViewHolder(View itemView) {
                super(itemView);
                this.mTitle = itemView.findViewById(R.id.title_book_card);
                this.mAuthor = itemView.findViewById(R.id.author_book_card);
                this.btn_download = itemView.findViewById(R.id.download);
                this.mPoster = itemView.findViewById(R.id.poster);
            }
        }

    }

}
