package com.example.ahmadz.firebase.main.database;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by ahmadz on 13/07/2016.
 */

public class FireBaseHelper {

	private static FirebaseDatabase mDatabase;

	public static FirebaseDatabase getDatabase() {
		if (mDatabase == null) {
			mDatabase = FirebaseDatabase.getInstance();
			mDatabase.setPersistenceEnabled(true);
		}

		return mDatabase;
	}

}