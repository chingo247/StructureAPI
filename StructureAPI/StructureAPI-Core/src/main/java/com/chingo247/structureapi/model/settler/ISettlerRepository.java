/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.model.settler;

import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface ISettlerRepository {
    
    public SettlerNode findByUUID(UUID uuid);
    
    public SettlerNode findById(Long id);
    
}
