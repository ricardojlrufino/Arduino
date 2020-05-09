package cc.arduino.view.treeselector.impl;

import static processing.app.Theme.scale;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.border.MatteBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.commons.lang3.StringUtils;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;

import cc.arduino.view.treeselector.TreeSelectorDataSource;
import cc.arduino.view.treeselector.TreeSelectorDialog;
import processing.app.BaseNoGui;
import processing.app.PreferencesData;
import processing.app.Theme;
import processing.app.syntax.PdeKeywords;
import processing.app.syntax.SketchTextArea;

public class SketchSelectorDialog extends TreeSelectorDialog{
  
  protected SketchTextArea textarea;
  protected RTextScrollPane scrollPane;

  public SketchSelectorDialog(String title, TreeSelectorDataSource dataSource) {
    super(title, dataSource);
    
    this.setSize(new Dimension(800, 500));
    
    treePane.setPreferredSize(new Dimension(400, 500));
    
    RSyntaxDocument document = createDocument("...");
    try {
      textarea = createTextArea(document);
      scrollPane = createScrollPane(textarea);
      applyPreferences();
      this.add(scrollPane, BorderLayout.CENTER);
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    setupListeners();
    
  }
  
  private void setupListeners() {
    
    tree.addTreeSelectionListener(new TreeSelectionListener() {
      public void valueChanged(TreeSelectionEvent e) {
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

        /* if nothing is selected */
        if (node == null)
          return;

        if (node != null && node.getUserObject() != null) {
          if (node.getUserObject() instanceof File) {
            File file = (File) node.getUserObject();
            if (file.exists() && !file.isDirectory()) {
              preview(file);
            }
          }
        }
      }
    });
    
  }

  private void preview(File file) {
    try {
      String text = BaseNoGui.loadFile(file);
      RSyntaxDocument document = createDocument(text);
      textarea.setDocument(document);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  private RSyntaxDocument createDocument(String contents) {
    RSyntaxDocument document = new RSyntaxDocument(RSyntaxDocument.SYNTAX_STYLE_CPLUSPLUS);
    document.putProperty(PlainDocument.tabSizeAttribute, PreferencesData.getInteger("editor.tabs.size"));

    // insert the program text into the document object
    try {
      document.insertString(0, contents, null);
    } catch (BadLocationException bl) {
      bl.printStackTrace();
    }
  
    return document;
  }

  private RTextScrollPane createScrollPane(SketchTextArea textArea) throws IOException {
    RTextScrollPane scrollPane = new RTextScrollPane(textArea, true);
    scrollPane.setBorder(new MatteBorder(0, 6, 0, 0, Theme.getColor("editor.bgcolor")));
    scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
    scrollPane.setLineNumbersEnabled(PreferencesData.getBoolean("editor.linenumbers"));
    scrollPane.setIconRowHeaderEnabled(false);

    Gutter gutter = scrollPane.getGutter();
    gutter.setBookmarkingEnabled(false);
    //gutter.setBookmarkIcon(CompletionsRenderer.getIcon(CompletionType.TEMPLATE));
    gutter.setIconRowHeaderInheritsGutterBackground(true);

    return scrollPane;
  }

  private SketchTextArea createTextArea(RSyntaxDocument document)
      throws IOException {
    final SketchTextArea textArea = new SketchTextArea(document, new PdeKeywords());
    textArea.setName("editor");
    textArea.setFocusTraversalKeysEnabled(false);
    //textArea.requestFocusInWindow();
    textArea.setMarkOccurrences(PreferencesData.getBoolean("editor.advanced"));
    textArea.setMarginLineEnabled(false);
    textArea.setCodeFoldingEnabled(PreferencesData.getBoolean("editor.code_folding"));
    textArea.setAutoIndentEnabled(PreferencesData.getBoolean("editor.indent"));
    textArea.setCloseCurlyBraces(PreferencesData.getBoolean("editor.auto_close_braces", true));
    textArea.setAntiAliasingEnabled(PreferencesData.getBoolean("editor.antialias"));
    textArea.setTabsEmulated(PreferencesData.getBoolean("editor.tabs.expand"));
    textArea.setTabSize(PreferencesData.getInteger("editor.tabs.size"));

    return textArea;
  }
  
  public void applyPreferences() {
    textarea.setCodeFoldingEnabled(PreferencesData.getBoolean("editor.code_folding"));
    scrollPane.setFoldIndicatorEnabled(PreferencesData.getBoolean("editor.code_folding"));
    scrollPane.setLineNumbersEnabled(PreferencesData.getBoolean("editor.linenumbers"));

    // disable line highlight and turn off the caret when disabling
//    textarea.setBackground(Theme.getColor("editor.external.bgcolor"));
    textarea.setHighlightCurrentLine(false);
    textarea.setEditable(false);
        
    // apply changes to the font size for the editor
    Font editorFont = scale(PreferencesData.getFont("editor.font"));
    
    // check whether a theme-defined editor font is available
    Font themeFont = Theme.getFont("editor.font");
    if (themeFont != null)
    {
      // Apply theme font if the editor font has *not* been changed by the user,
      // This allows themes to specify an editor font which will only be applied
      // if the user hasn't already changed their editor font via preferences.txt
      String defaultFontName = StringUtils.defaultIfEmpty(PreferencesData.getDefault("editor.font"), "").split(",")[0];
      if (defaultFontName.equals(editorFont.getName())) {
        editorFont = new Font(themeFont.getName(), themeFont.getStyle(), editorFont.getSize());
      }
    }
    
    textarea.setFont(editorFont);
    scrollPane.getGutter().setLineNumberFont(editorFont);
  }
  
  public static JFrame showDialog(String title, TreeSelectorDataSource dataSource, ActionListener onSelectListener) {
    TreeSelectorDialog frame = new SketchSelectorDialog(title, dataSource);
    frame.setOnSelectListener(onSelectListener);
    frame.setVisible(true);
    frame.load();
    return frame;
  }
}
