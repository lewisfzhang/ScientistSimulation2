public class Run {
    public static void main(String[] args) {
        // GO TO CONFIG TO SET PARAMETERS FOR MODEL RUN BEFORE PROCEEDING
        boolean run_model = true;
        boolean save_model = false;
        boolean use_collect = true; // determines whether analytics on scientist performance will be saved
        boolean append_data = false; // if true, adds a new set of collect data onto existing training data for neural net
        boolean train_data = false; // NOTE: if you want to use neural or funding, this must be false --> to change neural or funding, go to Config class file

        Config config = new Config(train_data);
        Time tot = new Time("total time");
        Model model;
        Collect collect;

        if (run_model) {
            model = new Model(config);

            for (int i = 0; i < config.time_periods; i++) {
                System.out.printf("%nstep%d", i);
                Time t = new Time("model step");
                model.step();
                t.stop_time();
            }

            if (save_model) Functions.serialize_model(model, config);
        } else model = Functions.deserialize_model(config);

        if (use_collect) {
            collect = new Collect(model);
            Time t = new Time("collect");

            System.out.print("\ncollect_data()");
            collect.collect_data();
            t.stop_time();

            System.out.print("\nneural_net_data()");
            collect.neural_net_data(append_data);
            t.stop_time();

            double performance = collect.scientist_performance();
            System.out.print("\nreturns per scientist = " + performance);

            tot.stop_time();
        }
    }
}