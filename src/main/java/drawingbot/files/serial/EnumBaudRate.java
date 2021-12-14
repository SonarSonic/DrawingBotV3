package drawingbot.files.serial;

public enum EnumBaudRate {

    BAUD9600(9600),
    BAUD110(110),
    BAUD300(300),
    BAUD600(600),
    BAUD1200(1200),
    BAUD2400(2400),
    BAUD4800(4800),
    BAUD14400(14400),
    BAUD19200(19200),
    BAUD28800(28800),
    BAUD38400(38400),
    BAUD56000(56000),
    BAUD57600(57600),
    BAUD115200(115200);

    public int baudRate;

    EnumBaudRate(int baudRate){
        this.baudRate = baudRate;
    }

    @Override
    public String toString() {
        return "" + baudRate;
    }

}
