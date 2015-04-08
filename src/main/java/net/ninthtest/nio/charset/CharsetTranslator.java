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

package net.ninthtest.nio.charset;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * Instances of <code>CharsetTranslator</code> translate byte streams from one
 * character encoding to another.
 * 
 * <p>
 * The {@link CharsetDecoder} and {@link CharsetEncoder} that are used to
 * perform the translation use {@link CodingErrorAction#REPORT} for both
 * malformed-input and unmappable-character actions. Use
 * <code>CharsetDecoder</code> and <code>CharsetEncoder</code> directly if this
 * behavior is not desirable.
 * </p>
 * 
 * <p>
 * The {@link #useXMLCharRefReplacement(boolean)} feature can be used to enable
 * replacement of unmappable characters with their XML character reference
 * equivalents. The replacement occurs on encoding, as characters are written
 * to the target output stream. This feature is useful when preparing text for
 * display on the Web.
 * </p>
 * 
 * <p>
 * Unlike <code>CharsetDecoder</code> and <code>CharsetEncoder</code>, there is
 * no support for incremental translation using {@link java.nio} buffers. All
 * translations are performed as single operations (though reads are buffered
 * internally, and the size of the internal character buffer can be
 * controlled).
 * </p>
 * 
 * <p>
 * <code>CharsetTranslator</code> instances always reset the internal
 * decoder/encoder before translating. Therefore, it is safe to re-use the same
 * instance for multiple translation operations.
 * </p>
 * 
 * <p>
 * <code>CharsetTranslator</code> implements {@link #equals(Object)} and
 * {@link #hashCode()}. This allows instances to be cached in a lookup table,
 * for example.
 * </p>
 * 
 * @author mattz
 * @version 2.0.1
 */
public class CharsetTranslator {
    /**
     * The default buffer size used when reading from the source input stream.
     * 
     * <p>
     * The buffer size is expressed as the maximum number of <i>characters</i>
     * that will be read from the source input stream at once.
     * </p>
     * 
     * @see #getBufferSize()
     * @see #setBufferSize(int)
     */
    public static final int DEFAULT_BUFFER_SIZE = 4096;

    private final Charset sourceCharset;

    private final CharsetDecoder sourceDecoder;

    private final Charset targetCharset;

    private final CharsetEncoder targetEncoder;

    private boolean useXMLCharRefReplacement;

    private int bufferSize = DEFAULT_BUFFER_SIZE;

    /**
     * Constructs a new <code>CharsetTranslator</code> that can translate from
     * the named source encoding to the named target encoding.
     * 
     * @param sourceCharsetName the character encoding used to decode bytes
     *        read from the source input stream
     * @param targetCharsetName the character encoding used to encode
     *        characters written to the target output stream
     * @throws IllegalCharsetNameException if the source or target charset name
     *         is illegal
     * @throws IllegalArgumentException if either
     *         <code>sourceCharsetName</code> or <code>targetCharsetName</code>
     *         is <code>null</code>
     * @throws UnsupportedCharsetException if the current JVM does not support
     *         the named source or target charset
     */
    public CharsetTranslator(
            String sourceCharsetName, String targetCharsetName) {
        this(Charset.forName(sourceCharsetName),
                Charset.forName(targetCharsetName));
    }

    /**
     * Constructs a new <code>CharsetTranslator</code> that can translate using
     * a decoder and encoder from the given source and target {@link Charset}s,
     * respectively.
     * 
     * @param sourceCharset the character encoding used to decode bytes read
     *        from the source input stream
     * @param targetCharset the character encoding used to encode characters
     *        written to the target output stream
     * @throws IllegalArgumentException if either <code>sourceCharset</code> or
     *         <code>targetCharset</code> is <code>null</code>
     */
    public CharsetTranslator(Charset sourceCharset, Charset targetCharset) {
        if ((sourceCharset == null) || (targetCharset == null)) {
            throw new IllegalArgumentException("null charset");
        }

        this.sourceCharset = sourceCharset;
        sourceDecoder = sourceCharset.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);

        this.targetCharset = targetCharset;
        targetEncoder = targetCharset.newEncoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT);
    }

    /**
     * Returns the source charset.
     * 
     * @return the charset that identifies the source input stream's assumed
     *         character encoding
     */
    public final Charset sourceCharset() {
        return sourceCharset;
    }

    /**
     * Returns the target charset.
     * 
     * @return the charset that identifies the desired character encoding for
     *         the target output stream
     */
    public final Charset targetCharset() {
        return targetCharset;
    }

    /**
     * Tells whether or not this translator will replace unmappable characters
     * with their XML character reference equivalents.
     * 
     * <p>
     * Replacement occurs as characters are written to the target output
     * stream.
     * </p>
     * 
     * @return <code>true</code> if this translator will use XML character
     *         reference replacements
     */
    public boolean isUsingXMLCharRefReplacement() {
        return useXMLCharRefReplacement;
    }

    /**
     * Tells this translator whether or not to use XML character reference
     * replacements.
     * 
     * <p>
     * Replacement occurs as characters are written to the target output
     * stream.
     * </p>
     * 
     * @param useXMLCharRefReplacement <code>true</code> if unmappable
     *        characters should be replaced with their XML character reference
     *        equivalents
     * @return this translator
     */
    @SuppressWarnings("hiding")
    public final CharsetTranslator useXMLCharRefReplacement(
            boolean useXMLCharRefReplacement) {
        this.useXMLCharRefReplacement = useXMLCharRefReplacement;

        return this;
    }

    /**
     * Returns the size of the buffer used when reading from the source input
     * stream.
     * 
     * @return the maximum number of <i>characters</i> that will be read from
     *         the source at once
     */
    public int getBufferSize() {
        return bufferSize;
    }

    /**
     * Sets the size of the buffer used when reading from the source input
     * stream.
     * 
     * @param bufferSize the maximum number of <i>characters</i> that will be
     *        read from the source at once
     * @throws IllegalArgumentException if the buffer size is less than 1 (one)
     */
    public void setBufferSize(int bufferSize) {
        if (bufferSize < 1) {
            throw new IllegalArgumentException("buffer size must be >= 1");
        }
        this.bufferSize = bufferSize;
    }

    /**
     * Translates a stream of bytes from one character encoding to another.
     * 
     * @param sourceStream the stream of bytes to be translated
     * @param targetStream the stream to which translated bytes are written
     * @throws IOException if any reading/decoding/encoding/writing operation
     *         fails
     */
    public void translate(InputStream sourceStream, OutputStream targetStream)
            throws IOException {
        Reader reader = new BufferedReader(
                new InputStreamReader(sourceStream, sourceDecoder.reset()));
        Writer writer = new BufferedWriter(
                new OutputStreamWriter(targetStream, targetEncoder.reset()));

        char[] buffer = new char[bufferSize];
        int count = -1;

        /*
         * avoid character iteration if not performing XML charref replacement!
         * (also avoids redundant useXMLCharRefReplacement checks)
         */
        if (!useXMLCharRefReplacement) {
            while ((count = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, count);
            }
        } else {
            /*
             * use a separate encoder (instead of sharing one between the
             * writer and this block) because it's not safe to call
             * #canEncode(char) on an in-progress encoder
             */
            CharsetEncoder encoder = targetCharset.newEncoder();

            int i = 0;
            int j = 0;
            while ((count = reader.read(buffer)) != -1) {
                for (i = 0, j = 0; j < count; ++j) {
                    if (!encoder.canEncode(buffer[j])) {
                        /*
                         * write all characters up to but not including the
                         * unmappable character...
                         */
                        writer.write(buffer, i, j - i);
                        /*
                         * ...then write the XML character reference for the
                         * unmappable character...
                         */
                        writer.write("&#" + ((int) buffer[j]) + ";");
                        /*
                         * ...and set the marker to the next character _after_
                         * the unmappable character
                         */
                        i = j + 1;
                    }
                }

                /*
                 * write any characters remaining in the buffer (as long as
                 * buffer[count-1] is not unmappable, there will be unwritten
                 * characters)
                 */
                if (i < count) {
                    writer.write(buffer, i, count - i);
                }
            }
        }

        writer.flush();
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * The hash code of a <code>CharsetTranslator</code> is based on the source
     * charset, target charset, and whether or not XML character reference
     * replacement is enabled.
     * </p>
     * 
     * @return a hash code value for this translator
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        int hc = sourceCharset.hashCode() ^ targetCharset.hashCode();

        if (useXMLCharRefReplacement) {
            hc = Integer.rotateLeft(hc, 11);
        }

        return hc;
    }

    /**
     * {@inheritDoc}
     * 
     * <p>
     * Two <code>CharsetTranslator</code> instances are considered equal if,
     * and only if, each instance is using the same source and target charset
     * <b><i>and</i></b> XML character reference replacement is either enabled
     * or disabled for <i>both</i> instances at the time of comparison.
     * </p>
     * 
     * @param obj the reference object with which to compare
     * @return <code>true</code> if this translator is the same as
     *         <code>obj</code>; <code>false</code> otherwise
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null) {
            return false;
        }

        if (obj instanceof CharsetTranslator) {
            CharsetTranslator other = (CharsetTranslator) obj;

            return sourceCharset.equals(other.sourceCharset)
                    && targetCharset.equals(other.targetCharset)
                    && (useXMLCharRefReplacement
                        == other.useXMLCharRefReplacement);
        }

        return false;
    }

    /**
     * {@inheritDoc}
     * 
     * @return a string indicating "source_charset_name to target_charset_name"
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new StringBuilder(sourceCharset.name()).append(" -> ")
                .append(targetCharset.name()).toString();
    }
}
