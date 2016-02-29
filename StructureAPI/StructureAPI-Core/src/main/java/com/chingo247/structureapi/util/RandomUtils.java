/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structureapi.util;

import java.util.Random;

/**
 *
 * @author Chingo
 */
public class RandomUtils {
    
    private static final Random r = new Random();
    
    public static int random(int min, int max, Random random) {
        return random.nextInt(max - min + 1) + min;
    }
    
    public static int random(int min, int max) {
        return random(min, max, r);
    }
    
}
