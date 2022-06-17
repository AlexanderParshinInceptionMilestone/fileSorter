package com.home.filesorter.types;

import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Locale;

public abstract class Type {
    final int ORIGINAL_DATE_EXIF_SUB = 36867;
    final int DIGITIZED_DATE_EXIF_SUB = 36868;
    final int DATE_EXIF_DO = 306;
    final String UNDEFINE_FOLDER = "undefine";
    String basicType;
    Path folder;
    Path path;
    Metadata metadata;
    File file;

    Type(Path path) {
        this.path = path;
        file = new File(path.toString());
        folder = Paths.get("");
        initMetadata();
    }

    abstract void initMetadata();

    public abstract Path getFolder(boolean isDateByMonth) throws IOException;

    protected final LocalDate getDateFromCommonMeta() {
        LocalDate exifSub = getExifSubDate();
        LocalDate exifDo = getExifDoDate();
        if (exifDo != null) {
            return exifDo;
        } else if (exifSub != null) {
            return exifSub;
        }
        return null;
    }

    protected String convertLocalDateToString(LocalDate localDate, boolean isDateByMonth) {
        StringBuilder sb = new StringBuilder(String.valueOf(localDate.getYear()));
        if(isDateByMonth){
            String month = "\\".concat(StringUtils.capitalize(localDate.getMonth().name().toLowerCase(Locale.ROOT)));
            sb.append(month);
        }
        return sb.toString();
    }

    private LocalDate getExifDoDate() {
        Date date = null;
        if(metadata != null) {
            Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (directory != null) {
                if (directory.containsTag(DATE_EXIF_DO)) {
                    date = directory.getDate(DATE_EXIF_DO);
                }
            }
        }
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    private LocalDate getExifSubDate() {
        Date date = null;
        if(metadata != null) {
            Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            if (directory != null) {
                if (directory.containsTag(DIGITIZED_DATE_EXIF_SUB)) {
                    date = directory.getDate(DIGITIZED_DATE_EXIF_SUB);
                } else if (directory.containsTag(ORIGINAL_DATE_EXIF_SUB)) {
                    date = directory.getDate(ORIGINAL_DATE_EXIF_SUB);
                }
            }
        }
        return date != null ? date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate() : null;
    }

    public String getBasicType(){
        return basicType;
    }

    public Path getPath() {
        return path;
    }

    private Directory getExifDODir() {
        return metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
    }

    private Directory getExifSubDir() {
        return metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    }

    protected LocalDate getAttributeDate(Path path) throws IOException {
        LocalDate date;
        BasicFileAttributes attr = Files.readAttributes(path, BasicFileAttributes.class);
        FileTime fileTime = attr.creationTime();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
        date = LocalDate.parse(fileTime.toString(), formatter);
        return date;
    }

    protected boolean isDateOld(LocalDate date) {
        return date.isBefore(LocalDate.parse("1990-01-01"));
    }
}