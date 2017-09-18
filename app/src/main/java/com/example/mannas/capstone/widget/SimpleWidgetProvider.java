package com.example.mannas.capstone.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.mannas.capstone.BookDetailFragment;
import com.example.mannas.capstone.BookListActivity;
import com.example.mannas.capstone.R;
import com.example.mannas.capstone.data.SharedPrefirencesManager;
import com.example.mannas.capstone.data.Util.Work;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Mannas on 9/14/2017.
 */

public class SimpleWidgetProvider extends AppWidgetProvider {
    public static String DATA_KEY = "data";


    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

            ArrayList<Work> ls = SharedPrefirencesManager.getLastLoadedSubjectWorks(context);
            if (ls != null && ls.size() > 0) {
                Integer randI = new Random().nextInt(ls.size() - 1);
                Work w = ls.get(randI);

                Intent itemIntent = new Intent(context,BookListActivity.class);
                itemIntent.putExtra(BookDetailFragment.ARG_ITEM_KEY, w);
                PendingIntent myPI = PendingIntent.getActivity(context, 0, itemIntent , 0);
                remoteViews.setOnClickPendingIntent(R.id.widget_book,myPI);

                try {
                    Picasso.with(context).load(w.getCoverUrl('M'))
                            .into(remoteViews, R.id.poster, appWidgetIds);
                } catch (Exception e) {
                    remoteViews.setImageViewResource(R.id.poster, R.drawable.ic_book);
                    e.printStackTrace();
                }
                remoteViews.setTextViewText(R.id.title_book_card, w.title);
                remoteViews.setTextViewText(R.id.author_book_card, w.authors.get(0).name);

            }
            Intent intent = new Intent(context, SimpleWidgetProvider.class);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.load_another, pendingIntent);
            appWidgetManager.updateAppWidget(appWidgetIds, remoteViews);
        }
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            int[] appWidgetIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            if (appWidgetIds != null && appWidgetIds.length > 0) {
                this.onUpdate(context, AppWidgetManager.getInstance(context), appWidgetIds);
                Log.i(SimpleWidgetProvider.class.getName(), context.getResources().getString(R.string.widget_refreshing_msg));
            }
        }
    }
}