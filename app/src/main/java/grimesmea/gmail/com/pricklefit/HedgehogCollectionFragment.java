package grimesmea.gmail.com.pricklefit;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    RecyclerView mRecyclerView;

    public HedgehogCollectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_hedgehog_collection, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_hedgehog_collection);
        View emptyView = rootView.findViewById(R.id.recyclerview_hedgehog_collection_empty);

        mHedgehogCollectionAdapter = new HedgehogCollectionAdapter(
                getActivity(), new HedgehogCollectionAdapter.HedgehogCollectionAdapterOnClickHandler() {
            @Override
            public void onClick(int hedgehogId) {
                ((Callback) getActivity())
                        .onItemSelected(HedgehogContract.HedgehogsEntry.buildHedgehogUri(
                                hedgehogId));
            }
        },
                emptyView);
        mRecyclerView.setAdapter(mHedgehogCollectionAdapter);


        SimpleDividerItemDecoration dividerItemDecoration = new SimpleDividerItemDecoration(getContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

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
        mHedgehogCollectionAdapter.notifyDataSetChanged();
        mRecyclerView.setAdapter(mHedgehogCollectionAdapter);
        mHedgehogCollectionAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mHedgehogCollectionAdapter.swapCursor(null);
    }

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * DetailFragmentCallback for when an item has been selected.
         */
        void onItemSelected(Uri hedgehogUri);
    }
}
