/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.chingo247.structurecraft.util;

import com.chingo247.structureapi.util.Progress;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Chingo
 */
public class ProgressTest {
    
    public ProgressTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    /**
     * Test of getProgress method, of class Progress.
     */
    @Test
    public void testGetProgress() {
        Progress instance = new Progress(100, 0.0);
        double expResult = 0.0;
        double result = instance.getProgress();
        assertEquals(expResult, result, 0.0);
    }
    
    @Test
    public void testGetProgressZero() {
        Progress instance = new Progress(1, 0);
        double expResult = 0.0;
        double result = instance.getProgress();
        assertEquals(expResult, result, 0.0);
    }
    
    @Test
    public void testGetProgressFifty() {
        Progress instance = new Progress(50, 0);
        double expResult = 0.0;
        double result = instance.getProgress();
        assertEquals(expResult, result, 50);
    }
    
    @Test
    public void testGetProgressHundred() {
        Progress instance = new Progress(1, 1);
        double expResult = 0.0;
        double result = instance.getProgress();
        assertEquals(expResult, result, 100);
    }
    
}
