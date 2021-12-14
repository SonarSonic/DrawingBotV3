package drawingbot.files.serial;

import drawingbot.DrawingBotV3;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.File;
import java.nio.file.Files;

public class SerialWriteTask extends Task<Boolean> {

    public byte[] bytes = new byte[0];
    public String title = "";
    public String error;

    public SerialWriteTask(String command){
        this.title = command;
        this.bytes = command.getBytes();
    }

    public SerialWriteTask(byte[] bytes){
        this.title = "Command";
        this.bytes = bytes;
    }

    public SerialWriteTask(File fileToSend){
        this.title = fileToSend.getName();
        try {
            this.bytes = Files.readAllBytes(fileToSend.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void setException(Throwable t) {
        super.setException(t);
        DrawingBotV3.INSTANCE.serialConnection.closePort();
        error = t.getMessage();
    }

    @Override
    protected void cancelled() {
        super.cancelled();
        DrawingBotV3.INSTANCE.serialConnection.closePort();
        error = "CANCELLED";
    }

    @Override
    protected Boolean call() {
        if(bytes.length == 0){
            error = "NO BYTES TO SEND";
        }else{
            DrawingBotV3.INSTANCE.serialConnection.writeTask = title;

            DrawingBotV3.INSTANCE.serialConnection.error = false;
            error = null;

            if(!DrawingBotV3.INSTANCE.serialConnection.openPort()){
                error = "FAILED TO OPEN SERIAL CONNECTION";
            }

            if(error == null){
                if(!DrawingBotV3.INSTANCE.serialConnection.writeAllData(bytes, 512)){
                    error = "PORT DISCONNECTED OR TASK CANCELLED";
                }
            }

            if(!DrawingBotV3.INSTANCE.serialConnection.closePort()){
                error = "FAILED TO CLOSE SERIAL CONNECTION";
            }
        }

        Platform.runLater(() -> {
            if(error == null) {
                DrawingBotV3.INSTANCE.serialConnection.progressLabel.set(title + " :" + "Finished");
            }else{
                DrawingBotV3.INSTANCE.serialConnection.error = true;
                DrawingBotV3.INSTANCE.serialConnection.progressLabel.set(title + " : " + "ERROR " + error);
            }
        });
        return this.error == null;
    }
}