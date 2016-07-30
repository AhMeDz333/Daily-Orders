package com.example.ahmadz.firebase.main.model;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.example.ahmadz.firebase.R;
import com.example.ahmadz.firebase.main.QuantityView;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by ahmadz on 7/22/16.
 */
public class OrderItemViewHolder extends RecyclerView.ViewHolder {
	@Bind(R.id.item_name) TextView itemName;
	@Bind(R.id.quantityView) public QuantityView quantityView;

	public OrderItemViewHolder(View root) {
		super(root);
		ButterKnife.bind(this, root);
	}

	public void bindViews(OrderItemMetaInfo metaInfo) {
		itemName.setText(metaInfo.getName());
		quantityView.setQuantity(metaInfo.getQuantity());
	}

	public void setAllTags(int position) {
		itemName.setTag(position);
		quantityView.setTag(position);
	}
}
