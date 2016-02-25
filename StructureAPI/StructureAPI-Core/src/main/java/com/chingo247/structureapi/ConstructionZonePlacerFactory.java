///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package com.chingo247.structureapi;
//
//import com.chingo247.structureapi.placing.constructionzone.ConstructionZonePlacer;
//import com.chingo247.xplatform.core.APlatform;
//import com.chingo247.xplatform.core.IWorld;
//
///**
// *
// * @author Chingo
// */
//class ConstructionZonePlacerFactory  {
//    
//    private StructureAPI structureAPI;
//
//    ConstructionZonePlacerFactory(StructureAPI structureAPI) {
//        this.structureAPI = structureAPI;
//    }
//    
//    public ConstructionZonePlacer createPlacer(String worldName) {
//        APlatform platform = structureAPI.getPlatform();
//        IWorld world = platform.getServer().getWorld(worldName);
//        if(world == null) {
//            throw new IllegalArgumentException("World with name '" + worldName + "' does not exist");
//        }
//        return new ConstructionZonePlacer(world);
//    }
//    
//}
