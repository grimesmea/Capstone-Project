package grimesmea.gmail.com.pricklefit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Displays hedgehogs in a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class HedgehogCollectionFragment extends Fragment {

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

        return rootView;
    }

}
