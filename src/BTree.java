import java.util.ArrayList;
import java.util.Collections;

public class BTree {
    int num_paths; // keeps track of number of branches in this BTree object
    int max_weight; // scientist can only learn maximum of 3 new ideas out of all discovered ideas per tp
    int height; // keeps track of the length of the longest path/branch in tree
    ArrayList<Node> leafs; // keeps track of all the leaves
    Node root = new Node(-1, 0, null); // dummy with idx of -1 to keep track of all ideas that have been discovered but not learned
    ArrayList<Integer> line = new ArrayList<>();

    BTree(ArrayList<Integer> k_paid, ArrayList<Integer> discov_ideas) { // k_paid = sci.k_paid_tot, both arraylists should have the same length
        this.max_weight = Config.max_weight;
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

    // iterates through all possible leafs (aka endpoints) and adds current Idea to possible paths
    void new_leafs(Node node, int weight, int val) {
        int curr_weight = weight + node.k;
        if(node.left != null) {
            new_leafs(node.left, curr_weight, val);
        }
        if(node.right != null) {
            new_leafs(node.right, curr_weight, val);
        }
        if(node.left == null && node.right == null) { // found a current leaf
            if (curr_weight < max_weight) { // adds two nodes indicating two possible paths (learn = 1, don't learn = 0)
                node.left = new Node(val, 0, node);
                node.right = new Node(val, 1, node);
            } else if (curr_weight > max_weight) { // if curr_weight == max_weight, do nothing
                throw new IllegalArgumentException("weight should not be greater, something wrong");
            }
        }
    }

    ArrayList<Integer> path_counter; // keeps track of which path we are currently on based on Node.k value --> 0's, 1's
    boolean first_path = true;

    // branch: returns a list of the indexes of the ideas based on model.idea_list (both already learned and new ideas that will be learned)
    ArrayList<Integer> next_branch() { // every node in the tree, whether or not k is paid in this tp 0 = no, 1 = yes
        if (first_path) {
            path_counter = new ArrayList<Integer>(Collections.nCopies(height, 0)); // height is size of arraylist
            first_path = false;
        }
        ArrayList<Integer> branch = new ArrayList<>(line);
        Node n = root;
        for (int i=0; i<path_counter.size(); i++) {
            n = (path_counter.get(i) == 0) ? n.left : n.right; // left node is 0, right node is 1 (see new_leafs function)
            if (n != null) {
                branch.add(n.idea_idx);
            } else {
                next_path(i); // i represents index of leaf in path_counter arraylist (last non-null Node)
                i = path_counter.size(); // break out of for loop
            }
        }
        return branch;
    }

    // "...01111" --> "...10000" as next path
    void next_path(int last_index) {
        Node n = get_Node(path_counter);
        int idx = last_index;
        while (n.right == null) { // find divergence node, assuming root.right != null
            n = n.parent;
            idx--;
        }

        n = n.right; // since we came up to divergence node from left, now we proceed right
        idx++;
        path_counter.set(idx, 1);

        while (n.left != null) { // once divergence found, loop through default 0 --> first path we should take since divergence node
            n = n.left;
            idx++;
            path_counter.set(idx, 0);
        }
    }

    Node get_Node(ArrayList<Integer> path) {
        Node n = root;
        Node temp;
        for (int i : path) {
            if (i == 0) {
                temp = n.left;
            } else { // i==1
                temp = n.right;
            }

            if (temp == null) { // path is dead, go back to parent
                break;
            } else {
                n = temp;
            }
        }
        return n;
    }
}

class Node {
    int idea_idx;
    int k; // k = 0 if don't learn, k = 1 if learn
    Node left;
    Node right;
    Node parent;

    Node(int idea_idx, int k, Node parent) {
        this.idea_idx = idea_idx;
        this.k = k;
        right = null;
        left = null;
        this.parent = parent;
    }
}