/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.bukkit;

import com.chingo247.structureapi.util.ConfigUpdater;
import java.io.File;
import org.bukkit.Bukkit;

/**
 *
 * @author Chingo
 */
public class BKStructureAPIConfigUpdater extends ConfigUpdater {

    public BKStructureAPIConfigUpdater(File currentConfig, File configToCheck) {
        super(currentConfig, configToCheck);
    }

    @Override
    public void onUpdate(String reason) {
        Bukkit.getConsoleSender().sendMessage("Updating 'StructureAPI' config, reason: " + reason);
    }
    
}
