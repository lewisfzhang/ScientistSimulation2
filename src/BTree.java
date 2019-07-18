import java.lang.reflect.Array;
import java.util.ArrayList;

public class BTree {
    public int num_paths; // keeps track of number of branches in this BTree object
    ArrayList<Node> leafs; // keeps track of all the leaves
    Node root = null; // the first idea that has been discovered but not yet learned
    ArrayList<Integer> line = new ArrayList<>();

    BTree(ArrayList<Integer> k_paid, ArrayList<Integer> discov_ideas) { // k_paid = sci.k_paid_tot, both arraylists should have the same length
        // only for ideas we haven't paid k for yet
        for (int i=0; i<discov_ideas.size(); i++) {
            if (discov_ideas[i] == 1) {
                if (k_paid == 0) { // idea has been discovered but not learned
                    generate_layer(i);
                } else { // idea has been discovered but already learned
                    line.add(i);
                }
            }
        }
    }

    void generate_layer(int val) {
        if (root == null) {
            root = new Node(val);
        }
    }

    void visitLeafs(Node node) {
        if(node.left != null) {
            visitLeafs(node.left);
        }
        if(node.right != null) {
            visitLeafs(node.right);
        }
        if(node.left == null && node.right == null) {
            //OMG! leaf!
        }
    }

    ArrayList<Integer> next_branch() { // every node in the tree, whether or not k is paid in this tp 0 = no, 1 = yes
        return null;
    }

}

class Node {
    int idea_idx;
    Node left;
    Node right;

    Node(int idea_idx) {
        this.idea_idx = idea_idx;
        right = null;
        left = null;
    }
}