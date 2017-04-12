package ru.ermakov.rssreader.db;

import android.database.Cursor;
import android.provider.BaseColumns;

/**
 * Класс который определяет содержимое таблицы Subscription.
 */
public abstract class SubscriptionEntry implements BaseColumns {
    public static final String TABLE_NAME = "subscription";

    /** Название RSS-подписки. */
    public static final String COLUMN_NAME = "name";
    /** URL RSS-подписки. */
    public static final String COLUMN_URL = "url";

    public static String getName(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
    }

    public static String getUrl(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndex(COLUMN_URL));
    }
}
