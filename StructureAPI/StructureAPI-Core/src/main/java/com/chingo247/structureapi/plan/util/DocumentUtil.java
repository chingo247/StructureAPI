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
package com.chingo247.structureapi.plan.util;

import fiber.core.impl.BuildContextImpl;
import fiber.core.impl.xml.ModelReader;
import java.io.File;
import java.io.IOException;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.xml.sax.SAXException;

/**
 * DocumentUtil for reading XML files
 * @author Chingo
 */
public class DocumentUtil {

    /**
     * Reads an xml file parses it to a dom4j Document. All elements in this document extend from LocatedElement and therefore
     * know the exact linenumber they were on.
     * @param xmlFile
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws DocumentException 
     */
    public static Document read(File xmlFile) throws IOException, SAXException, DocumentException {
        ModelReader reader = ModelReader.getNonValidatingInstance(new BuildContextImpl());
        return reader.read(xmlFile);
    }

}
