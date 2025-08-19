package frc.utils;

public class LimitSwitchConfig {
    public int channel;
    public String name;
    
    public boolean inverted = false;

    /** 
     * Constructor
     * @param channel - DIO channel number
     * @param name - name of limit switch for logging
     */
    public LimitSwitchConfig(int channel, String name) {
        this.channel = channel;
        this.name = name;
    }

    public LimitSwitchConfig withInvert(boolean invert) {
        this.inverted = invert;
        return this;
    }
}
