package com.example.mannas.capstone.connection;

/**
 * Created by Mannas on 4/5/2017.
 */

public interface ConnectionListener {

    /**
     * Note :- it can be called multi times in a row
     * it's system error
     * SO handle each unnecessary calls
     */
    void OnConnectionStateChanged(Boolean isOffline);

}
