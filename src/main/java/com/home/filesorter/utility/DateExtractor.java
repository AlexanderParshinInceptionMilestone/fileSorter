package com.home.filesorter.utility;
import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.avi.AviMetadataReader;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.drew.metadata.mp4.Mp4Directory;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.TimeZone;


public class DateExtractor {

    public LocalDate getDate(Path file) throws Exception {
        LocalDate date = getMetaDate(file);
        if(date == null){
            date = getAttributeDate(file);
        }
        return date;
    }

    private LocalDate getMetaDate(Path path) {
        LocalDate date = null;
        File file = new File(path.toString());
        String fileType = DataCollector.getFileType(path);
        Metadata metadata;
        Directory directory = null;
        int dateTag = 0;
        try {
            if (fileType.equals(DataCollector.IMAGE)) {
                metadata = ImageMetadataReader.readMetadata(file);
                directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
                dateTag = ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL;
            }
            if (fileType.equals(DataCollector.VIDEO)) {
                String videoType = detectVideoType(path);
                if(videoType.equals("mp4")||videoType.equals("mov")) {
                    metadata = Mp4MetadataReader.readMetadata(file);
                    directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
                    dateTag = Mp4Directory.TAG_CREATION_TIME;
                }
                if(videoType.equals("avi")) {
                    metadata = AviMetadataReader.readMetadata(file);
                    directory = metadata.getFirstDirectoryOfType(AviDirectory.class);
                    dateTag = AviDirectory.TAG_DATETIME_ORIGINAL;
                }
            }
            if (directory != null && directory.containsTag(dateTag)) {
                Date rawDate = directory.getDate(dateTag, TimeZone.getDefault());
                date = rawDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                System.out.println(rawDate);
            }
        } catch (Exception ex) {
            return null;
        }
        return date;
    }

    private LocalDate getAttributeDate(Path path) {
        LocalDate date = null;
        try {
            BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
            FileTime fileTime = attr.creationTime();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'");
            date = LocalDate.parse(fileTime.toString(), formatter);
        }
        catch (IOException ex){
            ex.printStackTrace();
        }
        return date;
    }

    private String detectVideoType(Path path){
        String type = "";
        try (BufferedInputStream stream = new BufferedInputStream(new FileInputStream(path.toFile()))) {
            FileType fileType = FileTypeDetector.detectFileType(stream);
            type = fileType.getCommonExtension();
        }
        catch (IOException ex){

        }
        return type;
    }
}
