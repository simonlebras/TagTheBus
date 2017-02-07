package fr.simonlebras.tagthebus.injection.components;

import javax.inject.Singleton;

import dagger.Component;
import fr.simonlebras.tagthebus.injection.modules.ApplicationModule;
import fr.simonlebras.tagthebus.injection.modules.PictureModule;
import fr.simonlebras.tagthebus.injection.modules.StationModule;
import fr.simonlebras.tagthebus.presentation.pictures.PictureCreationActivity;
import fr.simonlebras.tagthebus.presentation.pictures.PictureFullscreenActivity;
import okhttp3.OkHttpClient;

@Component(modules = {ApplicationModule.class})
@Singleton
public interface ApplicationComponent extends BaseComponent {
    OkHttpClient okHttpClient();

    StationComponent plus(StationModule module);

    PictureComponent plus(PictureModule module);

    void inject(PictureCreationActivity activity);

    void inject(PictureFullscreenActivity activity);
}
