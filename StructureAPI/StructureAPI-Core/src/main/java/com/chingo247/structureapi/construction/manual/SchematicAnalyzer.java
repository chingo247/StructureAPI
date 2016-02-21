/*
 * Copyright (C) 2016 Chingo
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
package com.chingo247.structureapi.construction.manual;

import com.chingo247.settlercraft.core.util.yaml.YAMLFormat;
import com.chingo247.settlercraft.core.util.yaml.YAMLNode;
import com.chingo247.settlercraft.core.util.yaml.YAMLProcessor;
import com.chingo247.structureapi.blockstore.NBTUtils;
import static com.chingo247.structureapi.blockstore.NBTUtils.getChildTag;
import static com.google.common.base.CharMatcher.is;
import com.google.common.collect.Maps;
import com.sk89q.jnbt.ByteArrayTag;
import com.sk89q.jnbt.NBTInputStream;
import com.sk89q.jnbt.NamedTag;
import com.sk89q.jnbt.ShortTag;
import com.sk89q.jnbt.Tag;
import com.sk89q.worldedit.blocks.BlockID;
import com.sk89q.worldedit.blocks.BlockType;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author Chingo
 */
public class SchematicAnalyzer {

    public static Map<String, Integer> count(File schematic) throws IOException {
        URL url = schematic.toURI().toURL();
        ReadableByteChannel rbc = Channels.newChannel(url.openStream());
        final InputStream is = Channels.newInputStream(rbc);
        return count(is);
    }

    public static Map<String, Integer> count(InputStream schematicStream) throws IOException {
        long start = System.currentTimeMillis();
        NBTInputStream stream = new NBTInputStream(new GZIPInputStream(schematicStream));
        NamedTag tag = stream.readNamedTag();
        stream.close();
        
        Map<String,Tag> schematic = (Map) tag.getTag().getValue();
        
       
        short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
        short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
        short height = getChildTag(schematic, "Height", ShortTag.class).getValue();
        
        byte[] ids = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
        byte[] data = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
        byte[] addId = new byte[0];

        if (schematic.containsKey("AddBlocks")) {
            addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
        }

        Map<String,Integer> countMap = Maps.newHashMap();
        
        int count = 0;
        for (int index = 0; index < ids.length; index++) {
            short bd = data[index];
            short id;
            if ((index >> 1) >= addId.length) { // No corresponding AddBlocks index
                id = (short) (ids[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    id = (short) (((addId[index >> 1] & 0x0F) << 8) + (ids[index] & 0xFF));
                } else {
                   id = (short) (((addId[index >> 1] & 0xF0) << 4) + (ids[index] & 0xFF));
                }
            }
            if(id == 0) {
                continue;
            }
            count++;
            addCount(countMap, id, bd);
        }
        
        countMap.put("0-0", (width * height * length) - count);
        
        long stop = System.currentTimeMillis(); 
        
        System.out.println("Finished in " + (stop - start) + "ms");
        return countMap;
    }
    
    private static void addCount(Map<String,Integer> map, short id, short data) {
        String key = String.valueOf(id) + "-" + String.valueOf(data);
        if (map.containsKey(key)) {
            int i = map.get(key);
            map.put(key, i + 1);
        } else {
            map.put(key, 1);
        }
    }
    
    public static void writeToFile(File file, Map<String,Integer> count) {
        writeToFile(file, count, false);
    }
    
    public static void writeToFile(File file, Map<String,Integer> count, boolean sortByCount) {
        YAMLProcessor processor = new YAMLProcessor(file, false, YAMLFormat.COMPACT);
        
        List<Map<String,Object>> list = new ArrayList<>();
        
        for(Entry<String,Integer> entry : count.entrySet()) {
            String[] arr = entry.getKey().split("-");
            
            int id = Integer.parseInt(arr[0]);
            int data = Integer.parseInt(arr[1]);
            
            Map<String,Object> n = new HashMap<>();
            n.put("id", id);
            n.put("data",  data);
            n.put("count", entry.getValue());
            list.add(n);
        }
        
        if(sortByCount) {
            Comparator<Map<String,Object>> comp = new Comparator<Map<String, Object>>() {

                @Override
                public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                    return ((Integer)o2.get("count")).compareTo((Integer)(o1.get("count")));
                }
            };
            Collections.sort(list, comp);
        }
        processor.setProperty("materials", list);
        processor.save();
    }
    
    public static void main(String[] args) {
        File plans = new File("F:\\GAMES\\MineCraftServers\\bukkit\\1.8\\Bukkit 1.8-SettlerCraft-2.2.0\\plugins\\SettlerCraft-StructureAPI\\plans");
        File schematic = new File(plans, "Shiganshina.schematic");
        File output = new File("count.yml");
        try {
            writeToFile(output, count(schematic), true);
        } catch (IOException ex) {
            Logger.getLogger(SchematicAnalyzer.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

}
