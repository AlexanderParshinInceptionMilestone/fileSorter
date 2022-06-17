package com.home.filesorter.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TypeFactory {
    public Type getType(Path path){
        String type = getMimeType(path);
        if(type == null || type.isEmpty()){
            return new DefaultType(path);}
        switch (type) {
            case "image/jpeg":
            case "image/tiff":
            case "image/arw":
            case "image/cr2":
            case "image/nef":
            case "image/orf":
            case "image/rw2":
                return new Image(path);
            case "video/avi":
                return new Avi(path);
            case "video/mp4":
            case "video/mov":
            case "video/3gpp":
                return new Mp4(path);
            default:
                return new DefaultType(path);
        }
    }

    private String getMimeType (Path path) {
        String fileType = "";
        try {
            fileType = Files.probeContentType(path);
        } catch (IOException e) {
            try {
                throw new IOException(String.format("undefined MIME type %s", path.toString()));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return fileType;
    }
}