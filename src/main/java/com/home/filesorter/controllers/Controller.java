package com.home.filesorter.controllers;
import com.home.filesorter.tasks.DataUpdater;
import com.home.filesorter.tasks.OrganizationWorker;
import com.home.filesorter.utility.*;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXToggleButton;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.addons.Indicator;
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.skins.DonutChartTileSkin;
import eu.hansolo.tilesfx.skins.StatusTileSkin;
import eu.hansolo.tilesfx.tools.FlowGridPane;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;
import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.nio.file.Paths;


public class Controller {
    @FXML
    private TextField sourceField;
    @FXML
    private TextField destinationField;
    @FXML
    private ToggleButton menuButton;
    @FXML
    private ToggleButton costumeButton;
    @FXML
    private ToggleButton infoButton;
    @FXML
    private JFXButton closeButton;
    @FXML
    private JFXButton sourceButton;
    @FXML
    private JFXButton destinationButton;
    @FXML
    private JFXButton skipButton;
    @FXML
    private Button yesButton;
    @FXML
    private Button noButton;
    @FXML
    private Label filePathLabel;
    @FXML
    private Label noDataLabel;
    @FXML
    private ImageView organizeAnimation;
    @FXML
    private ImageView organizeText;
    @FXML
    private ImageView waitingGif;
    @FXML
    private ImageView waitingLabel;
    @FXML
    private Button organizeButton;
    @FXML
    private Button stopButton;
    @FXML
    private FlowGridPane donutChartPane;
    @FXML
    private FlowGridPane statusIndicator;
    @FXML
    private ListView<FileInfo> failFilesList;
    @FXML
    private AnchorPane mainPanel;
    @FXML
    private AnchorPane configPanel;
    @FXML
    private AnchorPane infoPanel;
    @FXML
    private AnchorPane noDataPanel;
    @FXML
    private AnchorPane resultPanel;
    @FXML
    private AnchorPane cancelPane;
    @FXML
    private AnchorPane organizePane;
    @FXML
    private Pane ctrlFolderIcon;
    @FXML
    private ToggleButton toggleTypeButton;
    @FXML
    private JFXToggleButton copyFilter;
    @FXML
    private JFXToggleButton monthFilter;
    @FXML
    private JFXToggleButton hiddenFilter;
    @FXML
    private JFXToggleButton videoFilter;
    @FXML
    private JFXToggleButton imageFilter;
    @FXML
    private JFXToggleButton otherFilter;

    @FXML
    private Label ctrLabel;
    @FXML
    private ProgressBar progressBar;

    private OrganizationWorker organizationWorker;
    private ChartData chartData1;
    private ChartData chartData2;
    private ChartData chartData3;
    private Tile donutChartTile;
    private Tile statusBoard;
    private Data data;
    private DonutChartTileSkin donutChartTileSkin;
    private StatusTileSkin statusSkin;
    private ObservableList<ChartData> chartDataList;
    private LongProperty chartCtrCount;
    private boolean dataByFileType;
    private Thread copyThread;

    @FXML
    private void initialize() {
        createDonutChart();
        createStatusDashboard();
        menuButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            mainPanel.toFront();
        });
        costumeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            configPanel.toFront();
        });
        infoButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            infoPanel.toFront();
        });
        closeButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(copyThread != null && copyThread.isAlive()) {
                organizationWorker.pause();
                cancelPane.setVisible(true);
                cancelPane.toFront();
            }
            else {
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
                Platform.exit();
            }
        });
        sourceButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(sourceField.getTooltip() != null) {
                Tooltip tip = sourceField.getTooltip();
                sourceField.getStyleClass().removeIf(style -> style.equals("text-error"));
                tip.hide();
                Tooltip.uninstall(sourceField, tip);
            }
            openFileChooser(sourceField);
        });
        destinationButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(destinationField.getTooltip() != null) {
                Tooltip tip = destinationField.getTooltip();
                destinationField.getStyleClass().removeIf(style -> style.equals("text-error"));
                tip.hide();
                Tooltip.uninstall(destinationField, tip);
            }
            openFileChooser(destinationField);
        });
        stopButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            organizationWorker.pause();
            closeButton.setDisable(true);
            stopButton.setDisable(true);
            cancelPane.setVisible(true);
            sourceButton.setDisable(true);
            destinationButton.setDisable(true);
            sourceField.setDisable(true);
            destinationField.setDisable(true);
            cancelPane.toFront();
        });

        yesButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(!closeButton.isDisable()){
                organizationWorker.cancel();
                Stage stage = (Stage) closeButton.getScene().getWindow();
                stage.close();
            }
            organizationWorker.cancel();
            updateStatusTileData();
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
            closeButton.setDisable(false);
            cancelPane.setVisible(false);
            stopButton.setVisible(false);
            stopButton.setDisable(true);
            organizeButton.setDisable(true);
            organizeButton.setVisible(true);
            resultPanel.setVisible(true);
            resultPanel.toFront();
            filePathLabel.textProperty().unbind();
        });

        noButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            if(closeButton.isDisable()){
                closeButton.setDisable(false);
            }
            organizePane.toFront();
            organizationWorker.resume();
            cancelPane.setVisible(false);
            stopButton.setDisable(false);
        });

        skipButton.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            organizePane.setVisible(false);
            resultPanel.setVisible(false);
            progressBar.setProgress(0);
            organizeButton.setDisable(false);
            menuButton.setDisable(false);
            costumeButton.setDisable(false);
            infoButton.setDisable(false);
            sourceButton.setDisable(false);
            destinationButton.setDisable(false);
            sourceField.setDisable(false);
            destinationField.setDisable(false);
            filePathLabel.setText("");
        });
        sourceField.textProperty().addListener((observableValue, s, t1) -> {
            progressBar.progressProperty().setValue(0);
            if (!s.equals(t1)){
                updateData();
            }
        });
        destinationField.textProperty().addListener((observableValue, s, t1) -> {
            noDataPanel.setVisible(false);
            if (!s.equals(t1)){
                if(isEnoughSpace(destinationField.getText())){
                    organizeButton.setDisable(false);
                }
                else {
                    // not space
                }
            }
        });
        organizeButton.setOnAction(actionEvent -> {
            if(data.getTotalFiles() == 0){
                setToolTip(sourceField, "no one file found");
                return;
            }
            if(!isDirectoryExist(sourceField.getText())){
                setToolTip(sourceField, "directory doesn't exist");
                return;
            }
            if(!isDirectoryExist(destinationField.getText())){
                setToolTip(destinationField, "directory doesn't exist");
                return;
            }
                initFileCopyWorker();
                organizePane.setVisible(true);
                organizePane.toFront();
                filePathLabel.textProperty().bind(organizationWorker.messageProperty());
                progressBar.progressProperty().bind(organizationWorker.progressProperty());
                filePathLabel.setVisible(true);
                organizeButton.setDisable(true);
                organizeButton.setVisible(false);
                stopButton.setDisable(false);
                stopButton.setVisible(true);
                menuButton.setDisable(true);
                costumeButton.setDisable(true);
                infoButton.setDisable(true);
                sourceButton.setDisable(true);
                destinationButton.setDisable(true);
                sourceField.setDisable(true);
                destinationField.setDisable(true);
                copyThread = new Thread(organizationWorker);
                copyThread.start();
        });
    }

    private void openFileChooser(TextField field) {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        JFileChooser fileChooser = new MyFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fileChooser.setCurrentDirectory(new File(String.valueOf(FileSystemView.getFileSystemView().getHomeDirectory())));
        int option = fileChooser.showDialog( null,"Select Directory");
        if (option == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            if (!file.isDirectory()) {
                file = file.getParentFile();
            }
            field.setText(file.toString());
        }
    }

    private void createDonutChart(){
        data = new Data();
        chartDataList = initDonutChartData();
        chartCtrCount = new SimpleLongProperty();
        ctrLabel.textProperty().bindBidirectional(chartCtrCount, new StringConverter<Number>() {
            @Override
            public String toString(Number number) {
                String value = "";
                long data = chartCtrCount.get();
                if(dataByFileType) {
                    value = String.valueOf(data);
                }
                else {
                    value = Data.humanReadableByteCount(data);
                }
                return value;
            }

            @Override
            public Number fromString(String s) {
                return null;
            }
        });

        donutChartTile = new Tile(Tile.SkinType.GAUGE);
        donutChartTileSkin = new DonutChartTileSkin(donutChartTile);
        donutChartTile.setSkin(donutChartTileSkin);
        donutChartTile.setPrefWidth(800);
        donutChartTile.setPrefHeight(800);
        donutChartTile.setTextVisible(false);
        donutChartTile.setChartData(chartDataList);
        donutChartTile.setBackgroundColor(Color.web("transparent"));
        donutChartTile.setValueVisible(false);

        donutChartPane.add(donutChartTile, 0,0);
        donutChartPane.setHgap(5);
        donutChartPane.setVgap(5);
        donutChartPane.setAlignment(Pos.CENTER);
        donutChartPane.setCenterShape(true);
        donutChartPane.setPadding(new javafx.geometry.Insets(0));
        donutChartPane.setBackground(new Background(new BackgroundFill(Color.web("transparent"), CornerRadii.EMPTY, Insets.EMPTY)));
        donutChartPane.setMouseTransparent(true);
    }

    private ObservableList<ChartData> initDonutChartData(){
        chartData1 = new ChartData("Videos", Color.web("#f1975b"));
        chartData2 = new ChartData("Images", Color.web("#f5b58a"));
        chartData3 = new ChartData("Others", Color.web("#f9d2b9"));
        ObservableList<ChartData> pieChartData = FXCollections.observableArrayList(chartData1, chartData2, chartData3);
        return pieChartData;
    }

    private void updateData(){
        String dir = sourceField.getText();
        DataUpdater dataUpdater;
        if(!dir.equals("")) {
            dataUpdater = new DataUpdater(dir);
            noDataLabel.setVisible(false);
            noDataPanel.setVisible(true);
            waitingGif.setVisible(true);
            waitingLabel.setVisible(true);
            ctrlFolderIcon.setVisible(false);
            destinationButton.setDisable(true);
            organizeButton.setDisable(true);
            dataUpdater.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
                    new EventHandler<WorkerStateEvent>() {
                        @Override
                        public void handle(WorkerStateEvent t) {
                            data = dataUpdater.getValue();
                            waitingGif.setVisible(false);
                            waitingLabel.setVisible(false);
                            noDataPanel.setVisible(false);
                            destinationButton.setDisable(false);
                            organizeButton.setDisable(false);
                            updateChartTypeData();
                        }
                    });
            new Thread(dataUpdater).start();
        }
    }

    @FXML
    private void updateChartTypeData(){
        String dir = sourceField.getText();
        if(!dir.equals("")) {
            ctrLabel.setText("0");
            chartData1.setValue(0);
            chartData2.setValue(0);
            chartData3.setValue(0);
            chartData1.setValue(data.getVideos());
            chartData2.setValue(data.getImages());
            chartData3.setValue(data.getOthers());
            chartData1.setName(String.format("Video:\n %d", data.getVideos()));
            chartData2.setName(String.format("Image:\n %d", data.getImages()));
            chartData3.setName(String.format("Other:\n %d", data.getOthers()));
            dataByFileType = true;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateCtrSizeLabel();
                }
            }).start();
        }
    }
    @FXML
    private void updateChartSizeData(){
        String dir = sourceField.getText();
        if(!dir.equals("")) {
            chartData1.setValue(0);
            chartData2.setValue(0);
            chartData3.setValue(0);
            chartData1.setValue(data.getVideoSizeMbi());
            chartData2.setValue(data.getImageSizeMbi());
            chartData3.setValue(data.getOtherSizeMbi());
            chartData1.setName(String.format("Video:\n %s ", data.getHRVideoSize()));
            chartData2.setName(String.format("Image:\n %s ", data.getHRImageSize()));
            chartData3.setName(String.format("Other:\n %s ", data.getHROtherSize()));
            dataByFileType = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateCtrSizeLabel();
                }
            }).start();
        }
    }

    private void createStatusDashboard(){
        Indicator leftGraphics = new Indicator(Tile.RED);
        leftGraphics.setOn(true);
        Indicator middleGraphics = new Indicator(Tile.YELLOW);
        middleGraphics.setOn(true);
        Indicator rightGraphics = new Indicator(Tile.GREEN);
        rightGraphics.setOn(true);
        statusBoard = new Tile(Tile.SkinType.GAUGE);
        statusSkin = new StatusTileSkin(statusBoard);
        statusBoard.setSkin(statusSkin);
        statusBoard.setPrefSize(300, 200);
        statusBoard.setLeftText("FAIL");
        statusBoard.setMiddleText("WARNING");
        statusBoard.setRightText("SUCCESS");
        statusBoard.setLeftGraphics(leftGraphics);
        statusBoard.setMiddleGraphics(middleGraphics);
        statusBoard.setRightGraphics(rightGraphics);
        statusBoard.setBackgroundColor(Color.web("transparent"));
        statusBoard.setDescriptionAlignment(Pos.TOP_CENTER);
        statusIndicator.add(statusBoard, 0, 0);
        failFilesList.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> list) {
                return new MyFormatCell();
            }
        });

    }

    private void updateStatusTileData(){
        statusBoard.setDescription(String.format("TOTAL FILES: %d", organizationWorker.getTotalFiles()));
        changePrivatFieldStatusSkin();
        ObservableList<FileInfo> failFiles = organizationWorker.getFailFilesList();
        statusBoard.leftValueProperty().bind(organizationWorker.failFileProperty());
        statusBoard.middleValueProperty().bind(organizationWorker.interruptedFilesProperty());
        statusBoard.rightValueProperty().bind(organizationWorker.copiedFilesProperty());
        failFilesList.setItems(failFiles);
        failFilesList.setVisible(false);
        if(failFiles.size() > 0){
            failFilesList.setVisible(true);
        }
    }

    private void updateCtrSizeLabel() {
        KeyValue keyValue;
        if (dataByFileType) {
            keyValue = new KeyValue(chartCtrCount, data.getTotalFiles());
        } else {
            keyValue = new KeyValue(chartCtrCount, data.getTotalSize());
        }
        KeyFrame keyFrame = new KeyFrame(Duration.millis(500), keyValue);
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

    private void initFileCopyWorker(){
        organizationWorker = new OrganizationWorker(copyFilter.isSelected(), monthFilter.isSelected(), videoFilter.isSelected(), imageFilter.isSelected(), hiddenFilter.isSelected(), otherFilter.isSelected());
        organizationWorker.setSourceDirectory(sourceField.getText());
        organizationWorker.setDestinationDirectory(destinationField.getText());
        organizationWorker.setData(data);
        organizationWorker.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, (EventHandler<WorkerStateEvent>) workerStateEvent -> {
            updateStatusTileData();
            progressBar.progressProperty().unbind();
            filePathLabel.textProperty().unbind();
            resultPanel.toFront();
            filePathLabel.setVisible(false);
            organizeButton.setVisible(true);
            stopButton.setVisible(false);
            stopButton.setDisable(true);
            resultPanel.setVisible(true);
        });
    }


    private boolean isEnoughSpace(String path){
        File directory = new File(path);
        long freeSpace = directory.getFreeSpace();
        double totalFilesSize = data.getTotalSize();
        BigDecimal space = new BigDecimal(freeSpace);
        BigDecimal size = new BigDecimal(totalFilesSize);
        return space.compareTo(size) > 0;
    }

    private boolean isDirectoryExist(String path){
        File file = new File(path);
        return file.exists();
    }

    private void setToolTip(TextField field, String text){
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(10));
        Point2D p = field.localToScreen(5 , 4);
        field.getStyleClass().add("text-error");
        field.setTooltip(tooltip);
        tooltip.show(field, p.getX()-15,p.getY()+25);
    }


    private void changePrivatFieldStatusSkin() {
        try {
            Field field1 = statusSkin.getClass().getDeclaredField("description");
            field1.setAccessible(true);
            Label label = (Label) field1.get(statusSkin);
            label.setStyle("-fx-font-weight: bold; -fx-font-family: Consolas; -fx-font-size: 16px;");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private class MyFormatCell extends ListCell<FileInfo> {
        public MyFormatCell(){
            this.setOnMouseClicked(e -> {
                if (this.getItem() != null) {
                    FileChooser fc = new FileChooser();
                    Path path = Paths.get(this.getItem().getPath());
                    File file = new File(path.getParent().toString());
                    fc.setInitialDirectory(file);
                    fc.showOpenDialog(null);
                }
            });
        }
        @Override
        protected void updateItem(FileInfo item, boolean empty) {
            super.updateItem(item, empty);
            if(item != null) {
                setText(item.getPath());
                if (item.getStatus().equals(FileStatus.FAILED)) {
                    setTextFill(Color.RED);
                }
                if (item.getStatus().equals(FileStatus.INTERRUPTED)) {
                    setTextFill(Color.web("#fff599"));
                }
            }
        }
    }
}
