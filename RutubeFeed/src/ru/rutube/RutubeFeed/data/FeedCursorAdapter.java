package ru.rutube.RutubeFeed.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;

import ru.rutube.RutubeAPI.content.FeedContract;
import ru.rutube.RutubeAPI.tools.BitmapLruCache;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.views.ThumbnailView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 11.05.13
 * Time: 11:53
 * To change this template use File | Settings | File Templates.
 */
public class FeedCursorAdapter extends SimpleCursorAdapter {
    protected static final SimpleDateFormat sqlDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    protected static final SimpleDateFormat reprDateFormat = new SimpleDateFormat("d L y");
    private static final String LOG_TAG = FeedCursorAdapter.class.getName();
    protected ImageLoader imageLoader;
    protected static int item_layout_id = R.layout.feed_item;
    private Context context;
    private RequestQueue mRequestQueue;

    public interface LoadMoreListener
    {
        public void onLoadMore();
    }

    private LoadMoreListener loadMoreListener = null;

    public void setLoadMoreListener(LoadMoreListener listener) {
        loadMoreListener = listener;
    }
    public FeedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
        this.context = context;
        initImageLoader(context);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d(getClass().getName(), "new view");
        View view = inflater.inflate(item_layout_id, null);
        ThumbnailView thumbnailView = (ThumbnailView)view.findViewById(R.id.thumbnailImageView);
        thumbnailView.setDefaultImageResId(R.drawable.stub);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        Log.d(getClass().getName(), "bind view");
        try {
            int titleIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.TITLE);
            int thumbnailUriIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.THUMBNAIL_URI);
            int descriptionIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.DESCRIPTION);
            int createdIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.CREATED);
            int authorNameIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.AUTHOR_NAME);
            int avatarIndex = cursor.getColumnIndexOrThrow(FeedContract.FeedColumns.AVATAR_URI);

            String title = cursor.getString(titleIndex);
            String thumbnailUri = cursor.getString(thumbnailUriIndex);
            String description = cursor.getString(descriptionIndex);
            Date created = null;
            try {
                String created_str = cursor.getString(createdIndex);
                Log.d(getClass().getName(), "CR:" + created_str);
                created = sqlDateFormat.parse(created_str);
            } catch (ParseException ignored) {
                Log.d(getClass().getName(), "CR Parse error");
            }
            String authorName = cursor.getString(authorNameIndex);
            String avatarUri = cursor.getString(avatarIndex);
            TextView tv = (TextView) view.findViewById(R.id.titleTextView);
            tv.setText(title);
            tv = (TextView) view.findViewById(R.id.createdTextView);
            if (created != null)
                tv.setText(getCreatedText(created));
            tv = (TextView) view.findViewById(R.id.descriptionTextView);
            tv.setText(description);
            tv = (TextView) view.findViewById(R.id.authorTextView);
            tv.setText(authorName);
            NetworkImageView iv = (NetworkImageView) view.findViewById(R.id.thumbnailImageView);
            iv.setImageUrl(thumbnailUri, imageLoader);
            iv = (NetworkImageView) view.findViewById(R.id.avatarImageView);
            iv.setImageUrl(avatarUri, imageLoader);


        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    protected String getCreatedText(Date created) {
        Date now = new Date();
        long seconds = (now.getTime() - created.getTime()) / 1000;
        if (seconds < 3600)
            return context.getString(R.string.now);
        if (seconds < 24 * 3600)
            return context.getString(R.string.today);
        if (seconds < 2 * 24 * 3600)
            return context.getString(R.string.yesterday);
        if (seconds < 5 * 24 * 3600)
            return String.format(context.getString(R.string.days_ago_24, seconds / (24 * 3600)));
        if (seconds < 7 * 24 * 3600)
            return String.format(context.getString(R.string.days_ago_59, seconds / (24 * 3600)));
        if (seconds < 14 * 24 * 3600)
            return String.format(context.getString(R.string.week_ago, seconds / (7 * 24 * 3600)));
        if (seconds < 31 * 24 * 3600)
            return String.format(context.getString(R.string.weeks_ago, seconds / (7 * 24 * 3600)));
        return reprDateFormat.format(created);
    }

    protected void initImageLoader(Context context) {
        mRequestQueue = Volley.newRequestQueue(context);
        imageLoader = new ImageLoader(mRequestQueue, new BitmapLruCache());
    }

    @Override
    public Object getItem(int position) {
        Log.d(LOG_TAG, "getItem " + String.valueOf(position));
        if (position > getCount() - 10) {
            loadMore();
        }
        return super.getItem(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(LOG_TAG, "getView " + String.valueOf(position));
        if (position > getCount() - 10) {
            loadMore();
        }
        return super.getView(position, convertView, parent);
    }

    private void loadMore() {
        if (loadMoreListener!=null)
            loadMoreListener.onLoadMore();
    }
}
