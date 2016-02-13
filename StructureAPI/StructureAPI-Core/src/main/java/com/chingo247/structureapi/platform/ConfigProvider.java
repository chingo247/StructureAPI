/*
 * Copyright (C) 2015 ching
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
package com.chingo247.structureapi.platform;

import com.chingo247.settlercraft.core.util.yaml.YAMLFormat;
import com.chingo247.settlercraft.core.util.yaml.YAMLProcessor;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author ching
 */
public class ConfigProvider {
    
    private boolean menuEnabled = false;
    private boolean shopEnabled = false;
    private boolean useHolograms = false;
    private boolean allowsSubstructures = false;
    private boolean protectStructures = false;
    private boolean allowStructures = false;
    private boolean restrictedToZones = false;
    
    private final File f;
    
    private ConfigProvider (File f) {
        this.f = f;
    }

    public void setRestrictedToZones(boolean restrictedToZones) {
        this.restrictedToZones = restrictedToZones;
    }

    public boolean isRestrictedToZones() {
        return restrictedToZones;
    }
    
    public void setAllowStructures(boolean allowStructures) {
        this.allowStructures = allowStructures;
    }

    public boolean allowsStructures() {
        return allowStructures;
    }
    
    public boolean isMenuEnabled() {
        return menuEnabled;
    }

    public void setMenuEnabled(boolean menuEnabled) {
        this.menuEnabled = menuEnabled;
    }

    public boolean isShopEnabled() {
        return shopEnabled;
    }

    public void setShopEnabled(boolean shopEnabled) {
        this.shopEnabled = shopEnabled;
    }

    public boolean isUseHolograms() {
        return useHolograms;
    }

    public void setUseHolograms(boolean useHolograms) {
        this.useHolograms = useHolograms;
    }

    public boolean allowsSubstructures() {
        return allowsSubstructures;
    }

    public void setAllowsSubstructures(boolean allowsSubstructures) {
        this.allowsSubstructures = allowsSubstructures;
    }

    public boolean isProtectStructures() {
        return protectStructures;
    }

    public void setProtectStructures(boolean protectStructures) {
        this.protectStructures = protectStructures;
    }
    
    public void save() {
        save(new YAMLProcessor(f, true));
    }
    
    private void save(YAMLProcessor yamlp) {
        yamlp.setProperty("structures.allow-substructures", allowsSubstructures);
        yamlp.setProperty("structures.allow-structures", allowStructures);
        yamlp.setProperty("structures.restricted-to-zones", restrictedToZones);
        yamlp.setProperty("structures.use-holograms", useHolograms);
        yamlp.setProperty("structures.protected", protectStructures);
        yamlp.setProperty("menus.planmenu-enabled", menuEnabled);
        yamlp.setProperty("menus.planshop-enabled", shopEnabled);
        yamlp.save();
    }
    
    
    
    public static ConfigProvider loadOrCreateDefault(File f) throws IOException {
        if(!f.exists()) {
            f.createNewFile();
        }
        
        YAMLProcessor yamlp = new YAMLProcessor(f, true, YAMLFormat.EXTENDED);
        yamlp.load();
        
        ConfigProvider config = new ConfigProvider(f);
        config.setAllowsSubstructures(yamlp.getBoolean("structures.allow-substructures", true));
        config.setRestrictedToZones(yamlp.getBoolean("structures.restricted-to-zones", false));
        config.setAllowStructures(yamlp.getBoolean("structures.allow-structures", true));
        config.setUseHolograms(yamlp.getBoolean("structures.use-holograms", true));
        config.setProtectStructures(yamlp.getBoolean("structures.protected", true));
        config.setMenuEnabled(yamlp.getBoolean("menus.planmenu-enabled", true));
        config.setShopEnabled(yamlp.getBoolean("menus.planshop-enabled", true));
        yamlp.setComment("structures.allow-substructures", "Determines if placing of substructures (placing structures within structures) is allowed");
        yamlp.setComment("structures.planmenmu-enabled", "Determines whether the StructureAPI planmenu should be enabled", "Players can SELECT plans in this menu for FREE");
        yamlp.setComment("structures.protected", "Determines whether structures should be protected (Requires WorldGuard)");
        yamlp.setComment("structures.use-holograms", "Determines whether structures should place holograms (Requires HolographicDisplays)");
        yamlp.setComment("structures.allow-structures", "Determines if placing structures is allowed");
        yamlp.setComment("structures.restricted-to-zones", "Determines if placing structures is only allowed within zones");
        yamlp.setComment("structures.planmenmu-enabled", "Determines whether the StructureAPI planshop should be enabled", "Players can BUY plans from the menu (Requires Vault + a Vault supported Economy plugin)");
        config.save(yamlp);
        
        return config;
        
    }
    
}
