/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.util;

import com.google.common.base.Preconditions;
import java.text.DecimalFormat;

/**
 *
 * @author Chingo
 */
public class Progress implements IProgressable{
    
//    private static final DecimalFormat ONE_DECIMAL = new DecimalFormat("#.#");
    
    private double total;
    private double count;
    private boolean reportable = true;

    public Progress(double total, double count) {
        this.total = total;
        this.count = count;
    }

    @Override
    public double getProgress() {
        if(count == 0) {
            return 0;
        }
        return (double) ((count / total) * 100); 
    }
    
}
