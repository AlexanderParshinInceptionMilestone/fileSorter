package com.home.filesorter.types;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public class Mp4 extends Type{

    private int dateTag;

    Mp4(Path path){
        super(path);
        basicType = "video";
        dateTag = Mp4Directory.TAG_CREATION_TIME;
    }


    @Override
    void initMetadata() {
        try {
            metadata = Mp4MetadataReader.readMetadata(file);
        }
        catch (ImageProcessingException | IOException ex){
            System.out.println("no access to metadata : " + file.getName());
        }
    }

    @Override
    public Path getFolder(boolean isDateByMonth) throws IOException {
        LocalDate date = getDateFromMp4Meta();
        if (date != null && !isDateOld(date)) {
            return folder.resolve(convertLocalDateToString(date, isDateByMonth));
        }
        return folder.resolve(UNDEFINE_FOLDER);
    }

    private LocalDate getDateFromMp4Meta(){
        Directory directory = metadata.getFirstDirectoryOfType(Mp4Directory.class);
        if (directory != null && directory.containsTag(dateTag)) {
            Date rawDate = directory.getDate(dateTag, TimeZone.getDefault());
            return rawDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }
}
