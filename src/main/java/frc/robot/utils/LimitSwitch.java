package frc.robot.utils;

import edu.wpi.first.wpilibj.DigitalInput;

public class LimitSwitch extends DigitalInput{
    LimitSwitchConfig config;
    String name;

    public LimitSwitch(LimitSwitchConfig config){
        super(config.channel);
        this.config = config;
		name = config.name;
        addLog();
		LogManager.log(name + " limit switch initialized");
    }

    private void addLog() {
        LogManager.addEntry(name + "/isTriggered", this::isTriggered, 2);
    }

    public boolean isTriggered(){
        return get();
    }
}
