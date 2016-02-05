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
import com.chingo247.menuapi.menu.slots.CategorySlot;
import com.chingo247.menuapi.menu.slots.SlotFactory;
import com.chingo247.settlercraft.core.exception.SettlerCraftException;
import com.google.common.base.Preconditions;
import java.io.File;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;

/**
 *
 * @author Chingo
 */
public class StructurePlanMenuReader {
    
    public CategoryMenu read(File file) throws DocumentException, SettlerCraftException {
        Preconditions.checkArgument(file.exists(), "File '" + file.getAbsolutePath() + "' does not exist!");

        Document d = new SAXReader().read(file);
        
        CategoryMenu menu = new DefaultCategoryMenu("Buy & Build");
        
        List<Node> rows = d.selectNodes("Menu/SlotRow");
        if (rows.size() > 2) {
            throw new SettlerCraftException("Max rows is 2 for menu.xml");
        }
        
        boolean hasRow2 = false;

        for (int row = 0; row < rows.size(); row++) {
            Node rowNode = rows.get(row);
            List<Node> slotNodes = rowNode.selectNodes("Slot");
            // Slot #1 is reserved for all category
            if (row == 0 && slotNodes.size() > 8) {
                throw new SettlerCraftException(" 'SlotRow#1' has max 8 slots to customize");
            } else if (slotNodes.size() > 9) {
                throw new SettlerCraftException(" 'SlotRow#" + (row + 2) + "' has max 9 slots to customize");
            }
            int count = 0;

            for (Node categorySlotNode : slotNodes) {

                if (!categorySlotNode.hasContent()) {
                    count++;
                    continue;
                }

                Node mId = categorySlotNode.selectSingleNode("MaterialID");
                Node cat = categorySlotNode.selectSingleNode("Category");
//                Node ali = categorySlotNode.selectSingleNode("Aliases");

                if (mId == null) {
                    throw new SettlerCraftException("Missing 'MaterialID' element in 'SlotRow#" + (row + 1) + "' 'Slot#" + (count + 1) + "'");
                }
                if (cat == null) {
                    throw new SettlerCraftException("Missing 'Category' element in 'SlotRow#" + (row + 1) + "' 'Slot#" + (count + 1) + "'");
                }

                int id;
                try {
                    id = Integer.parseInt(mId.getText());
                } catch (NumberFormatException nfe) {
                    throw new SettlerCraftException("Invalid number for 'MaterialID' element in 'SlotRow#" + (row + 1) + "' 'Slot#" + (count + 1) + "'");
                }
                
                Node catNameNode = cat.selectSingleNode("Name");
                if(catNameNode == null) {
                    throw new SettlerCraftException("Missing 'Name' element in 'SlotRow#" + (row + 1) + "' 'Slot#" + (count + 1) + "'");
                }
                
                String category = catNameNode.getText();
                if (category.isEmpty()) {
                    Element catEl = (Element) cat;
                    category = catEl.attributeValue("value");
                }
                if (category.trim().isEmpty()) {
                    throw new SettlerCraftException("Empty 'Category' element in 'SlotRow#" + (row + 1) + "' and 'Slot#" + (count + 1) + "'");
                }
                category = category.replaceAll(" AND ", "&");

                Node synonymsNode = cat.selectSingleNode("Synonyms");
                
                // Set aliases
                String[] synonyms;
                if (synonymsNode == null) {
                    synonyms = new String[0];
                } else {
                    List<Node> synonymNodes = synonymsNode.selectNodes("Synonym");
                    synonyms = new String[synonymNodes.size()];
                    for (int j = 0; j < synonymNodes.size(); j++) {
                        String synonym = synonymNodes.get(j).getText();

                        if (synonym.isEmpty()) {
                            Element synoEl = (Element) cat;
                            synonym = synoEl.attributeValue("value");
                        }
                        if (synonym.trim().isEmpty()) {
                            throw new SettlerCraftException("Empty 'Synonym' element in  'SlotRow#" + (row + 1) + "' and 'Slot#" + (count + 1) + "' and 'Synonym#" + (j + 1) + "'");
                        }

                        synonyms[j] = synonymNodes.get(j).getText();
                    }
                }
                int slot = count;
                if (row == 0) {
                    slot += 1; // slot 0 is reserved...
                } else {

                    hasRow2 = true;
                }

                CategorySlot categorySlot = SlotFactory.getInstance().createCategorySlot(category, id);
                categorySlot.addSynonyms(synonyms);
                menu.setCategorySlot((row * 9) + slot, categorySlot);

                count++;
            }
            // fill remaining
            if (count < 8 && row == 0) {
                for (int i = count; i < 8; i++) {
                    menu.setLocked(i);
                }

            } else if (row > 0 && count < 9) {
                for (int i = count; i < 9; i++) {
                    menu.setLocked((row * 9) + i);
                }
            }
        }

        if (hasRow2) {
            menu.setLocked(19, 20, 21, 22, 23, 24, 25);
            menu.setActionSlot(18, "Previous", 173); // block of coal
            menu.setActionSlot(26, "Next", 173);
        } else {
            menu.setLocked(10, 11, 12, 13, 14, 15, 16);
            menu.setActionSlot(9, "Previous",173);
            menu.setActionSlot(17, "Next", 173);
        }
        return menu;
    }
    
}
