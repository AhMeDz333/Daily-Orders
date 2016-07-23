package com.example.ahmadz.firebase.main.adapter;

import android.content.Context;

import com.example.ahmadz.firebase.main.callback.OrderItemChangedListener;
import com.example.ahmadz.firebase.main.model.OrderItemMetaInfo;
import com.example.ahmadz.firebase.main.model.OrderItemViewHolder;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;

import me.himanshusoni.quantityview.QuantityView;

/**
 * Created by ahmadz on 7/22/16.
 */
public class OrderItemRecyclerAdapter extends FirebaseRecyclerAdapter<OrderItemMetaInfo, OrderItemViewHolder>{
	private final Context mContext;
	private final OrderItemChangedListener listener;

	public OrderItemRecyclerAdapter(Context mContext, OrderItemChangedListener listener, Class<OrderItemMetaInfo> modelClass, int modelLayout, Class<OrderItemViewHolder> viewHolderClass, DatabaseReference ref) {
		super(modelClass, modelLayout, viewHolderClass, ref);
		this.mContext = mContext;
		this.listener = listener;
	}

	@Override
	protected void populateViewHolder(OrderItemViewHolder viewHolder, OrderItemMetaInfo metaInfo, int position) {
		viewHolder.setAllTags(position);
		viewHolder.bindViews(metaInfo);
		viewHolder.quantityView.setOnQuantityChangeListener(new QuantityView.OnQuantityChangeListener() {
			@Override
			public void onQuantityChanged(int newQuantity, boolean programmatically) {
				if (programmatically)
					listener.onQuantityChanged(position, newQuantity);
			}
			@Override
			public void onLimitReached() {

			}
		});
	}
}
