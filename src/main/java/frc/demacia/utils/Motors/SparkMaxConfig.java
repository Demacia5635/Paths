package frc.demacia.utils.Motors;

/** 
 * Class to hold all Spark motor configuration
 * Applicable to REV Spark Max/Flex
 *  */
public class SparkMaxConfig extends BaseMotorConfig {

    /** 
     * Constructor
     * @param id - canbus ID
     * @param name - name of motor for logging
     */
    public SparkMaxConfig(int id, String name, Canbus canbus) {
        super(id, name, canbus);
    }

    public SparkMaxConfig(int id, String name) {
        super(id, name);
    }

    public SparkMaxConfig(int id, Canbus canbus) {
        super(id, canbus);
    }

    public SparkMaxConfig(int id, String name, Canbus canbus, BaseMotorConfig config) {
        super(id, name, canbus);
        copyBaseFields(config);
    }

    public SparkMaxConfig(int id, String name, BaseMotorConfig config) {
        super(id, name);
        copyBaseFields(config);
    }

    public SparkMaxConfig(int id, Canbus canbus, BaseMotorConfig config) {
        super(id, canbus);
        copyBaseFields(config);
    }

    @Override
    protected SparkMaxConfig self() {
        return this;
    }

    @Override
    public SparkMaxMotor create() {
        return new SparkMaxMotor(this);
    }
}