
package net.ninthtest.nio.charset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;

import org.junit.Test;

@SuppressWarnings("javadoc")
public class CharsetTranslatorTest {
    private static final String EXPECTED_UTF_8_STRING =
            "$=USD, \u00a5=JPY, \u20ac=EUR";

    private static final String EXPECTED_ISO_8859_1_STRING =
            "$=USD, \u00a5=JPY, &#8364;=EUR";

    private static final String EXPECTED_US_ASCII_STRING =
            "$=USD, &#165;=JPY, &#8364;=EUR";

    /*
     * test the branch case where the last character is unmappable in the
     * target encoding (see the "if (i < count)" conditional at the end of
     * CharsetTranslator#translate(InputStream, OutputStream))
     */
    private static final String EXPECTED_ISO_8859_1_STRING_2 =
            "USD $, JPY \u00a5, EUR &#8364;";

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCharsetNameConstructorNullSource() {
        new CharsetTranslator(null, "UTF-8");
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCharsetNameConstructorNullTarget() {
        new CharsetTranslator("windows-1252", null);
    }

    @Test
    public void testCharsetNameConstructor() {
        CharsetTranslator translator =
                new CharsetTranslator("windows-1252", "UTF-8");

        assertEquals(Charset.forName("windows-1252"),
                translator.sourceCharset());
        assertEquals(Charset.forName("UTF-8"), translator.targetCharset());
        assertFalse(translator.isUsingXMLCharRefReplacement());
        assertEquals(CharsetTranslator.DEFAULT_BUFFER_SIZE,
                translator.getBufferSize());
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCharsetConstructorNullSource() {
        new CharsetTranslator(null, Charset.forName("UTF-8"));
    }

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("unused")
    public void testCharsetConstructorNullTarget() {
        new CharsetTranslator(Charset.forName("windows-1252"), null);
    }

    @Test
    public void testCharsetConstructor() {
        CharsetTranslator translator =
                new CharsetTranslator(
                        Charset.forName("windows-1252"),
                        Charset.forName("UTF-8"));

        assertEquals(Charset.forName("windows-1252"),
                translator.sourceCharset());
        assertEquals(Charset.forName("UTF-8"), translator.targetCharset());
        assertFalse(translator.isUsingXMLCharRefReplacement());
        assertEquals(CharsetTranslator.DEFAULT_BUFFER_SIZE,
                translator.getBufferSize());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferSizeZero() {
        CharsetTranslator translator =
                new CharsetTranslator("windows-1252", "UTF-8");
        translator.setBufferSize(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBufferSizeNegative() {
        CharsetTranslator translator =
                new CharsetTranslator("windows-1252", "UTF-8");
        translator.setBufferSize(-1);
    }

    @Test
    public void testBufferSize() {
        CharsetTranslator translator =
                new CharsetTranslator("windows-1252", "UTF-8");

        assertEquals(CharsetTranslator.DEFAULT_BUFFER_SIZE,
                translator.getBufferSize());

        int newBufferSize = CharsetTranslator.DEFAULT_BUFFER_SIZE / 2;
        translator.setBufferSize(newBufferSize);

        assertEquals(newBufferSize, translator.getBufferSize());
    }

    @Test(expected = MalformedInputException.class)
    public void testTranslateMalformedSourceInput() throws IOException {
        CharsetTranslator translator =
                new CharsetTranslator("US-ASCII", "UTF-8");

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("utf8.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);
    }

    // http://unicode.org/Public/MAPPINGS/OBSOLETE/EASTASIA/OTHER/BIG5.TXT
    @Test(expected = MalformedInputException.class)
    public void testTranslateUnmappableSourceCharacter() throws IOException {
        CharsetTranslator translator = new CharsetTranslator("Big5", "UTF-8");

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("big5_unmappable.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);
    }

    /*
     * TODO: Is this even a worthwhile test? If reading from the source is
     * successful then the bytes passed to the encoder will always be a valid
     * Unicode byte sequence, so MalformedInputException should not be a
     * possibility?
     */
    // @Test(expected = MalformedInputException.class)
    // public void testTranslateMalformedTargetInput() throws IOException {
    // }

    // http://unicode.org/Public/MAPPINGS/OBSOLETE/EASTASIA/OTHER/BIG5.TXT
    @Test(expected = UnmappableCharacterException.class)
    public void testTranslateUnmappableTargetCharacter() throws IOException {
        CharsetTranslator translator =
                new CharsetTranslator("Big5-HKSCS", "Big5");

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("big5-hkscs.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);
    }

    @Test
    public void testTranslateUtf8ToUtf8() throws IOException {
        CharsetTranslator translator = new CharsetTranslator("UTF-8", "UTF-8");

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("utf8.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);

        assertEquals(EXPECTED_UTF_8_STRING,
                new String(out.toByteArray(), "UTF-8"));
    }

    @Test
    public void testTranslateUtf8ToIso88591WithXmlCharRef()
            throws IOException {
        CharsetTranslator translator =
                new CharsetTranslator("UTF-8", "ISO-8859-1");
        translator.useXMLCharRefReplacement(true);

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("utf8.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);

        assertEquals(EXPECTED_ISO_8859_1_STRING,
                new String(out.toByteArray(), "ISO-8859-1"));
    }

    @Test
    public void testTranslateUtf8ToIso88591WithXmlCharRef2()
            throws IOException {
        CharsetTranslator translator =
                new CharsetTranslator("UTF-8", "ISO-8859-1");
        translator.useXMLCharRefReplacement(true);

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("utf8_2.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);

        assertEquals(EXPECTED_ISO_8859_1_STRING_2,
                new String(out.toByteArray(), "ISO-8859-1"));
    }

    @Test
    public void testTranslateUtf8ToUsAsciiWithXmlCharRef() throws IOException {
        CharsetTranslator translator =
                new CharsetTranslator("UTF-8", "US-ASCII");
        translator.useXMLCharRefReplacement(true);

        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("utf8.txt");
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        translator.translate(in, out);

        assertEquals(EXPECTED_US_ASCII_STRING,
                new String(out.toByteArray(), "US-ASCII"));
    }

    @Test
    public void testEquals() {
        CharsetTranslator translator1 =
                new CharsetTranslator("windows-1252", "UTF-8");

        // for any non-null reference value x, x.equals(x) should return true
        assertTrue("CharsetTranslator instance is not equal to itself",
                translator1.equals(translator1));
        /*
         * For any non-null reference value x, x.equals(null) should return
         * false
         */
        assertFalse("CharsetTranslator instance is equal to null",
                translator1.equals(null));

        assertFalse(
                "CharsetTranslator instance equals non-CharsetTranslator instance",
                translator1.equals(new ByteArrayOutputStream()));

        CharsetTranslator translator2 =
                new CharsetTranslator("Shift_JIS", "UTF-8");

        assertFalse(
                "windows-1252->UTF-8 instance equals Shift_JIS->UTF-8 instance",
                translator1.equals(translator2));

        CharsetTranslator translator3 =
                new CharsetTranslator("windows-1252", "ISO-8859-1");

        assertFalse(
                "windows-1252->UTF-8 instance equals windows-1252->ISO-8859-1 instance",
                translator1.equals(translator3));

        CharsetTranslator translator4 =
                new CharsetTranslator("windows-1252", "UTF-8");

        assertTrue(
                "windows-1252->UTF-8 instance does not equal windows-1252->UTF-8 instance",
                translator1.equals(translator4));

        translator4.useXMLCharRefReplacement(true);

        assertFalse(
                "windows-1252->UTF-8 instance w/out xmlcharref equals windows-1252->UTF-8 instance w/ xmlcharref",
                translator1.equals(translator4));

        CharsetTranslator translator5 =
                new CharsetTranslator("windows-1252", "UTF-8");

        /*
         * for any non-null reference values x and y, x.equals(y) should return
         * true if and only if y.equals(x) returns true
         */
        assertTrue(
                "translator1 and translator5 equals() methods are not symmetric",
                translator1.equals(translator5)
                        && translator5.equals(translator1));

        CharsetTranslator translator6 =
                new CharsetTranslator("windows-1252", "UTF-8");

        /*
         * for any non-null reference values x, y, and z, if x.equals(y)
         * returns true and y.equals(z) returns true, then x.equals(z) should
         * return true
         */
        assertTrue(
                "translator1, translator5, and translator6 equals() methods are not transitive",
                translator1.equals(translator5)
                        && translator5.equals(translator6)
                        && translator1.equals(translator6));

        /*
         * for any non-null reference values x and y, multiple invocations of
         * x.equals(y) consistently return true or consistently return false,
         * provided no information used in equals comparisons on the objects is
         * modified
         */
        boolean fiveEqualsSix = translator5.equals(translator6);
        assertEquals(
                "translator5 and translator6 equals() methods are not consistently "
                        + fiveEqualsSix,
                fiveEqualsSix, translator5.equals(translator6));

        translator6.useXMLCharRefReplacement(true);
        fiveEqualsSix = translator5.equals(translator6);
        assertEquals(
                "translator5 and translator6 equals() methods are not consistently "
                        + fiveEqualsSix,
                fiveEqualsSix, translator5.equals(translator6));
    }

    @Test
    public void testHashCode() {
        CharsetTranslator translator1 =
                new CharsetTranslator("windows-1252", "UTF-8");

        /*
         * Whenever it is invoked on the same object more than once during an
         * execution of a Java application, the hashCode method must
         * consistently return the same integer, provided no information used
         * in equals comparisons on the object is modified.
         */
        int expectedHashCode = translator1.hashCode();
        assertTrue(
                "translator2 hashCode() returned different integer on successive invocations without modification",
                expectedHashCode == translator1.hashCode());

        translator1.useXMLCharRefReplacement(true);
        assertFalse(
                "translator2 hashCode() returned same integer on successive invocations with modification",
                expectedHashCode == translator1.hashCode());
    }

    @Test
    public void testHashCodeEqualsContract() {
        CharsetTranslator translator1 =
                new CharsetTranslator("windows-1252", "UTF-8");
        CharsetTranslator translator2 =
                new CharsetTranslator("windows-1252", "UTF-8");

        /*
         * If two objects are equal according to the equals(Object) method,
         * then calling the hashCode method on each of the two objects must
         * produce the same integer result.
         */
        assertTrue("translator1 is not equal to translator2",
                translator1.equals(translator2));
        assertTrue("translator1 hash code != translator2 hash code",
                translator1.hashCode() == translator2.hashCode());
    }

    @Test
    public void testToString() {
        CharsetTranslator translator =
                new CharsetTranslator("windows-1252", "UTF-8");

        assertEquals("windows-1252 -> UTF-8", translator.toString());
    }
}
