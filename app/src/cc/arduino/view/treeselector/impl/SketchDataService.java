package cc.arduino.view.treeselector.impl;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import cc.arduino.view.treeselector.TreeSelectorDataSource;
import processing.app.BaseNoGui;
import processing.app.helpers.FileUtils;

public class SketchDataService implements TreeSelectorDataSource {
  
  private File basePath;
  private DefaultMutableTreeNode rootNode;
  
  
  public SketchDataService(File basePath) {
    this.basePath = basePath;
    
  }
  
  @Override
  public void invalidate() {
    rootNode = null;
  }

  @Override
  public TreeNode get() {
    if(rootNode != null) return rootNode;
    rootNode = new DefaultMutableTreeNode();
    rootNode.setUserObject(basePath);
    addSketches(rootNode, basePath);
    return (TreeNode) rootNode;
  }
  
  protected static boolean addSketches(DefaultMutableTreeNode tree, File folder) {
    if (folder == null)
      return false;

    if (!folder.isDirectory()) return false;

    File[] files = folder.listFiles();
    // If a bad folder or unreadable or whatever, this will come back null
    if (files == null) return false;

    // Alphabetize files, since it's not always alpha order
    Arrays.sort(files, new Comparator<File>() {
      @Override
      public int compare(File file, File file2) {
        return file.getName().compareToIgnoreCase(file2.getName());
      }
    });

    boolean ifound = false;
    for (File subfolder : files) {
      boolean ignoreList = subfolder.getName().equals("libraries") || subfolder.getName().equals("hardware"); 
      if (!FileUtils.isSCCSOrHiddenFile(subfolder) && !ignoreList &&subfolder.isDirectory() && addSketchesSubmenu(tree, subfolder.getName(), subfolder)) {
        ifound = true;
      }
    }
    return ifound;
  }
  
  private static boolean addSketchesSubmenu(DefaultMutableTreeNode  menu, String name, File folder) {

    File entry = new File(folder, name + ".ino");
    if (!entry.exists() && (new File(folder, name + ".pde")).exists())
      entry = new File(folder, name + ".pde");

    // if a .pde file of the same prefix as the folder exists..
    if (entry.exists()) {

      if (!BaseNoGui.isSanitaryName(name)) {
//        if (!builtOnce) {
//          String complaining = I18n
//                  .format(
//                          tr("The sketch \"{0}\" cannot be used.\n"
//                                  + "Sketch names must contain only basic letters and numbers\n"
//                                  + "(ASCII-only with no spaces, "
//                                  + "and it cannot start with a number).\n"
//                                  + "To get rid of this message, remove the sketch from\n"
//                                  + "{1}"), name, entry.getAbsolutePath());
//          showMessage(tr("Ignoring sketch with bad name"), complaining);
//        }
        return false;
      }

      DefaultMutableTreeNode item = new DefaultMutableTreeNode(name);
      item.setUserObject(entry);
      menu.add(item);
      return true;
    }

    // don't create an extra menu level for a folder named "examples"
    if (folder.getName().equals("examples"))
      return addSketches(menu, folder);

    // not a sketch folder, but maybe a subfolder containing sketches
    DefaultMutableTreeNode submenu = new DefaultMutableTreeNode(name);
    submenu.setUserObject(folder);
    
    boolean found = addSketches(submenu, folder);
    if (found) {
      menu.add(submenu);
    }
    return found;
  }

}
