/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.platform;

import com.chingo247.xplatform.core.APlatform;
import com.chingo247.xplatform.core.IPlugin;
import com.chingo247.xplatform.core.IScheduler;

/**
 *
 * @author Chingo
 */
public interface IStructureAPIPlugin extends IPlugin {
    
    IScheduler getScheduler();
    
    APlatform getPlatform();
    
}
