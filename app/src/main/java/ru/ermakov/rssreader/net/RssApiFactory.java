package ru.ermakov.rssreader.net;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.simplexml.SimpleXmlConverterFactory;

/**
 * Класс отвечает за работу по сети через Retrofit.
 */
public class RssApiFactory {

    public static final String BASE_URL = "http://base.url";

    private static final int CONNECT_TIMEOUT = 15; // sec.
    private static final int WRITE_TIMEOUT = 30; // sec.
    private static final int READ_TIMEOUT = 30; // sec.

    private static OkHttpClient.Builder sHttpClientBuilder = new OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS);

    private static Retrofit.Builder sRetrofitBuilder = new Retrofit.Builder()
            .addConverterFactory(SimpleXmlConverterFactory.createNonStrict());

    public static RssApi createRssApiService() {
        return createRetrofit().create(RssApi.class);
    }

    private static Retrofit createRetrofit() {
        return sRetrofitBuilder
                .baseUrl(BASE_URL)
                .client(sHttpClientBuilder.build())
                .build();
    }
}
