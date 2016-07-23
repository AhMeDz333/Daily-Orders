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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;

public class DefaultOrderActivity extends AppCompatActivity implements OrderItemChangedListener {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.recyclerView_orders) RecyclerView recyclerOrders;
	@Bind(R.id.empty_message) TextView emptyMessage;
	@Bind(R.id.progress_bar) ProgressBar progressBar;

	private Context mContext;
	private DatabaseReference mDatabase;
	private OrderItemRecyclerAdapter mAdapter;
	private String userUID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_order);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		mContext = this;

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
				mDatabase.child(getString(R.string.default_order))
						.child(getString(R.string.orders_node))
						.child(userUID)
		);
		recyclerOrders.setAdapter(mAdapter);

		// for data tracking.
		mDatabase.child(getString(R.string.default_order))
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

					}
				});
	}

	private void setExtras() {
		Intent intent = getIntent();
		userUID = intent.getStringExtra(getString(R.string.uid));
	}

	private void clearAll() {
		mDatabase.child(getString(R.string.default_order))
				.child(getString(R.string.orders_node))
				.child(userUID).removeValue();
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
