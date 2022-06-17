package com.home.filesorter.types;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class DefaultType extends Type {
    final String OTHER_FOLDER = "Other";

    DefaultType(Path path){
        super(path);
        basicType = "default";
    }

    @Override
    void initMetadata() {
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        }
        catch (ImageProcessingException | IOException ex){
        }
    }

    @Override
    public Path getFolder(boolean isDateByMonth) {
        LocalDate date = getDateFromCommonMeta();
        try {
            if (date == null) {
                date = getAttributeDate(path);
            }
        } catch (IOException | DateTimeParseException e) {
            e.printStackTrace();
        }
        if (date != null) {
            return folder.resolve(OTHER_FOLDER).resolve(convertLocalDateToString(date, isDateByMonth));
        }
        return folder.resolve(OTHER_FOLDER);
    }
}
