package au.gov.australia.media.mrs_android.mediareleaseservice.adapter;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import au.gov.australia.media.mrs_android.mediareleaseservice.R;
import au.gov.australia.media.mrs_android.mediareleaseservice.domain.MediaRelease;
import au.gov.australia.media.mrs_android.mediareleaseservice.helper.DatabaseHelper;
import java.util.List;

/**
 * Created by paulk on 21/09/15.
 */
public class MediaReleaseAdapter extends RecyclerView.Adapter<MediaReleaseAdapter.MediaReleaseViewHolder> {
    private List<MediaRelease> mediaReleaseList;
    private Context context;

    public MediaReleaseAdapter(List<MediaRelease> mediaReleaseList, Context context) {
        this.mediaReleaseList = mediaReleaseList;
        this.context = context;
    }

    @Override
    public int getItemCount() {
        return mediaReleaseList.size();
    }

    @Override
    public void onBindViewHolder(MediaReleaseViewHolder mediaReleaseViewHolder, int i) {
        MediaRelease mediaRelease = mediaReleaseList.get(i);
        mediaReleaseViewHolder.vTitle.setText(mediaRelease.getTitle());
        mediaReleaseViewHolder.vSnippet.setText(mediaRelease.getSnippet());
        if(mediaRelease.isFavourited()) {
            mediaReleaseViewHolder.vFavIcon.setTextColor(Color.parseColor("#999900"));
        } else {
            mediaReleaseViewHolder.vFavIcon.setTextColor(Color.parseColor("#AAAAAA"));
        }
    }

    @Override
    public MediaReleaseViewHolder onCreateViewHolder(final ViewGroup viewGroup, int i) {
        final View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.card, viewGroup, false);
        return new MediaReleaseViewHolder(itemView);
    }

    public MediaRelease getItemAt(int position) {
        MediaRelease mediaRelease = mediaReleaseList.get(position);
        return mediaRelease;
    }

    public class MediaReleaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        protected TextView vTitle;
        protected TextView vSnippet;
        protected Button vFavIcon;
        protected Button vShareIcon;

        public MediaReleaseViewHolder(View v) {
            super(v);
            vTitle = (TextView) v.findViewById(R.id.title);
            vSnippet = (TextView) v.findViewById(R.id.snippet);
            vFavIcon = (Button) v.findViewById(R.id.fav_icon);
            vShareIcon = (Button) v.findViewById(R.id.share_icon);

            vFavIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = getAdapterPosition();
                    MediaRelease mediaRelease = getItemAt(position);
                    if (mediaRelease.isFavourited()) {
                        mediaRelease.setFavourited(false);
                    } else {
                        mediaRelease.setFavourited(true);
                    }

                    DatabaseHelper db = new DatabaseHelper(context);
                    SQLiteDatabase dbWrite = db.getWritableDatabase();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put(DatabaseHelper.OBJECT_COLUMN, DatabaseHelper.serializeObject(mediaRelease));
                    String selection = DatabaseHelper.ID_COLUMN + " = " + mediaRelease.getId();
                    try {
                        dbWrite.beginTransaction();
                        dbWrite.update(DatabaseHelper.MR_TABLE_NAME, contentValues, selection, null);
                        dbWrite.setTransactionSuccessful();
                        mediaReleaseList.set(position, mediaRelease);
                    } catch (SQLiteConstraintException sqlce) {
                        //Do nothing, already in there
                    } finally {
                        dbWrite.endTransaction();
                    }
                    notifyItemChanged(position);
                    notifyDataSetChanged();
                }
            });

            vShareIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        int position = getAdapterPosition();
                        MediaRelease mediaRelease = getItemAt(position);

                        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                        sharingIntent.setType("text/plain");

                        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, mediaRelease.getTitle());
                        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, mediaRelease.getUrl());
                        Intent i = Intent.createChooser(sharingIntent, "Share via");
                        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(i);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int position = getAdapterPosition();
            MediaRelease mediaRelease = getItemAt(position);
            mediaRelease.setVisited(true);

            DatabaseHelper db = new DatabaseHelper(context);
            SQLiteDatabase dbWrite = db.getWritableDatabase();

            ContentValues contentValues = new ContentValues();
            contentValues.put(DatabaseHelper.OBJECT_COLUMN, DatabaseHelper.serializeObject(mediaRelease));

            String selection = DatabaseHelper.ID_COLUMN + " = " + mediaRelease.getId();
            try {
                dbWrite.beginTransaction();
                dbWrite.update(DatabaseHelper.MR_TABLE_NAME, contentValues, selection, null);
                dbWrite.setTransactionSuccessful();
                mediaReleaseList.set(position, mediaRelease);
            } catch(SQLiteConstraintException sqlce) {
                //Do nothing, already in there
            } finally {
                dbWrite.endTransaction();
            }
            notifyItemChanged(position);
            notifyDataSetChanged();

            String url = mediaRelease.getUrl();
            Uri uri = Uri.parse(url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
