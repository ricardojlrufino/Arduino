package cc.arduino.view.treeselector.impl;

import static processing.app.I18n.format;
import static processing.app.I18n.tr;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import cc.arduino.view.treeselector.SimpleCategory;
import cc.arduino.view.treeselector.TreeSelectorDataSource;
import processing.app.BaseNoGui;
import processing.app.debug.TargetPlatform;
import processing.app.helpers.FileUtils;
import processing.app.packages.LibraryList;
import processing.app.packages.UserLibrary;
import processing.app.packages.UserLibraryFolder.Location;

public class ExamplesDataService implements TreeSelectorDataSource {
  
  private DefaultMutableTreeNode rootNode;
  
  public ExamplesDataService() {
    super();
  }
  
  @Override
  public void invalidate() {
    rootNode = null;
  }

  @Override
  public TreeNode get() {
     
     if(rootNode != null) return rootNode;
     rootNode = new DefaultMutableTreeNode("/");
     DefaultMutableTreeNode submenu = new DefaultMutableTreeNode(new SimpleCategory(tr("Built-in Examples")));
     rootNode.add(submenu);
     
     addSketches(submenu, BaseNoGui.getExamplesFolder());

     // Libraries can come from 4 locations: collect info about all four
     String boardId = null;
     String referencedPlatformName = null;
     String myArch = null;
     TargetPlatform targetPlatform = BaseNoGui.getTargetPlatform();
     if (targetPlatform != null) {
       myArch = targetPlatform.getId();
       boardId = BaseNoGui.getTargetBoard().getName();
       String core = BaseNoGui.getBoardPreferences().get("build.core", "arduino");
       if (core.contains(":")) {
         String refcore = core.split(":")[0];
         TargetPlatform referencedPlatform = BaseNoGui.getTargetPlatform(refcore, myArch);
         if (referencedPlatform != null) {
           referencedPlatformName = referencedPlatform.getPreferences().get("name");
         }
       }
     }

     // Divide the libraries into 7 lists, corresponding to the 4 locations
     // with the retired IDE libs further divided into their own list, and
     // any incompatible sketchbook libs further divided into their own list.
     // The 7th list of "other" libraries should always be empty, but serves
     // as a safety feature to prevent any library from vanishing.
     LibraryList allLibraries = BaseNoGui.librariesIndexer.getInstalledLibraries();
     LibraryList ideLibs = new LibraryList();
     LibraryList retiredIdeLibs = new LibraryList();
     LibraryList platformLibs = new LibraryList();
     LibraryList referencedPlatformLibs = new LibraryList();
     LibraryList sketchbookLibs = new LibraryList();
     LibraryList sketchbookIncompatibleLibs = new LibraryList();
     LibraryList otherLibs = new LibraryList();
     for (UserLibrary lib : allLibraries) {
       // Get the library's location - used for sorting into categories
       Location location = lib.getLocation();
       // Is this library compatible?
       List<String> arch = lib.getArchitectures();
       boolean compatible;
       if (myArch == null || arch == null || arch.contains("*")) {
         compatible = true;
       } else {
         compatible = arch.contains(myArch);
       }
       // IDE Libaries (including retired)
       if (location == Location.IDE_BUILTIN) {
         if (compatible) {
           // only compatible IDE libs are shown
           if (lib.getTypes().contains("Retired")) {
             retiredIdeLibs.add(lib);
           } else {
             ideLibs.add(lib);
           }
         }
       // Platform Libraries
       } else if (location == Location.CORE) {
         // all platform libs are assumed to be compatible
         platformLibs.add(lib);
       // Referenced Platform Libraries
       } else if (location == Location.REFERENCED_CORE) {
         // all referenced platform libs are assumed to be compatible
         referencedPlatformLibs.add(lib);
       // Sketchbook Libraries (including incompatible)
       } else if (location == Location.SKETCHBOOK) {
         if (compatible) {
           // libraries promoted from sketchbook (behave as builtin)
           if (!lib.getTypes().isEmpty() && lib.getTypes().contains("Arduino")
               && lib.getArchitectures().contains("*")) {
             ideLibs.add(lib);
           } else {
             sketchbookLibs.add(lib);
           }
         } else {
           sketchbookIncompatibleLibs.add(lib);
         }
       // Other libraries of unknown type (should never occur)
       } else {
         otherLibs.add(lib);
       }
     }

     // Add examples from libraries
     if (!ideLibs.isEmpty()) {
       ideLibs.sort();
       submenu = new DefaultMutableTreeNode(new SimpleCategory(tr("Examples for any board")));
       rootNode.add(submenu);
     }
     for (UserLibrary lib : ideLibs) {
       addSketchesSubmenu(submenu, lib);
     }

     if (!retiredIdeLibs.isEmpty()) {
       retiredIdeLibs.sort();
       DefaultMutableTreeNode retired = new DefaultMutableTreeNode(new SimpleCategory(tr("RETIRED")));
       rootNode.add(retired);
       for (UserLibrary lib : retiredIdeLibs) {
         addSketchesSubmenu(retired, lib);
       }
     }

     if (!platformLibs.isEmpty()) {
       platformLibs.sort();
       submenu = new DefaultMutableTreeNode(new SimpleCategory(format(tr("Examples for {0}"), boardId)));
       rootNode.add(submenu);
       for (UserLibrary lib : platformLibs) {
         addSketchesSubmenu(submenu, lib);
       }
     }

     if (!referencedPlatformLibs.isEmpty()) {
       referencedPlatformLibs.sort();
       submenu = new DefaultMutableTreeNode(new SimpleCategory(format(tr("Examples for {0}"), referencedPlatformName)));
       rootNode.add(submenu);
       for (UserLibrary lib : referencedPlatformLibs) {
         addSketchesSubmenu(submenu, lib);
       }
     }

     if (!sketchbookLibs.isEmpty()) {
       sketchbookLibs.sort();
       submenu = new DefaultMutableTreeNode(new SimpleCategory(tr("Examples from Custom Libraries")));
       rootNode.add(submenu);
       for (UserLibrary lib : sketchbookLibs) {
         addSketchesSubmenu(submenu, lib);
       }
     }

     if (!sketchbookIncompatibleLibs.isEmpty()) {
       sketchbookIncompatibleLibs.sort();
       DefaultMutableTreeNode incompatible = new DefaultMutableTreeNode(new SimpleCategory(tr("INCOMPATIBLE")));
       rootNode.add(incompatible);
       for (UserLibrary lib : sketchbookIncompatibleLibs) {
         addSketchesSubmenu(incompatible, lib);
       }
     }

     if (!otherLibs.isEmpty()) {
       otherLibs.sort();
       submenu = new DefaultMutableTreeNode(new SimpleCategory(tr("Examples from Other Libraries")));
       rootNode.add(submenu);
       for (UserLibrary lib : otherLibs) {
         addSketchesSubmenu(submenu, lib);
       }
     }  
     
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
  
  private static boolean addSketchesSubmenu(DefaultMutableTreeNode menu, UserLibrary lib) {
    return addSketchesSubmenu(menu, lib.getName(), lib.getInstalledFolder());
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
