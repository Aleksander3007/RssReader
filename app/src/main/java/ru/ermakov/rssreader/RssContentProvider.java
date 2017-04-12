package ru.ermakov.rssreader;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import ru.ermakov.rssreader.db.PostEntry;
import ru.ermakov.rssreader.db.RssDbOpenHelper;
import ru.ermakov.rssreader.db.SubscriptionEntry;

/**
 * ContentProvider для работы с RSS-подписками, постами.
 */
public class RssContentProvider extends ContentProvider {

    public static final String  AUTHORITY = "ru.ermakov.rssreader.RssContentProvider";

    public static final int CODE_SUBSCRIPTIONS = 100;
    public static final int CODE_POSTS = 200;

    public static final String PATH_SUBSCRIPTIONS = "subscriptions";
    public static final String PATH_POSTS = "posts";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    /** URI для доступа к RSS-подпискам. */
    public static final Uri SUBSCRIPTIONS_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_SUBSCRIPTIONS).build();
    public static final Uri POSTS_URI =
            BASE_CONTENT_URI.buildUpon().appendPath(PATH_POSTS).build();

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AUTHORITY, PATH_SUBSCRIPTIONS, CODE_SUBSCRIPTIONS);
        sUriMatcher.addURI(AUTHORITY, PATH_POSTS, CODE_POSTS);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = RssDbOpenHelper.getInstance(getContext()).getReadableDatabase();

        Cursor returnCursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_SUBSCRIPTIONS:
                returnCursor = db.query(
                        SubscriptionEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case CODE_POSTS:
                returnCursor = db.query(
                        PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            default:
                throw new UnsupportedOperationException("Неизвестный URI: " + uri);
        }

        if (returnCursor != null)
            returnCursor.setNotificationUri(getContext().getContentResolver(), uri);

        return returnCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {

        SQLiteDatabase db = RssDbOpenHelper.getInstance(getContext()).getWritableDatabase();

        Uri returnUri;
        switch (sUriMatcher.match(uri)) {
            case CODE_SUBSCRIPTIONS:
                long id = db.insert(SubscriptionEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(SUBSCRIPTIONS_URI, id);
                } else {
                    throw new android.database.SQLException("Не удалось вставить " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Неизвестный URI: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = RssDbOpenHelper.getInstance(getContext()).getWritableDatabase();

        int nDeleted;
        switch (sUriMatcher.match(uri)) {
            case CODE_SUBSCRIPTIONS:
                nDeleted = db.delete(
                        SubscriptionEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case CODE_POSTS:
                nDeleted = db.delete(
                        PostEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Неизвестный URI: " + uri);
        }

        if (nDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return nDeleted;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        SQLiteDatabase db = RssDbOpenHelper.getInstance(getContext()).getWritableDatabase();

        int nInserted = 0;
        switch (sUriMatcher.match(uri)) {
            case CODE_POSTS:
                db.beginTransaction();
                try {
                    for (ContentValues post : values) {
                        long rowId = db.insert(
                                PostEntry.TABLE_NAME,
                                null,
                                post
                        );
                        if (rowId != -1) nInserted++;
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return nInserted;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        SQLiteDatabase db = RssDbOpenHelper.getInstance(getContext()).getWritableDatabase();

        int nUpdated;
        switch (sUriMatcher.match(uri)) {
            case CODE_SUBSCRIPTIONS:
                nUpdated = db.update(
                        SubscriptionEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );
                break;
            default:
                throw new UnsupportedOperationException("Неизвестный URI: " + uri);
        }

        if (nUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return nUpdated;
    }
}
