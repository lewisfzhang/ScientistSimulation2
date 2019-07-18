import java.lang.reflect.Array;
import java.util.ArrayList;

public class BTree {
    public int num_paths; // keeps track of number of branches in this BTree object
    public int max_weight = 3; // scientist can only learn maximum of 3 new ideas out of all discovered ideas per tp
    ArrayList<Node> leafs; // keeps track of all the leaves
    Node root = new Node(-1, 0); // dummy with idx of -1 to keep track of all ideas that have been discovered but not learned
    ArrayList<Integer> line = new ArrayList<>();

    BTree(ArrayList<Integer> k_paid, ArrayList<Integer> discov_ideas) { // k_paid = sci.k_paid_tot, both arraylists should have the same length
        // only for ideas we haven't paid k for yet
        for (int i=0; i<discov_ideas.size(); i++) {
            if (discov_ideas.get(i) == 1) {
                if (k_paid.get(i) == 0) { // idea has been discovered but not learned
                    new_leafs(root, 0, i);
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

    void new_leafs(Node node, int weight, int val) {
        int curr_weight = weight + node.k;
        if(node.left != null) {
            new_leafs(node.left, curr_weight, val);
        }
        if(node.right != null) {
            new_leafs(node.right, curr_weight, val);
        }
        if(node.left == null && node.right == null) {
            if (curr_weight == )
            node.left
        }
    }

    ArrayList<Integer> next_branch() { // every node in the tree, whether or not k is paid in this tp 0 = no, 1 = yes
        return null;
    }

}

class Node {
    int idea_idx;
    int k; // k = 0 if don't learn, k = 1 if learn
    Node left;
    Node right;

    Node(int idea_idx, int k) {
        this.idea_idx = idea_idx;
        this.k = k;
        right = null;
        left = null;
    }
}