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
    private boolean protectConstructionZones = false;
    private boolean allowStructures = false;
    private boolean restrictedToZones = false;
    private String version;
    
    private final File f;
    
    private ConfigProvider (File f) {
        this.f = f;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
    
    public void setProtectConstructionZones(boolean protectConstructionZones) {
        this.protectConstructionZones = protectConstructionZones;
    }

    public boolean isProtectConstructionZones() {
        return protectConstructionZones;
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
    
    public static ConfigProvider load(File f) throws IOException {
        if(!f.exists()) {
            f.createNewFile();
        }
        
        YAMLProcessor yamlp = new YAMLProcessor(f, true, YAMLFormat.EXTENDED);
        yamlp.load();
        
        ConfigProvider config = new ConfigProvider(f);
        config.setVersion(yamlp.getString("version", null));
        config.setAllowsSubstructures(yamlp.getBoolean("structures.allow-substructures", true));
        config.setRestrictedToZones(yamlp.getBoolean("structures.restricted-to-zones", false));
        config.setAllowStructures(yamlp.getBoolean("structures.allow-structures", true));
        config.setUseHolograms(yamlp.getBoolean("structures.use-holograms", true));
        config.setProtectStructures(yamlp.getBoolean("structures.protected", true));
        config.setProtectConstructionZones(yamlp.getBoolean("constructionzones.protected", true));
        config.setMenuEnabled(yamlp.getBoolean("menus.planmenu-enabled", true));
        config.setShopEnabled(yamlp.getBoolean("menus.planshop-enabled", true));
        return config;
        
    }
    
}
