/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import java.util.List;

/**
 *
 * @author Chingo
 */
public class EventDispatcher implements IEventDispatcher {

    private EventBus eventBus;
    private AsyncEventBus asyncEventBus;

    public EventDispatcher(EventBus eventBus, AsyncEventBus asyncEventBus) {
        this.eventBus = eventBus;
        this.asyncEventBus = asyncEventBus;
    }
    
    @Override
    public void post(Object event) {
        this.eventBus.post(event);
        this.asyncEventBus.post(event);
    }

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public EventBus getAsyncEventBus() {
        return asyncEventBus;
    }

}
