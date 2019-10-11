import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;

// helper class with functions for Optimize/Smart_Optimize classes
class Opt_Func {
    // multiple two arraylists of same length
    static ArrayList<Integer> np_mult(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {
        Integer[] arr3 = new Integer[arr1.size()];
        for (int i = 0; i<arr3.length; i++) {
            arr3[i] = arr1.get(i) * arr2.get(i);
        }
        return new ArrayList<>(Arrays.asList(arr3));
    }

    static ArrayList<Double> int_minus_np(double num, ArrayList<Integer> arr) {
        Double[] out = new Double[arr.size()];
        for (int i = 0; i<out.length; i++) {
            out[i] = num - arr.get(i);
        }
        return new ArrayList<>(Arrays.asList(out));
    }

    // +1 ensures that each idea a scientist works on will have at least 1 unit of marg effort
    // max(curr_k[np.where(curr_k <= sci.avail_effort - 1)]) + 1
    static LinkedList<Integer> np_where_lesseq(ArrayList<Integer> arr, double target) {
        LinkedList<Integer> out = new LinkedList<>();
        for (int x : arr) {
            if (x <= target) {
                out.add(x);
            }
        }
        return out;
    }

    static ArrayList<Integer> arr_double_to_int(ArrayList<Double> arr) {
        ArrayList<Integer> out = new ArrayList<>();
        for (Double d : arr) {
            out.add((int) Math.round(d));
        }
        return out;
    }

    static void set_zero(ArrayList<Integer> arr, ArrayList<Integer> idx) { // arr and idx should have same length
        for (int i=0; i<idx.size(); i++) {
            if (idx.get(i) == 0) { // idea hasn't been discovered
                arr.set(i, 0);
            }
        }
    }

    // returns amount of effort for each idea that scientist allocates positive effort into given which "bin" effort is allocated to
    // branch is the index of the idea, multiplier is the chunk, each element of bin represents one "chunk" of effort
    static HashMap<Integer, Double> eff_chunker(int[] bin, ArrayList<Integer> branch, double increment) {
        HashMap<Integer, Double> out = new HashMap<>();
        for (int idea_finder : bin) {
            int idea_idx = branch.get(idea_finder);
            out.merge(idea_idx, increment, (x, y) -> x + y); // increments value if key exists, otherwise just add key,value
        }
        return out;
    }
}