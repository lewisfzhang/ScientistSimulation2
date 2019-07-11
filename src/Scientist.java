import java.util.HashMap;

public class Scientist {
    public int id;
    public int age;
    public HashMap<Integer, HashMap<String, Integer>> aspects;
    public double learning_speed;
    public double idea_max_var;
    public double idea_mean_var;
    public int avail_eff;

    public Model model;
    public int[] idea_eff_tp; // effort invested in each idea per time period
    public int[] idea_eff_all; // effort invested in each idea across all time periods

    public Scientist(Model model) {
        this.model = model;
    }
    public void step() {
        int num_ideas = model.idea_list.size();
        idea_eff_tp = new int[num_ideas];
        idea_eff_all = new int[num_ideas];


    }
}