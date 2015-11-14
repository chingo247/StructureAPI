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
package com.chingo247.structurecraft.plan.io.document;

import com.chingo247.structurecraft.plan.io.exception.ElementValueException;
import com.google.common.base.Preconditions;
import fiber.core.impl.xml.located.LocatedElement;
import java.io.File;
import org.apache.commons.lang.math.NumberUtils;
import org.dom4j.Element;
import org.dom4j.Node;

/**
 * An element which knows about it's file and the line numbers
 * @author Chingo
 */
public class LineElement {

    protected final LocatedElement le;
    private final File file;

    LineElement(File file, Element element) {
        Preconditions.checkNotNull(file);
        Preconditions.checkArgument(file.exists());
        if (!(element instanceof LocatedElement)) {
            throw new IllegalArgumentException("Element must be instance of " + LocatedElement.class);
        }
        this.le = (LocatedElement) element;
        this.file = file;
    }

    public Element getElement() {
        return le.createCopy();
    }

    public File getFile() {
        return file;
    }

    public String getElementName() {
        return le.getName();
    }

    public String getTextValue() {
        return le.getText();
    }

    public double getDoubleValue() throws ElementValueException {
        String error = "Value of element <" + le.getName() + "> on line " + le.getLine() + " was not a number";
        if (!hasText()) {
            throw new ElementValueException(error + ", but empty");
        }
        try {
            return Double.parseDouble(getTextValue());
        } catch (NumberFormatException nfe) {
            throw new ElementValueException(error);
        }
    }

    public boolean getBooleanValue() throws ElementValueException {
        String value = getTextValue().toLowerCase().trim();
        if (value.isEmpty() || (!value.equals("false") && !value.equals("true"))) {
                throw new ElementValueException("Error in element <" + le.getName() + "> ' of " + file.getAbsolutePath() + "' on line " + le.getLine() + ": "
                        + "value should be either of 'true' or 'false'");
           
        }
        return Boolean.parseBoolean(value);
    }

    public int getIntValue() throws ElementValueException {
        double d = getDoubleValue();
        return (int) Math.round(d);
    }

    public int getLine() {
        return le.getLine();
    }

    public boolean hasNumberValue() {
        return NumberUtils.isNumber(le.getText());
    }

    public boolean hasText() {
        return le.getText() != null && !le.getText().trim().isEmpty();
    }

    public boolean hasElement(String xPath) {
        return le.selectSingleNode(xPath) != null;
    }

    public void checkHasNumber() throws ElementValueException {
        if (!hasNumberValue()) {
            throw new ElementValueException("Value of element <" + le.getName() + "> on line " + le.getLine() + " in '" + file.getAbsolutePath() + "'" + " is not a number");
        }
    }

    public void checkNotNull(String xpath) throws ElementValueException {
        if (!hasElement(xpath)) {
            String element;
            if (xpath.contains("/")) {
                element = xpath.substring(xpath.lastIndexOf("/"));
            } else {
                element = xpath;
            }
            throw new ElementValueException("Expected element <" + element + "> within <" + le.getName() + "> on line " + le.getLine() + " in '" + file.getAbsolutePath() + "'");
        }
    }

    public void checkNotEmpty() throws ElementValueException {
        String text = le.getText().trim();
        if (text.isEmpty()) {
            throw new ElementValueException("Element <" + le.getName() + "> on line " + le.getLine() + "in '" + file.getAbsolutePath() + "'" + " was empty");
        }
    }

    public void checkNullOrNotEmpty(String xpath) throws ElementValueException {
        Node notNull = le.selectSingleNode(xpath);
        if (notNull != null && notNull.getText().trim().isEmpty()) {
            String element = xpath.substring(xpath.lastIndexOf("/"));
            throw new ElementValueException("No value for element <" + element + "> within <" + le.getName() + "> on line " + le.getLine());
        }
    }

    public LineElement selectSingleElement(String xpath) {
        Element e = (Element) le.selectSingleNode(xpath);
        if (e != null) {
            return new LineElement(file, e);
        }
        return null;
    }
    
    public void createIfNotExist(String elementName, Object value) {
        if(le.selectSingleNode(elementName) == null) {
            LocatedElement element = new LocatedElement(elementName);
            element.setText(String.valueOf(value));
            le.add(element);
        }
    }

}
