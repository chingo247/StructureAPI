/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.worldguard.protection;

/**
 *
 * @author Chingo
 */
public interface ICallback {
    
    void onComplete();
    
    void onFail(Exception ex);
    
}
