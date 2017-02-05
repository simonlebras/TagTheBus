package fr.simonlebras.tagthebus.injection.modules;

import android.content.Context;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.squareup.moshi.Moshi;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import fr.simonlebras.tagthebus.BuildConfig;
import fr.simonlebras.tagthebus.data.stations.list.network.JsonAdapterFactory;
import fr.simonlebras.tagthebus.data.stations.list.network.StationService;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;

@Module
public class ApplicationModule {
    private static final String CACHE_DIRECTORY = "HttpCache";
    private static final long CACHE_SIZE = 30 * 1024 * 1024;//30 MiB

    private static final int TIMEOUT = 10;//in seconds

    private static final String BASE_URL = "http://barcelonaapi.marcpous.com/";

    private final Context context;

    public ApplicationModule(Context context) {
        this.context = context.getApplicationContext();
    }

    @Provides
    @Singleton
    Context providesApplicationContext() {
        return context;
    }

    @Provides
    @Singleton
    Cache providesCache(Context context) {
        final File cacheDirectory = new File(context.getCacheDir(), CACHE_DIRECTORY);
        return new Cache(cacheDirectory, CACHE_SIZE);
    }

    @Provides
    @Singleton
    OkHttpClient providesOkHttpClient(Cache cache) {
        final OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(TIMEOUT, TimeUnit.SECONDS)
                .cache(cache);

        //add http logging in debug mode
        if (BuildConfig.DEBUG) {
            final HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(BODY);
            builder.addNetworkInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    @Provides
    @Singleton
    Retrofit providesRetrofit(OkHttpClient okHttpClient) {
        return new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(MoshiConverterFactory.create(new Moshi.Builder()
                        .add(JsonAdapterFactory.create())
                        .build()))
                .build();
    }

    @Provides
    @Singleton
    StationService providesStationService(Retrofit retrofit) {
        return retrofit.create(StationService.class);
    }
}
