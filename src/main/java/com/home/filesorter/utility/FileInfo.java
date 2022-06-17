package com.home.filesorter.utility;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.scene.control.Control;

public class FileInfo extends Control {

    private String path;
    private FileStatus status;

    public FileInfo(String path, FileStatus status){
        this.path = path;
        this.status = status;
    }

    public String getPath() {
        return path;
    }

    public FileStatus getStatus() {
        return status;
    }
}
