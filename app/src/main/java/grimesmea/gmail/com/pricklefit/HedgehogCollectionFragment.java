package grimesmea.gmail.com.pricklefit;


import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;


/**
 * Displays hedgehogs in a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class HedgehogCollectionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int HEDGEHOGS_LOADER = 200;
    private final String LOG_TAG = HedgehogCollectionFragment.class.getSimpleName();
    HedgehogCollectionAdapter mHedgehogCollectionAdapter;

    public HedgehogCollectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hedgehog_collection, container, false);

        RecyclerView mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_hedgehog_collection);
        View emptyView = rootView.findViewById(R.id.recyclerview_hedgehog_collection_empty);

        mHedgehogCollectionAdapter = new HedgehogCollectionAdapter(getActivity(), emptyView);
        mRecyclerView.setAdapter(mHedgehogCollectionAdapter);

        int columnCount = getResources().getInteger(R.integer.list_column_count);
        StaggeredGridLayoutManager sglm =
                new StaggeredGridLayoutManager(columnCount, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(sglm);

        getLoaderManager().initLoader(HEDGEHOGS_LOADER, null, this);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");
        CursorLoader cursorLoader = new CursorLoader(
                getContext(),
                HedgehogContract.HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mHedgehogCollectionAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHedgehogCollectionAdapter.swapCursor(null);
    }
}