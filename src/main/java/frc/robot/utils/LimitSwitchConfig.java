package frc.robot.utils;

public class LimitSwitchConfig {
    public int channel;
    public String name;

    /** 
     * Constructor
     * @param id - canbus ID
     * @param canbus - Name of canbus
     * @param name - name of Pigeon for logging
     */
    public LimitSwitchConfig(int channel, String name) {
        this.channel = channel;
        this.name = name;
    }
}
