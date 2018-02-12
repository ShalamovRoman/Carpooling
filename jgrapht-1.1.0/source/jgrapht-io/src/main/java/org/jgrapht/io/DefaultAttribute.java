/*
 * (C) Copyright 2017-2017, by Dimitrios Michail and Contributors.
 *
 * JGraphT : a free Java graph-theory library
 *
 * This program and the accompanying materials are dual-licensed under
 * either
 *
 * (a) the terms of the GNU Lesser General Public License version 2.1
 * as published by the Free Software Foundation, or (at your option) any
 * later version.
 *
 * or (per the licensee's choosing)
 *
 * (b) the terms of the Eclipse Public License v1.0 as published by
 * the Eclipse Foundation.
 */
package org.jgrapht.io;

import java.io.*;

/**
 * Default implementation of an attribute.
 * 
 * @param <T> the underlying type
 * 
 * @author Dimitrios Michail
 */
public class DefaultAttribute<T>
    implements Attribute, Serializable
{
    private static final long serialVersionUID = 366113727410278952L;

    private T value;
    private AttributeType type;

    /**
     * Create a new attribute
     * 
     * @param value the value
     * @param type the type
     */
    public DefaultAttribute(T value, AttributeType type)
    {
        this.value = value;
        this.type = type;
    }

    /**
     * Get the string value of the attribute
     * 
     * @return the string value of the attribute
     */
    @Override
    public String getValue()
    {
        return value.toString();
    }

    @Override
    public String toString()
    {
        return value.toString();
    }

    /**
     * Get the type of the attribute
     * 
     * @return the type of the attribute
     */
    @Override
    public AttributeType getType()
    {
        return type;
    }

    /**
     * Create a boolean attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(Boolean value)
    {
        return new DefaultAttribute<>(value, AttributeType.BOOLEAN);
    }

    /**
     * Create an integer attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(Integer value)
    {
        return new DefaultAttribute<>(value, AttributeType.INT);
    }

    /**
     * Create a long attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(Long value)
    {
        return new DefaultAttribute<>(value, AttributeType.LONG);
    }

    /**
     * Create a float attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(Float value)
    {
        return new DefaultAttribute<>(value, AttributeType.FLOAT);
    }

    /**
     * Create a double attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(Double value)
    {
        return new DefaultAttribute<>(value, AttributeType.DOUBLE);
    }

    /**
     * Create a string attribute
     * 
     * @param value the value
     * @return the attribute
     */
    public static Attribute createAttribute(String value)
    {
        return new DefaultAttribute<>(value, AttributeType.STRING);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("rawtypes")
    public boolean equals(Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultAttribute other = (DefaultAttribute) obj;
        if (type != other.type)
            return false;
        if (value == null) {
            if (other.value != null)
                return false;
        } else if (!value.equals(other.value))
            return false;
        return true;
    }

}
