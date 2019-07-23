import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class BTree {
    int num_paths = 1; // keeps track of number of branches in this BTree object
    int max_weight; // scientist can only learn maximum of 3 new ideas out of all discovered ideas per tp
    int height = 0; // keeps track of the length of the longest path/branch in tree
    ArrayList<Node> leafs; // keeps track of all the leaves
    Node root = new Node(-1, 0, null); // dummy with idx of -1 to keep track of all ideas that have been discovered but not learned
    ArrayList<Integer> line = new ArrayList<>();

    BTree(ArrayList<Integer> k_paid, ArrayList<Integer> discov_ideas) { // k_paid = sci.k_paid_tot, both arraylists should have the same length
        this.max_weight = Config.max_weight;  // shouldn't need to be used since we only have ~2 new ideas each period
        // only for ideas we haven't paid k for yet
        for (int i=0; i<discov_ideas.size(); i++) {
            if (discov_ideas.get(i) == 1) {
                if (k_paid.get(i) == 0) { // idea has been discovered but not learned
                    int last_paths = num_paths;
                    new_leafs(root, 0, i);
                    if (last_paths != num_paths) {height += 1;} // only increase height if at least one new leaf was added
                } else { // idea has been discovered but already learned
                    line.add(i);
                }
            }
        }
        // BTreePrinter.printNode(root); // prints entire BTree out
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
                num_paths += 1;  // think as left node extents path of existing node, right node creates one unique new path
            } else if (curr_weight > max_weight) { // if curr_weight == max_weight, do nothing
                throw new IllegalArgumentException("weight should not be greater, something wrong");
            }
        }
    }

    ArrayList<Integer> path_counter; // keeps track of which path we are currently on based on Node.k value --> 0's, 1's
    boolean first_path = true;

    // should only return 4 possible branches (2 new ideas each period, max_weight shouldn't have to be used)
    // branch: returns a list of the indexes of the ideas based on model.idea_list (both already learned and new ideas that will be learned)
    ArrayList<Integer> next_branch(boolean isLast) { // every node in the tree, whether or not k is paid in this tp 0 = no, 1 = yes
        if (first_path) {
            path_counter = new ArrayList<Integer>(Collections.nCopies(height, 0)); // height is size of arraylist
            first_path = false;
        }
        ArrayList<Integer> branch = new ArrayList<>(line);
        Node n = root;
        int i = 0;// i represents index of leaf in path_counter arraylist (last non-null Node)
        while (i<path_counter.size()) {
//        for (int i=0; i<path_counter.size(); i++) {
            n = (path_counter.get(i) == 0) ? n.left : n.right; // left node is 0, right node is 1 (see new_leafs function)
            if (n != null) {
                if (n.k == 1) {
                    branch.add(n.idea_idx); // we only care about ideas that will be learned/have been learned?
                }
            } else {
                i--;
                break;
            }
            i++;
        }
        if (i == path_counter.size()) {i--;} // cancel last i++ since we reached end of tree
        if (!isLast) {next_path(i);} // if last branch, don't generate any more
        return branch;
    }

    // "...01111" --> "...10000" as next path, updates path_counter
    void next_path(int last_index) {
        Node n = get_Node(path_counter);
        int idx = last_index;
        while (n.right == null) { // find divergence node, assuming root.right != null
            n = n.parent;
            idx--;
        }


        while (path_counter.get(idx + 1) == 1) { // keep on moving back up the tree if we were already at right --> [0,1] --> [1,0]
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

        Functions.set_remain_zero(path_counter, idx+1); // set remaining branches to 0 --> Nodes from this point on are null
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

// retrieved from https://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
class BTreePrinter {

    static void printNode(Node root) {
        int maxLevel = BTreePrinter.maxLevel(root);

        printNodeInternal(Collections.singletonList(root), 1, maxLevel);
    }

    private static void printNodeInternal(List<Node> nodes, int level, int maxLevel) {
        if (nodes.isEmpty() || BTreePrinter.isAllElementsNull(nodes))
            return;

        int floor = maxLevel - level;
        int endgeLines = (int) Math.pow(2, (Math.max(floor - 1, 0)));
        int firstSpaces = (int) Math.pow(2, (floor)) - 1;
        int betweenSpaces = (int) Math.pow(2, (floor + 1)) - 1;

        BTreePrinter.printWhitespaces(firstSpaces);

        List<Node> newNodes = new ArrayList<Node>();
        for (Node node : nodes) {
            if (node != null) {
                System.out.print(node.idea_idx);
                newNodes.add(node.left);
                newNodes.add(node.right);
            } else {
                newNodes.add(null);
                newNodes.add(null);
                System.out.print(" ");
            }

            BTreePrinter.printWhitespaces(betweenSpaces);
        }
        System.out.println("");

        for (int i = 1; i <= endgeLines; i++) {
            for (int j = 0; j < nodes.size(); j++) {
                BTreePrinter.printWhitespaces(firstSpaces - i);
                if (nodes.get(j) == null) {
                    BTreePrinter.printWhitespaces(endgeLines + endgeLines + i + 1);
                    continue;
                }

                if (nodes.get(j).left != null)
                    System.out.print("/");
                else
                    BTreePrinter.printWhitespaces(1);

                BTreePrinter.printWhitespaces(i + i - 1);

                if (nodes.get(j).right != null)
                    System.out.print("\\");
                else
                    BTreePrinter.printWhitespaces(1);

                BTreePrinter.printWhitespaces(endgeLines + endgeLines - i);
            }

            System.out.println("");
        }

        printNodeInternal(newNodes, level + 1, maxLevel);
    }

    private static void printWhitespaces(int count) {
        for (int i = 0; i < count; i++)
            System.out.print(" ");
    }

    private static <T extends Comparable<?>> int maxLevel(Node node) {
        if (node == null)
            return 0;

        return Math.max(BTreePrinter.maxLevel(node.left), BTreePrinter.maxLevel(node.right)) + 1;
    }

    private static <T> boolean isAllElementsNull(List<T> list) {
        for (Object object : list) {
            if (object != null)
                return false;
        }

        return true;
    }

}
