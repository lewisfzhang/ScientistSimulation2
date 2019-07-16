public class Idea {
    // instrinsic variables
    public int idea_mean; // aka inflection point
    public int idea_max; // multiple, curve goes from 0 - M
    public int idea_sds; 
    public int idea_k; // entry cost/barrier to enter an idea

    // data collecting variables
    public int total_effort; // Total amount of effort invested in idea to that point
    public int num_k; // number of scientists who have paid K for the idea and have the ability to invest
    
    public void init(Model model) {
    	this.idea_mean = 0; // NEED: poisson with lambda from model
    	this.idea_max = 0; // NEED: poisson with max from model
    	this.idea_sds = 0; // NEED: poisson with sds from model
    	this.idea_k = 0; // NEED: poisson with k mean from model
    	// create_idea_collectors(model.time_periods);
    }
}
