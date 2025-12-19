// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import frc.demacia.utils.Controller.CommandController;
import frc.demacia.utils.Controller.CommandController.ControllerType;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.Mechanisms.Arm;
import frc.demacia.utils.Mechanisms.BaseMechanism;
import frc.demacia.utils.Mechanisms.Intake;
import frc.demacia.utils.Mechanisms.StateBasedMechanism;
import frc.demacia.utils.Motors.TalonFXMotor;
import frc.demacia.utils.Sensors.DigitalEncoder;
import frc.demacia.utils.Sensors.DigitalSensorInterface;
import frc.demacia.utils.Sensors.LimitSwitch;
import frc.demacia.utils.chassis.Chassis;
import frc.robot.testMechanism.ClimebConstants;
import frc.robot.ChassisConstants.Robot1ChassisConstants;
import frc.robot.testMechanism.ArmConstants.ARM_STATES;
import frc.robot.testMechanism.ArmConstants.ArmAngleMotorConstants;
import frc.robot.testMechanism.ArmConstants.CalibrationConstants;
import frc.robot.testMechanism.ArmConstants.GripperAngleMotorConstants;
import frc.robot.testMechanism.ClimebConstants.CLIMB_STATES;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;


/**
 * This class is where the bulk of the robot should be declared. Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls). Instead, the structure of the robot (including
 * subsystems, commands, and trigger mappings) should be declared here.
 */
public class RobotContainer {

  Arm arm;
  StateBasedMechanism<?> climb;
  Intake gripper;

  Chassis chassis;
  // DriveCommand driveCommand;

  
  public static CommandController driverController;

  public static boolean isComp = DriverStation.isFMSAttached();
  private static boolean hasRemovedFromLog = false;

  public static int N_CYCLE = 0;
  public static double CYCLE_TIME = 0.02;

  // The robot's subsystems and commands are defined here...


  // Replace with CommandPS4Controller or CommandJoystick if needed

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    
    new LogManager();
    driverController = new CommandController(0, ControllerType.kXbox);

    chassis = new Chassis(Robot1ChassisConstants.CHASSIS_CONFIG);
    // driveCommand = new DriveCommand(chassis, driverController);

    setMechanism();

    // Configure the trigger bindings
    configureBindings();
  }

  private void setMechanism(){
    arm = new Arm("robot1 arm")
      .withMotors(new TalonFXMotor(ArmAngleMotorConstants.CONFIG), 
        new TalonFXMotor(GripperAngleMotorConstants.CONFIG))
      .withSensors(new LimitSwitch(ArmAngleMotorConstants.ARM_ANGlE_LIMIT), 
        new DigitalEncoder(GripperAngleMotorConstants.DIGITAL_ENCODER_CONFIG))
      .withState(Arm.creatState("L1", ARM_STATES.L1.getValues()))
      .withState(Arm.creatState("L2", ARM_STATES.L2.getValues()))
      .withState(Arm.creatState("L3", ARM_STATES.L3.getValues()))
      .withState(Arm.creatState("Coral Station", ARM_STATES.CORAL_STATION.getValues()))
      .withState(Arm.creatState("Starting", ARM_STATES.STARTING.getValues()))
      .withState(Arm.creatState("Climb", ARM_STATES.CLIMB.getValues()))
      .withState(Arm.creatState("PRE ALGAE BOTTOM", ARM_STATES.PRE_ALGAE_BOTTOM.getValues()))
      .withState(Arm.creatState("PRE ALGAE TOP", ARM_STATES.PRE_ALGAE_TOP.getValues()))
      .withState(Arm.creatState("AFTER ALGAE BOTTOM", ARM_STATES.AFTER_ALGAE_BOTTOM.getValues()))
      .withState(Arm.creatState("AFTER ALGAE TOP", ARM_STATES.AFTER_ALGAE_TOP.getValues()))
      .withButton(driverController.leftStick(), "L1")
      .withButton(driverController.upButton(), "Starting")
      .withButton(driverController.povUp(), "L3")
      .withButton(driverController.povDown(), "L2")
      .withButton(driverController.rightSetting(), "Coral Station")
      .withButton(driverController.povLeft(), "")
      .withModifier(1, () -> (arm.getMotor(1).getCurrentPosition() - ((((DigitalEncoder) arm.getSensor(1)).get() * 2 * Math.PI) - GripperAngleMotorConstants.ENCODER_BASE_ANGLE)))
      .withMotorLimits(0, ArmAngleMotorConstants.BACK_LIMIT, ArmAngleMotorConstants.FWD_LIMIT)
      .withMotorLimits(1, GripperAngleMotorConstants.BACK_LIMIT, GripperAngleMotorConstants.FWD_LIMIT)
      .withDefaultCommand()
      .withDriveMotor(0, driverController.leftStick(), () -> driverController.getLeftY() * -0.8)
      .withDriveMotor(1, driverController.leftStick(), () -> driverController.getRightY() * -0.4)
      .withStop(() -> driverController.leftBumper().getAsBoolean())
      .withAction(arm.createCalibrationAction(0, CalibrationConstants.ARM_ANGLE_POWER, () -> ((DigitalSensorInterface) arm.getSensor(0)).get(), ArmAngleMotorConstants.BASE_ANGLE, CalibrationConstants.ARM_ANGLE_START_POWER, CalibrationConstants.TIME_TO_CHANGE_POWER));

    climb = new StateBasedMechanism<>("robot1 climb")
    .withMotors(new TalonFXMotor(ClimebConstants.MOTOR_CONFIG))
    .withSensors(new LimitSwitch(ClimebConstants.LIMIT_SWITCH_CONFIG))
    .withConsumer((motor, values) -> {
      for (int i = 0; i < motor.length && i < values.length; i++) {
          motor[i].setDuty(values[i]);
      }})
    .withAction(BaseMechanism.createAction("open climb", CLIMB_STATES.OPEN.getValues())
      .withTime(0.2))
    .withAction(BaseMechanism.createAction("climb", CLIMB_STATES.CLIMB_POWER.getValues())
      .withFinish(() -> (climb.getMotor(0).getCurrentPosition() > ClimebConstants.ClimbConstants.HAS_CLIMED_ANGLE)))
    .withButton(driverController.downButton(), "open climb")
    .withButton(driverController.leftButton(), "climb")
    .withDriveMotor(0, driverController.povLeft(), () -> -driverController.getRightY())
    .withStop(() -> driverController.leftBumper().getAsBoolean())
    ;
  }

  public static boolean isComp() {
    return isComp;
  }

  public static void setIsComp(boolean isComp) {
    RobotContainer.isComp = isComp;
    if(!hasRemovedFromLog && isComp) {
      hasRemovedFromLog = true;
      LogManager.removeInComp();
    }
  }

  /**
   * Use this method to define your trigger->command mappings. Triggers can be created via the
   * {@link Trigger#Trigger(java.util.function.BooleanSupplier)} constructor with an arbitrary
   * predicate, or via the named factories in {@link
   * edu.wpi.first.wpilibj2.command.button.CommandGenericHID}'s subclasses for {@link
   * CommandXboxController Xbox}/{@link edu.wpi.first.wpilibj2.command.button.CommandPS4Controller
   * PS4} controllers or {@link edu.wpi.first.wpilibj2.command.button.CommandJoystick Flight
   * joysticks}.
   */
  private void configureBindings() {
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    // An example command will be run in autonomous
    return null;
  }
}
