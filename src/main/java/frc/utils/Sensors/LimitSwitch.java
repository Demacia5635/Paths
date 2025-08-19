package frc.utils.Sensors;

import edu.wpi.first.wpilibj.DigitalInput;
import frc.utils.Log.LogManager;

public class LimitSwitch extends DigitalInput{
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
        LogManager.addEntry(name + "/isTriggered", this::isTriggered, 2);
    }

    public boolean isTriggered(){
        return !(inverted == get());
    }
}
