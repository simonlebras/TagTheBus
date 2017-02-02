package fr.simonlebras.tagthebus;

import android.app.Application;
import android.os.Build;
import android.os.StrictMode;

import com.squareup.leakcanary.LeakCanary;

import timber.log.Timber;


public final class TagTheBusApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //setup LeakCanary
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);

        if (BuildConfig.DEBUG) {
            //setup Timber
            Timber.plant(new Timber.DebugTree());

            //setup StrictMode
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyLog()
                    .build());

            final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder()
                    .detectActivityLeaks()
                    .detectLeakedClosableObjects()
                    .penaltyLog();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                builder.detectFileUriExposure();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                builder.detectCleartextNetwork();
            }
            StrictMode.setVmPolicy(builder.build());
        }
    }
}
