package ru.rutube.RutubeFeed.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;

import ru.rutube.RutubeAPI.models.Constants;
import ru.rutube.RutubeFeed.R;
import ru.rutube.RutubeFeed.ctrl.FeedController;
import ru.rutube.RutubePlayer.ui.FragmentPlayer;
import ru.rutube.RutubePlayer.ui.PlayerActivity;

/**
 * Created with IntelliJ IDEA.
 * User: Сергей
 * Date: 05.05.13
 * Time: 12:56
 * To change this template use File | Settings | File Templates.
 */
public class FeedFragment extends ListFragment implements FeedController.FeedView {
    private static final String LOG_TAG = FeedFragment.class.getName();
    private MenuItem refreshItem;
    private Uri feedUri;
    private ListView sgView;
    private FeedController mController;

    public ListAdapter getListAdapter() {
        return sgView.getAdapter();
    }
    public void setListAdapter(ListAdapter adapter) {
        sgView.setAdapter(adapter);
    }

    @Override
    public void openPlayer(Uri uri) {
        Activity activity = getActivity();
        assert activity != null;
        // TODO: перевести на intent-filter
        Intent intent = new Intent(activity, PlayerActivity.class);
        intent.setData(uri);
        startActivity(intent);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mController.onListItemClick(position);
    }

    public void showError() {
        Activity activity = getActivity();
        if (activity != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.
                    setTitle(android.R.string.dialog_alert_title).
                    setMessage(getString(R.string.faled_to_load_data)).
                    create().
                    show();
        }
        doneRefreshing();
    }

    private void init() {
        initFeedUri();
        mController = new FeedController(feedUri);
        mController.attach(getActivity(), this);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mController.detach();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
        setHasOptionsMenu(true);
        init();
    }

    private void initFeedUri() {
        Bundle args = getArguments();
        if (args != null)
            feedUri = args.getParcelable(Constants.Params.FEED_URI);
        if (feedUri == null) {
            Activity activity = getActivity();
            assert activity != null;
            feedUri = activity.getIntent().getData();
        }
        Log.d(LOG_TAG, "Feed Uri:" + String.valueOf(feedUri));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        Log.d(LOG_TAG, "onPrepareOptionsMenu");
        refreshItem = menu.findItem(R.id.menu_refresh);
        super.onPrepareOptionsMenu(menu);
    }

    public void setRefreshing() {
        if (refreshItem == null) {
            Log.d(LOG_TAG, "empty refresh item");
            return;
        }
        Activity activity = getActivity();
        if (activity == null)
            return;
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_btn, null);
        assert iv != null;
        Animation rotation = AnimationUtils.loadAnimation(activity, R.anim.rotate_icon);
        assert rotation != null;
        rotation.setRepeatCount(Animation.INFINITE);
        iv.startAnimation(rotation);
        refreshItem.setActionView(iv);
    }

    public void doneRefreshing() {
        if (refreshItem == null)
            return;
        View actionView = refreshItem.getActionView();
        if (actionView != null)
            actionView.clearAnimation();
        refreshItem.setActionView(null);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        Log.d(LOG_TAG, "onCreateView");
        sgView = (ListView)inflater.inflate(R.layout.feed_fragment, container, false);
        return sgView;
    }

}
