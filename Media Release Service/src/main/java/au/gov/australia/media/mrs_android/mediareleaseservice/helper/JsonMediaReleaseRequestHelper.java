package au.gov.australia.media.mrs_android.mediareleaseservice.helper;

import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.widget.Toast;
import au.gov.australia.media.mrs_android.mediareleaseservice.domain.MediaRelease;
import au.gov.australia.media.mrs_android.mediareleaseservice.fragments.HomeFragment;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Created by paulk on 21/09/15.
 */
public class JsonMediaReleaseRequestHelper extends AsyncTask<String, Void, String> {

    Context context;
    Fragment fragment;

    public JsonMediaReleaseRequestHelper(Context context) {
        this.context = context;
    }

    @Override
    protected void onPreExecute() {
        Toast toast = Toast.makeText(context, "Updating Media Releases", Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    protected String doInBackground(String... params) {
        return GET(params[0]);
    }

    @Override
    protected void onPostExecute(String json) {
        Toast toast = Toast.makeText(context, "Finished updating Media Releases", Toast.LENGTH_SHORT);
        toast.show();
        if(fragment != null) {
            ((HomeFragment)fragment).refreshContent();
        }
    }


    public String GET(String url) {
        InputStream inputStream;
        String result = "";
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse httpResponse = httpclient.execute(new HttpGet(url));
            inputStream = httpResponse.getEntity().getContent();
            if(inputStream != null)
                result = convertInputStreamToString(inputStream);

        } catch (Exception e) {
            System.out.println(e.getLocalizedMessage());
        }

        try {
            DatabaseHelper db = new DatabaseHelper(context);
            SQLiteDatabase dbWrite = db.getWritableDatabase();
            JSONArray json = new JSONArray(result);
            for(int i = 0; i < json.length(); i++) {
                JSONObject jsonObject = json.getJSONObject(i);
                MediaRelease mediaRelease = new MediaRelease(jsonObject);
                mediaRelease.setFavourited(false);
                mediaRelease.setCreated(new Date());
                mediaRelease.setHidden(false);
                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseHelper.ID_COLUMN, mediaRelease.getId());
                contentValues.put(DatabaseHelper.OBJECT_COLUMN, DatabaseHelper.serializeObject(mediaRelease));
                if(dbWrite.rawQuery("SELECT " + DatabaseHelper.ID_COLUMN + " FROM " + DatabaseHelper.MR_TABLE_NAME + " WHERE ID = " + mediaRelease.getId(), null).getCount() <= 0) {
                    try {
                        dbWrite.beginTransaction();
                        dbWrite.insertOrThrow(DatabaseHelper.MR_TABLE_NAME, null, contentValues);
                        dbWrite.setTransactionSuccessful();
                    } catch (SQLiteConstraintException sqlce) {
                        //Do nothing, already in there
                    } finally {
                        dbWrite.endTransaction();
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        return result;
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line;
        String result = "";
        while((line = bufferedReader.readLine()) != null) {
            result += line;
        }

        inputStream.close();
        return result;

    }

    public void setFragment(Fragment fragment) {
        this.fragment = fragment;
    }
}
