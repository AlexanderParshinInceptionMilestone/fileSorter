package com.home.filesorter.types;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;

public class Image extends Type {

    public Image(Path path){
        super(path);
        basicType = "image";
    }

    @Override
    void initMetadata() {
        try {
            metadata = ImageMetadataReader.readMetadata(file);
        }
        catch (ImageProcessingException | IOException ex){
            System.out.println("no access to metadata : " + file.getName());
        }
    }

    @Override
    public Path getFolder(boolean isDateByMonth) {
        LocalDate date = getDateFromCommonMeta();
        if (date != null) {
            return folder.resolve(convertLocalDateToString(date, isDateByMonth));
        }
        return folder.resolve(UNDEFINE_FOLDER);
    }
}
