package kilpatrick.paul.mediareleaseservice.fragments;

import android.app.Fragment;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import kilpatrick.paul.mediareleaseservice.R;
import kilpatrick.paul.mediareleaseservice.adapter.MediaReleaseAdapter;
import kilpatrick.paul.mediareleaseservice.domain.MediaRelease;
import kilpatrick.paul.mediareleaseservice.helper.DatabaseHelper;

import java.util.ArrayList;

/**
 * Created by paulk on 25/09/15.
 */
public class SearchFragment extends Fragment {

    private MediaReleaseAdapter mediaReleaseAdapter;
    private RecyclerView recList;
    private ArrayList<MediaRelease> allMediaReleases;
    private ArrayList mediaReleaseList;
    private EditText searchText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setupMediaReleaseList();
        return inflater.inflate(R.layout.search, container, false);
    }

    private void setupMediaReleaseList() {
        mediaReleaseList = new ArrayList(1);
        allMediaReleases = new ArrayList(1);
        DatabaseHelper db = new DatabaseHelper(getActivity().getApplicationContext());
        SQLiteDatabase dbReadableDatabase = db.getReadableDatabase();
        Cursor cursor = dbReadableDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.MR_TABLE_NAME + " ORDER BY " + DatabaseHelper.ID_COLUMN + " DESC", null);
        while (cursor.moveToNext()) {
            byte[] object = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.OBJECT_COLUMN));
            MediaRelease mr = (MediaRelease) DatabaseHelper.deserializeObject(object);
            if (mr != null) {
                mediaReleaseList.add(mr);
                allMediaReleases.add(mr);
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setupAdapter(true);
    }

    private void setupAdapter(boolean all) {
        recList = (RecyclerView) getActivity().findViewById(R.id.searchCardList);
        searchText = (EditText) getActivity().findViewById(R.id.inputSearch);

        searchText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performFilter();
                    return true;
                }
                return false;
            }
        });

        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);
        if(all) {
            mediaReleaseAdapter = new MediaReleaseAdapter(allMediaReleases, getActivity().getApplicationContext());
        } else {
            mediaReleaseAdapter = new MediaReleaseAdapter(mediaReleaseList, getActivity().getApplicationContext());
        }
        recList.setAdapter(mediaReleaseAdapter);
    }

    private void performFilter() {
        for (int count = 0; count < mediaReleaseList.size(); count++) {
            recList.getAdapter().notifyItemRemoved(count);
        }
        setupMediaReleaseList();
        mediaReleaseList = new ArrayList(1);
        for (MediaRelease release : allMediaReleases) {
            String s = searchText.getText().toString().toLowerCase();
            if (!mediaReleaseList.contains(release) && (release.getTitle().toLowerCase().contains(s) || release.getUrl().toLowerCase().contains(s) || release.getSnippet().toLowerCase().contains(s))) {
                mediaReleaseList.add(release);
            }
        }

        setupAdapter(false);

        for (int count = 0; count < mediaReleaseList.size(); count++) {
            recList.getAdapter().notifyItemInserted(count);
        }
        recList.getAdapter().notifyDataSetChanged();
        recList.scrollToPosition(0);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
