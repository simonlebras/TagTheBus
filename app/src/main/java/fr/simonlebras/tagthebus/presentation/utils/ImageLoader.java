package fr.simonlebras.tagthebus.presentation.utils;

import android.content.Context;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.GlideModule;

import java.io.InputStream;

import fr.simonlebras.tagthebus.TagTheBusApplication;
import okhttp3.OkHttpClient;


public class ImageLoader implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
        final OkHttpClient okHttpClient = ((TagTheBusApplication) context.getApplicationContext()).getApplicationComponent()
                .okHttpClient();
        glide.register(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(okHttpClient));
    }
}
