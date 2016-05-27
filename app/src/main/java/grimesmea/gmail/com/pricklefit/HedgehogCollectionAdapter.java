package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
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
    public class HedgieCollectionAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView hedgieImage;
        public final ImageView heartImage1;
        public final ImageView heartImage2;
        public final ImageView heartImage3;
        public final ImageView heartImage4;
        public final ImageView heartImage5;

        public HedgieCollectionAdapterViewHolder(View view) {
            super(view);
            hedgieImage = (ImageView) view.findViewById(R.id.hedgehog_image);
            heartImage1 = (ImageView) view.findViewById(R.id.heart_1);
            heartImage2 = (ImageView) view.findViewById(R.id.heart_2);
            heartImage3 = (ImageView) view.findViewById(R.id.heart_3);
            heartImage4 = (ImageView) view.findViewById(R.id.heart_4);
            heartImage5 = (ImageView) view.findViewById(R.id.heart_5);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d("HCAdapter", "ViewHolder clicked!");
            Intent intent = new Intent(mContext, HedgehogDetailActivity.class);
            mContext.startActivity(intent);
        }
    }
}
