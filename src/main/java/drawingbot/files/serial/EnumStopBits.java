package drawingbot.files.serial;

import com.fazecast.jSerialComm.SerialPort;

public enum EnumStopBits {

    BITS_1("1 bit", SerialPort.ONE_STOP_BIT),
    BITS_15("1.5 bits", SerialPort.ONE_POINT_FIVE_STOP_BITS),
    BITS_2("2 bits", SerialPort.TWO_STOP_BITS);

    public String displayName;
    public int flag;

    EnumStopBits(String displayName, int flag){
        this.displayName = displayName;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return displayName;
    }

}
