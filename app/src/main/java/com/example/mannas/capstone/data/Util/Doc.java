package com.example.mannas.capstone.data.Util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Mannas on 8/30/2017.
 */

public class Doc implements Parcelable {
    public String cover_i;
    public Boolean has_fulltext;
    public Integer edition_count;
    public String title;
    public ArrayList<String> edition_key;
    public ArrayList<String> author_name;
    public String first_publish_year;
    public String key;
    public ArrayList<String> ia;
    public ArrayList<String> author_key;
    public Boolean public_scan_b;





    protected Doc(Parcel in) {
        cover_i = in.readString();
        byte has_fulltextVal = in.readByte();
        has_fulltext = has_fulltextVal == 0x02 ? null : has_fulltextVal != 0x00;
        edition_count = in.readByte() == 0x00 ? null : in.readInt();
        title = in.readString();
        if (in.readByte() == 0x01) {
            edition_key = new ArrayList<String>();
            in.readList(edition_key, String.class.getClassLoader());
        } else {
            edition_key = null;
        }
        if (in.readByte() == 0x01) {
            author_name = new ArrayList<String>();
            in.readList(author_name, String.class.getClassLoader());
        } else {
            author_name = null;
        }
        first_publish_year = in.readString();
        key = in.readString();
        if (in.readByte() == 0x01) {
            ia = new ArrayList<String>();
            in.readList(ia, String.class.getClassLoader());
        } else {
            ia = null;
        }
        if (in.readByte() == 0x01) {
            author_key = new ArrayList<String>();
            in.readList(author_key, String.class.getClassLoader());
        } else {
            author_key = null;
        }
        byte public_scan_bVal = in.readByte();
        public_scan_b = public_scan_bVal == 0x02 ? null : public_scan_bVal != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cover_i);
        if (has_fulltext == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (has_fulltext ? 0x01 : 0x00));
        }
        if (edition_count == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeInt(edition_count);
        }
        dest.writeString(title);
        if (edition_key == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(edition_key);
        }
        if (author_name == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(author_name);
        }
        dest.writeString(first_publish_year);
        dest.writeString(key);
        if (ia == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(ia);
        }
        if (author_key == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(author_key);
        }
        if (public_scan_b == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (public_scan_b ? 0x01 : 0x00));
        }
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Doc> CREATOR = new Parcelable.Creator<Doc>() {
        @Override
        public Doc createFromParcel(Parcel in) {
            return new Doc(in);
        }

        @Override
        public Doc[] newArray(int size) {
            return new Doc[size];
        }
    };
}