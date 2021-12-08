package com.uidroid.uidroid.handler;

public interface IClickHandler {

    void subscribeAction(Integer id, IViewAction action);

    void executeActions(Integer id);

    void unsubscribeActions(Integer id);

}
