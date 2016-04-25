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
import com.chingo247.structureapi.exeption.StructureAPIException;
import java.io.File;
import java.io.IOException;
import org.neo4j.register.Register.Int;

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
    private boolean demolishIsRollback = false;
    
    private String version;
    
    private final File f;
    
    private ConfigProvider (File f) {
        this.f = f;
    }


    
    public void setVersion(String version) {
        this.version = version;
    }

    public void setDemolishIsRollback(boolean demolishIsRollback) {
        this.demolishIsRollback = demolishIsRollback;
    }

    public boolean isDemolishIsRollback() {
        return demolishIsRollback;
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
    
    public static ConfigProvider load(File f) throws IOException, StructureAPIException {
        if(!f.exists()) {
            f.createNewFile();
        }
        
        YAMLProcessor yamlp = new YAMLProcessor(f, true, YAMLFormat.EXTENDED);
        yamlp.load();
        
        ConfigProvider config = new ConfigProvider(f);
        config.setVersion(yamlp.getString("version", null));
        config.setAllowsSubstructures(getValue(yamlp, "structures.allow-substructures", Boolean.class));
//        config.setRestrictedToZones(getValue(yamlp, "structures.restricted-to-zones", Boolean.class));
        config.setUseHolograms(getValue(yamlp, "structures.use-holograms", Boolean.class));
        config.setProtectStructures(getValue(yamlp, "structures.protected", Boolean.class));
//        config.setProtectConstructionZones(getValue(yamlp, "constructionzones.protected", Boolean.class));
        config.setMenuEnabled(getValue(yamlp, "menus.planmenu-enabled", Boolean.class));
        config.setShopEnabled(getValue(yamlp, "menus.planshop-enabled", Boolean.class));
        config.setDemolishIsRollback(getValue(yamlp, "structures.demolish-is-rollback", Boolean.class));
        
        return config;
        
    }
    
    public static String getVersion(File config) throws IOException {
        YAMLProcessor yamlp = new YAMLProcessor(config, true, YAMLFormat.EXTENDED);
        yamlp.load();
        return yamlp.getString("version", null);
    }
    
    private static <T> T getValue(YAMLProcessor processor, String path, Class<T> expectedValue) throws StructureAPIException {
        
        Object value = processor.getProperty(path);
        if(value != null) {
            try {
                return expectedValue.cast(value);
            } catch(ClassCastException ex) {
                
                if(expectedValue.getClass().isAssignableFrom(Boolean.class)) {
                   throw new StructureAPIException("Invalid value in config path '"+path+"': Expected 'true' or 'false' "+path+"'");
                }
                if(expectedValue.getClass().isAssignableFrom(Integer.class)) {
                   throw new StructureAPIException("Invalid value in config path '"+path+"': Expected a round number' "+path+"'");
                }
                if(expectedValue.getClass().isAssignableFrom(Double.class)) {
                   throw new StructureAPIException("Invalid value in config path: Expected a round number at '"+path+"'");
                }
                throw new StructureAPIException("Invalid value in config at: '"+path+"'");
                
            }
        } else {
            throw new StructureAPIException("Missing '" + path + "' in config!");
        }
        
        
    }
    
}
