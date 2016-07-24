package com.example.ahmadz.firebase.main.utils;

import android.content.Context;
import android.text.InputType;

import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.ahmadz.firebase.R;

/**
 * Created by ahmadz on 7/24/16.
 */
public class DialogHelper {
	private Context mContext;

	public DialogHelper(Context mContext){
		this.mContext = mContext;
	}

	public void showMessageDialog(String title, String content){
		new MaterialDialog.Builder(mContext)
				.title(title)
				.content(content)
				.positiveText("OK")
				.show();
	}

	public void showConfirmationDialog(DialogCallback listener, String title, String content, String positive, String negative){
		new MaterialDialog.Builder(mContext)
				.title(title)
				.content(content)
				.positiveText(positive)
				.negativeText(negative)
				.onPositive((dialog, which) -> listener.onDialogMissionFulfilled(true))
				.onNegative(((dialog1, which1) -> listener.onDialogMissionFulfilled(false)))
				.show();
	}

	public void showInputDialog(InputCallback listener, String title, String content){
		new MaterialDialog.Builder(mContext)
				.title(title)
				.titleGravity(GravityEnum.CENTER)
				.titleColor(mContext.getResources().getColor(R.color.colorPrimaryDark))
				.inputType(InputType.TYPE_CLASS_TEXT)
				.input(content, "", (dialog, input) -> {
					listener.onInputReceived(input.toString());
				})
				.show();
	}

	public interface DialogCallback{
		void onDialogMissionFulfilled(boolean positive);
	}
	public interface InputCallback{
		void onInputReceived(String input);
	}
}
