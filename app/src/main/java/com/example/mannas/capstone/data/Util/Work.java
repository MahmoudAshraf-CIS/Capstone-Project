package com.example.mannas.capstone.data.Util;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by Mannas on 8/23/2017.
 */

public class Work implements Parcelable {
    @SuppressWarnings("unused")
    public static final Parcelable.Creator<Work> CREATOR = new Parcelable.Creator<Work>() {
        @Override
        public Work createFromParcel(Parcel in) {
            return new Work(in);
        }

        @Override
        public Work[] newArray(int size) {
            return new Work[size];
        }
    };
    public Boolean printdisabled;
    public String cover_id;
    public ArrayList<String> ia_collection;
    public Boolean has_fulltext;
    public Integer edition_count;
    public Boolean checked_out;
    public String title;
    public Boolean public_scan;
    public String cover_edition_key;
    public Boolean lendinglibrary;
    public String lending_edition;
    public String first_publish_year;
    public String key;
    public ArrayList<Author> authors;
    public String ia;
    public String lending_identifier;
    public ArrayList<String> subject;

    public Work() {
    }

    protected Work(Parcel in) {
        byte printdisabledVal = in.readByte();
        printdisabled = printdisabledVal == 0x02 ? null : printdisabledVal != 0x00;
        cover_id = in.readString();
        if (in.readByte() == 0x01) {
            ia_collection = new ArrayList<String>();
            in.readList(ia_collection, String.class.getClassLoader());
        } else {
            ia_collection = null;
        }
        byte has_fulltextVal = in.readByte();
        has_fulltext = has_fulltextVal == 0x02 ? null : has_fulltextVal != 0x00;
        edition_count = in.readByte() == 0x00 ? null : in.readInt();
        byte checked_outVal = in.readByte();
        checked_out = checked_outVal == 0x02 ? null : checked_outVal != 0x00;
        title = in.readString();
        byte public_scanVal = in.readByte();
        public_scan = public_scanVal == 0x02 ? null : public_scanVal != 0x00;
        cover_edition_key = in.readString();
        byte lendinglibraryVal = in.readByte();
        lendinglibrary = lendinglibraryVal == 0x02 ? null : lendinglibraryVal != 0x00;
        lending_edition = in.readString();
        first_publish_year = in.readString();
        key = in.readString();
        if (in.readByte() == 0x01) {
            authors = new ArrayList<Author>();
            in.readList(authors, Author.class.getClassLoader());
        } else {
            authors = null;
        }
        ia = in.readString();
        lending_identifier = in.readString();
        if (in.readByte() == 0x01) {
            subject = new ArrayList<String>();
            in.readList(subject, String.class.getClassLoader());
        } else {
            subject = null;
        }
    }

    public String getCoverUrl(Character S_M_L) {
        return "https://covers.openlibrary.org/b/id/" +
                cover_id + "-" + S_M_L + ".jpg";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (printdisabled == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (printdisabled ? 0x01 : 0x00));
        }
        dest.writeString(cover_id);
        if (ia_collection == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(ia_collection);
        }
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
        if (checked_out == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (checked_out ? 0x01 : 0x00));
        }
        dest.writeString(title);
        if (public_scan == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (public_scan ? 0x01 : 0x00));
        }
        dest.writeString(cover_edition_key);
        if (lendinglibrary == null) {
            dest.writeByte((byte) (0x02));
        } else {
            dest.writeByte((byte) (lendinglibrary ? 0x01 : 0x00));
        }
        dest.writeString(lending_edition);
        dest.writeString(first_publish_year);
        dest.writeString(key);
        if (authors == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(authors);
        }
        dest.writeString(ia);
        dest.writeString(lending_identifier);
        if (subject == null) {
            dest.writeByte((byte) (0x00));
        } else {
            dest.writeByte((byte) (0x01));
            dest.writeList(subject);
        }
    }
}
