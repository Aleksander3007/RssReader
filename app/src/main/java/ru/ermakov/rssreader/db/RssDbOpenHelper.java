package ru.ermakov.rssreader.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ru.ermakov.rssreader.R;

/**
 * Класс для работы с БД RSS-подписок.
 */
public class RssDbOpenHelper extends SQLiteOpenHelper {

    private static volatile RssDbOpenHelper sInstance;

    public static final String TAG = RssDbOpenHelper.class.getSimpleName();

    private static final String DATABASE_NAME = "rss.db";
    private static final int DATABASE_VERSION = 1;

    private final List<ContentValues> initSubscriptions = new ArrayList<>();

    public synchronized static RssDbOpenHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (RssDbOpenHelper.class) {
                if (sInstance == null)
                    sInstance = new RssDbOpenHelper(context);
            }
        }
        return sInstance;
    }

    private RssDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        createInitData(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sqlCreateTableSubscription = "CREATE TABLE " + SubscriptionEntry.TABLE_NAME + " (" +
                SubscriptionEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SubscriptionEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                SubscriptionEntry.COLUMN_URL + " TEXT NOT NULL " +
                ");";

        String sqlCreateTablePost = "CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                PostEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                PostEntry.COLUMN_DESCRIPTION + " TEXT, " +
                PostEntry.COLUMN_CHANNEL_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + PostEntry.COLUMN_CHANNEL_ID + ") REFERENCES " +
                    SubscriptionEntry.TABLE_NAME + "("  + SubscriptionEntry._ID + ")" +
                    "ON DELETE CASCADE " +
                ");";

        db.beginTransaction();
        try {
            db.execSQL(sqlCreateTableSubscription);
            db.execSQL(sqlCreateTablePost);
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }

        addInitDataInDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onConfigure(SQLiteDatabase db)
    {
        super.onConfigure(db);
        db.execSQL("PRAGMA foreign_keys=ON");
    }

    /**
     * Создать начальные RSS-подписки.
     */
    private void createInitData(Context context) {

        String[] subscriptionNames = context.getResources().getStringArray(R.array.subscription_names);
        String[] subscriptionUrls = context.getResources().getStringArray(R.array.subscription_urls);

        if (subscriptionNames.length != subscriptionNames.length)
            Log.e(TAG, "Количество начальных имен подписок не совпадает с количеством начальных URL этих подписок");

        int nSubscription = subscriptionNames.length;
        for (int iItem = 0; iItem < nSubscription; iItem++) {
            ContentValues itemValues = new ContentValues();
            itemValues.put(SubscriptionEntry.COLUMN_NAME, subscriptionNames[iItem]);
            itemValues.put(SubscriptionEntry.COLUMN_URL, subscriptionUrls[iItem]);
            initSubscriptions.add(itemValues);
        }
    }

    /**
     * Добавить начальные подписки в БД.
     */
    private void addInitDataInDatabase(SQLiteDatabase db) {
        db.beginTransaction();
        try {
            for (ContentValues item : initSubscriptions) {
                db.insertOrThrow(SubscriptionEntry.TABLE_NAME, null, item);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException sglex) {
            sglex.printStackTrace();
        }
        finally {
            db.endTransaction();
        }
    }
}
