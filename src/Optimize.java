import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Collections;

// helper class with functions for Optimize class
class Opt_Func {
    // multiple two arraylists of same length
    static ArrayList<Integer> np_mult(ArrayList<Integer> arr1, ArrayList<Integer> arr2) {
        Integer[] arr3 = new Integer[arr1.size()];
        for (int i = 0; i<arr3.length; i++) {
            arr3[i] = arr1.get(i) * arr2.get(i);
        }
        return new ArrayList<>(Arrays.asList(arr3));
    }

    static ArrayList<Integer> int_minus_np(int num, ArrayList<Integer> arr) {
        Integer[] out = new Integer[arr.size()];
        for (int i = 0; i<out.length; i++) {
            out[i] = num - arr.get(i);
        }
        return new ArrayList<>(Arrays.asList(out));
    }

    // +1 ensures that each idea a scientist works on will have at least 1 unit of marg effort
    // max(curr_k[np.where(curr_k <= sci.avail_effort - 1)]) + 1
    static LinkedList<Integer> np_where_lesseq(ArrayList<Integer> arr, int target) {
        LinkedList<Integer> out = new LinkedList<>();
        for (int i=0; i<arr.size(); i++) {
            if (arr.get(i) <= target) {
                out.add(arr.get(i));
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
}

public class Optimize {
    static HashMap<String, ArrayList<Integer>> investing_helper(Scientist sci) {
        // df that keeps track of scientist's transactions within current time period
        // for k_paid: 0 if already paid, # 1 if paid this period
        HashMap<String, ArrayList<Integer>> inv_dict = new HashMap<>();
        inv_dict.put("idea_idx", new ArrayList<>());
        inv_dict.put("marg_eff", new ArrayList<>());
        inv_dict.put("k_paid", new ArrayList<>());

        // ARRAY: extra cost for each idea, which is a column in the scientist.perceived_returns df
        ArrayList<Integer> k = Opt_Func.arr_double_to_int(sci.perceived_rewards.get("Idea K"));

        // k for ideas that haven't been discovered will be zero, arraylist k is updated through void function
        Opt_Func.set_zero(k, sci.discov_ideas);

        // ARRAY: same logic as idea_k_paid_tot where 0 = haven't learned, 1 = learned, copy over old ArrayList
        ArrayList<Integer> k_paid_present = new ArrayList<>(sci.ideas_k_paid_tot);

        // keeps on investing while scientist has available effort
        while (sci.avail_effort > 0) {
            // k_paid is 0 if scientist hasn 't paid learning cost
            // True = 1, False = 0-- > so if scientist hasn 't paid learning cost, curr_k = 1 * k
            ArrayList<Integer> curr_k = Opt_Func.np_mult(k_paid_present, k);

            // SCALAR: increment based on ONLY ideas where a scientist is able
            // to invest research effort after entering learning barrier
            // +1 ensures that each idea a scientist works on will have at least 1 unit of marg effort
            // max(curr_k[np.where(curr_k <= sci.avail_effort - 1)]) + 1
            int increment = Collections.max(Opt_Func.np_where_lesseq(curr_k, sci.avail_effort - 1)) + 1;

            // given available effort a scientist left, all of them have been learned
            // -- > scientist invest all remaining effort on research given above belief
            // ASSUMPTION: scientist can only research an idea he can fully invest in
            if (increment == 1) {
                increment = sci.avail_effort;
            }

            // ARRAY: marg effort for each idea
            sci.marg_eff = Opt_Func.int_minus_np(increment, curr_k);

            // choosing which optimization to use
            int idea_idx = greedy_returns(sci);

            k_paid_present.set(idea_idx, 1);  // mark down that the scientist has paid learning cost for this idea

            inv_dict = update_df(inv_dict, idea_idx, sci); // record transaction

            sci.avail_effort -= increment;
        }
        return inv_dict; // returns all transactions scientist has made in this tp

    }

    static int greedy_returns(Scientist sci) {
        ArrayList<Double> mean = sci.perceived_rewards.get("Idea Mean");
        ArrayList<Double> sds = sci.perceived_rewards.get("Idea SDS");
        ArrayList<Double> max = sci.perceived_rewards.get("Idea Max");
        double max_rtn = 0;
        int max_idx = 0;

        for (int idx=0; idx<sci.discov_ideas.size(); idx++) {
            if (sci.discov_ideas.get(idx) == 1) { // only iterate through discovered ideas
                Idea i = sci.model.idea_list.get(idx);
                int start_idx = i.total_effort;
                int end_idx = start_idx + sci.marg_eff.get(idx);
                double rtn = Idea.get_returns(mean.get(idx), sds.get(idx), max.get(idx), start_idx, end_idx);
                if (rtn > max_rtn) {
                    max_rtn = rtn;
                    max_idx = idx;
                }
            }
        }
        return max_idx;
    }

    // adds the current transaction to the list of transactions
    static HashMap<String, ArrayList<Integer>> update_df(HashMap<String, ArrayList<Integer>> inv_dict, int idea_idx, Scientist sci) {
        // checks if idea_idx is already in the df
        int idx = inv_dict.get("idea_idx").indexOf(idea_idx);

        if (idx == -1) { // if idea_idx is not in df
            inv_dict.get("idea_idx").add(idea_idx);
            inv_dict.get("marg_eff").add(sci.marg_eff.get(idea_idx));
            // for k_paid, same logic as sci.curr_k calculation
            // assuming this array hasn't changed since start of tp
            // goal is to keep track of which ideas the scientist learned in this period
            // (or in other words, the ones they hadn't learned before this time period)
            inv_dict.get("k_paid").add((sci.ideas_k_paid_tot.get(idea_idx) == 0) ? 1 : 0); // true = 1, false = 0
        } else { // if idea already exists in df
            // idea_idx and k_paid do not need to be updated since they were already established in initial entry
            Functions.arr_increment_int(inv_dict.get("marg_eff"), idx, sci.marg_eff.get(idea_idx));
        }
        return inv_dict;
    }

}
