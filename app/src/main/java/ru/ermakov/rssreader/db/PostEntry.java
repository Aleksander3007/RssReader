package ru.ermakov.rssreader.db;

import android.provider.BaseColumns;

/**
 * Класс который определяет содержимое таблицы Post.
 */
public abstract class PostEntry implements BaseColumns {
    public static final String TABLE_NAME = "post";

    /** Заголовок поста. */
    public static final String COLUMN_TITLE = "title";
    /** Описание поста. */
    public static final String COLUMN_DESCRIPTION = "description";
    /** Id rss-канала, к которому относится пост. */
    public static final String COLUMN_CHANNEL_ID = "channel";
}
