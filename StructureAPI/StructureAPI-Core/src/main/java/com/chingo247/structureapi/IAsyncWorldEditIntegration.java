/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi;

import java.util.UUID;
import org.primesoft.asyncworldedit.api.IAsyncWorldEdit;

/**
 *
 * @author Chingo
 */
public interface IAsyncWorldEditIntegration {
    
    IAsyncWorldEdit getAsyncWorldEdit();
    
    boolean isQueueLocked(UUID player);
    
}
