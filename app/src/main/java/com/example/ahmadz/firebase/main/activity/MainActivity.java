package com.example.ahmadz.firebase.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.ahmadz.firebase.R;
import com.example.ahmadz.firebase.main.adapter.OrderItemRecyclerAdapter;
import com.example.ahmadz.firebase.main.callback.HidingScrollListener;
import com.example.ahmadz.firebase.main.callback.OrderItemChangedListener;
import com.example.ahmadz.firebase.main.database.FireBaseHelper;
import com.example.ahmadz.firebase.main.model.OrderItemMetaInfo;
import com.example.ahmadz.firebase.main.model.OrderItemViewHolder;
import com.example.ahmadz.firebase.main.utils.DialogHelper;
import com.facebook.FacebookSdk;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OrderItemChangedListener {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.recyclerView_orders) RecyclerView recyclerOrders;
	@Bind(R.id.empty_message) TextView emptyMessage;
	@Bind(R.id.progress_bar) ProgressBar progressBar;
	@Bind(R.id.float_menu)
	FloatingActionMenu floatMenu;

	private final String TAG = this.getClass().getSimpleName();
	private String TODAY;
	private Context mContext;
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener authListener;
	private DatabaseReference mDatabase;
	private OrderItemRecyclerAdapter mAdapter;
	private String userUID;
	private DialogHelper dialogHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
//		getSupportActionBar().setIcon(R.mipmap.ic_launcher);

		mContext = this;
		TODAY = getDate();
		dialogHelper = new DialogHelper(mContext);
		//initialize Facebook SDK.
		FacebookSdk.sdkInitialize(getApplicationContext());
		setupAuthentication();

		if(mAuth.getCurrentUser() == null) //if not signed in don't go further.
			return;

		initFabMenu();
		setupRecyclerViewSync();
	}

	private String getDate() {
		Calendar cal = Calendar.getInstance();
		return String.format("%s-%s-%s", cal.get(Calendar.DAY_OF_MONTH), cal.get(Calendar.MONTH)+1, cal.get(Calendar.YEAR));
	}

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(authListener);
	}

	@OnClick(R.id.fab)
	public void addItemFabClicked(){
		floatMenu.close(true);
		dialogHelper.showInputDialog(
				input -> addNewItem(new OrderItemMetaInfo(input)),
				"Add Order Item!",
				"Order Item Name..."
		);
	}

	private void addNewItem(OrderItemMetaInfo orderItemMetaInfo) {
		mDatabase.
				child(TODAY).
				child(getString(R.string.orders_node))
				.push().setValue(orderItemMetaInfo);
	}

	@OnClick(R.id.fab_order_default)
	public void orderDefaultFabClicked(){
		floatMenu.close(true);
		progressBar.setVisibility(View.VISIBLE);

		mDatabase
				.child(getString(R.string.defaults))
				.child(getString(R.string.orders_node))
				.child(userUID)
				.addListenerForSingleValueEvent(new ValueEventListener() {
					@Override
					public void onDataChange(DataSnapshot dataSnapshot) {
						progressBar.setVisibility(View.INVISIBLE);

						if (dataSnapshot == null || dataSnapshot.getChildrenCount() == 0){
							dialogHelper.showMessageDialog("Empty Defaults", "No default items were found!");
							return;
						}

						for (DataSnapshot child : dataSnapshot.getChildren()) {
							addNewItem(child.getValue(OrderItemMetaInfo.class));
						}

						dialogHelper.showMessageDialog("Success", "Items were added successfully.");
					}
					@Override
					public void onCancelled(DatabaseError databaseError) {
						progressBar.setVisibility(View.INVISIBLE);
					}
				});
	}

	private void startDefaultOrderActivity() {
		Intent intent = new Intent(mContext, DefaultOrderActivity.class);
		intent.putExtra(getString(R.string.uid), userUID);
		startActivity(intent);
	}

	private void setupRecyclerViewSync() {
		progressBar.setVisibility(View.VISIBLE);
		recyclerOrders.setHasFixedSize(true);
		recyclerOrders.setLayoutManager(new LinearLayoutManager(this));

		userUID = mAuth.getCurrentUser().getUid();
		mDatabase = FireBaseHelper.getDatabase()
				.getReference();

		mAdapter = new OrderItemRecyclerAdapter(
				mContext,
				this,
				OrderItemMetaInfo.class,
				R.layout.order_item_layout,
				OrderItemViewHolder.class,
				mDatabase.child(TODAY).child(getString(R.string.orders_node))
		);
		recyclerOrders.setAdapter(mAdapter);

		// for data tracking.
		mDatabase.child(TODAY).child(getString(R.string.orders_node))
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

	private void setupAuthentication() {
		mAuth = FirebaseAuth.getInstance();

		authListener = fireBaseAuth -> {
			Log.i(TAG, "setupAuthentication: done");
			FirebaseUser user = fireBaseAuth.getCurrentUser();
			if (user == null) {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}
		};
	}

	@Override
	public void onQuantityChanged(int position, int quantity) {
		if (quantity == 0){
			progressBar.setVisibility(View.VISIBLE);
			mAdapter.getRef(position).removeValue().
					addOnCompleteListener(task -> {
						mAdapter.notifyDataSetChanged();
						progressBar.setVisibility(View.INVISIBLE);
					});
			return;
		}
		Map<String, Object> map = new HashMap<>();
		map.put(getString(R.string.quantity), quantity);
		mAdapter.getRef(position).updateChildren(map);
	}

	private void initFabMenu(){
		floatMenu.setClosedOnTouchOutside(true);
		recyclerOrders.setOnScrollListener(new HidingScrollListener() {
			@Override
			public void onHide() {
				FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) floatMenu.getLayoutParams();
				int fabBottomMargin = lp.bottomMargin;
				floatMenu.animate().translationY(floatMenu.getHeight()+fabBottomMargin).setInterpolator(new AccelerateInterpolator(2)).start();
			}

			@Override
			public void onShow() {
				toolbar.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2));
				floatMenu.animate().translationY(0).setInterpolator(new DecelerateInterpolator(2)).start();
			}
		});
	}

	private void signOut() {
		dialogHelper.showConfirmationDialog(
				positive -> signMeOut(),
				"Log-Out",
				"Are you sure you wanna Logout?",
				"Yes", "Cancel"
		);
	}

	private void signMeOut(){
		mAuth.signOut();
		// TODO: 7/23/16 logout from facebook
	}

	@Override
	public void onStop() {
		super.onStop();
		if (authListener != null) {
			mAuth.removeAuthStateListener(authListener);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()){
			case R.id.action_signout:
				signOut();
				break;
			case R.id.default_order:
				startDefaultOrderActivity();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
