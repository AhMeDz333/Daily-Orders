package com.example.ahmadz.firebase.main.adapter;

import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.ahmadz.firebase.R;
import com.example.ahmadz.firebase.main.QuantityView;
import com.example.ahmadz.firebase.main.callback.OrderItemChangedListener;
import com.example.ahmadz.firebase.main.model.OrderItemMetaInfo;
import com.example.ahmadz.firebase.main.model.OrderItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

/**
 * Created by ahmadz on 7/22/16.
 */
public class OrderItemRecyclerAdapter extends FirebaseRecyclerAdapter<OrderItemMetaInfo, OrderItemViewHolder>{
	private final Context mContext;
	private final OrderItemChangedListener listener;
	private int lastPosition = -1;

	public OrderItemRecyclerAdapter(Context mContext, OrderItemChangedListener listener, Class<OrderItemMetaInfo> modelClass, int modelLayout, Class<OrderItemViewHolder> viewHolderClass, DatabaseReference ref) {
		super(modelClass, modelLayout, viewHolderClass, ref);
		this.mContext = mContext;
		this.listener = listener;
	}

	@Override
	public void onBindViewHolder(OrderItemViewHolder viewHolder, int position) {
		super.onBindViewHolder(viewHolder, position);
		viewHolder.setAllTags(position);
	}

	@Override
	protected void populateViewHolder(OrderItemViewHolder viewHolder, OrderItemMetaInfo metaInfo, int position) {
		Animation animation = AnimationUtils.loadAnimation(mContext,
				(position > lastPosition) ? R.anim.up_from_bottom
						: R.anim.down_from_top);
		viewHolder.itemView.startAnimation(animation);
		lastPosition = position;

		viewHolder.setAllTags(position);
		viewHolder.bindViews(metaInfo);
		viewHolder.quantityView.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
			@Override
			public void onQuantityChanged(View view, int newQuantity, boolean programmatically) {
				int tag = (int)view.getTag();
				if (!programmatically)
					listener.onQuantityChanged(tag, newQuantity);
			}
			@Override
			public void onLimitReached() {}
		});
	}

	@Override
	public void onViewDetachedFromWindow(OrderItemViewHolder holder) {
		super.onViewDetachedFromWindow(holder);
		holder.itemView.clearAnimation();
	}
}
