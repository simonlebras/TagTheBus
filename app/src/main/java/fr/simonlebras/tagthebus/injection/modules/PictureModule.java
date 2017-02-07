package fr.simonlebras.tagthebus.injection.modules;

import dagger.Module;
import dagger.Provides;
import fr.simonlebras.tagthebus.data.pictures.PictureRepository;
import fr.simonlebras.tagthebus.data.pictures.PictureRepositoryImpl;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;

@Module
public class PictureModule {
    @Provides
    @PerFragment
    PictureRepository providesPictureRepository(PictureRepositoryImpl repository) {
        return repository;
    }
}
