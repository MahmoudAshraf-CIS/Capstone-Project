package com.example.mannas.capstone;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.example.mannas.capstone.data.Loaders.DownloadsLoader;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Mannas on 9/14/2017.
 */

public class DownloadsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<ArrayList<String>>

{
    final Integer DOWNLOADS_LOADER_ID = 7;
    DownloadsAdapter adapter;
    View no_downloads, loading_indicator, activity_downloads;
    RecyclerView recyclerView;
    Snackbar snackbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_downloads);

        adapter = new DownloadsAdapter(null);

        recyclerView = (RecyclerView) findViewById(R.id.downloads_recycler);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new GridLayoutManager(getApplicationContext(), 1, 1, false));

        no_downloads = findViewById(R.id.no_downloads);
        loading_indicator = findViewById(R.id.loading_indicator);
        activity_downloads = findViewById(R.id.activity_downloads);

        getSupportLoaderManager().restartLoader(DOWNLOADS_LOADER_ID, null, this).forceLoad();


    }


    @Override
    public Loader<ArrayList<String>> onCreateLoader(int id, Bundle args) {
        if (id == DOWNLOADS_LOADER_ID) {
            loading_indicator.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
            no_downloads.setVisibility(View.GONE);
            return new DownloadsLoader(this);
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<ArrayList<String>> loader, ArrayList<String> data) {
        if (loader != null && loader.getId() == DOWNLOADS_LOADER_ID && data != null && data.size() > 0) {
            loading_indicator.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            no_downloads.setVisibility(View.GONE);
            adapter.changeDataSet(data);
        } else {
            loading_indicator.setVisibility(View.GONE);
            recyclerView.setVisibility(View.GONE);
            no_downloads.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<String>> loader) {

    }


    public class DownloadsAdapter
            extends RecyclerView.Adapter<DownloadsAdapter.ViewHolder> {
        public static final String DATA_SET_KEY = "dataSet";

        ArrayList<String> downlads;

        public DownloadsAdapter(ArrayList<String> downlads) {
            this.downlads = downlads;

        }

        public void changeDataSet(ArrayList<String> downlads) {
            this.downlads = downlads;
            notifyDataSetChanged();

        }

        public void addToDataSet(ArrayList<String> downlads) {
            if (downlads != null) {
                if (this.downlads != null) {
                    this.downlads.addAll(downlads);
                    notifyDataSetChanged();
                } else {
                    this.downlads = new ArrayList<>();
                    this.downlads.addAll(downlads);
                    notifyDataSetChanged();
                }
            }
        }

        public ArrayList<String> getDataSet() {
            return downlads;
        }

        @Override
        public DownloadsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.downloads_item, parent, false);
            return new DownloadsAdapter.ViewHolder(view);
        }

        int getDrawableRes(String name) {
            switch (name.substring(name.lastIndexOf('.'))) {
                case ".pdf":
                    return R.drawable.pdf;
                case ".txt":
                    return R.drawable.txt;
                case ".epub":
                    return R.drawable.epub;
                default:
                    return -1;
            }
        }

        private String getExtension(String name) {
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

        @Override
        public void onBindViewHolder(final DownloadsAdapter.ViewHolder holder, final int position) {
            holder.mTitle.setText(
                    downlads.get(position).substring(downlads.get(position).indexOf('-') + 1)
            );

            holder.mTitle.setCompoundDrawablesWithIntrinsicBounds(getDrawableRes(downlads.get(position))
                    , 0, 0, 0);
            holder.mTitle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    File file = new File(ExternalMemoryManager.getOfflinePath(downlads.get(holder.getAdapterPosition())));
                    MimeTypeMap myMime = MimeTypeMap.getSingleton();
                    String mimeType = myMime.getMimeTypeFromExtension(getExtension(downlads.get(holder.getAdapterPosition())));

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.fromFile(file), mimeType);
//                    intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);


                    try {
                        startActivity(intent);
                    } catch (ActivityNotFoundException e) {
                        if (snackbar != null)
                            snackbar.dismiss();
                        snackbar = Snackbar.make(activity_downloads, getResources().getString(R.string.no_handler_for_this_type_of_file), Snackbar.LENGTH_LONG);
                        snackbar.show();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            Integer i = downlads == null ? 0 : downlads.size();
            ;
            return i;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            public final TextView mTitle;

            public ViewHolder(View itemView) {
                super(itemView);
                this.mTitle = itemView.findViewById(R.id.title);
            }
        }

    }


}
