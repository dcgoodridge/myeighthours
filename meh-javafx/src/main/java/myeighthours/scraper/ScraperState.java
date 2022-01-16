package myeighthours.scraper;


import javafx.concurrent.Worker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ScraperState {

    private static final Logger LOG = LoggerFactory.getLogger(ScraperState.class);

    public static final ScraperState UNKNOWN = new Builder().build();

    private final long timeStart;

    private final long timeFinish;

    private final Worker.State state;

    private ScraperState(Builder builder) {
        this.timeStart = builder.timeStart;
        this.timeFinish = builder.timeFinish;
        this.state = builder.state;
    }

    public Worker.State getState() {
        return state;
    }

    public long getTimeStart() {
        return timeStart;
    }

    public long getTimeFinish() {
        return timeFinish;
    }

    public static class Builder {
        private long timeStart;
        private long timeFinish;
        private Worker.State state = Worker.State.READY;

        public Builder() {}

        public Builder(ScraperState builder) {
            this.state = builder.state;
            this.timeStart = builder.timeStart;
            this.timeFinish = builder.timeFinish;
        }

        public Builder state(Worker.State state) {
            this.state = state;
            return this;
        }

        public Builder timeStart(long timeStart) {
            this.timeStart = timeStart;
            return this;
        }

        public Builder timeFinish(long timeFinish) {
            this.timeFinish = timeFinish;
            return this;
        }

        public ScraperState build() {
            return new ScraperState(this);
        }

    }

}
