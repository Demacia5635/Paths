package frc.demacia.utils.Motors;


/** 
 * Class to hold all Talon FX/SRX configuration
 * Applicable to Phoenix 6
 *  */
public class TalonFXConfig extends BaseMotorConfig {

    /** 
     * Constructor
     * @param id - canbus ID
     * @param name - name of motor for logging
     */
    public TalonFXConfig(int id, String name, Canbus canbus) {
        super(id, name, canbus);
    }

    public TalonFXConfig(int id, String name) {
        super(id, name);
    }

    public TalonFXConfig(int id, Canbus canbus) {
        super(id, canbus);
    }

    public TalonFXConfig(int id, String name, Canbus canbus, BaseMotorConfig config) {
        super(id, name, canbus);
        copyBaseFields(config);
    }

    public TalonFXConfig(int id, String name, BaseMotorConfig config) {
        super(id, name);
        copyBaseFields(config);
    }

    public TalonFXConfig(int id, Canbus canbus, BaseMotorConfig config) {
        super(id, canbus);
        copyBaseFields(config);
    }

    @Override
    protected TalonFXConfig self() {
        return this;
    }

    @Override
    public TalonFXMotor create() {
        return new TalonFXMotor(this);
    }
}