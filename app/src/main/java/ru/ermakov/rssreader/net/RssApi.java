package ru.ermakov.rssreader.net;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Интерфейс, объявляющий методы для работы с RSS.
 */
public interface RssApi {
    @GET
    Call<RssResponse> getChannel(@Url String url);
}
