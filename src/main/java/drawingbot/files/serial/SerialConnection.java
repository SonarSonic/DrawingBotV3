package drawingbot.files.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import com.fazecast.jSerialComm.SerialPortEvent;
import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.function.Consumer;

public class SerialConnection {

    public SimpleStringProperty portName = new SimpleStringProperty("COM9");
    public SimpleObjectProperty<EnumBaudRate> baudRate = new SimpleObjectProperty<>(EnumBaudRate.BAUD9600);
    public SimpleObjectProperty<EnumDataBits> dataBits = new SimpleObjectProperty<>(EnumDataBits.BITS_8);
    public SimpleObjectProperty<EnumSerialParity> parity = new SimpleObjectProperty<>(EnumSerialParity.NONE);
    public SimpleObjectProperty<EnumStopBits> stopBits = new SimpleObjectProperty<>(EnumStopBits.BITS_1);
    public SimpleObjectProperty<EnumFlowControl> flowControl = new SimpleObjectProperty<>(EnumFlowControl.RTS_CTS);

    public boolean checkHardClipLimits = false;
    public SimpleBooleanProperty portOpen = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty portWriting = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty shouldPause = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty shouldCancel = new SimpleBooleanProperty(false);

    public String writeTask = "";
    public SimpleFloatProperty bufferProperty = new SimpleFloatProperty(0);
    public SimpleStringProperty bufferLabel = new SimpleStringProperty("");
    public SimpleFloatProperty progressProperty = new SimpleFloatProperty(0);
    public SimpleStringProperty progressLabel = new SimpleStringProperty("");
    public SimpleStringProperty elapsedTimeProperty = new SimpleStringProperty("");
    public SimpleStringProperty remainingTimeProperty = new SimpleStringProperty("");
    public SimpleStringProperty portOutput = new SimpleStringProperty("");

    public SerialPort serialPort = null;
    public boolean error = false;
    public int bufferSize = -1;
    public int bufferSpace = -1;

    public boolean rxdTick = false;
    public boolean txdTick = false;

    public static SerialConnection getInstance(){
        return DrawingBotV3.INSTANCE.serialConnection;
    }

    public boolean openPort(){
        if((serialPort == null || !serialPort.isOpen()) && !portOpen.get()){
            serialPort = SerialPort.getCommPort(portName.get());

            if(!serialPort.openPort()){
                return false;
            }

            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            serialPort.setComPortParameters(baudRate.get().baudRate, dataBits.get().flag, stopBits.get().flag, parity.get().flag);
            serialPort.setFlowControl(flowControl.get().flag);

            serialPort.addDataListener(new SerialPortDataListener() {

                @Override
                public int getListeningEvents() {
                    return SerialPort.LISTENING_EVENT_DATA_RECEIVED;
                }

                @Override
                public void serialEvent(SerialPortEvent event) {
                    onDataReceived(event);
                }
            });

            portOpen.set(true);

            if(checkHardClipLimits){
                int[] hardLimits = checkHardClipLimits();
                if(hardLimits != null){
                    DrawingBotV3.INSTANCE.controller.serialConnectionController.showHPGLHardLimitsDialog(hardLimits);
                }
                checkHardClipLimits = false;
            }
        }
        return true;
    }

    public boolean closePort(){
        if(portOpen.get()){
            if(!serialPort.closePort()){
                return false;
            }
            portOpen.set(false);
            portWriting.set(false);
            shouldPause.set(false);
            shouldCancel.set(false);
            portOutput.setValue("");
            error = false;
        }
        return true;
    }


    public boolean writeAllData(byte[] data, int chunkSize){
        portWriting.set(true);

        long startTime = System.currentTimeMillis();
        long lastUpdate = -1;

        String message = sendRequest('\u001B' + ".L", false, 400);
        if (!message.isEmpty()) {
            bufferSize = Integer.parseInt(message);
        }

        error = false;
        int writtenBytes = 0;
        for(int offset = 0; offset < data.length; offset+=chunkSize){

            if((lastUpdate == -1 || (System.currentTimeMillis() - lastUpdate > 500)) && messageCallback == null) {
                sendRequestCallback('\u001B' + ".B", false, this::onBufferSpaceReceived);
                lastUpdate = System.currentTimeMillis();
            }

            int write = serialPort.writeBytes(data, Math.min(chunkSize, data.length-writtenBytes), offset);

            while (write != -1 && shouldPause.get() && !shouldCancel.get()){
                ///pause plotting
            }

            if(!shouldCancel.get() && write != -1){
                writtenBytes += write;

                int finalWrittenBytes = writtenBytes;
                float progress = (float)writtenBytes / data.length;
                float buffer = bufferSize == -1 || bufferSpace == -1 ? -1 : (float)(bufferSize - bufferSpace) / bufferSize;
                String bLabel = bufferSize == -1 || bufferSpace == -1 ? "" : (bufferSize - bufferSpace) + " bytes";

                Platform.runLater(() -> {
                    bufferProperty.set(buffer);
                    bufferLabel.set(bLabel);
                    progressProperty.set(progress);
                    progressLabel.set(writeTask + " : " + finalWrittenBytes + " / " + data.length + " bytes");

                    long elapsedTime = System.currentTimeMillis() - startTime;
                    elapsedTimeProperty.setValue((elapsedTime / 1000) / 60 + " m " + (elapsedTime / 1000) % 60 + " s");

                    long remainingTime = (long)(elapsedTime / progress) - elapsedTime;
                    remainingTimeProperty.setValue((remainingTime / 1000) / 60 + " m " + (remainingTime / 1000) % 60 + " s");
                });


            }else{
                error = write == -1;
                progressProperty.set(0);
                portWriting.set(false);
                shouldCancel.set(false);
                shouldPause.set(false);
                return false;
            }
        }
        progressProperty.set(1);
        portWriting.set(false);
        return true;
    }

    public void onBufferSizeReceived(String message){
        if (!message.isEmpty()) {
            bufferSize = Integer.parseInt(message);
        }
    }

    public void onBufferSpaceReceived(String message){
        if (!message.isEmpty()) {
            bufferSpace = Integer.parseInt(message);
        }
    }

    public boolean shouldPrint = true;
    public String buildingMessage = "";
    public String latestMessage = "";
    public Consumer<String> messageCallback;

    public void onDataReceived(SerialPortEvent event){
        byte[] newData = event.getReceivedData();
        String newString = new String(newData);

        int index = newString.indexOf('\r');
        if(index != -1){
            buildingMessage += newString.substring(0, index);
            latestMessage = buildingMessage;
            buildingMessage = "";

            if(messageCallback != null){
                messageCallback.accept(latestMessage);
                messageCallback = null;
            }
        }else{
            buildingMessage += newString;
        }

        if(shouldPrint){
            portOutput.set(portOutput.getValue() + newString.replace('\r', '\n'));
        }

        rxdTick = true;
    }

    public boolean sendMessage(String request, boolean print){
        latestMessage = "";
        messageCallback = null;
        shouldPrint = print;

        if(request.startsWith("\\x1B.")){
            request = "\u001B" + request.substring(4);
        }

        byte[] bytes = request.getBytes();
        txdTick = true;
        return serialPort.writeBytes(bytes, bytes.length) != -1;
    }

    public void sendRequestCallback(String request, boolean print, Consumer<String> callback) {
        if(sendMessage(request, print)){
            messageCallback = callback;
        }
    }

    public String sendRequest(String request, boolean print) {
        return sendRequest(request, print, 100);
    }

    public String sendRequest(String request, boolean print, long timeout) {
        if(sendMessage(request, print)){
            try {
                Thread.sleep(timeout);
            } catch (InterruptedException e) {
                return "";
            }
            return latestMessage;
        }
        return "";
    }

    public String getPlotterIdentification(){
        return sendRequest("OI;", false, 200);
    }

    public int[] getHardClipLimits(){
        String reply = sendRequest("OH;", false, 200);
        if (!reply.isEmpty()) {
            String[] variables = reply.split(",");
            if (variables.length == 4) {
                int xMin = Integer.parseInt(variables[0]);
                int yMin = Integer.parseInt(variables[1]);
                int xMax = Integer.parseInt(variables[2]);
                int yMax = Integer.parseInt(variables[3]);

                if (xMin == 0 && xMax == 0 || yMin == 0 && yMax == 0) {
                    //the plotter has errored or has no paper loaded
                    return null;
                }
                return new int[]{xMin, yMin, xMax, yMax};
            }
        }
        return null;
    }

    public int[] checkHardClipLimits() {
        int[] hardLimits = getHardClipLimits();
        if(hardLimits != null){
            if (DrawingBotV3.INSTANCE.hpglXMin.get() != hardLimits[0] || DrawingBotV3.INSTANCE.hpglYMin.get() != hardLimits[1] || DrawingBotV3.INSTANCE.hpglXMax.get() != hardLimits[2] || DrawingBotV3.INSTANCE.hpglYMax.get() != hardLimits[3]) {
                return hardLimits;
            }
        }
        return null;
    }

}
