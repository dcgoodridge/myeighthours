package myeighthours;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;


public class WorkState {

    private static final Logger LOG = LoggerFactory.getLogger(WorkState.class);

    public static final WorkState UNKNOWN = new Builder().state(STATE.UNKNOWN).build();

    public enum STATE {
        WORKING, RESTING, UNKNOWN
    }

    private final STATE state;

    private final Instant timeStart;

    private final Instant timeFinish;

    private final float progress;

    private WorkState(Builder builder) {
        this.state = builder.state;
        this.timeStart = builder.timeStart;
        this.timeFinish = builder.timeFinish;
        this.progress = builder.progress;
    }

    public STATE getState() {
        return state;
    }

    public long getTimeStart() {
        return timeStart.toEpochMilli();
    }

    public long getTimeFinish() {
        return timeFinish.toEpochMilli();
    }

    public float getProgress() {
        return progress;
    }

    public static class Builder {

        private STATE state = STATE.UNKNOWN;

        private Instant timeStart;

        private Instant timeFinish;

        private float progress;

        public Builder() {}

        public Builder(WorkState builder) {
            this.state = builder.state;
            this.timeStart = builder.timeStart;
            this.timeFinish = builder.timeFinish;
            this.progress = builder.progress;
        }

        public Builder state(STATE state) {
            this.state = state;
            return this;
        }

        public Builder timeStart(long timeStart) {
            this.timeStart = Instant.ofEpochMilli(timeStart);
            return this;
        }

        public Builder timeFinish(long timeFinish) {
            this.timeFinish = Instant.ofEpochMilli(timeFinish);
            return this;
        }

        public Builder progress(float progress) {
            this.progress = progress;
            return this;
        }

        public WorkState build() {
            return new WorkState(this);
        }

    }

}
