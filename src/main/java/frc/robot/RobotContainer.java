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
import frc.demacia.utils.Sensors.DigitalSensorInterface;
import frc.demacia.utils.Sensors.LimitSwitch;
import frc.demacia.utils.chassis.Chassis;
import frc.robot.ChassisConstants.Robot1ChassisConstants;
import frc.robot.testMechanism.ClimebConstants;
import frc.robot.testMechanism.ArmConstants.ARM_STATES;
import frc.robot.testMechanism.ArmConstants.ArmAngleMotorConstants;
import frc.robot.testMechanism.ClimebConstants.CLIMB_STATES;
import frc.robot.testMechanism.ClimebConstants.ClimbConstants;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Notifier;
import edu.wpi.first.wpilibj.Timer;
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

  private double simPos = 0, simVel = 0, simAcc = 0, simVolts = 0;
    // These are the "Correct Answers" your SysID tool should find:
    private final double REAL_KS = 0.5;
    private final double REAL_KV = 1.5;
    private final double REAL_KA = 0.2; 
    private double simTime = 0;

  // The robot's subsystems and commands are defined here...


  // Replace with CommandPS4Controller or CommandJoystick if needed

  /** The container for the robot. Contains subsystems, OI devices, and commands. */
  public RobotContainer() {
    
    new LogManager();
    // driverController = new CommandController(0, ControllerType.kXbox);

    // chassis = new Chassis(Robot1ChassisConstants.CHASSIS_CONFIG);
    // driveCommand = new DriveCommand(chassis, driverController);

    // setMechanism();

    configureFakeMotorLog();

    // Configure the trigger bindings
    configureBindings();
  }

  private void configureFakeMotorLog() {
    // We use a Notifier instead of a Command. 
    // Notifiers run completely independently of the Command Scheduler and Robot State.
    Notifier physicsLoop = new Notifier(() -> {
        double dt = 0.02; 
        simTime += dt;

        // Generate voltage (Sine wave: -8V to 8V)
        simVolts = 8.0 * Math.sin(simTime * 2.0); 

        // Physics calculation (Simulate Motor Response)
        double friction = REAL_KS * Math.signum(simVel);
        double backEmf = REAL_KV * simVel;

        // Apply voltage only if it overcomes friction (Stiction)
        if (Math.abs(simVolts) < REAL_KS && Math.abs(simVel) < 0.001) {
            simAcc = 0;
            simVel = 0;
        } else {
            // Newton's 2nd Law: F = ma  ->  a = F/m  ->  a = (V - BackEMF - Friction) / kV_angular_acceleration_constant
            simAcc = (simVolts - friction - backEmf) / REAL_KA;
        }

        simVel += simAcc * dt;
        simPos += simVel * dt;
    });

    // Start the physics loop at 50Hz (0.02s)
    physicsLoop.startPeriodic(0.02);

    // Setup Logging
    LogManager.addEntry("FakeSimMotor Position, Velocity, Acceleration, Voltage, Current, CloseLoopError, CloseLoopSP", 
        () -> simPos,
        () -> simVel,
        () -> simAcc,
        () -> simVolts,
        () -> 0.0, // Current
        () -> 0.0, // Error
        () -> 0.0  // Setpoint
    )
    .WithIsMotor()
    .withLogLevel(frc.demacia.utils.Log.LogEntryBuilder.LogLevel.LOG_AND_NT_NOT_IN_COMP)
    .build();
  }

  private void setMechanism(){
    arm = new Arm("robot1 arm")
      .withMotors(new TalonFXMotor(ArmAngleMotorConstants.CONFIG))
      .withSensors(new LimitSwitch(ArmAngleMotorConstants.ARM_ANGlE_LIMIT))
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
      .withStartingOption("Starting")
      // .withButton(driverController.povRight(), "L1")
      // .withButton(driverController.upButton(), "Starting")
      // .withButton(driverController.povUp(), "L3")
      // .withButton(driverController.povDown(), "L2")
      // .withButton(driverController.rightSetting(), "Coral Station")
      // .withDriveMotor(0, driverController.povLeft(), () -> driverController.getLeftY() * -0.8)
      .withDefaultCommand()
      .withMotorLimits(0, ArmAngleMotorConstants.BACK_LIMIT, ArmAngleMotorConstants.FWD_LIMIT)
      // .withStop(() -> driverController.leftBumper().getAsBoolean())
      ;

    Timer moveLittileMoreTimer = new Timer();
    climb = new StateBasedMechanism<>("robot1 climb")
    .withMotors(new TalonFXMotor(ClimebConstants.MOTOR_CONFIG))
    .withSensors(new LimitSwitch(ClimebConstants.LIMIT_SWITCH_CONFIG))
    .withConsumer((motor, values) -> {
      for (int i = 0; i < motor.length && i < values.length; i++) {
          motor[i].setDuty(values[i]);
      }})
    .withAction(BaseMechanism.creatAction("open climb", CLIMB_STATES.OPEN.getValues())
      .withExecute(() -> {if (!((DigitalSensorInterface) climb.getSensor(0)).get()) {
          moveLittileMoreTimer.start();
        }})
      .withFinish(() -> moveLittileMoreTimer.hasElapsed(0.2))
      .withEnd(() -> {moveLittileMoreTimer.stop();
        moveLittileMoreTimer.reset();}))
    .withAction(BaseMechanism.creatAction("climb", CLIMB_STATES.CLIMB_POWER.getValues())
      .withFinish(() -> (climb.getMotor(0).getCurrentPosition() > ClimebConstants.ClimbConstants.HAS_CLIMED_ANGLE)))
    // .withButton(driverController.downButton(), "open climb")
    // .withButton(driverController.leftButton(), "climb")
    // .withDriveMotor(0, driverController.povLeft(), () -> -driverController.getRightY())
    // .withStop(() -> driverController.leftBumper().getAsBoolean())
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
