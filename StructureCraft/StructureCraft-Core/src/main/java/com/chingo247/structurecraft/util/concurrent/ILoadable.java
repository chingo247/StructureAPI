/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.util.concurrent;

/**
 *
 * @author Chingo
 */
public interface ILoadable {
    /**
     * Start the loading.
     * @throws Exception on any error
     */
    void load() throws Exception;

}
