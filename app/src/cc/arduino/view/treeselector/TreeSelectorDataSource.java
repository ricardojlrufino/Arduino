package cc.arduino.view.treeselector;

import javax.swing.tree.TreeNode;

public interface TreeSelectorDataSource {
  
  TreeNode get();
  
  /** invalidate cache */
  void invalidate();

}
