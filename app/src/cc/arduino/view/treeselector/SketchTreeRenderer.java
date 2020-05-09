package cc.arduino.view.treeselector;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.io.File;
import java.util.function.Supplier;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class SketchTreeRenderer extends DefaultTreeCellRenderer {
  private static final String SPAN_FORMAT = "<span style='color:%s;'>%s</span>";
  private static final String SPAN_CATEGORY_FORMAT = "<span style='color:%s;'> --- %s ---</span>";
  private Supplier<String> filterTextSupplier;
  private Font boldFont;
  private Font normalFont;

  public SketchTreeRenderer(Supplier<String> filterTextSupplier) {
    this.filterTextSupplier = filterTextSupplier;
    Icon leafIcon = new ImageIcon(getClass().getResource("script.png"));
    Icon folder = new ImageIcon(getClass().getResource("folder.png"));
    setClosedIcon(folder);
    setOpenIcon(folder);
    setLeafIcon(leafIcon);
  }

  @Override
  public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf,
                                                int row, boolean hasFocus) {
    super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

    // Init fonts..
    if (boldFont == null) {
      normalFont = this.getFont();
      boldFont = (normalFont.deriveFont(normalFont.getStyle() | Font.BOLD));
    }

    DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
    Object userObject = node.getUserObject();

    if (userObject instanceof File) {
      File file = (File) userObject;
      String text;
      String name = file.getName().replace(".ino", "");

      if (file.isDirectory()) {
        text = String.format(SPAN_FORMAT, "rgb(255, 255, 255)", renderFilterMatch(node, name));
        setFont(boldFont);

      } else {
        setFont(normalFont);
        text = String.format(SPAN_FORMAT, "rgb(255, 255, 255)", renderFilterMatch(node, name));
      }

      this.setText("<html>" + text + "</html>");

    } else if (userObject instanceof SimpleCategory) {
      this.setText("<html>" + String.format(SPAN_CATEGORY_FORMAT, "rgb(255, 255, 255)",
                                            renderFilterMatch(node, ((SimpleCategory) userObject).getName())));
      setFont(boldFont);
    }

    if (sel) {
      this.setOpaque(true);
      this.setBackground(new Color(23, 161, 165));

    } else {
      this.setOpaque(true);
      this.setBackground(new Color(0, 100, 104));
    }

    return this;
  }

  private String renderFilterMatch(DefaultMutableTreeNode node, String text) {
    if (node.isRoot()) {
      return text;
    }
    String textToFilter = filterTextSupplier.get();
    return HtmlHighlighter.highlightText(text, textToFilter);
  }
}