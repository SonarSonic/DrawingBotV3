package drawingbot.files.serial;

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortEvent;
import com.fazecast.jSerialComm.SerialPortMessageListener;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class SerialConnection {

    public SimpleStringProperty portName = new SimpleStringProperty("COM9");
    public SimpleObjectProperty<EnumBaudRate> baudRate = new SimpleObjectProperty<>(EnumBaudRate.BAUD9600);
    public SimpleObjectProperty<EnumDataBits> dataBits = new SimpleObjectProperty<>(EnumDataBits.BITS_8);
    public SimpleObjectProperty<EnumSerialParity> parity = new SimpleObjectProperty<>(EnumSerialParity.NONE);
    public SimpleObjectProperty<EnumStopBits> stopBits = new SimpleObjectProperty<>(EnumStopBits.BITS_1);
    public SimpleObjectProperty<EnumFlowControl> flowControl = new SimpleObjectProperty<>(EnumFlowControl.RTS_CTS);

    public SimpleBooleanProperty portOpen = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty portWriting = new SimpleBooleanProperty(false);
    public SimpleBooleanProperty shouldInterrupt = new SimpleBooleanProperty(false);

    public String writeTask = "";
    public SimpleFloatProperty bufferProperty = new SimpleFloatProperty(0);
    public SimpleFloatProperty progressProperty = new SimpleFloatProperty(0);
    public SimpleStringProperty progressLabel = new SimpleStringProperty("");
    public SimpleStringProperty elapsedTimeProperty = new SimpleStringProperty("");
    public SimpleStringProperty remainingTimeProperty = new SimpleStringProperty("");
    public SimpleStringProperty portOutput = new SimpleStringProperty("");

    public SerialPort serialPort = null;
    public boolean error = false;

    public boolean openPort(){
        if((serialPort == null || !serialPort.isOpen()) && !portOpen.get()){
            serialPort = SerialPort.getCommPort(portName.get());

            if(!serialPort.openPort()){
                return false;
            }

            serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
            serialPort.setComPortParameters(baudRate.get().baudRate, dataBits.get().flag, stopBits.get().flag, parity.get().flag);
            serialPort.setFlowControl(flowControl.get().flag);

            serialPort.addDataListener(new SerialPortMessageListener() {
                @Override
                public byte[] getMessageDelimiter() {
                    return new byte[]{'\r'};
                }

                @Override
                public boolean delimiterIndicatesEndOfMessage() {
                    return true;
                }

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
        }
        return true;
    }

    public boolean closePort(){
        if(portOpen.get()){
            if(!serialPort.closePort()){
                return false;
            }
            portOpen.set(false);
            portOutput.setValue("");
            error = false;
        }
        return true;
    }

    public boolean writeAllData(byte[] data, int chunkSize){
        long startTime = System.currentTimeMillis();
        int bufferSize = -1;
        int bufferSpace = -1;

        error = false;
        portWriting.set(true);
        int writtenBytes = 0;
        for(int offset = 0; offset < data.length; offset+=chunkSize){
            int write = serialPort.writeBytes(data, Math.min(chunkSize, data.length-writtenBytes), offset);
            if(!shouldInterrupt.get() && write != -1){
                writtenBytes += write;

                int finalWrittenBytes = writtenBytes;
                float progress = (float)writtenBytes / data.length;
                float buffer = (float)bufferSpace / bufferSize;


                Platform.runLater(() -> {
                    bufferProperty.set(buffer);
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
                return false;
            }
        }
        progressProperty.set(1);
        portWriting.set(false);
        return true;
    }

    public CountDownLatch messageLatch = new CountDownLatch(1);
    public String latestMessage = "";

    public void resetMessageLatch(){
        messageLatch = new CountDownLatch(1);
    }

    public void onDataReceived(SerialPortEvent event){
        byte[] messageData = event.getReceivedData();
        latestMessage = new String(messageData);
        messageLatch.countDown();
        portOutput.set(portOutput.getValue() + latestMessage + "\n");
    }

    public String sendRequest(String request) {
        serialPort.writeBytes(request.getBytes(), request.length());
        try {
            if(!messageLatch.await(100, TimeUnit.MILLISECONDS)){
                resetMessageLatch();
                return "";
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        resetMessageLatch();
        return latestMessage;
    }

}
