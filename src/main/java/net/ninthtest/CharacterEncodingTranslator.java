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

package net.ninthtest;

import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.charset.UnmappableCharacterException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ProgressMonitorInputStream;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.SwingWorker.StateValue;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.ninthtest.nio.charset.CharsetTranslator;
import net.ninthtest.swing.util.DimensionHelper;

/**
 * <code>CharacterEncodingTranslator</code> is a console and GUI front-end for
 * {@link CharsetTranslator}.
 * 
 * <p>
 * <b>GUI usage:</b>
 * </p>
 * 
 * <pre>
 * java[w] -jar cetrans.jar
 * </pre>
 * 
 * <p>
 * <b>Console usage:</b>
 * </p>
 * 
 * <pre>
 * java -jar cetrans.jar [-xmlcharref] source-filename source-encoding target-filename target-encoding
 * </pre>
 * 
 * @author mattz
 * @version 2.0.1
 */
public class CharacterEncodingTranslator {
    /** The command-line usage message. */
    public static final String USAGE =
            "CONSOLE USAGE:\n"
                    + "\tjava -jar cetrans.jar [-xmlcharref] <source-filename>"
                    + " <source-encoding> <target-filename> <target-encoding>\n"
                    + "GUI USAGE:\n"
                    + "\tjava[w] -jar cetrans.jar\n";

    /** The current application SemVer version string. */
    public static final String VERSION = "2.0.1";

    private static final ResourceBundle RESOURCES =
            ResourceBundle.getBundle("cetrans");

    private static final int TEXT_SIZE = 59;

    private static final String DEFAULT_TARGET_ENCODING = "UTF-8";

    private final JTextField inTextField = new JTextField();

    private final JButton inButton = new JButton();

    private final JComboBox<String> inCharsets = new JComboBox<String>();

    private final JButton translateButton = new JButton();

    private final JCheckBox xmlCharRefPref = new JCheckBox();

    private final JTextField outTextField = new JTextField();

    private final JButton outButton = new JButton();

    private final JComboBox<String> outCharsets = new JComboBox<String>();

    private final JFileChooser fileChooser = new JFileChooser();

    private final JMenuBar menuBar = new JMenuBar();

    /**
     * Builds the GUI components for the <i>Character Encoding Translator</i>
     * application.
     */
    public CharacterEncodingTranslator() {
        JFrame frame = new JFrame(RESOURCES.getString("frame.title"));

        initializeComponents();
        doLayout(frame.getContentPane());

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);
    }

    /*
     * Configures the user interface Swing components.
     */
    private void initializeComponents() {
        FileNameExtensionFilter csvFilter = new FileNameExtensionFilter(
                RESOURCES.getString("filter.description.csv"), "csv");
        fileChooser.addChoosableFileFilter(csvFilter);

        FileNameExtensionFilter txtFilter = new FileNameExtensionFilter(
                RESOURCES.getString("filter.description.txt"), "txt");
        fileChooser.addChoosableFileFilter(txtFilter);

        fileChooser.setFileFilter(csvFilter);

        inTextField.setColumns(TEXT_SIZE);

        inButton.setText(RESOURCES.getString("button.text.open"));
        inButton.setMnemonic(KeyEvent.VK_O);
        inButton.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent event) {
                if (JFileChooser.APPROVE_OPTION
                == fileChooser.showOpenDialog(inTextField)) {
                    inTextField.setText(
                            fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        final String[] availableCharsets =
                Charset.availableCharsets().keySet().toArray(new String[0]);

        inCharsets.setModel(
                new DefaultComboBoxModel<String>(availableCharsets));
        inCharsets.setName("inCharsets");
        inCharsets.setSelectedItem(Charset.defaultCharset().name());
        inCharsets.setEditable(false);

        translateButton.setText(RESOURCES.getString("button.text.translate"));
        translateButton.setName("translateButton");
        translateButton.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent event) {
                if (event.getSource() == translateButton) {
                    final File inFile = new File(inTextField.getText());
                    final File outFile = new File(outTextField.getText());

                    if (filesAreAcceptable(inFile, outFile)) {
                        final String inEncoding =
                                (String) inCharsets.getSelectedItem();
                        final String outEncoding =
                                (String) outCharsets.getSelectedItem();

                        try {
                            translate(
                                    inFile, inEncoding, outFile, outEncoding);
                        } catch (IOException ex) {
                            JOptionPane.showMessageDialog(
                                    translateButton, ex.getLocalizedMessage(),
                                    ex.getClass().getName(),
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        xmlCharRefPref.setText(
                RESOURCES.getString("checkbox.text.use_xml_charref"));
        xmlCharRefPref.setName("xmlCharRefPref");

        outTextField.setColumns(TEXT_SIZE);

        outButton.setText(RESOURCES.getString("button.text.save"));
        outButton.setMnemonic(KeyEvent.VK_S);
        outButton.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent e) {
                if (JFileChooser.APPROVE_OPTION
                == fileChooser.showSaveDialog(outTextField)) {
                    outTextField.setText(
                            fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        outCharsets.setModel(
                new DefaultComboBoxModel<String>(availableCharsets));
        outCharsets.setName("outCharsets");
        outCharsets.setSelectedItem(DEFAULT_TARGET_ENCODING);
        outCharsets.setEditable(false);

        JMenu help = new JMenu(RESOURCES.getString("menu.help.text"));
        JMenuItem about = new JMenuItem(
                RESOURCES.getString("about.label") + '\u2026');
        about.addActionListener(new ActionListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void actionPerformed(ActionEvent event) {
                JOptionPane.showMessageDialog(null,
                        MessageFormat.format(
                                RESOURCES.getString("about.message"),
                                CharacterEncodingTranslator.VERSION),
                        RESOURCES.getString("about.label"),
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });
        help.add(about);
        menuBar.add(help);
    }

    /*
     * Applies the GUI layout for the application.
     */
    private void doLayout(final Container container) {
        DimensionHelper.normalizeWidth(inTextField, outTextField);
        DimensionHelper.normalizeWidth(inCharsets, outCharsets);
        DimensionHelper.normalizeWidth(inButton, outButton);
        DimensionHelper.normalizeHeight(inTextField, inButton, inCharsets,
                translateButton, outTextField, outButton, outCharsets);

        FlowLayout inLayout = new FlowLayout(FlowLayout.LEFT, 5, 10);
        inLayout.setAlignOnBaseline(true);

        JPanel inPanel = new JPanel(inLayout);
        inPanel.add(inTextField);
        inPanel.add(inButton);
        inPanel.add(inCharsets);

        FlowLayout translateLayout = new FlowLayout(FlowLayout.CENTER, 5, 10);
        translateLayout.setAlignOnBaseline(true);

        JPanel translatePanel = new JPanel(translateLayout);
        translatePanel.add(translateButton);
        translatePanel.add(xmlCharRefPref);

        FlowLayout outLayout = new FlowLayout(FlowLayout.LEFT, 5, 10);
        outLayout.setAlignOnBaseline(true);

        JPanel outPanel = new JPanel(outLayout);
        outPanel.add(outTextField);
        outPanel.add(outButton);
        outPanel.add(outCharsets);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.add(inPanel);
        container.add(translatePanel);
        container.add(outPanel);
    }

    /*
     * Ensures that the specified input and output files are reasonable before
     * attempting the translation.
     */
    private boolean filesAreAcceptable(File inFile, File outFile) {
        if (inFile.getPath().isEmpty()) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("warning.message.choose_input"),
                    RESOURCES.getString("warning.title.cant_continue"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (outFile.getPath().isEmpty()) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("warning.message.choose_output"),
                    RESOURCES.getString("warning.title.cant_continue"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (outFile.equals(inFile)) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("warning.message.same_input_output"),
                    RESOURCES.getString("warning.title.cant_continue"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (!inFile.canRead()) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("warning.message.input_notfound"),
                    RESOURCES.getString("warning.title.cant_continue"),
                    JOptionPane.WARNING_MESSAGE);
            return false;
        } else if (outFile.exists()) {
            return (JOptionPane.YES_OPTION
                == JOptionPane.showConfirmDialog(translateButton,
                    RESOURCES.getString("yesno.message.output_exists"),
                    RESOURCES.getString("yesno.title.user_input"),
                    JOptionPane.YES_NO_OPTION));
        }

        return true;
    }

    /*
     * Performs the translation in a background thread.
     */
    private void translate(final File inFile, final String inEncoding,
            final File outFile, final String outEncoding)
            throws FileNotFoundException {
        final ProgressMonitorInputStream monitorStream =
                new ProgressMonitorInputStream(translateButton,
                        RESOURCES.getString("monitor.message.translating"),
                        new FileInputStream(inFile));
        monitorStream.getProgressMonitor().setNote(
                inFile.getName() + " \u2192 " + outFile.getName());
        final FileOutputStream outStream = new FileOutputStream(outFile);

        final SwingWorker<Boolean, Void> task =
                new SwingWorker<Boolean, Void>() {
                    @SuppressWarnings("synthetic-access")
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        CharsetTranslator translator =
                                new CharsetTranslator(inEncoding, outEncoding);
                        translator.useXMLCharRefReplacement(
                                xmlCharRefPref.isSelected());
                        translator.translate(monitorStream, outStream);

                        return true;
                    }
                };
        task.addPropertyChangeListener(new PropertyChangeListener() {
            @SuppressWarnings("synthetic-access")
            @Override
            public void propertyChange(PropertyChangeEvent event) {
                /*
                 * Swing is not thread-safe, so it is possible that the task is
                 * actually DONE when the state property is only reporting a
                 * change from PENDING to STARTED (for a relatively small input
                 * file, this will almost certainly be the case); to ensure
                 * that this block is only entered once, both the task state
                 * _and_ the state property value must be DONE
                 */
                if ("state".equals(event.getPropertyName())
                        && (StateValue.DONE == event.getNewValue())
                        && (StateValue.DONE == task.getState())) {
                    /*
                     * testing on both Max OS X and Windows 7 shows that
                     * neither the task nor the monitor actually report being
                     * canceled when the progress IS canceled; since we can't
                     * rely on this approach, we need to check explicitly for
                     * an InterruptedIOException (yuck)
                     */
                    try {
                        if (task.get()) {
                            JOptionPane.showMessageDialog(
                                    translateButton,
                                    RESOURCES
                                            .getString("info.message.success"),
                                    RESOURCES
                                            .getString("info.title.translated"),
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } catch (ExecutionException ex) {
                        handleExecutionException(ex);
                    } catch (InterruptedException ex) {
                        /*
                         * should never happen since we're only here if the
                         * task state is DONE
                         */
                        assert false;
                    }

                    translateButton.setText(
                            RESOURCES.getString("button.text.translate"));
                    translateButton.setEnabled(true);
                }
            }
        });
        task.execute();

        translateButton.setEnabled(false);
        translateButton.setText(
                RESOURCES.getString("monitor.message.translating"));
    }

    /*
     * Displays an appropriate dialog message based on the cause of a failed
     * translation.
     */
    private final void handleExecutionException(ExecutionException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof InterruptedIOException) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("warning.message.canceled"),
                    RESOURCES.getString("warning.title.canceled"),
                    JOptionPane.WARNING_MESSAGE);
        } else if (cause instanceof MalformedInputException) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("error.message.malformed"),
                    RESOURCES.getString("error.title.failed"),
                    JOptionPane.ERROR_MESSAGE);
        } else if (cause instanceof UnmappableCharacterException) {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("error.message.unmappable"),
                    RESOURCES.getString("error.title.failed"),
                    JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(translateButton,
                    RESOURCES.getString("error.message.other"),
                    RESOURCES.getString("error.title.failed"),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Launches <i>Character Encoding Translator</i> as a GUI or console
     * application.
     * 
     * <p>
     * Without command-line arguments, the application runs as a GUI. With
     * command-line arguments, the application runs on the console.
     * </p>
     * 
     * <p>
     * To run on the console, provide the following positional arguments:
     * </p>
     * 
     * <dl>
     * <dt><b>"-xmlcharref"</b></dt>
     * <dd>(optional) the literal flag "-xmlcharref" enables XML character
     * reference replacement</dd>
     * <dt><i>source-filename</i></dt>
     * <dd>(required) the path to the input file</dd>
     * <dt><i>source-encoding</i></dt>
     * <dd>(required) the character encoding of the input file</dd>
     * <dt><i>target-filename</i></dt>
     * <dd>(required) the path to the output file</dd>
     * <dt><i>target-encoding</i></dt>
     * <dd>(required) the desired character encoding of the output file</dd>
     * </dl>
     * 
     * @param args the command-line arguments
     * @throws ClassNotFoundException if the L&amp;F class name is not found on
     *         the CLASSPATH
     * @throws InstantiationException if the L&amp;F class cannot be
     *         instantiated
     * @throws IllegalAccessException if the current user does not have
     *         permission to access the L&amp;F class
     * @throws UnsupportedLookAndFeelException if the L&amp;F class name is not
     *         recognized
     */
    public static void main(String[] args)
            throws ClassNotFoundException, InstantiationException,
            IllegalAccessException, UnsupportedLookAndFeelException {
        switch (args.length) {
        case 0:
            UIManager.setLookAndFeel(
                    UIManager.getCrossPlatformLookAndFeelClassName());

            SwingUtilities.invokeLater(new Runnable() {
                @SuppressWarnings("unused")
                @Override
                public void run() {
                    new CharacterEncodingTranslator();
                }
            });
            break;
        case 4:
            /* falls through */
        case 5:
            boolean useXmlCharRef = "-xmlcharref".equals(args[0]);
            int i = useXmlCharRef ? 1 : 0;
            String sourceFilename = args[i++];
            String sourceEncoding = args[i++];
            String targetFilename = args[i++];
            String targetEncoding = args[i++];

            InputStream sourceStream = null;
            OutputStream targetStream = null;
            int status = 0;
            try {
                sourceStream = new FileInputStream(sourceFilename);
                targetStream = new FileOutputStream(targetFilename);

                (new CharsetTranslator(sourceEncoding, targetEncoding))
                        .translate(sourceStream, targetStream);
            } catch (Exception ex) {
                System.err.println(ex.toString());
                status = 1;
            } finally {
                if (targetStream != null) {
                    try {
                        targetStream.close();
                    } catch (IOException ex) {
                        System.err.println(ex.toString());
                    }
                }
                if (sourceStream != null) {
                    try {
                        sourceStream.close();
                    } catch (IOException ex) {
                        System.err.println(ex.toString());
                    }
                }
            }

            System.exit(status);
            break;
        default:
            System.err.println(USAGE);
            System.exit(1);
        }
    }
}
