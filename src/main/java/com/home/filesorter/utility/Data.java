package com.home.filesorter.utility;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Locale;

public class Data {
    private int video;
    private int image;
    private int other;
    private long videoSize;
    private long imageSize;
    private long otherSize;

    public Data(){
    }

    public int getVideos() {
        return video;
    }

    public void addVideo() {
        this.video = video + 1;
    }

    public int getImages() {
        return image;
    }

    public void addImage() {
        this.image = image + 1;
    }

    public int getOthers() {
        return other;
    }

    public void addOther() {
        this.other = other + 1;
    }

    public int getTotalFiles() {
        return video + image + other;
    }

    public double getVideoSizeMbi() {
        return getMbiSize(videoSize);
    }

    public long getRawVideoSize() {
        return videoSize;
    }

    public void setVideoSize(long videoSize) {
        this.videoSize += videoSize;
    }

    public double getImageSizeMbi() {
        return getMbiSize(imageSize);
    }

    public long getRawImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize += imageSize;
    }

    public double getOtherSizeMbi() {
        return getMbiSize(otherSize);
    }

    public long getRawOtherSize() {
        return otherSize;
    }

    public void setOtherSize(long otherSize) {
        this.otherSize += otherSize;
    }

    public long getTotalSize(){
        long total = videoSize + imageSize + otherSize;
        return total;
    }

    public String getHRVideoSize (){
        return humanReadableByteCount(videoSize);
    }

    public String getHRImageSize (){
        return humanReadableByteCount(imageSize);
    }

    public String getHROtherSize (){
        return humanReadableByteCount(otherSize);
    }

    public String getHRTotalSize (){
        return humanReadableByteCount(getTotalSize());
    }

    public static String humanReadableByteCount(long bytes) {
        long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
        if (absB < 1024) {
            return bytes + " B";
        }
        long value = absB;
        CharacterIterator ci = new StringCharacterIterator("KMGTPE");
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
            ci.next();
        }
        value *= Long.signum(bytes);
        return String.format(Locale.US,"%.1f %cB", value / 1024.0, ci.current());
    }

    public double getMbiSize(long bytes) {
        long totalSizeFactor = videoSize + imageSize + otherSize;
        long absB = totalSizeFactor == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(totalSizeFactor);
        if (absB < 1024) {
            return bytes;
        }
        long value = bytes;
        for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
            value >>= 10;
        }
        value *= Long.signum(bytes);
        return value / 1024.0;
    }
}
