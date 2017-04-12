package ru.ermakov.rssreader.loaders;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v4.content.Loader;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Response;
import ru.ermakov.rssreader.RssContentProvider;
import ru.ermakov.rssreader.data.Subscription;
import ru.ermakov.rssreader.db.PostEntry;
import ru.ermakov.rssreader.net.RssApi;
import ru.ermakov.rssreader.net.RssApiFactory;
import ru.ermakov.rssreader.net.RssResponse;

/**
 * Loader для загрузки постов rss-подписок.
 */
public class PostsLoader extends Loader {

    public static final String TAG = PostsLoader.class.getSimpleName();

    private final Subscription mSubscription;

    public PostsLoader(Context context, Subscription subscription) {
        super(context);
        this.mSubscription = subscription;
    }

    @Override
    protected void onStartLoading() {
        super.onStopLoading();
        forceLoad();
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        /** Пока идет чтение с сервера, мы считываем данные и БД. */
        new CacheAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        new NetworkAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * AsyncTask для получения постов из БД.
     */
    private class CacheAsyncTask extends AsyncTask<Void, Void, List<RssResponse.Post>> {
        @Override
        protected List<RssResponse.Post> doInBackground(Void... params) {
            Cursor cursor = getContext().getContentResolver().query(
                    RssContentProvider.POSTS_URI,
                    null,
                    PostEntry.COLUMN_CHANNEL_ID + " = ?",
                    new String[]{String.valueOf(mSubscription.getId())},
                    null
            );

            List<RssResponse.Post> posts = new ArrayList<>();
            if (cursor.moveToFirst()) {
                // Получаем индексы столбцов в курсоре для дальнейшего обращения по ним.
                int titleIndex = cursor.getColumnIndex(PostEntry.COLUMN_TITLE);
                int descriptionIndex = cursor.getColumnIndex(PostEntry.COLUMN_DESCRIPTION);
                do {
                    String postTitle = cursor.getString(titleIndex);
                    String postDescription = cursor.getString(descriptionIndex);
                    posts.add(new RssResponse.Post(postTitle, postDescription));
                } while (cursor.moveToNext());
            }
            cursor.close();

            return posts;
        }

        @Override
        protected void onPostExecute(List<RssResponse.Post> posts) {
            super.onPostExecute(posts);
            deliverResult(posts);
        }
    }

    /**
     * AsyncTask для получения постов с сервера.
     */
    private class NetworkAsyncTask extends AsyncTask<Void, Void, List<RssResponse.Post>> {
        @Override
        protected List<RssResponse.Post> doInBackground(Void... params) {

            RssApi rssService = RssApiFactory.createRssApiService();
            try {
                Response<RssResponse> response = rssService.getChannel(mSubscription.getUrl())
                        .execute();
                return (response.isSuccessful()) ? response.body().getPosts() : null;
            } catch (IOException e) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Проблемы с интернет-соединением", Toast.LENGTH_SHORT)
                            .show();
                }
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<RssResponse.Post> posts) {
            super.onPostExecute(posts);
            if (posts != null) {
                deliverResult(posts);
                new DatabaseInsertAsyncTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, posts);
            }

        }
    }

    /**
     * AsyncTask для добавления постов в БД.
     */
    private class DatabaseInsertAsyncTask extends AsyncTask<List<RssResponse.Post>, Void, Boolean> {

        @Override
        protected Boolean doInBackground(List<RssResponse.Post>... params) {
            ContentResolver resolver = getContext().getContentResolver();

            resolver.delete(
                    RssContentProvider.POSTS_URI,
                    PostEntry.COLUMN_CHANNEL_ID + " = ?",
                    new String[]{String.valueOf(mSubscription.getId())}
            );

            resolver.bulkInsert(
                    RssContentProvider.POSTS_URI,
                    convertPosts(params[0])
            );

            return true;
        }

        /**
         * Конвертировать массив объектов RssResponse.Post в массив объектов ContentValues.
         * @param posts массив объектов RssResponse.Post.
         * @return массив объектов ContentValues.
         */
        private ContentValues[] convertPosts(List<RssResponse.Post> posts) {
            ContentValues[] contentValuesArray = new ContentValues[posts.size()];
            for (int iPost = 0; iPost < posts.size(); iPost++) {
                ContentValues contentValues = new ContentValues();
                contentValues.put(PostEntry.COLUMN_CHANNEL_ID, mSubscription.getId());
                contentValues.put(PostEntry.COLUMN_TITLE, posts.get(iPost).getTitle());
                contentValues.put(PostEntry.COLUMN_DESCRIPTION, posts.get(iPost).getDescription());
                contentValuesArray[iPost] = contentValues;
            }

            return contentValuesArray;
        }
    }
}
