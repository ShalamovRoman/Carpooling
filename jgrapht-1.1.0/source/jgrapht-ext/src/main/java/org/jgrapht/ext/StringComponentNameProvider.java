/*
 * (C) Copyright 2005-2017, by Trevor Harmon and Contributors.
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
package org.jgrapht.ext;

/**
 * Generates names by invoking {@link #toString()} on them. This assumes that the object's
 * {@link #toString()} method returns a unique string representation.
 *
 * @param <T> the component type
 * 
 * @author Trevor Harmon
 * @deprecated Use {@link org.jgrapht.io.StringComponentNameProvider} instead.
 */
@Deprecated
public class StringComponentNameProvider<T>
    extends org.jgrapht.io.StringComponentNameProvider<T>
    implements ComponentNameProvider<T>
{
}

// End StringComponentNameProvider.java
