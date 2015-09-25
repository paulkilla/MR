package au.gov.australia.media.mrs_android.mediareleaseservice.fragments;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import au.gov.australia.media.mrs_android.mediareleaseservice.R;
import au.gov.australia.media.mrs_android.mediareleaseservice.adapter.MediaReleaseAdapter;
import au.gov.australia.media.mrs_android.mediareleaseservice.domain.MediaRelease;
import au.gov.australia.media.mrs_android.mediareleaseservice.helper.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by paulk on 25/09/15.
 */
public class FavouritesFragment extends Fragment {

    private MediaReleaseAdapter mediaReleaseAdapter;
    private RecyclerView recList;
    private ArrayList mediaReleaseList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mediaReleaseList = new ArrayList(1);
        DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
        SQLiteDatabase dbReadableDatabase = db.getReadableDatabase();
        Cursor cursor = dbReadableDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.MR_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            byte[] object = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.OBJECT_COLUMN));
            MediaRelease mr = (MediaRelease)DatabaseHelper.deserializeObject(object);
            if(mr != null && mr.isFavourited()) {
                mediaReleaseList.add(mr);
            }
        }



        return inflater.inflate(R.layout.recycle, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        recList = (RecyclerView) getActivity().findViewById(R.id.cardList);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mediaReleaseAdapter = new MediaReleaseAdapter(mediaReleaseList, getActivity().getApplicationContext());
        recList.setAdapter(mediaReleaseAdapter);


        ItemTouchHelper.SimpleCallback swipeCallBack = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                //Remove swiped item from list and notify the RecyclerView
                int position = viewHolder.getAdapterPosition();
                MediaRelease mediaRelease = (MediaRelease) mediaReleaseList.get(position);
                mediaRelease.setHidden(true);

                DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
                SQLiteDatabase dbWrite = db.getWritableDatabase();

                ContentValues contentValues = new ContentValues();
                contentValues.put(DatabaseHelper.OBJECT_COLUMN, DatabaseHelper.serializeObject(mediaRelease));

                String selection = DatabaseHelper.ID_COLUMN + " = " + mediaRelease.getId();
                try {
                    dbWrite.beginTransaction();
                    dbWrite.update(DatabaseHelper.MR_TABLE_NAME, contentValues, selection, null);
                    dbWrite.setTransactionSuccessful();
                    mediaReleaseList.remove(position);
                    recList.getAdapter().notifyItemRemoved(position);
                    recList.getAdapter().notifyDataSetChanged();
                } catch(SQLiteConstraintException sqlce) {
                    //Do nothing, already in there
                } finally {
                    dbWrite.endTransaction();
                }
            }
        };

        ItemTouchHelper swipeLeftHelper = new ItemTouchHelper(swipeCallBack);
        swipeLeftHelper.attachToRecyclerView(recList);
    }

}
