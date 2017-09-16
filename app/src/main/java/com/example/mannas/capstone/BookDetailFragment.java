package com.example.mannas.capstone;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.mannas.capstone.data.ContentProvider.Contract;
import com.example.mannas.capstone.data.Loaders.BookDetailLoader;
import com.example.mannas.capstone.data.Util.Author;
import com.example.mannas.capstone.data.Util.Work;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import co.lujun.androidtagview.TagContainerLayout;
import co.lujun.androidtagview.TagView;

/**
 * A fragment representing a single Book detail screen.
 * This fragment is either contained in a {@link BookListActivity}
 * in two-pane mode (on tablets) or a {@link BookDetailActivity}
 * on handsets.
 */
public class BookDetailFragment extends Fragment
implements LoaderManager.LoaderCallbacks<BookDetailLoader.BookDetailResponse>
{
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_KEY = "item_id";
    public static final String OLID_KEY = "OLID_KEY";
    public static final String TITLE_KEY = "TITLE_KEY";

    private final Integer BOOK_DETAIL_LOADER_ID_useWork = 2;
    private final Integer BOOK_DETAIL_LOADER_ID_useOLID = 3;
    /**
     * The dummy content this fragment is presenting.
     */
    private Work mItem;
    private BookDetailLoader.BookDetailResponse detailResponse;

    @BindView(R.id.fragment_book_detail) CoordinatorLayout fragment;
    @BindView(R.id.cover_img) ImageView cover_img;
    @BindView(R.id.by_statment) TextView by_statment; // D
    @BindView(R.id.publish_date) TextView publish_date;
    @BindView(R.id.available_offline) View available_offline;
    @BindView(R.id.offline_pdf) View offline_pdf;
    @BindView(R.id.offline_epub) View offline_epub;
    @BindView(R.id.offline_txt) View offline_txt;
    @BindView(R.id.number_of_pages) TextView number_of_pages;
    @BindView(R.id.notes) TextView notes;

    @BindView(R.id.subjects_tag_view) TagContainerLayout subjects_tag_view;
    @BindView(R.id.subjects_recycler_view) RecyclerView subjectsRecycler;

//    @BindView(R.id.authors_tag_view) TagContainerLayout authors_tag_view;
    @BindView(R.id.authors_recycler_view) RecyclerView authorsRecycler;

    @BindView(R.id.has_downloads_view) View has_downloads_view;
    @BindView(R.id.nothas_downloads_view) View nothas_downloads_view;
    @BindView(R.id.pdf) View pdf;
    @BindView(R.id.epub) View epub;
    @BindView(R.id.txt) View txt;
    @BindView(R.id.view_online) View view_online;
    @BindView(R.id.fab) FloatingActionButton fab;

    private Unbinder unbinder;
    String offlinePath_pdf,offlinePath_epub,offlinePath_txt , mOLID , mTitle;
    int MaxSubjectsCount =10;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public BookDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments()!=null && getArguments().containsKey(ARG_ITEM_KEY)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mItem =  getArguments().getParcelable(ARG_ITEM_KEY);
            offlinePath_pdf = ExternalMemoryManager.getOfflinePath( mItem,".pdf");
            offlinePath_epub = ExternalMemoryManager.getOfflinePath( mItem,".epub");
            offlinePath_txt = ExternalMemoryManager.getOfflinePath( mItem,".txt");
            getLoaderManager().restartLoader(BOOK_DETAIL_LOADER_ID_useWork,null,this).forceLoad();


        }else if(getArguments()!=null && getArguments().containsKey(OLID_KEY)){
            mOLID = getArguments().getString(OLID_KEY);
            mTitle = getArguments().getString(TITLE_KEY);

            offlinePath_pdf = ExternalMemoryManager.getOfflinePath(mOLID,mTitle,".pdf");
            offlinePath_epub = ExternalMemoryManager.getOfflinePath( mOLID,mTitle,".epub");
            offlinePath_txt = ExternalMemoryManager.getOfflinePath( mOLID,mTitle,".txt");
            mItem = null;
            getLoaderManager().restartLoader(BOOK_DETAIL_LOADER_ID_useOLID,null,this).forceLoad();

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_book_detail, container, false);
        unbinder = ButterKnife.bind(this, rootView);

        CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) rootView.findViewById(R.id.toolbar_layout);
        if (appBarLayout != null) {
            appBarLayout.setTitle(mTitle);
        }
        // Show the dummy content as text in a TextView.
        if (mItem != null) {
            deploy_mItem();
        }
        setOfflinePanal();
        return rootView;
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }


    void deployDetailResponse(){
        by_statment.setText(detailResponse.by_statement==null?"By :- N/A" : detailResponse.by_statement );
        String nOfPagesStr =
                getResources().getString(R.string.is)+" "
                + (detailResponse.number_of_pages==null?"N/A":detailResponse.number_of_pages.toString())+" "
                + getResources().getString(R.string.pages);

        number_of_pages.setText(nOfPagesStr);
        notes.setText(detailResponse.notes==null? "Notes :- N/A":detailResponse.notes);
        if(detailResponse.authors!=null){
            authorsRecycler.setAdapter(new AuthorRecyclerAdapter(detailResponse.authors));
            authorsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),1,false));
        }

//        if(detailResponse.subjects!=null){
//            ArrayList<String> authorsNames = new ArrayList<>();
//            for(int i=0;i<detailResponse.subjects.size();i++)
//                authorsNames.add(detailResponse.subjects.get(i).name);
//            authors_tag_view.setTags(authorsNames);
//            authors_tag_view.setGravity(Gravity.CENTER);
//            authors_tag_view.setBackgroundColor(Color.TRANSPARENT);
//            authors_tag_view.setBorderColor(Color.TRANSPARENT);
//
//            authors_tag_view.setBorderRadius(1.0f);
//            authors_tag_view.setOnTagClickListener(new TagView.OnTagClickListener() {
//                @Override
//                public void onTagClick(int position, String text) {
//                    Intent i = new Intent(getContext(),WebViewActivity.class);
//                    i.putExtra("url",detailResponse.subjects.get(position).url);
//                    startActivity(i);
//                }
//                @Override
//                public void onTagLongClick(int position, String text) {
//
//                }
//                @Override
//                public void onTagCrossClick(int position) {
//
//                }
//            });
//        }

        if(detailResponse.ebooks!=null && detailResponse.ebooks.size()>0 && detailResponse.ebooks.get(0).formats!=null){
            has_downloads_view.setVisibility(View.VISIBLE);
            View.OnClickListener listener = getDownloadListener();
            if(detailResponse.ebooks.get(0).formats.pdf!=null &&detailResponse.ebooks.get(0).formats.pdf.url!=null)
                pdf.setOnClickListener(listener);
            else
                pdf.setVisibility(View.GONE);
            if(detailResponse.ebooks.get(0).formats.epub!=null &&detailResponse.ebooks.get(0).formats.epub.url!=null)
                epub.setOnClickListener(listener);
            else
                epub.setVisibility(View.GONE);
            if(detailResponse.ebooks.get(0).formats.text!=null &&detailResponse.ebooks.get(0).formats.text.url!=null)
                txt.setOnClickListener(listener);
            else
                txt.setVisibility(View.GONE);
        }else{
            nothas_downloads_view.setVisibility(View.VISIBLE);
        }

        view_online.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(android.net.Uri.parse("https://openlibrary.org"+(detailResponse.key!=null ? detailResponse.key : "")));
                startActivity(intent);
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT,
                        ("https://openlibrary.org"+(detailResponse.key!=null ? detailResponse.key : "")));
                view.getContext().startActivity(Intent.createChooser(sharingIntent, "Share Using ?" ) );
            }
        });


        if(mItem==null){
            if(detailResponse.cover!=null && detailResponse.cover.medium!=null)
                Picasso.with(getContext()).load(detailResponse.cover.medium)
                        .placeholder(R.drawable.ic_book).error(R.drawable.ic_book).into(cover_img);
            else
                cover_img.setImageResource(R.drawable.ic_book);

            String s = getResources().getString(R.string.published) + ((detailResponse.publish_date!=null) ?detailResponse.publish_date:"N/A");
            publish_date.setText(s);

            if(detailResponse.subjects!=null){
                ArrayList<String> subjects = new ArrayList<>();
                int k = Math.min(MaxSubjectsCount,(detailResponse.subjects==null?0:detailResponse.subjects.size()));
                for(int i=0; i < k ;i++){
                    subjects.add(detailResponse.subjects.get(i).name);
                }
                setsubjects_tag_view(subjects);
            }
        }
    }

    void deploy_mItem(){
       if(mItem!=null){
           Picasso.with(getContext()).load(mItem.getCoverUrl('M'))
                   .placeholder(R.drawable.ic_book).error(R.drawable.ic_book).into(cover_img);
           String s = getResources().getString(R.string.published)+ mItem.first_publish_year;
           publish_date.setText(s);

//        if(mItem.subject!=null){
//            subjectsRecycler.setAdapter(new RelatedSubsRecyclerAdapter(mItem.subject));
////            subjectsRecycler.setLayoutManager(new LinearLayoutManager(getContext(),1,false));
//            subjectsRecycler.setLayoutManager(new StaggeredGridLayoutManager(2,1));
//        }

           if(mItem.subject!=null){
               setsubjects_tag_view(mItem.subject);
           }
       }
    }

    void setOfflinePanal(){
        available_offline.setVisibility((offlinePath_pdf==null &&offlinePath_epub==null&&offlinePath_txt==null)?
                View.GONE :View.VISIBLE );

        View.OnClickListener listener=new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.offline_pdf:
                        ExternalMemoryManager.openFile(offlinePath_pdf,getContext(),fragment);
                        break;
                    case R.id.offline_epub:
                        ExternalMemoryManager.openFile(offlinePath_epub,getContext(),fragment);
                        break;
                    case R.id.offline_txt:
                        ExternalMemoryManager.openFile(offlinePath_txt,getContext(),fragment);
                        break;
                    default:
                        break;
                }
            }
        };
        if(offlinePath_pdf==null)
            offline_pdf.setVisibility(View.GONE);
        else
            offline_pdf.setOnClickListener(listener);

        if(offlinePath_epub==null)
            offline_epub.setVisibility(View.GONE);
        else
            offline_epub.setOnClickListener(listener);

        if(offlinePath_txt==null)
            offline_txt.setVisibility(View.GONE);
        else
            offline_txt.setOnClickListener(listener);


    }

    void setsubjects_tag_view(ArrayList<String> subjects){
        if(subjects!=null){
            if(subjects.size()>MaxSubjectsCount){
                ArrayList<String> ls = new ArrayList<>();
                for(int i=0 ,k=Math.min(MaxSubjectsCount, subjects.size()) ; i<k ;i++){
                    ls.add(subjects.get(i));
                }
                subjects = ls;
                ls=null;
            }
            subjects_tag_view.setTags(subjects);
            subjects_tag_view.setGravity(Gravity.CENTER);
            subjects_tag_view.setBackgroundColor(Color.TRANSPARENT);
            subjects_tag_view.setBorderColor(Color.TRANSPARENT);
            subjects_tag_view.setBorderRadius(1.0f);
            subjects_tag_view.setVisibility(View.VISIBLE);

            subjects_tag_view.setOnTagClickListener(new TagView.OnTagClickListener() {
                @Override
                public void onTagClick(int position, String text) {
                    //String sub = mItem.subject.get(position);
                    Intent i = new Intent(getContext(),BookListActivity.class);
                    i.putExtra(BookListActivity.SUBJECT_KEY,text);
                    getContext().startActivity(i);
                }
                @Override
                public void onTagLongClick(int position, String text) {

                }
                @Override
                public void onTagCrossClick(int position) {

                }
            });

            new AsyncTask< ArrayList<String>,Void,Void>(){
                @Override
                protected Void doInBackground(ArrayList<String>... arrayLists) {
                    ContentValues v = new ContentValues(1);
                    Gson g = new Gson();
                    String s = g.toJson(arrayLists[0]).toString();
                    v.put(Contract.SubjectList.Columns.SubjectList,s);
                    v.put(Contract.SubjectList.Columns.ID,1);

                    ArrayList<String> ls = g.fromJson(s,ArrayList.class);

                    getContext().getContentResolver().insert(Contract.SubjectList.uri, v);
                    return null;

                }

            }.execute(subjects);
        }
    }

    private View.OnClickListener getDownloadListener(){

        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean isCase = false,success=false;
                switch (view.getId()){
                    case R.id.pdf:
                        if(mItem!=null)
                        success = ExternalMemoryManager.downloadFile(getContext(),".pdf",mItem,detailResponse.ebooks.get(0).formats.pdf.url);
                        else
                            success= ExternalMemoryManager.downloadFile(view.getContext(),".pdf",mOLID, mTitle,detailResponse.ebooks.get(0).formats.pdf.url);
                        isCase= true;
                        break;
                    case R.id.epub:
                        if(mItem!=null)
                        success = ExternalMemoryManager.downloadFile(getContext(),".epub",mItem,detailResponse.ebooks.get(0).formats.epub.url);
                        else
                            success= ExternalMemoryManager.downloadFile(view.getContext(),".epub",mOLID, mTitle,detailResponse.ebooks.get(0).formats.epub.url);
                        isCase= true;
                        break;
                    case R.id.txt:
                        if(mItem!=null)
                        success = ExternalMemoryManager.downloadFile(getContext(),".txt",mItem,detailResponse.ebooks.get(0).formats.text.url);
                        else
                            success= ExternalMemoryManager.downloadFile(view.getContext(),".txt",mOLID, mTitle,detailResponse.ebooks.get(0).formats.text.url);
                        isCase= true;
                        break;
                    default:
                }
                if(isCase){
                    Snackbar snackbar;
                    if(success){
                        snackbar = Snackbar.make(fragment, "Starting Download", Snackbar.LENGTH_LONG);
                    }
                    else {
                        snackbar = Snackbar.make(fragment, "Failed to Start Download !/nCan't create the download folder.", Snackbar.LENGTH_LONG);
                    }
                    snackbar.show();
                }
            }
        };

    }

    @Override
    public Loader<BookDetailLoader.BookDetailResponse> onCreateLoader(int id, Bundle args) {
        if(id== BOOK_DETAIL_LOADER_ID_useWork)
            return new BookDetailLoader(getContext(),mItem);
        else if(id==BOOK_DETAIL_LOADER_ID_useOLID)
            return new BookDetailLoader(getContext(),mOLID);
        return null;
    }

    @Override
    public void onLoadFinished(Loader<BookDetailLoader.BookDetailResponse> loader, final BookDetailLoader.BookDetailResponse data) {
        detailResponse = data;
        if(detailResponse!=null) {
            deployDetailResponse();
        }
    }
    @Override
    public void onLoaderReset(Loader<BookDetailLoader.BookDetailResponse> loader) {

    }

    public class AuthorRecyclerAdapter
            extends RecyclerView.Adapter<AuthorRecyclerAdapter.ViewHolder> {
        ArrayList<Author> authors;
        public AuthorRecyclerAdapter(ArrayList<Author> authors) {
            this.authors = authors;
        }

        public void changeDataSet(ArrayList<Author> authors){
            this.authors=authors;
            notifyDataSetChanged();
        }

        public void addToDataSet(ArrayList<Author> authors){
            if(this.authors!=null)
                this.authors.addAll(authors);
            else{
                this.authors= new ArrayList<>();
                this.authors.addAll(authors);
            }
            notifyDataSetChanged();
        }
        public  ArrayList<Author> getDataSet(){
            return authors;
        }

        @Override
        public AuthorRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.author_item, parent, false);
            return new AuthorRecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final AuthorRecyclerAdapter.ViewHolder holder, int position) {

            holder.mAuthor.setText(authors.get(position).name);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(),WebViewActivity.class);
                    i.putExtra("url",authors.get(holder.getAdapterPosition()).url);
                    startActivity(i);
                }
            });

        }

        @Override
        public int getItemCount() {

            Integer i= authors ==null?0: ( authors.size()>10?10: authors.size());

            return i;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView mAuthor;

            public ViewHolder(View itemView ) {
                super(itemView);
                this.mAuthor = itemView.findViewById(R.id.author_name);
            }
        }
    }

    public class RelatedSubsRecyclerAdapter
            extends RecyclerView.Adapter<RelatedSubsRecyclerAdapter.ViewHolder> {
        ArrayList<String> subjects;
        public RelatedSubsRecyclerAdapter(ArrayList<String> subjects) {

            this.subjects =  subjects;
        }

        public void changeDataSet(ArrayList<String> subjects){
            this.subjects = subjects;
            notifyDataSetChanged();
        }

        public void addToDataSet(ArrayList<String> subjects){
            if(this.subjects !=null)
                this.subjects.addAll(subjects);
            else{
                this.subjects = new ArrayList<>();
                this.subjects.addAll(subjects);
            }
            notifyDataSetChanged();
        }
        public  ArrayList<String> getDataSet(){
            return subjects;
        }

        @Override
        public RelatedSubsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.subject_item, parent, false);
            return new RelatedSubsRecyclerAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RelatedSubsRecyclerAdapter.ViewHolder holder, int position) {

            holder.subject_name.setText(subjects.get(holder.getAdapterPosition()));
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });

        }

        @Override
        public int getItemCount() {
            Integer i= subjects ==null?0:  subjects.size();

//            Integer i= subjects ==null?0: ( subjects.size()>10?10: subjects.size());
            return i;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView subject_name;

            public ViewHolder(View itemView ) {
                super(itemView);
                this.subject_name = itemView.findViewById(R.id.subject_name);
            }
        }
    }

}
