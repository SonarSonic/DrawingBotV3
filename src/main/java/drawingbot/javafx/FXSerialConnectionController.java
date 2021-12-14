package drawingbot.javafx;

import com.fazecast.jSerialComm.SerialPort;
import drawingbot.DrawingBotV3;
import drawingbot.files.ConfigFileHandler;
import drawingbot.files.FileUtils;
import drawingbot.files.presets.JsonLoaderManager;
import drawingbot.files.presets.types.PresetSerialPortSettings;
import drawingbot.files.serial.*;
import drawingbot.integrations.vpype.PresetVpypeSettings;
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
        comboBoxSerialPort.setValue(serialPorts.isEmpty() ? "COM9" : serialPorts.get(1));
        comboBoxSerialPort.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.portName);

        choiceBoxBaudRate.setItems(FXCollections.observableArrayList(EnumBaudRate.values()));
        choiceBoxBaudRate.setValue(EnumBaudRate.BAUD9600);
        choiceBoxBaudRate.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.baudRate);

        choiceBoxDataBits.setItems(FXCollections.observableArrayList(EnumDataBits.values()));
        choiceBoxDataBits.setValue(EnumDataBits.BITS_8);
        choiceBoxDataBits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.dataBits);

        choiceBoxStopBits.setItems(FXCollections.observableArrayList(EnumStopBits.values()));
        choiceBoxStopBits.setValue(EnumStopBits.BITS_1);
        choiceBoxStopBits.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.stopBits);

        choiceBoxParity.setItems(FXCollections.observableArrayList(EnumSerialParity.values()));
        choiceBoxParity.setValue(EnumSerialParity.NONE);
        choiceBoxParity.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.parity);

        choiceBoxFlowControl.setItems(FXCollections.observableArrayList(EnumFlowControl.values()));
        choiceBoxFlowControl.setValue(EnumFlowControl.NONE);
        choiceBoxFlowControl.valueProperty().bindBidirectional(DrawingBotV3.INSTANCE.serialConnection.flowControl);


        buttonOpenPort.setOnAction(close -> DrawingBotV3.INSTANCE.serialConnection.openPort());
        buttonOpenPort.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portOpen);

        buttonClosePort.setOnAction(close -> DrawingBotV3.INSTANCE.serialConnection.closePort());
        buttonClosePort.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portOpen.not());

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
    public Button buttonSendFile = null;

    public void updateOutputHPGLFiles(){
        //todo
    }

    public void initSerialReadWritePanel(){
        paneSerialReadWrite.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portOpen.not());

        textAreaSerialOutput.textProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portOutput);
        textAreaSerialOutput.setEditable(false);
        buttonSendCommand.setOnAction(e -> DrawingBotV3.INSTANCE.serialConnection.sendRequest(textFieldCommand.getText()));
        buttonSendCommand.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portWriting);

        updateOutputHPGLFiles();
        comboBoxHPGLFile.onMouseClickedProperty().addListener((observable, oldValue, newValue) -> updateOutputHPGLFiles());
        comboBoxHPGLFile.setItems(outputHPGLFiles);

        buttonImportHPGLFile.setOnAction(e -> FXHelper.importFile(file -> comboBoxHPGLFile.setValue(file.toString()), FileUtils.FILTER_HPGL));
        buttonCancelFile.setOnAction(e -> {
            if(DrawingBotV3.INSTANCE.serialConnection.portWriting.get()){
                DrawingBotV3.INSTANCE.serialConnection.shouldInterrupt.set(true);
            }
        });
        buttonCancelFile.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portWriting.not());

        buttonSendFile.setOnAction(e -> {
            if(!comboBoxHPGLFile.getValue().isEmpty() && Files.exists(new File(comboBoxHPGLFile.getValue()).toPath())){
                DrawingBotV3.INSTANCE.serialConnectionWriteService.submit(new SerialWriteTask(new File(comboBoxHPGLFile.getValue())));
            }
        });
        buttonSendFile.disableProperty().bind(DrawingBotV3.INSTANCE.serialConnection.portWriting);


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

    public ProgressBar progressBarBuffer = null;

    public void initSerialStatusPanel(){
        progressBarBuffer.progressProperty().bind(DrawingBotV3.INSTANCE.serialConnection.bufferProperty);
        labelElapsedTime.textProperty().bind(DrawingBotV3.INSTANCE.serialConnection.elapsedTimeProperty);
        labelRemainingTime.textProperty().bind(DrawingBotV3.INSTANCE.serialConnection.remainingTimeProperty);
    }

    public void tick(){
        if(DrawingBotV3.INSTANCE.controller.serialConnectionSettingsStage.isShowing()){
            if(DrawingBotV3.INSTANCE.serialConnection.portOpen.get() && DrawingBotV3.INSTANCE.serialConnection.serialPort != null){
                SerialPort port = DrawingBotV3.INSTANCE.serialConnection.serialPort;
                circleRXD.setFill(port.bytesAvailable() != 0 ? Color.YELLOW : Color.GREY);
                circleTXD.setFill(port.bytesAwaitingWrite() != 0 ? Color.YELLOW : Color.GREY);
                circleCTS.setFill(port.getCTS() ? Color.SPRINGGREEN : Color.GREY);
                circleDCD.setFill(port.getDCD() ? Color.SPRINGGREEN : Color.GREY);
                circleDSR.setFill(port.getDSR() ? Color.SPRINGGREEN : Color.GREY);
                circleRing.setFill(port.getRI() ? Color.SPRINGGREEN : Color.GREY);
                circleError.setFill(DrawingBotV3.INSTANCE.serialConnection.error ? Color.RED : Color.GREY);
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

    public void initSerialProgressBar(){
        progressBarSerialWrite.progressProperty().bind(DrawingBotV3.INSTANCE.serialConnection.progressProperty);
        labelProgressBarLabel.textProperty().bind(DrawingBotV3.INSTANCE.serialConnection.progressLabel);
    }
}
