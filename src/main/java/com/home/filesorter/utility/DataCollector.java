package com.home.filesorter.utility;
import com.home.filesorter.types.Type;
import com.home.filesorter.types.TypeFactory;
import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class DataCollector {
    private static Data data;
    private static TypeFactory typeFactory;
    public static final String VIDEO = "video";
    public static final String IMAGE = "image";

    private DataCollector() throws Exception {
        throw new Exception("Not intended for instantiation");
    }

    public static Data collect(String directory){
        data = new Data();
        typeFactory = new TypeFactory();
        Path dir = Paths.get(directory);
        try {
            Files.walkFileTree(dir, new SimpleFileVisitor<>(){
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    collectData(file);
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private static void collectData (Path path) {
        Type type = typeFactory.getType(path);
        long fileSize = getFileSize(path);
        if(type.getBasicType().equals(VIDEO)) {
            data.addVideo();
            data.setVideoSize(fileSize);
            return;
        }
        if(type.getBasicType().equals(IMAGE)) {
            data.addImage();
            data.setImageSize(fileSize);
            return;
        }
        data.addOther();
        data.setOtherSize(fileSize);
    }

    public static long getFileSize(Path path){
        File file = new File(path.toString());
        long size = FileUtils.sizeOf(file);
        return size;
    }

    public static String getFileType(Path path){
        String fileType = "";
        try {
            String contentType = Files.probeContentType(path);
            if(contentType != null){
                fileType = contentType.replaceAll("/.*", "").trim();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fileType;
    }
}

