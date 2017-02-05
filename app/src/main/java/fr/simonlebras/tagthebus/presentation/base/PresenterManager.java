package fr.simonlebras.tagthebus.presentation.base;

import android.support.v4.util.ArrayMap;

import java.util.UUID;

public class PresenterManager {
    private final ArrayMap<UUID, BasePresenter<? extends BaseView>> cache = new ArrayMap<>();

    public BasePresenter<? extends BaseView> get(UUID uuid) {
        return cache.get(uuid);
    }

    public void put(UUID uuid, BasePresenter<? extends BaseView> presenter) {
        cache.put(uuid, presenter);
    }

    void remove(UUID uuid) {
        cache.remove(uuid);
    }
}
