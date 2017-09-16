package com.example.mannas.capstone.connection;

import android.content.Context;
import android.content.Intent;

/**
 * Created by Mannas on 4/5/2017.
 */
public class ConnectionBroadcastReceiver extends android.content.BroadcastReceiver {

    public ConnectionBroadcastReceiver(){

    }

    /**
     * Note :- this method is called multi times by the system
     *      after stackoverflow it ,turns that it's the system fault :)
     *      So => Handle each unnecessary change in each {@link ConnectionListener}
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        BroadcastManager.getInstance().Dispatch( context );
    }
}
