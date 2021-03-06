package kilpatrick.paul.mediareleaseservice.fragments;

import android.app.Fragment;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import kilpatrick.paul.mediareleaseservice.R;
import kilpatrick.paul.mediareleaseservice.adapter.MediaReleaseAdapter;
import kilpatrick.paul.mediareleaseservice.domain.MediaRelease;
import kilpatrick.paul.mediareleaseservice.helper.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by paulk on 25/09/15.
 */
public class FavouritesFragment extends Fragment {

    private MediaReleaseAdapter mediaReleaseAdapter;
    private RecyclerView recList;
    private ArrayList mediaReleaseList;
    private SwipeRefreshLayout swipeLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mediaReleaseList = new ArrayList(1);
        DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
        SQLiteDatabase dbReadableDatabase = db.getReadableDatabase();
        Cursor cursor = dbReadableDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.MR_TABLE_NAME + " ORDER BY " + DatabaseHelper.ID_COLUMN + " DESC", null);
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

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeLayout.setRefreshing(false);
            }
        });
        swipeLayout.setColorScheme(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
    }

}
