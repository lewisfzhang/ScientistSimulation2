import java.util.*;

public class Scientist {
    public int id;
    public int age;
    public HashMap<Integer, Idea> aspects;
    public double learning_speed;
    public double idea_max_var;
    public double idea_mean_var;
    public int avail_eff;

    public ArrayList<Integer> idea_eff_tp; // effort invested in each idea per time period
    public ArrayList<Integer> idea_eff_all; // effort invested in each idea across all time periods
}