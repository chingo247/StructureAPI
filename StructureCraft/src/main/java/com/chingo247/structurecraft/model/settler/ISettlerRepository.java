/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.model.settler;

import com.chingo247.structurecraft.model.settler.Settler;
import java.util.UUID;

/**
 *
 * @author Chingo
 */
public interface ISettlerRepository {
    
    public Settler findByUUID(UUID uuid);
    
    public Settler findById(Long id);
    
}
