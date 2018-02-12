/*
 * (C) Copyright 2006-2017, by John V Sichi and Contributors.
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
package org.jgrapht.util;

/**
 * TypeUtil isolates type-unsafety so that code which uses it for legitimate reasons can stay
 * warning-free.
 *
 * @param <T> the type of the result
 *
 * @author John V. Sichi
 */
public class TypeUtil<T>
{
    /**
     * Casts an object to a type.
     *
     * @param o object to be cast
     * @param typeDecl conveys the target type information; the actual value is unused and can be
     *        null since this is all just stupid compiler tricks
     * @param <T> the type of the result
     *
     * @return the result of the cast
     */
    @SuppressWarnings("unchecked")
    public static <T> T uncheckedCast(Object o, TypeUtil<T> typeDecl)
    {
        return (T) o;
    }
}

// End TypeUtil.java
