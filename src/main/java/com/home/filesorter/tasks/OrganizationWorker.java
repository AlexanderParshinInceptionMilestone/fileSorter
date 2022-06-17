package com.home.filesorter.tasks;
import com.home.filesorter.types.Type;
import com.home.filesorter.types.TypeFactory;
import com.home.filesorter.utility.*;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import org.apache.commons.lang3.StringUtils;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.DosFileAttributeView;
import java.nio.file.attribute.DosFileAttributes;
import java.time.LocalDate;


public class OrganizationWorker extends Task {
    private final boolean IS_COPIED;
    private final boolean SORT_BY_MONTH;
    private final boolean SORT_HIDDEN;
    private final boolean SORT_BY_VIDEO;
    private final boolean SORT_BY_IMAGE;
    private final boolean SORT_BY_OTHER;
    private final String VIDEO_TYPE = "video";
    private final String IMAGE_TYPE = "image";
    private DateExtractor dateExtractor;
    private Data data;
    private Path sourceDirectory;
    private Path destinationDirectory;
    private long totalFilesSize;
    private long progressFilesSize;
    private TypeFactory typeFactory;

    private IntegerProperty totalFiles;
    private IntegerProperty copiedFiles;
    private IntegerProperty failFiles;
    private IntegerProperty interruptedFiles;
    private ObservableList<FileInfo> failFilesList;
    private boolean wait;

    public OrganizationWorker(boolean copy, boolean monthSort, boolean videoSort, boolean imageSort, boolean hiddenSort, boolean otherSort){
        this.IS_COPIED = copy;
        this.SORT_BY_MONTH = monthSort;
        this.SORT_HIDDEN = hiddenSort;
        this.SORT_BY_VIDEO = videoSort;
        this.SORT_BY_IMAGE = imageSort;
        this.SORT_BY_OTHER = otherSort;
        dateExtractor = new DateExtractor();
        totalFiles = new SimpleIntegerProperty();
        copiedFiles = new SimpleIntegerProperty();
        failFiles = new SimpleIntegerProperty();
        interruptedFiles = new SimpleIntegerProperty();
        failFilesList = FXCollections.observableArrayList();
        typeFactory = new TypeFactory();
    }

    @Override
    protected Object call() {
        organize();
        return null;
    }

    private void organize() {
        updateFilesSize();
        updateFilesCount();
        this.updateProgress(progressFilesSize, totalFilesSize);
        try {
            Files.walkFileTree(sourceDirectory, new SimpleFileVisitor<>() {

                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    updateMessage(file.toString());
                    sort(file);
                    if(isCancelled()){
                        return FileVisitResult.TERMINATE;
                    }
                    return FileVisitResult.CONTINUE;
                }
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            failFiles.set(failFiles.get() + 1);
                        }
                    });
                    failFilesList.add(new FileInfo(file.toString(), FileStatus.FAILED));
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private void sort(Path file) throws IOException {
        Path folder;
        Type type = typeFactory.getType(file);
        if (isSorted(type)) {
            folder = type.getFolder(SORT_BY_MONTH);
            Path destinationDir = destinationDirectory.resolve(folder);
            Path destinationPath = destinationDir.resolve(file.getFileName());
            createDirectory(destinationDir);
            if (IS_COPIED) {
                copy(file, destinationPath);
            } else {
                copy(file, destinationPath);
                try {
                    DosFileAttributes attr = Files.readAttributes(file, DosFileAttributes.class);
                    if(attr.isReadOnly()){
                        DosFileAttributeView dos = Files.getFileAttributeView(file,DosFileAttributeView.class);
                        dos.setReadOnly(false);
                    }
                    Files.delete(file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            Platform.runLater(() -> {
                copiedFiles.set(copiedFiles.get() + 1);
            });
        }
    }

    private boolean isSorted(Type type){
        boolean sortHidden = isHidden(type.getPath()) ? SORT_HIDDEN : true;
        String basicType = type.getBasicType();
        if((SORT_BY_VIDEO || SORT_BY_IMAGE || SORT_BY_OTHER) && sortHidden) {
            if (basicType.equals(VIDEO_TYPE)) {
                return SORT_BY_VIDEO;
            }
            if (basicType.equals(IMAGE_TYPE)) {
                return SORT_BY_IMAGE;
            }
            return SORT_BY_OTHER;
        }
        return sortHidden;
    }

    private void copy(Path source, Path destination) {
        File sourceFile = new File(source.toString());
        File destinationFile = new File(destination.toString());
        try (InputStream in = new FileInputStream(sourceFile); OutputStream out = new FileOutputStream(destinationFile)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                synchronized (this) {
                    if (wait) {
                        try {
                            this.wait();
                        } catch (InterruptedException e) {
                            failFilesList.add(new FileInfo(source.toString(), FileStatus.INTERRUPTED));
                            Platform.runLater(new Runnable() {
                                @Override
                                public void run() {
                                    interruptedFiles.set(1);
                                }
                            });
                            wait = true;
                            this.cancel();
                        }
                    }
                }
                if (isCancelled()) {
                    break;
                }
                out.write(buf, 0, len);
                progressFilesSize = progressFilesSize + len;
                this.updateProgress(progressFilesSize, totalFilesSize);
            }
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            failFiles.set(failFiles.get() + 1);
            failFilesList.add(new FileInfo(source.toString(), FileStatus.FAILED));
            ex.printStackTrace();
        }
    }

    private void updateFilesSize(){
        if (SORT_BY_VIDEO || SORT_BY_IMAGE || SORT_BY_OTHER) {
            if (SORT_BY_VIDEO) {
                totalFilesSize += data.getRawVideoSize();
            }
            if (SORT_BY_IMAGE) {
                totalFilesSize += data.getRawImageSize();
            }
            if (SORT_BY_OTHER) {
                totalFilesSize += data.getRawOtherSize();
            }
        }
        else {
            totalFilesSize = data.getTotalSize();
        }
    }

    private void updateFilesCount(){
        totalFiles.set(data.getTotalFiles());
    }

    private boolean isHidden(Path file) {
        boolean isHidden = false;
        DosFileAttributeView dos = Files.getFileAttributeView(file, DosFileAttributeView.class);
        try {
            isHidden = dos.readAttributes().isHidden();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return isHidden;
    }

    private Path getCreationDate(Path file){
        Path path = Path.of("undefined");
        try {
            LocalDate localDate = dateExtractor.getDate(file);
            if (localDate != null) {
                String month = StringUtils.capitalize(localDate.getMonth().name().toLowerCase());
                String year = String.valueOf(localDate.getYear());
                if (SORT_BY_MONTH) {
                    path = Path.of(year).resolve(month);
                } else {
                    path = Path.of(year);
                }
            }
        }
        catch (Exception exception){
        }
        return path;
    }

    private void createDirectory(Path path){
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                try {
                    throw new Exception("Enable to create directory " + path);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void setSourceDirectory (String sourceDir) {
        Path sourceDirectory = Path.of(sourceDir);
        if(Files.isDirectory(sourceDirectory)) {
            this.sourceDirectory = sourceDirectory;
        }
    }
    public void setDestinationDirectory (String destinationDir) {
        Path destinationDirectory = Path.of(destinationDir);
        if(Files.isDirectory(destinationDirectory)) {
            this.destinationDirectory = destinationDirectory;
        }
    }
    public void setData(Data data) {
        this.data = data;
    }

    public IntegerProperty copiedFilesProperty() {
        return copiedFiles;
    }


    public IntegerProperty failFileProperty() {
        return failFiles;
    }

    public IntegerProperty interruptedFilesProperty() {
        return interruptedFiles;
    }

    public int getTotalFiles() {
        return totalFiles.get();
    }

    public IntegerProperty totalFilesProperty() {
        return totalFiles;
    }

    public ObservableList<FileInfo> getFailFilesList() {
        return failFilesList;
    }

    public void pause (){
        wait = true;
    }

    public void resume (){
        synchronized (this) {
            wait = false;
            this.notify();
        }
    }
}

