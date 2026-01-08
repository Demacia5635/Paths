package frc.robot.testMechanism.shooter.subsystems;

import edu.wpi.first.wpilibj.AnalogInput;
import frc.demacia.utils.mechanisms.StateBaseMechanism;
import frc.demacia.utils.motors.MotorInterface;
import frc.demacia.utils.motors.TalonFXMotor;
import frc.demacia.utils.sensors.LimitSwitch;
import frc.demacia.utils.sensors.SensorInterface;
import frc.robot.testMechanism.shooter.ShooterConstants.Config;
import frc.robot.testMechanism.shooter.ShooterConstants.Hardware;
import frc.robot.testMechanism.shooter.ShooterConstants.SHOOTER_STATE;

public class Shooter extends StateBaseMechanism {
    
    private final AnalogInput noteSensor;
    private final double NOTE_VOLTAGE_THRESHOLD = 4.7;

    public Shooter() {
        super("Shooter", 
            new MotorInterface[] {
                new TalonFXMotor(Config.ANGLE_CONFIG),
                new TalonFXMotor(Config.SHOOTER_UP_CONFIG),
                new TalonFXMotor(Config.SHOOTER_DOWN_CONFIG)
            }, 
            new SensorInterface[] {
                new LimitSwitch(Config.LIMIT_CONFIG)
            }, 
            SHOOTER_STATE.class);

        noteSensor = new AnalogInput(Hardware.ANALOG_INPUT_ID);
        
        setPositionMechanism(0); // Angle motor index
        setStartingOption(SHOOTER_STATE.IDLE);
    }

    public boolean isNoteIn() {
        return noteSensor.getVoltage() < NOTE_VOLTAGE_THRESHOLD;
    }

    public boolean isLimitPressed() {
        return ((LimitSwitch) getSensor(Hardware.LIMIT_SWITCH_NAME)).get();
    }

    @Override
    public void periodic() {
        super.periodic();
        if (isLimitPressed()) {
            getMotor(0).setEncoderPosition(0); 
            setCalibration(true);
        }
    }
}