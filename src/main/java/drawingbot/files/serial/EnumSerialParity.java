package drawingbot.files.serial;

import com.fazecast.jSerialComm.SerialPort;
import drawingbot.utils.Utils;

public enum EnumSerialParity {

    NONE(SerialPort.NO_PARITY),
    ODD(SerialPort.ODD_PARITY),
    EVEN(SerialPort.EVEN_PARITY),
    MARK(SerialPort.MARK_PARITY),
    SPACE(SerialPort.SPACE_PARITY);

    public int flag;

    EnumSerialParity(int flag){
        this.flag = flag;
    }

    @Override
    public String toString() {
        return Utils.capitalize(name());
    }

}
