package fr.simonlebras.tagthebus.injection.components;

import dagger.Subcomponent;
import fr.simonlebras.tagthebus.injection.modules.StationModule;
import fr.simonlebras.tagthebus.injection.scopes.PerFragment;
import fr.simonlebras.tagthebus.presentation.stations.list.StationListFragment;
import fr.simonlebras.tagthebus.presentation.stations.list.StationListPresenter;
import fr.simonlebras.tagthebus.presentation.stations.map.StationMapFragment;
import fr.simonlebras.tagthebus.presentation.stations.map.StationMapPresenter;

@Subcomponent(modules = {StationModule.class})
@PerFragment
public interface StationComponent extends BaseComponent {
    StationMapPresenter stationMapPresenter();

    StationListPresenter stationListPresenter();

    void inject(StationMapFragment fragment);

    void inject(StationListFragment fragment);
}
