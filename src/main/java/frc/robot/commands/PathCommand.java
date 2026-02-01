// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.commands;
import java.util.ArrayList;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.chassis.Chassis;
import frc.robot.chassis.Paths.DemaciaTrajectory;

/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class PathCommand extends Command {
  /** Creates a new tstingPathCommand. */

   Chassis chassis;
   DemaciaTrajectory demaciaTrajectory;
  ArrayList<Pose2d> pointList = new ArrayList<>();  

  public PathCommand(Chassis chassis) {
    this.chassis = chassis;
    pointList.clear();
    pointList.add(new Pose2d(chassis.getPose().getX(), chassis.getPose().getY(), chassis.getGyroAngle()));
    pointList.add(new Pose2d(1.5, 7.9, new Rotation2d(Math.toRadians(0))));
    // pointList.add(new Pose2d(4.4, 3.55, new Rotation2d(Math.toRadians(90))));
    // pointList.add(new Pose2d(1.45, 1, new Rotation2d(Math.toRadians(60))));
    demaciaTrajectory = new DemaciaTrajectory(pointList);
    addRequirements(chassis);
    // Use addRequirements() here to declare subsystem dependencies.
  }

  // Called when the command is initially scheduled.
  @Override
  public void initialize() {} 

  // Called every time the scheduler runs while the command is scheduled.
  @Override
  public void execute() {
    ChassisSpeeds needSpeed = demaciaTrajectory.calculateSpeeds(chassis.getChassisSpeedsRobotRel(), chassis.getPose());
    chassis.setVelocities(needSpeed);
  }

  // Called once the command ends or is interrupted.
  @Override
  public void end(boolean interrupted) {
    chassis.stop();
  }

  // Returns true when the command should end.
  @Override
  public boolean isFinished() {
    return demaciaTrajectory.isFinishedTrajectory();
  }
}
