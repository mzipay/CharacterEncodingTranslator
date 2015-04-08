
package net.ninthtest;

import java.nio.charset.Charset;
import java.util.ResourceBundle;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.uispec4j.Button;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.interception.BasicHandler;
import org.uispec4j.interception.MainClassAdapter;
import org.uispec4j.interception.WindowInterceptor;

@Ignore("can't get UISpec4J working on JDK 1.8.0_05-b13, OS X 10.10.2")
@SuppressWarnings("javadoc")
public class CharacterEncodingTranslatorGuiTest extends UISpecTestCase {
    private static final ResourceBundle RESOURCES =
            ResourceBundle.getBundle("cetrans");

    @Before
    @Override
    public void setUp() {
        setAdapter(new MainClassAdapter(
                CharacterEncodingTranslator.class, new String[0]));
    }

    @Test
    public void testDefaultSourceEncoding() {
        getMainWindow().getComboBox("inCharsets").selectionEquals(
                Charset.defaultCharset().name()).check();
    }

    @Test
    public void testDefaultTargetEncoding() {
        getMainWindow().getComboBox("outCharsets").selectionEquals("UTF-8")
                .check();
    }

    @Test
    public void testUseXmlCharRefDefault() {
        assertFalse(getMainWindow().getCheckBox("xmlCharRefPref").isSelected()
                .isTrue());
    }

    @Test
    public void testNoSourceFilename() {
        Button translateButton = getMainWindow().getButton("translateButton");

        WindowInterceptor
                .init(translateButton.triggerClick())
                .process(
                        BasicHandler
                                .init()
                                .assertTitleEquals(
                                        RESOURCES
                                                .getString("warning.title.cant_continue"))
                                .assertContainsText(
                                        RESOURCES
                                                .getString("warning.message.choose_input"))
                                .triggerButtonClick("OK")
                ).run();
    }
}

