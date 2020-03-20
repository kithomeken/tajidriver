package com.tajidriver.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {UserDetails.class, TripDetails.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;
    private static final String DB_NAME = "appDatabase_driver.db";

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                        AppDatabase.class, DB_NAME)
                        .allowMainThreadQueries()
                        .addCallback(new RoomDatabase.Callback() {
                            @Override
                            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                                super.onCreate(db);
                            }
                        })
                        .build();
            }
        }

        return INSTANCE;
    }

    // Data Access Objects Initialization
    public abstract UserDetailsDao userDetailsDao();

    public abstract TripDetailsDao tripDetailsDao();
}