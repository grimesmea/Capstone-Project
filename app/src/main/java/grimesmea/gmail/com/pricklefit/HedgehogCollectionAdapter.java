package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * {@link HedgehogCollectionAdapter} exposes a list of hedgehogs from a
 * {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class HedgehogCollectionAdapter extends RecyclerView.Adapter<HedgehogCollectionAdapter.HedgieCollectionAdapterViewHolder> {

    private static final int VIEW_TYPE_ACTIVE_HEDGEHOG = 0;
    private static final int VIEW_TYPE_INACTIVE_HEDGEHOG = 1;

    final private Context mContext;
    final private View mEmptyView;

    public HedgehogCollectionAdapter(Context context, View emptyView) {
        mContext = context;
        mEmptyView = emptyView;
    }

    @Override
    public HedgieCollectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewGroup instanceof RecyclerView) {
            int layoutId = -1;
            switch (viewType) {
                case VIEW_TYPE_ACTIVE_HEDGEHOG: {
                    layoutId = R.layout.list_item_hedgehog_active;
                    break;
                }
                case VIEW_TYPE_INACTIVE_HEDGEHOG: {
                    layoutId = R.layout.list_item_hedgehog_inactive;
                    break;
                }
            }

            View view = LayoutInflater.from(viewGroup.getContext()).inflate(layoutId, viewGroup, false);
            HedgieCollectionAdapterViewHolder hedgieCollectionAdapterViewHolder = new HedgieCollectionAdapterViewHolder(view);

            if (viewType == VIEW_TYPE_ACTIVE_HEDGEHOG) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) hedgieCollectionAdapterViewHolder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }

            return hedgieCollectionAdapterViewHolder;
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(HedgieCollectionAdapterViewHolder hedgieCollectionAdapterViewHolder, int position) {
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_ACTIVE_HEDGEHOG : VIEW_TYPE_INACTIVE_HEDGEHOG;
    }

    @Override
    public int getItemCount() {
        return 3;
    }

    /**
     * Cache of the children views for a hedgehog list item.
     */
    public class HedgieCollectionAdapterViewHolder extends RecyclerView.ViewHolder {
        public final ImageView hedgieImage;

        public HedgieCollectionAdapterViewHolder(View view) {
            super(view);
            hedgieImage = (ImageView) view.findViewById(R.id.hedgehog_image);
        }
    }
}
