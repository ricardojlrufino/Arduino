package cc.arduino.view.treeselector;
import static processing.app.I18n.tr;

import java.util.function.BiPredicate;

import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import cc.arduino.contributions.ui.FilterJTextField;

public class TreeFilterDecorator {
    private final JTree tree;
    private DefaultMutableTreeNode originalRootNode;
    private BiPredicate<Object, String> userObjectMatcher;
    private FilterJTextField filterField;

    public TreeFilterDecorator(JTree tree, BiPredicate<Object, String> userObjectMatcher) {
        this.tree = tree;
        this.originalRootNode = (DefaultMutableTreeNode) tree.getModel().getRoot();
        this.userObjectMatcher = userObjectMatcher;
    }

    public static TreeFilterDecorator decorate(JTree tree, BiPredicate<Object, String> userObjectMatcher) {
        TreeFilterDecorator tfd = new TreeFilterDecorator(tree, userObjectMatcher);
        tfd.init();
        return tfd;
    }

    public FilterJTextField getFilterField() {
        return filterField;
    }
    
    public void setOriginalRootNode(DefaultMutableTreeNode originalRootNode) {
      this.originalRootNode = originalRootNode;
    }

    private void init() {
        initFilterField();
    }

    private void initFilterField() {
      filterField = new FilterJTextField(tr("Filter your search..."), 350){
          @Override
          protected void onFilter(String[] _filters) {
            filterTree(String.join(" ", _filters));
          }
        };
        filterField.getAccessibleContext().setAccessibleDescription(tr("Search Filter"));
        
        filterField.setMinSearchLengh(3);
    }

    private void filterTree(String text) {
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        System.out.println("filter treee... -> " + text);
        if (text.equals("") && tree.getModel().getRoot() != originalRootNode) {
            model.setRoot(originalRootNode);
//            JTreeUtil.setTreeExpandedState(tree, true);
              JTreeUtil.expandImediateChilds(tree);
        } else {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                DefaultMutableTreeNode newRootNode = matchAndBuildNode(text, originalRootNode);
                model.setRoot(newRootNode);
                JTreeUtil.setTreeExpandedState(tree, true);
              };
            });
        }
    }

    private DefaultMutableTreeNode matchAndBuildNode(final String text, DefaultMutableTreeNode oldNode) {
        if (!oldNode.isRoot() && userObjectMatcher.test(oldNode.getUserObject(), text)) {
            return JTreeUtil.copyNode(oldNode);
        }
        DefaultMutableTreeNode newMatchedNode = oldNode.isRoot() ? new DefaultMutableTreeNode(oldNode
                .getUserObject()) : null;
        for (DefaultMutableTreeNode childOldNode : JTreeUtil.children(oldNode)) {
            DefaultMutableTreeNode newMatchedChildNode = matchAndBuildNode(text, childOldNode);
            if (newMatchedChildNode != null) {
                if (newMatchedNode == null) {
                    newMatchedNode = new DefaultMutableTreeNode(oldNode.getUserObject());
                }
                newMatchedNode.add(newMatchedChildNode);
            }
        }
        return newMatchedNode;
    }
}