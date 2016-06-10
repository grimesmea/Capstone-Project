package grimesmea.gmail.com.pricklefit;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
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

    private Cursor mCursor;
    private ImageView hedgehogImageView;
    private ImageView heartImage1;
    private ImageView heartImage2;
    private ImageView heartImage3;
    private ImageView heartImage4;
    private ImageView heartImage5;
    private ImageView[] heartImageViews;


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
        final Hedgehog hedgehog;

        int hedgehogImageResource;
        Drawable hedgehogDrawable;

        mEmptyView.setVisibility(getItemCount() == 0 ? View.VISIBLE : View.GONE);

        mCursor.moveToPosition(position);
        hedgehog = new Hedgehog(mCursor);
        Log.d("HCAdapter", hedgehog.getSilhouetteImageName());

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

        Log.d("HCAdapter", String.valueOf(hedgehogImageResource));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource, mContext.getTheme());
        } else {
            hedgehogDrawable = mContext.getResources().getDrawable(hedgehogImageResource);
        }

        hedgehogImageView.setImageDrawable(hedgehogDrawable);
        hedgehogImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = new Bundle();
                args.putParcelable("hedgehogParcelable", hedgehog);

                Intent intent = new Intent(mContext, HedgehogDetailActivity.class)
                        .putExtra("hedgehog", args);
                mContext.startActivity(intent);
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

    public Cursor getCursor() {
        return mCursor;
    }

    /**
     * Cache of the children views for a hedgehog list item.
     */
    public class HedgieCollectionAdapterViewHolder extends RecyclerView.ViewHolder {

        public HedgieCollectionAdapterViewHolder(View view) {
            super(view);
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
