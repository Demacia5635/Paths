// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.Paths;

import java.security.KeyStore.LoadStoreParameter;
import java.util.ArrayList;
import java.util.function.Consumer;
import java.util.function.Supplier;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import frc.demacia.utils.Log.LogManager;
import frc.demacia.utils.chassis.Chassis;


/* You should consider using the more terse Command factories API instead https://docs.wpilib.org/en/stable/docs/software/commandbased/organizing-command-based.html#defining-commands */
public class FollowTrajectory extends Command {

  Consumer<ChassisSpeeds> setSpeeds;
  Supplier<Pose2d> currentPose;
  Supplier<ChassisSpeeds> currentSpeeds;
  DemaciaTrajectory trajectory;
  Chassis chassis;
  ArrayList<Pose2d> trajectoryPoints;

  public FollowTrajectory(ArrayList<Pose2d> trajectoryPoints, Consumer<ChassisSpeeds> setSpeeds, Chassis chassis) {
    // this(setSpeeds, , new DemaciaTrajectory(trajectoryPoints));
    this.chassis = chassis;
    this.setSpeeds = setSpeeds;
    this.trajectoryPoints = trajectoryPoints;
    trajectory = new DemaciaTrajectory(trajectoryPoints);
    // LogManager.log("path point "  + trajectory.getPathPoint() + "trajectory point" + trajectory.getTrjectoryPoint());
  }

  @Override
  public void execute() {
    ChassisSpeeds s = trajectory.calculateSpeeds(chassis.getChassisSpeedsFieldRel(), chassis.getPose());
    // setSpeeds.accept(trajectory.calculateSpeeds(chassis.getChassisSpeedsRobotRel(), chassis.getPose()));
    chassis.setVelocities(s);
    LogManager.log("path point " + trajectory.getPathPoint());
  }

  @Override
  public void end(boolean interrupted) {
    // setSpeeds.accept(new ChassisSpeeds());
    chassis.stop();
  }

  @Override
  public boolean isFinished() {
    return trajectory.isFinishedTrajectory();
  }
}
