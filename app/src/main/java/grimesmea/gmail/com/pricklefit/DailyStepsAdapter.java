package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Collections;
import java.util.List;

/**
 * {@link DailyStepsAdapter} exposes a list of daily step counts as well as an average daily step
 * count from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class DailyStepsAdapter extends RecyclerView.Adapter<DailyStepsAdapter.DailyStepsAdapterViewHolder> {

    private final String LOG_TAG = DailyStepsAdapter.class.getSimpleName();

    final private Context mContext;
    final private List<DailyStepsDTO> mDailyStepTotals;
    final private View mEmptyView;

    public DailyStepsAdapter(Context context, List<DailyStepsDTO> dailyStepTotals, View emptyView) {
        mContext = context;
        mDailyStepTotals = dailyStepTotals;
        Collections.sort(dailyStepTotals);
        mEmptyView = emptyView;
    }

    @Override
    public DailyStepsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.list_item_daily_steps;

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            return new DailyStepsAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(DailyStepsAdapterViewHolder viewHolder, int position) {
        if (getItemCount() == 0) {
            TextView emptyView = (TextView) mEmptyView.findViewById(R.id.recyclerview_daily_steps_empty);
            int message = R.string.empty_daily_steps_list;

            emptyView.setText(message);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mEmptyView.setVisibility(View.GONE);
        }

        DailyStepsDTO dailySteps = mDailyStepTotals.get(position);

        viewHolder.dayOfWeekView.setText(dailySteps.getFormattedDay());
        viewHolder.stepCountView.setText(String.format("%,d", dailySteps.getSteps()));
    }

    @Override
    public int getItemCount() {
        return mDailyStepTotals.size();
    }

    /**
     * Cache of the children views for a daily step count list item.
     */
    public class DailyStepsAdapterViewHolder extends RecyclerView.ViewHolder {
        public final TextView dayOfWeekView;
        public final TextView stepCountView;

        public DailyStepsAdapterViewHolder(View view) {
            super(view);
            dayOfWeekView = (TextView) view.findViewById(R.id.list_item_day_of_week);
            stepCountView = (TextView) view.findViewById(R.id.list_item_step_count);
        }
    }
}
