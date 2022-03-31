package com.uidroid.uidroid.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("unused")
public class ClickHandler implements IClickHandler {

    private final Map<Integer, List<IViewAction>> actions =
            new ConcurrentHashMap<>(new LinkedHashMap<>());

    public ClickHandler() {

    }

    @Override
    public synchronized void subscribeAction(Integer id, IViewAction action) {
        if (id != null && action != null) {
            actions.computeIfAbsent(id, k -> new ArrayList<>());

            Objects.requireNonNull(actions.get(id)).add(action);
        }
    }

    @Override
    public synchronized void executeActions(Integer id) {
        if (id == null) {
            return;
        }

        List<IViewAction> actionsForId = actions.get(id);

        if (actionsForId != null) {
            for (IViewAction action: actionsForId) {
                action.onClick();
            }
        }
    }

    @Override
    public synchronized void unsubscribeActions(Integer id) {
        actions.remove(id);
    }

}
