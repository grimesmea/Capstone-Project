package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    private boolean isWaitingForResponse;

    public DailyStepsAdapter(Context context, List<DailyStepsDTO> dailyStepTotals, View emptyView,
                             boolean isWaitingForResponse) {
        mContext = context;
        mDailyStepTotals = dailyStepTotals;
        Collections.sort(dailyStepTotals);
        mEmptyView = emptyView;
        this.isWaitingForResponse = isWaitingForResponse;
        Log.d(LOG_TAG, "New adapter created");

        if (isWaitingForResponse) {
            updateEmptyViewMessage(R.string.empty_daily_steps_list_waiting_for_response);
        } else {
            updateEmptyViewMessage(R.string.empty_daily_steps_list_no_data);
        }
    }

    public void updateEmptyViewMessage(int message) {
        TextView emptyView = (TextView) mEmptyView.findViewById(R.id.recyclerview_daily_steps_empty);
        emptyView.setText(message);
    }

    @Override
    public DailyStepsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Log.d(LOG_TAG, "onCreateViewHolder");
        if (viewGroup instanceof RecyclerView) {
            int layoutId = R.layout.list_item_daily_steps;

            Log.d(LOG_TAG, "New viewholder created");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            return new DailyStepsAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(DailyStepsAdapterViewHolder viewHolder, int position) {
        Log.d(LOG_TAG, "onBindViewHolder");
        Log.d(LOG_TAG, "isWaitingForResponse:" + isWaitingForResponse);
        Log.d(LOG_TAG, "getItemCount = " + getItemCount());
        if (getItemCount() == 0) {
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
