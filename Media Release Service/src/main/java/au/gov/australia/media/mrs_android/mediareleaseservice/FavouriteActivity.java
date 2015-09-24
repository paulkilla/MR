package au.gov.australia.media.mrs_android.mediareleaseservice;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import au.gov.australia.media.mrs_android.mediareleaseservice.adapter.MediaReleaseAdapter;
import au.gov.australia.media.mrs_android.mediareleaseservice.domain.MediaRelease;
import au.gov.australia.media.mrs_android.mediareleaseservice.helper.DatabaseHelper;

import java.util.ArrayList;


public class FavouriteActivity extends AppCompatActivity {

    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private ArrayList mediaReleaseList;
    private MediaReleaseAdapter mediaReleaseAdapter;
    private RecyclerView recList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mediaReleaseList = new ArrayList(1);
        DatabaseHelper db = new DatabaseHelper(getApplicationContext());
        SQLiteDatabase dbReadableDatabase = db.getReadableDatabase();
        Cursor cursor = dbReadableDatabase.rawQuery("SELECT * FROM " + DatabaseHelper.MR_TABLE_NAME, null);
        while (cursor.moveToNext()) {
            byte[] object = cursor.getBlob(cursor.getColumnIndex(DatabaseHelper.OBJECT_COLUMN));
            MediaRelease mr = (MediaRelease)DatabaseHelper.deserializeObject(object);
            if(mr != null && !mr.isHidden() && mr.isFavourited()) {
                mediaReleaseList.add(mr);
            } else {
                System.out.println("MediaRelease is null or selected as hidden");
            }
        }
        setContentView(R.layout.activity_main);

        recList = (RecyclerView) findViewById(R.id.cardList);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recList.setLayoutManager(llm);

        mediaReleaseAdapter = new MediaReleaseAdapter(mediaReleaseList, getApplicationContext());
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

                DatabaseHelper db = new DatabaseHelper(getApplicationContext());
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
                    mAdapter.notifyDataSetChanged();
                } catch(SQLiteConstraintException sqlce) {
                    //Do nothing, already in there
                } finally {
                    dbWrite.endTransaction();
                }
            }
        };

        ItemTouchHelper swipeLeftHelper = new ItemTouchHelper(swipeCallBack);
        swipeLeftHelper.attachToRecyclerView(recList);

        mDrawerList = (ListView)findViewById(R.id.navList);
        addDrawerItems();
        initToolbar();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    private void addDrawerItems() {
        final String[] osArray = { "Home", "Favourites", "Subscribe by Email", "Search" };
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, osArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                mDrawerList.setItemChecked(position, true);
                mDrawerLayout.closeDrawer(mDrawerList);
                Intent newActivity = new Intent(getApplicationContext(), MainActivity.class);
                ;
                if (osArray[position].equals("Favourites")) {
                    newActivity = new Intent(getApplicationContext(), FavouriteActivity.class);
                } else if (osArray[position].equals("Subscribe by Email")) {
                    newActivity = new Intent(getApplicationContext(), MainActivity.class);
                } else if (osArray[position].equals("Search")) {
                    newActivity = new Intent(getApplicationContext(), MainActivity.class);
                }

                startActivity(newActivity);
            }
        });
    }

    private void initToolbar() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        setSupportActionBar(mToolbar);
        setTitle(getString(R.string.app_name));
        mToolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar ,  R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                getSupportActionBar().setTitle(R.string.app_name);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                getSupportActionBar().setTitle(R.string.menu_expanded_title);
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };


        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }
}
