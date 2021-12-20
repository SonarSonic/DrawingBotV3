package drawingbot.javafx;

import com.fazecast.jSerialComm.SerialPort;
import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.ExportFormats;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetSerialPortSettings;
import drawingbot.files.serial.*;
import drawingbot.javafx.controls.DialogExportHPGLHardLimitWarning;
import drawingbot.javafx.controls.DialogExportHPGLPlotterDetect;
import drawingbot.utils.UnitsLength;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.control.cell.ComboBoxListCell;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.io.File;
import java.nio.file.Files;
import java.util.Optional;

public class FXSerialConnectionController {


    public void initialize(){
        initSerialConfigurationPanel();
        initSerialReadWritePanel();
        initSerialStatusPanel();
        initSerialProgressBar();


        DrawingBotV3.INSTANCE.controller.serialConnectionSettingsStage.setOnHidden(e -> ConfigFileHandler.getApplicationSettings().markDirty());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////SERIAL CONFIGURATION

    public ComboBox<GenericPreset<PresetSerialPortSettings>> comboBoxSerialPortPreset = null;
    public MenuButton menuButtonSerialPortPresets = null;

    public VBox vBoxPortSettings = null;

    public ObservableList<String> serialPorts = FXCollections.observableArrayList();
    public ComboBox<String > comboBoxSerialPort = null;
    public ChoiceBox<EnumStopBits> choiceBoxStopBits = null;
    public ChoiceBox<EnumSerialParity> choiceBoxParity = null;
    public ChoiceBox<EnumBaudRate> choiceBoxBaudRate = null;
    public ChoiceBox<EnumDataBits> choiceBoxDataBits = null;
    public ChoiceBox<EnumFlowControl> choiceBoxFlowControl = null;

    public Button buttonClosePort = null;
    public Button buttonOpenPort = null;

    public void updateSerialPorts(){
        SerialPort[] ports = SerialPort.getCommPorts();
        serialPorts.clear();
        for(SerialPort port : ports){
            serialPorts.add(port.getSystemPortName());
        }
    }

    public void initSerialConfigurationPanel(){


        comboBoxSerialPortPreset.setItems(JsonLoaderManager.SERIAL_PORT_CONFIGS.presets);
        comboBoxSerialPortPreset.setValue(JsonLoaderManager.SERIAL_PORT_CONFIGS.getDefaultPreset());
        comboBoxSerialPortPreset.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                JsonLoaderManager.SERIAL_PORT_CONFIGS.applyPreset(newValue);
            }
        });

        FXHelper.setupPresetMenuButton(JsonLoaderManager.SERIAL_PORT_CONFIGS, menuButtonSerialPortPresets, false, comboBoxSerialPortPreset::getValue, (preset) -> {
            comboBoxSerialPortPreset.setValue(preset);

            ///force update rendering
            comboBoxSerialPortPreset.setItems(JsonLoaderManager.SERIAL_PORT_CONFIGS.presets);
            comboBoxSerialPortPreset.setButtonCell(new ComboBoxListCell<>());
        });

        updateSerialPorts();
        comboBoxSerialPort.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> updateSerialPorts());
        comboBoxSerialPort.setItems(serialPorts);
        comboBoxSerialPort.setValue(serialPorts.isEmpty() ? "COM9" : serialPorts.get(0));
        comboBoxSerialPort.valueProperty().bindBidirectional(SerialConnection.getInstance().portName);

        choiceBoxBaudRate.setItems(FXCollections.observableArrayList(EnumBaudRate.values()));
        choiceBoxBaudRate.setValue(EnumBaudRate.BAUD9600);
        choiceBoxBaudRate.valueProperty().bindBidirectional(SerialConnection.getInstance().baudRate);

        choiceBoxDataBits.setItems(FXCollections.observableArrayList(EnumDataBits.values()));
        choiceBoxDataBits.setValue(EnumDataBits.BITS_8);
        choiceBoxDataBits.valueProperty().bindBidirectional(SerialConnection.getInstance().dataBits);

        choiceBoxStopBits.setItems(FXCollections.observableArrayList(EnumStopBits.values()));
        choiceBoxStopBits.setValue(EnumStopBits.BITS_1);
        choiceBoxStopBits.valueProperty().bindBidirectional(SerialConnection.getInstance().stopBits);

        choiceBoxParity.setItems(FXCollections.observableArrayList(EnumSerialParity.values()));
        choiceBoxParity.setValue(EnumSerialParity.NONE);
        choiceBoxParity.valueProperty().bindBidirectional(SerialConnection.getInstance().parity);

        choiceBoxFlowControl.setItems(FXCollections.observableArrayList(EnumFlowControl.values()));
        choiceBoxFlowControl.setValue(EnumFlowControl.NONE);
        choiceBoxFlowControl.valueProperty().bindBidirectional(SerialConnection.getInstance().flowControl);


        buttonOpenPort.setOnAction(close -> SerialConnection.getInstance().openPort());
        buttonOpenPort.disableProperty().bind(SerialConnection.getInstance().portOpen);

        buttonClosePort.setOnAction(close -> SerialConnection.getInstance().closePort());
        buttonClosePort.disableProperty().bind(SerialConnection.getInstance().portOpen.not());

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////SERIAL READ / WRITE

    public ObservableList<String> outputHPGLFiles = FXCollections.observableArrayList();

    public Pane paneSerialReadWrite = null;

    public TextArea textAreaSerialOutput = null;
    public TextField textFieldCommand = null;
    public Button buttonSendCommand = null;
    public ComboBox<String> comboBoxHPGLFile = null;

    public Button buttonImportHPGLFile = null;
    public Button buttonCancelFile = null;
    public Button buttonDetectPlotter = null;
    public Button buttonSendFile = null;

    public void initSerialReadWritePanel(){
        paneSerialReadWrite.disableProperty().bind(SerialConnection.getInstance().portOpen.not());

        textAreaSerialOutput.textProperty().bind(SerialConnection.getInstance().portOutput);
        textAreaSerialOutput.setEditable(true);

        buttonSendCommand.setOnAction(e -> SerialConnection.getInstance().sendRequest(textFieldCommand.getText(), true));
        buttonSendCommand.disableProperty().bind(SerialConnection.getInstance().portWriting);

        comboBoxHPGLFile.setItems(outputHPGLFiles);

        buttonImportHPGLFile.setOnAction(e -> FXHelper.importFile(file -> comboBoxHPGLFile.setValue(file.toString()), FileUtils.FILTER_HPGL));
        buttonCancelFile.setOnAction(e -> {
            if(SerialConnection.getInstance().portWriting.get()){
                SerialConnection.getInstance().shouldCancel.set(true);
            }
        });
        buttonCancelFile.disableProperty().bind(SerialConnection.getInstance().portWriting.not());

        buttonSendFile.setOnAction(e -> {
            if(SerialConnection.getInstance().portWriting.get()){
                SerialConnection.getInstance().shouldPause.set(!SerialConnection.getInstance().shouldPause.get());
            }else if(!comboBoxHPGLFile.getValue().isEmpty() && Files.exists(new File(comboBoxHPGLFile.getValue()).toPath())){
                DrawingBotV3.INSTANCE.startTask(DrawingBotV3.INSTANCE.serialConnectionWriteService, new SerialWriteTask(new File(comboBoxHPGLFile.getValue())));
            }
        });

        Runnable updateSendButton = () -> Platform.runLater(() -> buttonSendFile.setText(SerialConnection.getInstance().portWriting.getValue() ? (SerialConnection.getInstance().shouldPause.get() ? "Resume" : "Pause") : "Send File"));
        SerialConnection.getInstance().portWriting.addListener((observable, oldValue, newValue) -> updateSendButton.run());
        SerialConnection.getInstance().shouldPause.addListener((observable, oldValue, newValue) -> updateSendButton.run());

        buttonDetectPlotter.setOnAction(e -> showDetectPlotterDialog());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////SERIAL READ / WRITE
    public Circle circleRXD = null;
    public Circle circleTXD = null;
    public Circle circleCTS = null;
    public Circle circleDCD = null;
    public Circle circleDSR = null;
    public Circle circleRing = null;
    public Circle circleError = null;

    public Label labelElapsedTime = null;
    public Label labelRemainingTime = null;

    public void initSerialStatusPanel(){
        labelElapsedTime.textProperty().bind(SerialConnection.getInstance().elapsedTimeProperty);
        labelRemainingTime.textProperty().bind(SerialConnection.getInstance().remainingTimeProperty);
    }

    public void tick(){
        if(DrawingBotV3.INSTANCE.controller.serialConnectionSettingsStage.isShowing()){
            if(SerialConnection.getInstance().portOpen.get() && SerialConnection.getInstance().serialPort != null){
                SerialPort port = SerialConnection.getInstance().serialPort;
                circleRXD.setFill(port.bytesAvailable() != 0 || SerialConnection.getInstance().rxdTick ? Color.YELLOW : Color.GREY);
                circleTXD.setFill(port.bytesAwaitingWrite() != 0 || SerialConnection.getInstance().txdTick ? Color.YELLOW : Color.GREY);
                circleCTS.setFill(port.getCTS() ? Color.SPRINGGREEN : Color.GREY);
                circleDCD.setFill(port.getDCD() ? Color.SPRINGGREEN : Color.GREY);
                circleDSR.setFill(port.getDSR() ? Color.SPRINGGREEN : Color.GREY);
                circleRing.setFill(port.getRI() ? Color.SPRINGGREEN : Color.GREY);
                circleError.setFill(SerialConnection.getInstance().error ? Color.RED : Color.GREY);

                SerialConnection.getInstance().rxdTick = false;
                SerialConnection.getInstance().txdTick = false;
            }else{
                circleRXD.setFill(Color.GREY);
                circleTXD.setFill(Color.GREY);
                circleCTS.setFill(Color.GREY);
                circleDCD.setFill(Color.GREY);
                circleDSR.setFill(Color.GREY);
                circleRing.setFill(Color.GREY);
                circleError.setFill(Color.GREY);
            }
        }
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    ////PROGRESS BAR

    public ProgressBar progressBarSerialWrite = null;
    public Label labelProgressBarLabel = null;


    public ProgressBar progressBarBuffer = null;
    public Label progressBarBufferLabel = null;

    public void initSerialProgressBar(){
        progressBarBuffer.progressProperty().bind(SerialConnection.getInstance().bufferProperty);
        progressBarBufferLabel.textProperty().bind(SerialConnection.getInstance().bufferLabel);

        progressBarSerialWrite.progressProperty().bind(SerialConnection.getInstance().progressProperty);
        labelProgressBarLabel.textProperty().bind(SerialConnection.getInstance().progressLabel);
    }

    public void showDetectPlotterDialog(){
        String plotterID = SerialConnection.getInstance().getPlotterIdentification();
        int[] hardLimits = SerialConnection.getInstance().getHardClipLimits();

        Platform.runLater(() -> {
            DialogExportHPGLPlotterDetect dialog = new DialogExportHPGLPlotterDetect(plotterID, hardLimits);
            Optional<Boolean> result = dialog.showAndWait();
            if(result.isPresent() && result.get()){
                applyDrawingArea(dialog.plotterWidthHPGL, dialog.plotterHeightHPGL);
                applyHardClipLimits(hardLimits, false);
            }
        });

    }

    public void showHPGLHardLimitsDialog(int[] hardLimits){
        Platform.runLater(() -> {
            DialogExportHPGLHardLimitWarning dialog = new DialogExportHPGLHardLimitWarning(hardLimits);
            Optional<Boolean> result = dialog.showAndWait();
            if(result.isPresent() && result.get()){
                applyHardClipLimits(hardLimits, true);
            }
        });
    }

    public static void applyDrawingArea(float width, float height){
        DrawingBotV3.INSTANCE.useOriginalSizing.set(false);
        DrawingBotV3.INSTANCE.controller.textFieldDrawingWidth.setText(String.valueOf(width));
        DrawingBotV3.INSTANCE.controller.textFieldDrawingHeight.setText(String.valueOf(height));
        DrawingBotV3.INSTANCE.inputUnits.set(UnitsLength.MILLIMETRES);
    }

    public static void applyHardClipLimits(int[] hardLimits, boolean reExport){
        DrawingBotV3.INSTANCE.controller.exportController.textFieldHPGLXMin.setText(String.valueOf(hardLimits[0]));
        DrawingBotV3.INSTANCE.controller.exportController.textFieldHPGLYMin.setText(String.valueOf(hardLimits[1]));
        DrawingBotV3.INSTANCE.controller.exportController.textFieldHPGLXMax.setText(String.valueOf(hardLimits[2]));
        DrawingBotV3.INSTANCE.controller.exportController.textFieldHPGLYMax.setText(String.valueOf(hardLimits[3]));
        if(reExport){
            FXHelper.exportFile(ExportFormats.EXPORT_HPGL, false);
        }
    }
}
