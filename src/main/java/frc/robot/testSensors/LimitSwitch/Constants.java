package frc.robot.testSensors.LimitSwitch;

import frc.demacia.utils.Sensors.LimitSwitchConfig;

public class Constants {
    public static final int limitSwitchChannel = 0;
    public static final String limitSwitchName = "Test Limit Switch";
    public static final boolean limitSwitchInverted = false;

    public static final LimitSwitchConfig limitSwitchConfig = new LimitSwitchConfig(limitSwitchChannel, limitSwitchName)
            .withInvert(limitSwitchInverted);
}
