
package net.ninthtest.swing.util;

import static org.junit.Assert.assertEquals;

import java.awt.Dimension;

import javax.swing.JComponent;

import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("javadoc")
public class DimensionHelperTest {
    private JComponent component1;

    private JComponent component2;

    private JComponent component3;

    @Before
    @SuppressWarnings("serial")
    public void setUp() {
        component1 = new JComponent() {
            /* test */
        };
        component1.setPreferredSize(new Dimension(2, 73));

        component2 = new JComponent() {
            /* test */
        };
        component2.setPreferredSize(new Dimension(47, 53));

        component3 = new JComponent() {
            /* test */
        };
        component3.setPreferredSize(new Dimension(23, 97));
    }

    @Test
    public void testNormalizeWidth() {
        DimensionHelper.normalizeWidth(component1, component2, component3);

        assertEquals("component1", 47, component1.getPreferredSize().width);
        assertEquals("component2", 47, component2.getPreferredSize().width);
        assertEquals("component3", 47, component3.getPreferredSize().width);

        assertEquals("component1", 73, component1.getPreferredSize().height);
        assertEquals("component2", 53, component2.getPreferredSize().height);
        assertEquals("component3", 97, component3.getPreferredSize().height);
    }

    @Test
    public void testSetPreferredWidth() {
        DimensionHelper.setPreferredWidth(
                13, component1, component2, component3);

        assertEquals("component1", 13, component1.getPreferredSize().width);
        assertEquals("component2", 13, component2.getPreferredSize().width);
        assertEquals("component3", 13, component3.getPreferredSize().width);

        assertEquals("component1", 73, component1.getPreferredSize().height);
        assertEquals("component2", 53, component2.getPreferredSize().height);
        assertEquals("component3", 97, component3.getPreferredSize().height);
    }

    @Test
    public void testNormalizeHeight() {
        DimensionHelper.normalizeHeight(component1, component2, component3);

        assertEquals("component1", 97, component1.getPreferredSize().height);
        assertEquals("component2", 97, component2.getPreferredSize().height);
        assertEquals("component3", 97, component3.getPreferredSize().height);

        assertEquals("component1", 2, component1.getPreferredSize().width);
        assertEquals("component2", 47, component2.getPreferredSize().width);
        assertEquals("component3", 23, component3.getPreferredSize().width);
    }

    @Test
    public void testSetPreferredHeight() {
        DimensionHelper.setPreferredHeight(
                83, component1, component2, component3);

        assertEquals("component1", 83, component1.getPreferredSize().height);
        assertEquals("component2", 83, component2.getPreferredSize().height);
        assertEquals("component3", 83, component3.getPreferredSize().height);

        assertEquals("component1", 2, component1.getPreferredSize().width);
        assertEquals("component2", 47, component2.getPreferredSize().width);
        assertEquals("component3", 23, component3.getPreferredSize().width);
    }
}
