package myeighthours.scraper;

import javafx.concurrent.Task;
import myeighthours.Fichaje;

import java.util.List;

public abstract class WebscraperTaskAbstract extends Task<List<Fichaje>> {

    public abstract long getTimestampFinish();

    public abstract long getTimestampStart();

}
