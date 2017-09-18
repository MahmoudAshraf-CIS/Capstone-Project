package com.example.mannas.capstone.connection;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.ArrayList;

/**
 * Created by Mannas on 4/5/2017.
 */
public class BroadcastManager {
    private static BroadcastManager mBroadcastManager;
    private static ArrayList<ConnectionListener> listeners;

    // SingleTon
    private BroadcastManager() {
    }

    /**
     * Dispatch the {@link ConnectionBroadcastReceiver OnRecive() call} ,
     * singleton manager ,
     * keeps all the listeners in a list and when the Dispatch is called
     * it loops through all the listeners
     * calling the OnConnectionStateChanged();
     */
    public static BroadcastManager getInstance() {
        if (mBroadcastManager == null) {
            mBroadcastManager = new BroadcastManager();
            listeners = new ArrayList<>();
        }
        return mBroadcastManager;
    }

    /**
     * Dispatch the call to all the listeners at once
     */
    public void Dispatch(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Boolean isOffline = true;

        NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null) {
            if (info.isAvailable() && info.isConnected())
                isOffline = false;
        }

        int size = listeners.size();
        for (int i = 0; i < size; i++) {
            listeners.get(i).OnConnectionStateChanged(isOffline);
        }
    }

    /**
     * Register a listener waiting for the NetworkState Change
     *
     * @param listener
     */
    public void RegisterListener(ConnectionListener listener) {
        listeners.add(listener);
    }

    /**
     * Remove the Listener
     *
     * @param listener Listener to Unregister
     * @return true if it successfully removed, false other wise
     */
    public Boolean UnRegisterListener(ConnectionListener listener) {

        for (int i = 0; i < listeners.size(); i++) {
            if (listeners.get(i).equals(listener)) {
                listeners.remove(i);
                return true;
            }
        }
        return false;
    }

    public Integer count() {
        return listeners.size();
    }
}
