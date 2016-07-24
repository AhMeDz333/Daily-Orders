package com.example.ahmadz.firebase.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ahmadz.firebase.R;
import com.example.ahmadz.firebase.main.adapter.OrderItemRecyclerAdapter;
import com.example.ahmadz.firebase.main.callback.OrderItemChangedListener;
import com.example.ahmadz.firebase.main.database.FireBaseHelper;
import com.example.ahmadz.firebase.main.model.OrderItemMetaInfo;
import com.example.ahmadz.firebase.main.model.OrderItemViewHolder;
import com.example.ahmadz.firebase.main.utils.DialogHelper;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DefaultOrderActivity extends AppCompatActivity implements OrderItemChangedListener {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.recyclerView_orders) RecyclerView recyclerOrders;
	@Bind(R.id.empty_message) TextView emptyMessage;
	@Bind(R.id.progress_bar) ProgressBar progressBar;

	private Context mContext;
	private DatabaseReference mDatabase;
	private OrderItemRecyclerAdapter mAdapter;
	private String userUID;
	private DialogHelper dialogHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_order);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;
		dialogHelper = new DialogHelper(mContext);

		setExtras();
		initRecyclerView();
	}

	private void initRecyclerView() {
		progressBar.setVisibility(View.VISIBLE);
		recyclerOrders.setHasFixedSize(true);
		recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

		mDatabase = FireBaseHelper.getDatabase()
				.getReference();

		mAdapter = new OrderItemRecyclerAdapter(
				mContext,
				this,
				OrderItemMetaInfo.class,
				R.layout.order_item_layout,
				OrderItemViewHolder.class,
				mDatabase.child(getString(R.string.defaults))
						.child(getString(R.string.orders_node))
						.child(userUID)
		);
		recyclerOrders.setAdapter(mAdapter);

		// for data tracking.
		mDatabase.child(getString(R.string.defaults))
				.child(getString(R.string.orders_node))
				.child(userUID)
				.addValueEventListener(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						progressBar.setVisibility(View.INVISIBLE);
						if (dataSnapshot == null || dataSnapshot.getChildrenCount() == 0){
							emptyMessage.setVisibility(View.VISIBLE);
						}else{
							emptyMessage.setVisibility(View.INVISIBLE);
						}
					}
					@Override
					public void onCancelled(DatabaseError databaseError) {
						progressBar.setVisibility(View.INVISIBLE);
					}
				});
	}

	private void setExtras() {
		Intent intent = getIntent();
		userUID = intent.getStringExtra(getString(R.string.uid));
	}

	@OnClick(R.id.fab)
	public void addItemFabClicked(){
		dialogHelper.showInputDialog(
				input -> addNewItem(new OrderItemMetaInfo(input)),
				"Add Order Item!",
				"Order Item Name..."
		);
	}

	private void addNewItem(OrderItemMetaInfo orderItemMetaInfo) {
		mDatabase.child(getString(R.string.defaults))
				.child(getString(R.string.orders_node))
				.child(userUID)
				.push().setValue(orderItemMetaInfo);
	}

	private void clearAll() {
		mDatabase.child(getString(R.string.defaults))
				.child(getString(R.string.orders_node))
				.child(userUID).removeValue();

		dialogHelper.showMessageDialog("Clear All", "All Clear Commander!");
	}

	@Override
	public void onQuantityChanged(int position, int quantity) {
		if (quantity == 0){
			mAdapter.getRef(position).removeValue();
			return;
		}
		Map<String, Object> map = new HashMap<>();
		map.put(getString(R.string.quantity), quantity);
		mAdapter.getRef(position).updateChildren(map);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_default_order, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.clear_all:
				clearAll();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
