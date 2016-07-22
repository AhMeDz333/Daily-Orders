package com.example.ahmadz.firebase;

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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.facebook.FacebookSdk;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

	@Bind(R.id.toolbar) Toolbar toolbar;
	@Bind(R.id.nickname) EditText nicknameField;
	@Bind(R.id.recyclerView_quotes) RecyclerView recyclerQuotes;

	private final String TAG = this.getClass().getSimpleName();
	private final String QUOTES = "quotes";
	private final String USERS = "users";
	private final String NICKNAME = "nickname";
	private FirebaseAuth.AuthStateListener authListener;
	private FirebaseAuth mAuth;
	private DatabaseReference mDatabase;
	private FirebaseRecyclerAdapter<String, RecyclerViewHolder> mAdapter;
	private String userID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ButterKnife.bind(this);
		setSupportActionBar(toolbar);
		//initialize Facebook SDK.
		FacebookSdk.sdkInitialize(getApplicationContext());

		setupAuthentication();

		if(mAuth.getCurrentUser() != null) {//if signed in.
			setupRecyclerViewSync();
			setupNicknameSync();
		}
	}

	private void setupNicknameSync() {
		mDatabase.child(NICKNAME).addValueEventListener(new ValueEventListener() {
			@Override
			public void onDataChange(DataSnapshot dataSnapshot) {
				nicknameField.setText(dataSnapshot.getValue(String.class));
			}
			@Override
			public void onCancelled(DatabaseError databaseError) {}
		});
	}

	@Override
	public void onStart() {
		super.onStart();
		mAuth.addAuthStateListener(authListener);
	}

	@OnClick(R.id.fab)
	public void fabClicked(){
		new MaterialDialog.Builder(this)
				.title("Add Quote")
				.titleGravity(GravityEnum.CENTER)
				.titleColor(getResources().getColor(R.color.colorPrimaryDark))
				.inputType(InputType.TYPE_CLASS_TEXT)
				.input("Enter new quote..", "", (dialog, input) -> {
					String quoteText = input.toString();
					mDatabase.child(QUOTES).push().setValue(quoteText);

				}).show();
	}

	@OnClick(R.id.button_save_nickname)
	public void saveNicknameClicked(){
		String nickname = nicknameField.getText().toString();
		mDatabase.child(NICKNAME).setValue(nickname);
	}

	private void setupRecyclerViewSync() {
		recyclerQuotes.setHasFixedSize(true);
		recyclerQuotes.setLayoutManager(new LinearLayoutManager(this));

		userID = mAuth.getCurrentUser().getUid();
		mDatabase = FirebaseDatabase.getInstance().
				getReference(USERS).
				child(userID);

		mAdapter = new FirebaseRecyclerAdapter<String, RecyclerViewHolder>(
				String.class,
				android.R.layout.two_line_list_item,
				RecyclerViewHolder.class,
				mDatabase.child(QUOTES)
		) {
			@Override
			protected void populateViewHolder(RecyclerViewHolder viewHolder, String model, int position) {
				viewHolder.setQuoteText(model);
			}
		};

		recyclerQuotes.setAdapter(mAdapter);
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
			case R.id.action_settings:
				Toast.makeText(this, "Not yet Implemented!", Toast.LENGTH_SHORT).show();
				break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class RecyclerViewHolder extends RecyclerView.ViewHolder {
		private TextView quoteText;

		public RecyclerViewHolder(View root) {
			super(root);
			quoteText = (TextView) root.findViewById(android.R.id.text1);
		}

		public void setQuoteText(String text){
			quoteText.setText(text);
		}
	}
}
