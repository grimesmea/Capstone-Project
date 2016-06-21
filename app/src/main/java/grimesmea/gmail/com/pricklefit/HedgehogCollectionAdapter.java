package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * {@link HedgehogCollectionAdapter} exposes a list of hedgehogs from a
 * {@link android.database.Cursor} to a {@link android.support.v7.widget.RecyclerView}.
 */
public class HedgehogCollectionAdapter extends RecyclerView.Adapter<HedgehogCollectionAdapter.HedgehogCollectionAdapterViewHolder> {

    private static final int VIEW_TYPE_ACTIVE_HEDGEHOG = 0;
    private static final int VIEW_TYPE_INACTIVE_HEDGEHOG = 1;
    private final String LOG_TAG = HedgehogCollectionAdapter.class.getSimpleName();
    final private Context mContext;
    final private View mEmptyView;
    final private HedgehogCollectionAdapterOnClickHandler mClickHandler;

    private Cursor mCursor;
    private LinearLayout hedgehogContainer;
    private ImageView hedgehogImageView;
    private ImageView heartImage1;
    private ImageView heartImage2;
    private ImageView heartImage3;
    private ImageView heartImage4;
    private ImageView heartImage5;
    private ImageView[] heartImageViews;


    public HedgehogCollectionAdapter(Context context, HedgehogCollectionAdapterOnClickHandler onClickHandler, View emptyView) {
        mContext = context;
        mClickHandler = onClickHandler;
        mEmptyView = emptyView;
    }

    @Override
    public HedgehogCollectionAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
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
            HedgehogCollectionAdapterViewHolder hedgehogCollectionAdapterViewHolder = new HedgehogCollectionAdapterViewHolder(view);

            if (viewType == VIEW_TYPE_ACTIVE_HEDGEHOG) {
                StaggeredGridLayoutManager.LayoutParams layoutParams = (StaggeredGridLayoutManager.LayoutParams) hedgehogCollectionAdapterViewHolder.itemView.getLayoutParams();
                layoutParams.setFullSpan(true);
            }

            return hedgehogCollectionAdapterViewHolder;
        } else {
            throw new RuntimeException("Not bound to RecyclerView");
        }
    }

    @Override
    public void onBindViewHolder(HedgehogCollectionAdapterViewHolder viewHolder, int position) {
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

        mCursor.moveToPosition(position);

        Hedgehog hedgehog = new Hedgehog(mCursor);
        updateHedgehogViews(hedgehog);
    }

    private void updateHedgehogViews(final Hedgehog hedgehog) {
        int hedgehogImageResource;
        Drawable hedgehogDrawable;

        if (hedgehog.getIsUnlocked()) {
            hedgehogImageResource = mContext.getResources().getIdentifier(hedgehog.getImageName(), "drawable", mContext.getPackageName());
            for (int i = 0; i < 5; i++) {
                if (i < hedgehog.getHappinessLevel()) {
                    heartImageViews[i].setImageResource(R.drawable.heart_filled);
                } else {
                    heartImageViews[i].setImageResource(R.drawable.heart_outline);
                }
            }
        } else {
            hedgehogImageResource = mContext.getResources().getIdentifier(hedgehog.getSilhouetteImageName(), "drawable", mContext.getPackageName());
            for (int i = 0; i < 5; i++) {
                heartImageViews[i].setImageResource(R.drawable.heart_filled_grey);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource, mContext.getTheme());
        } else {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource);
        }

        hedgehogImageView.setImageDrawable(hedgehogDrawable);
        hedgehogContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickHandler.onClick(hedgehog.getId());
            }
        });
    }


    @Override
    public int getItemViewType(int position) {
        return (position == 0) ? VIEW_TYPE_ACTIVE_HEDGEHOG : VIEW_TYPE_INACTIVE_HEDGEHOG;
    }

    @Override
    public int getItemCount() {
        if (mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    public interface HedgehogCollectionAdapterOnClickHandler {
        void onClick(int hedgehogId);
    }

    /**
     * Cache of the children views for a hedgehog list item.
     */
    public class HedgehogCollectionAdapterViewHolder extends RecyclerView.ViewHolder {

        public HedgehogCollectionAdapterViewHolder(View view) {
            super(view);
            hedgehogContainer = (LinearLayout) view.findViewById(R.id.hedgehog_container);
            hedgehogImageView = (ImageView) view.findViewById(R.id.hedgehog_image);
            heartImage1 = (ImageView) view.findViewById(R.id.heart_1);
            heartImage2 = (ImageView) view.findViewById(R.id.heart_2);
            heartImage3 = (ImageView) view.findViewById(R.id.heart_3);
            heartImage4 = (ImageView) view.findViewById(R.id.heart_4);
            heartImage5 = (ImageView) view.findViewById(R.id.heart_5);

            heartImageViews = new ImageView[]{
                    heartImage1, heartImage2, heartImage3, heartImage4, heartImage5
            };
        }
    }
}

