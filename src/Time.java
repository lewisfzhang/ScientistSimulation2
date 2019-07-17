public class Time {
    public long runtime; // keeps track of the starting time
    public long pause; // keeps track of time paused
    public String name;

    public Time(String name) {
        this.runtime = System.currentTimeMillis();
        this.pause = 0;
        this.name = name;
    }

    public void stop_time() {
        long stop = System.currentTimeMillis();
        double time_elapsed = (stop - this.runtime)/1000.0;
        System.out.printf("%n%s elapsed runtime: %.3f seconds%n", this.name, time_elapsed);
        this.runtime = stop;
    }

    public void pause_time() {
        this.pause = System.currentTimeMillis();
    }

    public void resume_time() {
        long time = System.currentTimeMillis();
        if (this.pause == 0) {
            this.runtime = time; // we want time to actually start here (pause_time() has never been called yet)
        } else {
            long pause_length = time - this.pause;
            this.runtime += pause_length; // adds the length of pause, which cancels out when stop_time() is called
        }
    }

}
