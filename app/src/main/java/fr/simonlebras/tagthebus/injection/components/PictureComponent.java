package fr.simonlebras.tagthebus.injection.components;

import dagger.Subcomponent;
import fr.simonlebras.tagthebus.injection.modules.PictureModule;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.presentation.pictures.PictureListFragment;
import fr.simonlebras.tagthebus.presentation.pictures.PictureListPresenter;

@Subcomponent(modules = {PictureModule.class})
@PerFragment
public interface PictureComponent {
    PictureListPresenter pictureListPresenter();

    void inject(PictureListFragment fragment);
}
