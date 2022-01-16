package myeighthours;


import myeighthours.scraper.ScraperState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MehServiceState {

    private static final Logger LOG = LoggerFactory.getLogger(MehServiceState.class);

    public static final MehServiceState UNKNOWN = new MehServiceState.Builder().crawlerstate(ScraperState.UNKNOWN).workstate(WorkState.UNKNOWN).build();

    private final WorkState workState;

    private final ScraperState scraperState;

    private MehServiceState(Builder builder) {
        this.workState = builder.workState;
        this.scraperState = builder.scraperState;
    }

    public WorkState getWorkState() {
        return workState;
    }

    public ScraperState getScraperState() {
        return scraperState;
    }

    public static class Builder {
        private WorkState workState = WorkState.UNKNOWN;
        private ScraperState scraperState = ScraperState.UNKNOWN;

        public Builder() {
        }

        public Builder(MehServiceState builder) {
            this.workState = builder.workState;
            this.scraperState = builder.scraperState;
        }

        public Builder workstate(WorkState workState) {
            this.workState = workState;
            return this;
        }

        public Builder crawlerstate(ScraperState scraperState) {
            this.scraperState = scraperState;
            return this;
        }

        public MehServiceState build() {
            return new MehServiceState(this);
        }

    }

}
