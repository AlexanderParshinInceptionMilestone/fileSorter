package com.home.filesorter.types;

import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.avi.AviMetadataReader;
import com.drew.imaging.mp4.Mp4MetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.avi.AviDirectory;
import com.drew.metadata.mp4.Mp4Directory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.TimeZone;

public class Avi extends Type{

    private int dateTag;

    Avi (Path path) {
        super(path);
        basicType = "video";
        dateTag = AviDirectory.TAG_DATETIME_ORIGINAL;
    }

    @Override
    void initMetadata() {
        try {
            metadata = AviMetadataReader.readMetadata(file);
        }
        catch (ImageProcessingException | IOException ex){
            System.out.println("no access to metadata : " + file.getName());
        }
    }

    @Override
    public Path getFolder(boolean isDateByMonth) throws IOException {
        LocalDate date = getDateFromAviMeta();
        if (date != null && !isDateOld(date)) {
            return folder.resolve(convertLocalDateToString(date, isDateByMonth));
        }
        return folder.resolve(UNDEFINE_FOLDER);
    }

    private LocalDate getDateFromAviMeta(){
        Directory directory = metadata.getFirstDirectoryOfType(AviDirectory.class);
        if (directory != null && directory.containsTag(dateTag)) {
            Date rawDate = directory.getDate(dateTag, TimeZone.getDefault());
            return rawDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }
        return null;
    }
}
