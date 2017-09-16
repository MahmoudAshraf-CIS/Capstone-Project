package com.example.mannas.capstone.data.ContentProvider;

import android.content.UriMatcher;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Mannas on 8/24/2017.
 */

public class Contract {
    public static final String Authority = Contract.class.getPackage().getName();

    public static final Uri Base_content_URI = Uri.parse("content://"+Authority);

    public static final class TableIdentifier{
        public static final int ID_SubjectResponse = 1;
        public static final int ID_BookDetailResponse = 2;
        public static final int ID_SubjectList= 3;
    }


    public static class SubjectResponse implements BaseColumns {
        public static final String TABLE_NAME = "SubjectResponse";
        public static final String PATH = TABLE_NAME;
        public static final Uri uri = Base_content_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static class Columns {
            public static final String subject_name = "subject_name";
            public static final String offset ="m_offset";
            public static final String json ="json";
        }

        public static final String CreationSQL_DDL =
                "CREATE TABLE "+ TABLE_NAME +"("
                + Columns.subject_name +" TEXT    NOT NULL," +
                  Columns.offset        +" INTEGER NOT NULL," +
                  Columns.json           +" TEXT," +
                " PRIMARY KEY (" +  Columns.subject_name +","+ Columns.offset+ ")" +
                " ON CONFLICT REPLACE" +
                ");";


        public static final String DropSQL_DDL = "DROP TABLE IF EXISTS "+ TABLE_NAME;

    }
    public static class BookDetailResponse implements BaseColumns {
        public static final String TABLE_NAME = "BookDetailResponse";
        public static final String PATH = TABLE_NAME;
        public static final Uri uri = Base_content_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static class Columns {
            public static final String OLID = "OLID";
            public static final String json ="json";
        }

        public static final String CreationSQL_DDL =
                "CREATE TABLE "+ TABLE_NAME +"("
                + Columns.OLID    +" TEXT , " +
                  Columns.json    +" TEXT, " +
                " PRIMARY KEY (" +  Columns.OLID + ")" +
                " ON CONFLICT REPLACE" +
                " );";


        public static final String DropSQL_DDL = "DROP TABLE IF EXISTS "+ TABLE_NAME;

    }

    public static class SubjectList implements BaseColumns {
        public static final String TABLE_NAME = "SubjectList";
        public static final String PATH = TABLE_NAME;
        public static final Uri uri = Base_content_URI.buildUpon().appendPath(TABLE_NAME).build();

        public static class Columns {
            public static final String SubjectList = "SubjectList";
            public static final String ID = "ID";
        }

        public static final String CreationSQL_DDL =
                "CREATE TABLE "+ TABLE_NAME +"("
                        + Columns.SubjectList +" TEXT    NOT NULL," +
                        Columns.ID + " INTEGER NOT NULL,"+
                        " PRIMARY KEY (" +  Columns.SubjectList + ")" +
                        " ON CONFLICT REPLACE" +
                        ");";

        public static final String DropSQL_DDL = "DROP TABLE IF EXISTS "+ TABLE_NAME;

    }

    public static final UriMatcher mUriMatcher;
    static {
        mUriMatcher = new UriMatcher(-1);

        mUriMatcher.addURI(Contract.Authority, SubjectResponse.PATH, TableIdentifier.ID_SubjectResponse);
        mUriMatcher.addURI(Contract.Authority, BookDetailResponse.PATH, TableIdentifier.ID_BookDetailResponse);
        mUriMatcher.addURI(Contract.Authority, SubjectList.PATH, TableIdentifier.ID_SubjectList);

    }
}
