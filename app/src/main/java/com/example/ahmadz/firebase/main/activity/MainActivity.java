package com.example.ahmadz.firebase.main.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.ahmadz.firebase.R;
import com.example.ahmadz.firebase.main.adapter.OrderItemRecyclerAdapter;
import com.example.ahmadz.firebase.main.callback.OrderItemChangedListener;
import com.example.ahmadz.firebase.main.database.FireBaseHelper;
import com.example.ahmadz.firebase.main.model.OrderItemMetaInfo;
import com.example.ahmadz.firebase.main.model.OrderItemViewHolder;
import com.facebook.FacebookSdk;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity implements OrderItemChangedListener {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.recyclerView_orders) RecyclerView recyclerOrders;

	private final String TAG = this.getClass().getSimpleName();
//	private final String NICKNAME = "nickname";
	private String TODAY;
	private String ORDERS = "orders";
	private Context mContext;
	private FirebaseAuth mAuth;
	private FirebaseAuth.AuthStateListener authListener;
	private DatabaseReference mDatabase;
	private OrderItemRecyclerAdapter mAdapter;
	private String userUID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		mContext = this;
		TODAY = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)+"";
		//initialize Facebook SDK.
		FacebookSdk.sdkInitialize(getApplicationContext());

		setupAuthentication();

		if(mAuth.getCurrentUser() != null) {//if signed in.
			setupRecyclerViewSync();
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(authListener);
	}

	@OnClick(R.id.fab)
	public void fabClicked(){
//		new MaterialDialog.Builder(mContext)
//				.title("Order!")
//				.content("Do you want to use the default order or create a new one?")
//				.positiveText("Default")
//				.negativeText("New")
//				.onPositive((dialog, which) -> startNewOrderActivity())
//				.onNegative((dialog, which) -> orderDefault())
//				.show();
		new MaterialDialog.Builder(mContext)
				.title("Add Order Item!")
				.titleGravity(GravityEnum.CENTER)
				.titleColor(getResources().getColor(R.color.colorPrimaryDark))
				.inputType(InputType.TYPE_CLASS_TEXT)
				.input("Order Item Name...", "", (dialog, input) -> {
					String itemName = input.toString();
					addNewItem(itemName);
				})
				.show();
	}

	private void addNewItem(String itemName) {
		OrderItemMetaInfo orderItemMetaInfo = new OrderItemMetaInfo(itemName);
		mDatabase.child(TODAY).child(ORDERS).push().setValue(orderItemMetaInfo);
	}

//	private void startNewOrderActivity() {
//		Intent intent = new Intent(mContext, NewOrderActivity.class);
//		intent.putExtra(getString(R.string.uid), userUID);
//		startActivity(intent);
//	}

	private void setupRecyclerViewSync() {
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
				mDatabase.child(TODAY).child(ORDERS)
		);
		recyclerOrders.setAdapter(mAdapter);
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
		Map<String, Object> map = new HashMap<>();
		map.put(getString(R.string.quantity), quantity);
		mAdapter.getRef(position).updateChildren(map);
	}

	public void signOut() {
		new MaterialDialog.Builder(this)
				.title("Log-Out")
				.content("Are you sure you wanna Logout?")
				.positiveText("Yes")
				.negativeText("Cancel")
				.onPositive((dialog, which) -> signMeOut())
				.show();
	}

	public void signMeOut(){
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
				Toast.makeText(this, "To be implemented!", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}
}
