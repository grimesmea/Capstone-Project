package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * {@link DailyStepsAdapter} exposes a list of daily step counts as well as an average daily step
 * count from a {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class DailyStepsAdapter extends RecyclerView.Adapter<DailyStepsAdapter.DailyStepsAdapterViewHolder> {

    private static final int VIEW_TYPE_AVERAGE_STEPS = 0;
    private static final int VIEW_TYPE_DAILY_STEPS = 1;

    final private Context mContext;
    final private View mEmptyView;
    final private boolean mIsLandscape;

    public DailyStepsAdapter(Context context, boolean isLandscape, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
        mIsLandscape = isLandscape;
    }

    @Override
    public DailyStepsAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_AVERAGE_STEPS: {
                    if (!mIsLandscape) {
                        layoutId = R.layout.average_steps;
                    } else {
                        layoutId = R.layout.list_item_daily_steps;
                    }
                    break;
                }
                case VIEW_TYPE_DAILY_STEPS: {

                    layoutId = R.layout.list_item_daily_steps;
                    break;
                }
            }

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            return new DailyStepsAdapterViewHolder(view);
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(DailyStepsAdapterViewHolder dailyStepsAdapterViewHolder, int position) {
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_AVERAGE_STEPS : VIEW_TYPE_DAILY_STEPS;
    }

    @Override
    public int getItemCount() {
        if (mIsLandscape) {
            return 6;
        } else {
            return 7;
        }
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
