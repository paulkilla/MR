package au.gov.australia.media.mrs_android.mediareleaseservice.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import au.gov.australia.media.mrs_android.mediareleaseservice.domain.MediaRelease;

import java.io.*;

/**
 * Created by paulk on 21/09/15.
 */
public class DatabaseHelper extends SQLiteOpenHelper {


    private static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "MR";
    public static final String MR_TABLE_NAME = "mediaReleases";
    public static final String TOPICS_TABLE_NAME = "topics";
    public static final String ID_COLUMN = "id";
    public static final String OBJECT_COLUMN = "object";
    private static final String MR_TABLE_CREATE =
            "CREATE TABLE " + MR_TABLE_NAME + " (" +
                    ID_COLUMN + " INT PRIMARY KEY NOT NULL, " +
                    OBJECT_COLUMN + " BLOB NOT NULL);";

    private static final String TOPICS_TABLE_CREATE =
            "CREATE TABLE " + TOPICS_TABLE_NAME + " (" +
                    ID_COLUMN + " INT PRIMARY KEY NOT NULL, " +
                    OBJECT_COLUMN + " BLOB NOT NULL);";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(MR_TABLE_CREATE);
        db.execSQL(TOPICS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static byte[] serializeObject(Object o) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try {
            ObjectOutput out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.close();

            // Get the bytes of the serialized object
            byte[] buf = bos.toByteArray();

            return buf;
        } catch(IOException ioe) {
            return null;
        }
    }

    public static Object deserializeObject(byte[] b) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(b));
            Object object = in.readObject();
            in.close();

            return object;
        } catch(ClassNotFoundException cnfe) {

            return null;
        } catch(IOException ioe) {

            return null;
        }
    }
}
