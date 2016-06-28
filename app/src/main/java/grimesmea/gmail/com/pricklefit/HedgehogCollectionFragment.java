package grimesmea.gmail.com.pricklefit;


import android.content.Context;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;


/**
 * Displays hedgehogs in a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class HedgehogCollectionFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final String LOG_TAG = HedgehogCollectionFragment.class.getSimpleName();

    private static final int HEDGEHOGS_LOADER = 200;

    private boolean isSingleChoiceMode;
    private boolean isAutoSelectView;
    private long intialSelectedHedgehogPosition = -1;

    HedgehogCollectionAdapter mHedgehogCollectionAdapter;
    RecyclerView mRecyclerView;

    public HedgehogCollectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onInflate(Context context, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(context, attrs, savedInstanceState);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HedgehogCollectionFragment,
                0, 0);
        isSingleChoiceMode = a.getBoolean(R.styleable.HedgehogCollectionFragment_singleChoiceMode, false);
        isAutoSelectView = a.getBoolean(R.styleable.HedgehogCollectionFragment_autoSelectView, false);
        a.recycle();
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
            public void onClick(int hedgehogId,HedgehogCollectionAdapter.HedgehogCollectionAdapterViewHolder viewHolder) {
                ((Callback) getActivity())
                        .onItemSelected(HedgehogContract.HedgehogsEntry.buildHedgehogUri(
                                hedgehogId));
            }
        },
                emptyView,
                isSingleChoiceMode);
        mRecyclerView.setAdapter(mHedgehogCollectionAdapter);


        SimpleDividerItemDecoration dividerItemDecoration = new SimpleDividerItemDecoration(getContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        LinearLayoutManager linearLayoutManager =
                new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);

        getLoaderManager().initLoader(HEDGEHOGS_LOADER, null, this);

        if (savedInstanceState != null) {
            mHedgehogCollectionAdapter.onRestoreInstanceState(savedInstanceState);
        }

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        mHedgehogCollectionAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
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

        if(data.getCount() != 0) {
            mRecyclerView.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        // Since we know we're going to get items, we keep the listener around until
                        // we see Children.
                    if (mRecyclerView.getChildCount() > 0) {
                        int position = mHedgehogCollectionAdapter.getSelectedItemPosition();

                        mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);

                        if (position == RecyclerView.NO_POSITION &&
                                -1 != intialSelectedHedgehogPosition) {
                            Cursor data = mHedgehogCollectionAdapter.getCursor();
                            int count = data.getCount();
                            for (int i = 0; i < count; i++) {
                                data.moveToPosition(i);
                                if (data.getInt(TodaysStepsFragment.COL_HEDGEHOG_ID) == intialSelectedHedgehogPosition) {
                                    position = i;
                                    break;
                                }
                            }
                        }

                        if (position == RecyclerView.NO_POSITION){
                            position = 0;
                        }

                        // If we don't need to restart the loader, and there's a desired position to restore
                        // to, do so now.
                        mRecyclerView.smoothScrollToPosition(position);

                        RecyclerView.ViewHolder viewHolder = mRecyclerView.findViewHolderForAdapterPosition(position);

                        if (null != viewHolder && isAutoSelectView) {
                            mHedgehogCollectionAdapter.selectView(viewHolder);
                        }
                        return true;
                    }
                    return false;
                }
            });
        }
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
