package com.example.ahmadz.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class LoginActivity extends AppCompatActivity {

	@Bind(R.id.email) EditText inputEmail;
	@Bind(R.id.password) EditText inputPassword;
	@Bind(R.id.login_facebook) LoginButton facebookButton;
	@Bind(R.id.progressBar) ProgressBar progressBar;

	private final String TAG = this.getClass().getSimpleName();
	private FirebaseAuth mAuth;
	private CallbackManager mCallbackManager;
	private OnCompleteListener<AuthResult> onCompletionListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		ButterKnife.bind(this);

//		try {
//			PackageInfo info = getPackageManager().getPackageInfo(
//					getPackageName(),
//					PackageManager.GET_SIGNATURES);
//			for (Signature signature : info.signatures) {
//				MessageDigest md = MessageDigest.getInstance("SHA");
//				md.update(signature.toByteArray());
//				Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//			}
//		}
//		catch (PackageManager.NameNotFoundException e) {
//
//		}
//		catch (NoSuchAlgorithmException e) {
//
//		}

		mAuth = FirebaseAuth.getInstance();

		if (mAuth.getCurrentUser() != null) {
			startActivity(new Intent(LoginActivity.this, MainActivity.class));
			finish();
		}

		setupFacebookAuth();
	}

	private void setupFacebookAuth() {
//		authListener = firebaseAuth -> {
//			Log.i(TAG, "setupFacebookAuth: changed.");
//			if (firebaseAuth.getCurrentUser() != null)
//				Toast.makeText(this, firebaseAuth.getCurrentUser().getUid(), Toast.LENGTH_SHORT).show();
//		};
//
//		mAuth.addAuthStateListener(authListener);

		onCompletionListener =  task -> {
			progressBar.setVisibility(View.GONE);
			Log.i(TAG, "setupFacebookAuth: " + task.isSuccessful());

			if (!task.isSuccessful()) {
				Toast.makeText(LoginActivity.this, getString(R.string.auth_failed), Toast.LENGTH_LONG).show();

			} else {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			}
		};
		// Initialize Facebook Login button
		mCallbackManager = CallbackManager.Factory.create();
		facebookButton.setReadPermissions("email", "public_profile");
		facebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
			@Override
			public void onSuccess(LoginResult loginResult) {
				Log.i(TAG, "facebook:onSuccess:" + loginResult);
				handleFacebookAccessToken(loginResult.getAccessToken());
			}

			@Override
			public void onCancel() {
				Log.i(TAG, "facebook:onCancel");
			}

			@Override
			public void onError(FacebookException error) {
				Log.i(TAG, "facebook:onError", error);
			}
		});
	}

	private void handleFacebookAccessToken(AccessToken token) {
		Log.i(TAG, "handleFacebookAccessToken:" + token);

		AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

		mAuth.signInWithCredential(credential)
				.addOnCompleteListener(this, onCompletionListener);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mCallbackManager.onActivityResult(requestCode, resultCode, data);
	}

	@OnClick(R.id.sign_in_button)
	public void signInClicked(){
		String email = inputEmail.getText().toString();
		String password = inputPassword.getText().toString();

		if (TextUtils.isEmpty(email)) {
			Toast.makeText(this, "Enter email address!", Toast.LENGTH_SHORT).show();
			return;
		}

		if (TextUtils.isEmpty(password)) {
			Toast.makeText(this, "Enter password!", Toast.LENGTH_SHORT).show();
			return;
		}

		progressBar.setVisibility(View.VISIBLE);

		signIn(email, password);
	}

	private void signIn(String email, String password) {
		//authenticate user
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, onCompletionListener);
	}

	@OnClick(R.id.sign_up_button)
	public void signUpClicked(){
		startActivity(new Intent(LoginActivity.this, SignupActivity.class));
		finish();
	}
}
