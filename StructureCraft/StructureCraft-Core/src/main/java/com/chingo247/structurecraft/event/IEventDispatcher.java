/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.event;

import com.google.common.eventbus.EventBus;

/**
 * Sends the event to all EventManagers/Eventbusses
 * @author Chingo
 */
public interface IEventDispatcher {
    
    void register(EventBus eventBus);
    
    void post(Object event);
    
}
