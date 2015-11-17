/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event;

import com.google.common.collect.Lists;
import com.google.common.eventbus.EventBus;
import java.util.List;

/**
 *
 * @author Chingo
 */
public class EventDispatcher implements IEventDispatcher {

    private List<EventBus> eventBusses;

    public EventDispatcher() {
        this.eventBusses = Lists.newArrayList();
    }
    
    @Override
    public void register(EventBus eventBus) {
        this.eventBusses.add(eventBus);
    }

    @Override
    public void post(Object event) {
        for(EventBus e : eventBusses) {
            e.post(event);
        }
    }

}
