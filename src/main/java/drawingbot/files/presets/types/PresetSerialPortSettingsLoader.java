package drawingbot.files.presets.types;

import drawingbot.DrawingBotV3;
import drawingbot.files.presets.AbstractSettingsLoader;
import drawingbot.files.serial.*;
import drawingbot.javafx.GenericPreset;
import drawingbot.javafx.GenericSetting;
import drawingbot.utils.EnumJsonType;

import java.util.List;

public class PresetSerialPortSettingsLoader extends AbstractSettingsLoader<PresetSerialPortSettings> {

    public PresetSerialPortSettingsLoader() {
        super(PresetSerialPortSettings.class, EnumJsonType.SERIAL_PORT_CONFIG, "user_serial_port_presets.json");
    }

    public void registerSettings(){
        registerSetting(GenericSetting.createStringSetting(DrawingBotV3.class, "portName", "COM9", false, (app, value) -> app.serialConnection.portName.setValue(value)).setGetter(app -> app.serialConnection.portName.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "baudRate", List.of(EnumBaudRate.values()), EnumBaudRate.BAUD9600, false, (app, value) -> app.serialConnection.baudRate.set(value)).setGetter(app -> app.serialConnection.baudRate.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "dataBits", List.of(EnumDataBits.values()), EnumDataBits.BITS_8, false, (app, value) -> app.serialConnection.dataBits.set(value)).setGetter(app -> app.serialConnection.dataBits.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "stopBits", List.of(EnumStopBits.values()), EnumStopBits.BITS_1, false, (app, value) -> app.serialConnection.stopBits.set(value)).setGetter(app -> app.serialConnection.stopBits.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "parity", List.of(EnumSerialParity.values()), EnumSerialParity.NONE, false, (app, value) -> app.serialConnection.parity.set(value)).setGetter(app -> app.serialConnection.parity.get()));
        registerSetting(GenericSetting.createOptionSetting(DrawingBotV3.class, "flowControl", List.of(EnumFlowControl.values()), EnumFlowControl.RTS_CTS, false, (app, value) -> app.serialConnection.flowControl.set(value)).setGetter(app -> app.serialConnection.flowControl.get()));

    }

    @Override
    public GenericPreset<PresetSerialPortSettings> getDefaultPreset() {
        return presets.stream().filter(p -> p.presetName.equals("Default")).findFirst().orElse(null);
    }

    @Override
    protected PresetSerialPortSettings getPresetInstance(GenericPreset<PresetSerialPortSettings> preset) {
        return new PresetSerialPortSettings();
    }
}
