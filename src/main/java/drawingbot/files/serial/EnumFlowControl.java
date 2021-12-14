package drawingbot.files.serial;

import com.fazecast.jSerialComm.SerialPort;

public enum EnumFlowControl {

    NONE("None", SerialPort.FLOW_CONTROL_DISABLED),
    CTS("CTS", SerialPort.FLOW_CONTROL_CTS_ENABLED),
    RTS_CTS("RTS/CTS", SerialPort.FLOW_CONTROL_RTS_ENABLED | SerialPort.FLOW_CONTROL_CTS_ENABLED),
    DSR("DSR", SerialPort.FLOW_CONTROL_DSR_ENABLED),
    DTR_DSR("DTR/DSR", SerialPort.FLOW_CONTROL_DTR_ENABLED | SerialPort.FLOW_CONTROL_DSR_ENABLED),
    XON_XOFF("XOn/XOff", SerialPort.FLOW_CONTROL_XONXOFF_IN_ENABLED | SerialPort.FLOW_CONTROL_XONXOFF_OUT_ENABLED);

    public String displayName;
    public int flag;

    EnumFlowControl(String displayName, int flag){
        this.displayName = displayName;
        this.flag = flag;
    }

    @Override
    public String toString() {
        return displayName;
    }



}
