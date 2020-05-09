package cc.arduino.view.treeselector;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.BiPredicate;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingWorker;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreeSelectionModel;

import cc.arduino.view.treeselector.impl.SketchSelectorDialog;
import processing.app.Base;

public class TreeSelectorDialog extends JFrame {

  private ActionListener onSelectListener;
  protected JTree tree;
  protected JPanel treePane;
  private JScrollPane scroller;
  protected JPanel loadingPanel;
  private TreeSelectorDataSource dataSource;
  private TreeFilterDecorator filterDecorator;

  public TreeSelectorDialog(String title, TreeSelectorDataSource dataSource) {
    super(title);
    this.dataSource = dataSource;
    this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    this.setSize(new Dimension(500, 400));
    
    tree = new JTree(new DefaultTreeModel(new DefaultMutableTreeNode("...")));

    // FIX: gtk dont allow set backgroud
    javax.swing.plaf.metal.MetalTreeUI treeUI = new MetalTreeUI();
    tree.setUI(treeUI);

    final Font currentFont = tree.getFont();
    final Font bigFont = new Font(currentFont.getName(), currentFont.getStyle(), currentFont.getSize() + 2);
    tree.setFont(bigFont);

    filterDecorator = TreeFilterDecorator.decorate(tree, createUserObjectMatcher());
    tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

    tree.setCellRenderer(new SketchTreeRenderer(() -> filterDecorator.getFilterField().getText()));

    this.getContentPane().setBackground(new Color(0, 100, 104));

    treePane = new JPanel(); // holds JScrollPane and filter, allow extensons
    treePane.setLayout(new BorderLayout());
    scroller = new JScrollPane(tree);
    treePane.add(scroller);
    treePane.add(filterDecorator.getFilterField(), BorderLayout.NORTH);
    
    this.add(treePane, BorderLayout.WEST);
    
    this.setLocationRelativeTo(null);
    // frame.setUndecorated(true);

    // Allow set backgroud..
    scroller.getViewport().setOpaque(false);
    scroller.setOpaque(false);
    tree.setOpaque(false);
    treePane.setOpaque(false);

    
    // Loading (on GlassPane)...
    loadingPanel = (JPanel) this.getGlassPane();
    loadingPanel.setVisible(true);
    loadingPanel.setLayout(new BorderLayout());
    JProgressBar bar = new JProgressBar();
    bar.setIndeterminate(true);
    loadingPanel.add(bar, BorderLayout.SOUTH);

    Base.registerWindowCloseKeys(this.getRootPane(), e -> {
      this.setVisible(false);
      this.dispose();
    });

    Base.setIcon(this);

    setupListeners();

  };

  private void setupListeners() {
    // Double-Click event
    MouseListener ml = new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int selRow = tree.getRowForLocation(e.getX(), e.getY());
        if (selRow != -1) {
          if (e.getClickCount() == 1) {
            // mySingleClick(selRow, selPath);
          } else if (e.getClickCount() == 2) {
            if (onSelectListener != null) {
              DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();

              if (node != null && node.getUserObject() != null) {
                onSelectListener.actionPerformed(new ActionEvent(node.getUserObject(), 0, null));
              }

            }
          }
        }
      }
    };
    tree.addMouseListener(ml);

    // ENTER event.
    tree.addKeyListener(new KeyAdapter() {
      public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == 10) {
          DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
          if (node != null)
            onSelectListener.actionPerformed(new ActionEvent(node.getUserObject(), 0, null));
        }
      }

      public void keyTyped(KeyEvent e) {
      }

      public void keyPressed(KeyEvent e) {
      }
    });

  }

  public void setOnSelectListener(ActionListener onSelectListener) {
    this.onSelectListener = onSelectListener;
  }

  public void load() {

    SwingWorker sw1 = new SwingWorker() {

      @Override
      protected TreeNode doInBackground() throws Exception {
        return dataSource.get();
      }

      @Override
      protected void done() {
        try {
          DefaultMutableTreeNode root = (DefaultMutableTreeNode) get();
          DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
          model.setRoot(root);
          filterDecorator.setOriginalRootNode(root);
          loadingPanel.setVisible(false);
          JTreeUtil.expandImediateChilds(tree);
        } catch (InterruptedException | ExecutionException e) {
          e.printStackTrace();
        }

      }
    };

    // executes the swingworker on worker thread
    sw1.execute();
  }

  private static BiPredicate<Object, String> createUserObjectMatcher() {
    return (userObject, textToFilter) -> {
      if (userObject instanceof File) {
        File pp = (File) userObject;
        return pp.getName().toLowerCase().contains(textToFilter);
      } else {
        return userObject.toString().toLowerCase().contains(textToFilter);
      }
    };
  }

  public static JFrame showDialog(String title, TreeSelectorDataSource dataSource, ActionListener onSelectListener) {
    TreeSelectorDialog frame = new TreeSelectorDialog(title, dataSource);
    frame.setOnSelectListener(onSelectListener);
    frame.setVisible(true);
    frame.load();
    return frame;
  }

}
