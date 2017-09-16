package com.example.mannas.capstone.data.Util;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Mannas on 8/23/2017.
 */

public class Publisher implements Parcelable {
    public Integer count;
    public String name;
    public String key;
    public String url;


    protected Publisher(Parcel in) {
        count = in.readByte() == 0x00 ? null : in.readInt();
        name = in.readString();
        key = in.readString();
        url = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (count == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(count);
        }
        dest.writeString(name);
        dest.writeString(key);
        dest.writeString(url);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Publisher> CREATOR = new Parcelable.Creator<Publisher>() {
        @Override
        public Publisher createFromParcel(Parcel in) {
            return new Publisher(in);
        }

        @Override
        public Publisher[] newArray(int size) {
            return new Publisher[size];
        }
    };
}