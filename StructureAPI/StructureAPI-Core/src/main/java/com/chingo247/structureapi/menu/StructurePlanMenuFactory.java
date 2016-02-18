/*
 * Copyright (C) 2015 Chingo
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package com.chingo247.structureapi.menu;

import com.chingo247.menuapi.menu.CategoryMenu;
import com.chingo247.menuapi.menu.DefaultCategoryMenu;
import com.chingo247.settlercraft.core.SettlerCraft;
import com.chingo247.structureapi.placement.IPlacement;
import com.chingo247.xplatform.core.APlatform;
import com.chingo247.structureapi.plan.IStructurePlan;
import com.google.common.base.Preconditions;
import com.sk89q.worldedit.regions.CuboidRegion;

/**
 *
 * @author Chingo
 */
public class StructurePlanMenuFactory {

    public static final String PLAN_MENU_TAG = "planMenu";
    private final CategoryMenu template;
    private final APlatform platform;

    public StructurePlanMenuFactory(APlatform platform, CategoryMenu menu) {
        Preconditions.checkNotNull(platform);
        Preconditions.checkNotNull(menu);
        this.template = menu;
        this.template.setTag(PLAN_MENU_TAG);
        this.platform = platform;
    }
    
    public void clearAll() {
        template.clearAll();
    }

    public void load(IStructurePlan plan) {
        IPlacement placement = plan.getPlacement();
       
        CuboidRegion region = placement.getCuboidRegion();
        
        int width = region.getMaximumPoint().getBlockX();
        int height = region.getMaximumPoint().getBlockY();
        int length = region.getMaximumPoint().getBlockZ();
        
        String id = plan.getId();
        String name = plan.getName();
        String category = plan.getCategory();
        String description = plan.getDescription();
        
        double price = plan.getPrice();
        
        
        template.addItem(new StructurePlanItem(platform, id, name, category, price, width, height, length, description));
    }
    
    

    public CategoryMenu createPlanMenu() {
        CategoryMenu categoryMenu = new DefaultCategoryMenu(SettlerCraft.getInstance().getEconomyProvider(),template.getTitle(), template.getView(), template.getAllItems());
        template.setTag(PLAN_MENU_TAG);
        return categoryMenu;
    }
    
    

}
