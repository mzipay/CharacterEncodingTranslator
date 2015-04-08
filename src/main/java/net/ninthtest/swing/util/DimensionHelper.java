/*
 * Copyright (c) 2010 Matthew Zipay <mattz@ninthtest.net>
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

package net.ninthtest.swing.util;

import java.awt.Dimension;

import javax.swing.JComponent;

/**
 * This class provides helper methods for working with AWT {@link Dimension}s.
 * 
 * @author mattz
 * @version 2.0.1
 * @see Dimension
 */
public final class DimensionHelper {
    /**
     * Sets all specified components to the same width.
     * 
     * <p>
     * The normalized width is calculated as the maximum preferred width among
     * the specified components.
     * </p>
     * 
     * @param components the components to be resized
     */
    public static final void normalizeWidth(JComponent... components) {
        int width = 0;

        for (JComponent component : components) {
            if (component.getPreferredSize().width > width) {
                width = component.getPreferredSize().width;
            }
        }

        setPreferredWidth(width, components);
    }

    /**
     * Sets all specified components to the same width.
     * 
     * @param width the desired width
     * @param components the components to be resized
     */
    public static final void setPreferredWidth(
            int width, JComponent... components) {
        for (JComponent component : components) {
            component.setPreferredSize(
                    new Dimension(width, component.getPreferredSize().height));
        }
    }

    /**
     * Sets all specified components to the same height.
     * 
     * <p>
     * The normalized height is calculated as the maximum preferred height
     * among the specified components.
     * </p>
     * 
     * @param components the components to be resized
     */
    public static final void normalizeHeight(JComponent... components) {
        int height = 0;

        for (JComponent component : components) {
            if (component.getPreferredSize().height > height) {
                height = component.getPreferredSize().height;
            }
        }

        setPreferredHeight(height, components);
    }

    /**
     * Sets all specified components to the same height.
     * 
     * @param height the desired height
     * @param components the components to be resized
     */
    public static final void setPreferredHeight(
            int height, JComponent... components) {
        for (JComponent component : components) {
            component.setPreferredSize(
                    new Dimension(component.getPreferredSize().width, height));
        }
    }

    private DimensionHelper() {
        /* never instantiated */
    }
}
