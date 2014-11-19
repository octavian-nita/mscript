package com.mscript.parse.gui;

import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import com.mscript.Function;
import com.mscript.parse.MScriptLexer;
import com.mscript.parse.MScriptParser;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.gui.TreeTextProvider;
import org.antlr.v4.runtime.tree.gui.TreeViewer.DefaultTreeTextProvider;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Oct 27, 2014
 */
public class MScriptTestRig extends javax.swing.JFrame {

    private static final Logger logger = Logger.getLogger(MScriptTestRig.class.getName());

    public static void main(String args[]) {

        // Try to load a default functions library file:
        try {
            Function.loadLibrary("functions.properties");
        } catch (Throwable ex) {
            logger.log(SEVERE, "Cannot load functions library", ex);
        }

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable ex) {
            logger.log(WARNING, "", ex);
        }

        Icon empty = new Icon() {

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                // Do nothing.
            }

            @Override
            public int getIconWidth() {
                return 0;
            }

            @Override
            public int getIconHeight() {
                return 0;
            }
        };
        UIManager.put("Tree.closedIcon", empty);
        UIManager.put("Tree.openIcon", empty);
        UIManager.put("Tree.leafIcon", empty);

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                new MScriptTestRig().setVisible(true);
            }
        });
    }

    public MScriptTestRig() {
        initComponents();
        setLocationRelativeTo(null); // position in the center of the screen

        srcPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control O"), "openScript");
        srcPane.getActionMap().put("openScript", new AbstractAction("openScript") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (JFileChooser.APPROVE_OPTION != scriptChooser.showOpenDialog(MScriptTestRig.this)) {
                    return;
                }

                File script = scriptChooser.getSelectedFile();
                try (BufferedReader reader = new BufferedReader(new FileReader(script))) {
                    StringBuilder source = new StringBuilder();

                    String line;
                    while ((line = reader.readLine()) != null) {
                        source.append(line).append(NL);
                    }

                    srcPane.setText(source.toString()); // would have been better to use a SwingWorker to load scripts
                } catch (IOException ioe) {
                    JOptionPane.showMessageDialog(MScriptTestRig.this,
                                                  "Cannot open/read file " + script.getAbsolutePath() + ": " +
                                                  ioe.getMessage() + "!", "An error has occurred",
                                                  JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        srcPane.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new ParseWorker().execute(); // create a new ParseWorker each time; SwingWorkers are not normally reused
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                getRootPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                new ParseWorker().execute(); // create a new ParseWorker each time; SwingWorkers are not normally reused
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scriptChooser = new javax.swing.JFileChooser();
        javax.swing.JSplitPane mainSplit = new javax.swing.JSplitPane();
        javax.swing.JPanel rightPanel = new javax.swing.JPanel();
        javax.swing.JScrollPane treeScroll = new javax.swing.JScrollPane();
        treeView = new javax.swing.JTree();
        javax.swing.JSplitPane leftSplit = new javax.swing.JSplitPane();
        javax.swing.JScrollPane errScroll = new javax.swing.JScrollPane();
        errList = new javax.swing.JList();
        javax.swing.JScrollPane srcScroll = new javax.swing.JScrollPane();
        srcPane = new javax.swing.JTextPane() {

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getUI().getPreferredSize(this).width <= getParent().getSize().width;
            }
        };

        scriptChooser.setCurrentDirectory(Paths.get(".").toFile());
        scriptChooser.setDialogTitle("Open an MScript file");
        scriptChooser.setFileFilter(MSCRIPT_FILTER);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("MScript Test Rig, v. 0.1");
        setName("mainFrame"); // NOI18N

        mainSplit.setBorder(javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4));
        mainSplit.setDividerLocation(400);
        mainSplit.setDividerSize(4);
        mainSplit.setResizeWeight(0.5);
        mainSplit.setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));

        rightPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));

        treeScroll.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        treeScroll.setBorder(javax.swing.BorderFactory
                                 .createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0),
                                                     "Abstract Syntax Tree",
                                                     javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                                                     javax.swing.border.TitledBorder.DEFAULT_POSITION, LABEL_FONT));

        treeView.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
            javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        treeView.setModel(new DefaultTreeModel(null));
        treeView.setAutoscrolls(true);
        treeScroll.setViewportView(treeView);

        javax.swing.GroupLayout rightPanelLayout = new javax.swing.GroupLayout(rightPanel);
        rightPanel.setLayout(rightPanelLayout);
        rightPanelLayout.setHorizontalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(treeScroll, javax.swing.GroupLayout.Alignment.TRAILING,
                                          javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE));
        rightPanelLayout.setVerticalGroup(
            rightPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(treeScroll, javax.swing.GroupLayout.DEFAULT_SIZE, 517, Short.MAX_VALUE));

        mainSplit.setRightComponent(rightPanel);

        leftSplit.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        leftSplit.setDividerLocation(350);
        leftSplit.setDividerSize(4);
        leftSplit.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplit.setResizeWeight(0.5);

        errScroll.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        errScroll.setBorder(javax.swing.BorderFactory
                                .createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0),
                                                    "Syntax Errors",
                                                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                                                    javax.swing.border.TitledBorder.DEFAULT_POSITION, LABEL_FONT));

        errList.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
            javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        errList.setFont(CODE_FONT);
        errList.setForeground(new java.awt.Color(255, 51, 51));
        errList.setModel(new DefaultListModel<SyntaxError>());
        errList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        errList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                errListValueChanged(evt);
            }
        });
        errList.addFocusListener(new java.awt.event.FocusAdapter() {

            public void focusGained(java.awt.event.FocusEvent evt) {
                errListFocusGained(evt);
            }
        });
        errScroll.setViewportView(errList);

        leftSplit.setRightComponent(errScroll);

        srcScroll.setBackground(javax.swing.UIManager.getDefaults().getColor("control"));
        srcScroll.setBorder(javax.swing.BorderFactory
                                .createTitledBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0),
                                                    "MScript (Ctrl+O to open existing file)",
                                                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                                                    javax.swing.border.TitledBorder.DEFAULT_POSITION, LABEL_FONT));

        srcPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
            javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)),
            javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        srcPane.setFont(CODE_FONT);
        srcPane.addCaretListener(new javax.swing.event.CaretListener() {

            public void caretUpdate(javax.swing.event.CaretEvent evt) {
                srcPaneCaretUpdate(evt);
            }
        });
        srcScroll.setViewportView(srcPane);

        leftSplit.setLeftComponent(srcScroll);

        mainSplit.setLeftComponent(leftSplit);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(mainSplit, javax.swing.GroupLayout.Alignment.TRAILING,
                                                      javax.swing.GroupLayout.DEFAULT_SIZE, 700, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                      .addComponent(mainSplit, javax.swing.GroupLayout.Alignment.TRAILING));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void errListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_errListValueChanged
        goToSyntaxError();
    }//GEN-LAST:event_errListValueChanged

    private void errListFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_errListFocusGained
        goToSyntaxError();
    }//GEN-LAST:event_errListFocusGained

    private void srcPaneCaretUpdate(javax.swing.event.CaretEvent evt) {//GEN-FIRST:event_srcPaneCaretUpdate
        final javax.swing.event.CaretEvent caretEvent = evt;
        SwingUtilities.invokeLater(new Runnable() { // scroll the text component so that the caret remains visible...

            @Override
            public void run() {
                try {
                    JTextComponent component = (JTextComponent) caretEvent.getSource();
                    Rectangle r = component.modelToView(component.getCaretPosition());
                    r.x += 2;
                    component.scrollRectToVisible(r);
                } catch (Exception exception) {
                    logger.log(WARNING, "", exception);
                }
            }
        });
    }//GEN-LAST:event_srcPaneCaretUpdate

    private void goToSyntaxError() {
        if (errList.isSelectionEmpty()) {
            return;
        }
        SyntaxError error = (SyntaxError) errList.getSelectedValue();

        // Compute the caret position in the source code pane:
        int caretPos = 0, lineCount = error.line - 1;
        String[] lines = srcPane.getText().split(NL);
        for (int i = 0; i < lineCount; i++) {
            caretPos += lines[i].length() + 1;
        }

        try {
            srcPane.setCaretPosition(error.charPositionInLine + caretPos);
            srcPane.requestFocusInWindow();
        } catch (Throwable throwable) {
            logger.log(SEVERE, "Cannot go to the syntax error location in code", throwable);
        }
    }

    private DefaultMutableTreeNode createViewTree(ParseTree parseTree, TreeTextProvider textProvider) {
        if (parseTree == null) {
            return null;
        }

        String text = textProvider.getText(parseTree);
        if (text != null) {
            text = text.replaceAll("(\r?\n)|\r", "<NEWLINE>"); // make white spaces visible somehow...
        }

        DefaultMutableTreeNode viewTree = new DefaultMutableTreeNode(text);

        int childCount = parseTree.getChildCount();
        for (int i = 0; i < childCount; i++) {
            viewTree.add(createViewTree(parseTree.getChild(i), textProvider));
        }
        return viewTree;
    }

    private void expandViewTree() {
        int j = treeView.getRowCount(), i = 0;
        while (i < j) {
            treeView.expandRow(i++);
            j = treeView.getRowCount();
        }
    }

    private class ParseWorker extends SwingWorker<ParseTree, SyntaxError> {

        private MScriptLexer mScriptLexer;

        private MScriptParser mScriptParser;

        private final List<SyntaxError> syntaxErrors = new ArrayList<>(); // accumulates errors for later reporting

        @Override
        protected ParseTree doInBackground() throws Exception {
            mScriptLexer = new MScriptLexer(new ANTLRInputStream(srcPane.getText()));
            mScriptParser = new MScriptParser(new CommonTokenStream(mScriptLexer));
            mScriptParser.addErrorListener(new BaseErrorListener() {

                @Override
                public <T extends Token> void syntaxError(@NotNull Recognizer<T, ?> recognizer,
                                                          @Nullable T offendingSymbol, int line, int charPositionInLine,
                                                          @NotNull String message,
                                                          @Nullable RecognitionException exception) {
                    syntaxErrors.add(
                        new SyntaxError(recognizer, offendingSymbol, line, charPositionInLine, message, exception));
                }
            });
            return mScriptParser.script();
        }

        @Override
        protected void done() {
            try {
                synchronized (MScriptTestRig.class) {
                    // Update the syntax errors list:
                    DefaultListModel<SyntaxError> errModel = (DefaultListModel<SyntaxError>) errList.getModel();
                    errModel.clear();
                    for (SyntaxError syntaxError : syntaxErrors) {
                        errModel.addElement(syntaxError);
                    }

                    // Update the parse tree:
                    DefaultTreeModel treeModel = (DefaultTreeModel) treeView.getModel();
                    treeModel.setRoot(createViewTree(get(), new DefaultTreeTextProvider(Arrays.asList(mScriptParser.
                                                     getRuleNames()))));
                    expandViewTree();
                }
            } catch (Throwable throwable) {
                JOptionPane.showMessageDialog(MScriptTestRig.this,
                                              "Cannot parse the MScript source: " + throwable.getMessage() + "!",
                                              "An error has occurred", JOptionPane.ERROR_MESSAGE);
                logger.log(SEVERE, "", throwable);
            }
            MScriptTestRig.this.getRootPane().setCursor(Cursor.getDefaultCursor());
        }
    }

    private static class SyntaxError {

        final Recognizer<?, ?> recognizer;

        final Object offendingSymbol;

        final int line;

        final int charPositionInLine;

        final String message;

        final RecognitionException recognitionException;

        final String asString;

        public SyntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
                           int charPositionInLine, @NotNull String message, @Nullable RecognitionException exception) {
            this.recognizer = recognizer;
            this.offendingSymbol = offendingSymbol;
            this.line = line;
            this.charPositionInLine = charPositionInLine;
            this.message = message;
            this.recognitionException = exception;

            asString = "line " + line + ", column " + charPositionInLine + ": " + message;
        }

        @Override
        public String toString() {
            return asString;
        }
    }

    private static final FileFilter MSCRIPT_FILTER = new FileFilter() {

        @Override
        public boolean accept(File file) {
            String path = file.getName().toLowerCase();
            return path.endsWith(".mscript") || path.endsWith(".mst") || path.endsWith(".ms") || file.isDirectory();
        }

        @Override
        public String getDescription() {
            return "MScript files (*.mscript, *.mst, *.ms)";
        }
    };

    private static final Font CODE_FONT = new Font(Font.MONOSPACED, Font.BOLD, 14);

    private static final Font LABEL_FONT = new Font(Font.SANS_SERIF, Font.BOLD, 12);

    private static final String NL = System.getProperty("line.separator", "\n");

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList errList;
    private javax.swing.JFileChooser scriptChooser;
    private javax.swing.JTextPane srcPane;
    private javax.swing.JTree treeView;
    // End of variables declaration//GEN-END:variables
}
