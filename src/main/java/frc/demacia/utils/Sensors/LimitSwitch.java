package frc.demacia.utils.Sensors;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.demacia.utils.Log.LogManager;

public class LimitSwitch extends DigitalInput implements DigitalSensorInterface{
    LimitSwitchConfig config;
    String name;

    boolean inverted;

    public LimitSwitch(LimitSwitchConfig config){
        super(config.channel);
        this.config = config;
		name = config.name;
        configLimitSwitch();
        addLog();
		LogManager.log(name + " limit switch initialized");
    }

    private void configLimitSwitch() {
        inverted = config.isInverted;
    }

    private void addLog() {
        LogManager.addEntry(name + "/isTriggered", this::get, 3);
    }

    public String getName(){
        return config.name;
    }

    public boolean get(){
        return !(inverted == super.get());
    }
}
