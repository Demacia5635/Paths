package frc.demacia.utils.Motors;


/** * Configuration class specifically for TalonFX motors.
 * Extends the base configuration to support Phoenix 6 specific parameters.
 */
public class TalonFXConfigDemasia extends BaseMotorConfig<TalonFXConfigDemasia> {

    /** * Creates a new TalonFX Configuration.
      * @param id The CAN bus ID of the motor
      * @param canbus The name of the CAN bus
      * @param name The name of motor for logging and dashboard
      */
    public TalonFXConfigDemasia(int id, Canbus canbus, String name) {
        super(id, name, canbus);
        motorClass = MotorControllerType.TalonFX;
    }

    /** * Creates a new TalonFX Configuration by copying another config.
      * @param id The new CAN bus ID
      * @param name The new name
      * @param config The existing configuration to copy from
      */
    public TalonFXConfigDemasia(int id, String name, BaseMotorConfig<?> config) {
        super(id, name);
        copyBaseFields(config);
    }
}