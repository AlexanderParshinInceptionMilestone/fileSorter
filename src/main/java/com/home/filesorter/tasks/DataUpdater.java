package com.home.filesorter.tasks;

import com.home.filesorter.utility.Data;
import com.home.filesorter.utility.DataCollector;
import javafx.concurrent.Task;

public class DataUpdater extends Task<Data> {
    private String directory;

    public DataUpdater(String directory) {
        this.directory = directory;
    }

    @Override
    protected Data call() throws Exception {
        return DataCollector.collect(directory);
    }
}
