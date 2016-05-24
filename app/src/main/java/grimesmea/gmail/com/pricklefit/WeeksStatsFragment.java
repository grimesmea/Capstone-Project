package grimesmea.gmail.com.pricklefit;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


/**
 * Encapsulates fetching the daily step counts for the last week as well as the average daily step
 * count and displaying it as a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class WeeksStatsFragment extends Fragment {

    RecyclerView mRecyclerView;
    DailyStepsAdapter mDailyStepsAdapter;

    public WeeksStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weeks_stats, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_daily_steps);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        View emptyView = rootView.findViewById(R.id.recyclerview_daily_steps_empty);
        mRecyclerView.setHasFixedSize(true);

        boolean isLandscape = rootView.findViewById(R.id.average_steps) != null ? true : false;

        mDailyStepsAdapter = new DailyStepsAdapter(getActivity(), isLandscape, emptyView);
        mRecyclerView.setAdapter(mDailyStepsAdapter);

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }
}
