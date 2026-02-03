// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.chassis.paths;

import edu.wpi.first.math.MathUtil;
import edu.wpi.first.wpilibj.Timer;

/**
 * Distance-based trapezoid profile with exponential jerk limiting.
 * Calculates velocity setpoints based on remaining distance rather than time.
 */
public class TrapezoidExpo {
    private final double maxVelocity;
    private final double maxAcceleration;
    private final double maxJerk;
    
    private double lastVelocity = 0;
    private double lastAcceleration = 0;
    private double lastTime = 0;
    
    public boolean debug = false;
    
    private static final double CYCLE_DT = 0.02; // 20ms cycle time
    
    /**
     * Constructor with jerk limiting
     * @param maxVelocity Maximum velocity (m/s or units/s)
     * @param maxAcceleration Maximum acceleration (m/s² or units/s²)
     * @param maxJerk Maximum jerk (m/s³ or units/s³)
     */
    public TrapezoidExpo(double maxVelocity, double maxAcceleration, double maxJerk) {
        this.maxVelocity = maxVelocity;
        this.maxAcceleration = maxAcceleration;
        this.maxJerk = maxJerk;
    }
    
    /**
     * Calculate the next velocity setpoint based on remaining distance
     * @param distanceLeft Remaining distance to target
     * @param currentVelocity Current velocity
     * @param targetVelocity Final target velocity (usually 0)
     * @return Velocity setpoint for this cycle
     */
    public double calculate(double distanceLeft, double currentVelocity, double targetVelocity) {
        // Handle negative distances by flipping signs
        if (distanceLeft < 0) {
            return -calculate(-distanceLeft, -currentVelocity, -targetVelocity);
        }
        
        double time = Timer.getFPGATimestamp();
        
        // Velocity filtering to prevent sensor noise from causing issues
        if (time - lastTime <= CYCLE_DT) {
            if (lastAcceleration > 0 && currentVelocity < lastVelocity) {
                currentVelocity = lastVelocity;
            } else if (lastAcceleration < 0 && currentVelocity > lastVelocity) {
                currentVelocity = lastVelocity;
            }
        }
        
        // Calculate desired acceleration based on remaining distance
        double desiredAcceleration = calculateDesiredAcceleration(
            distanceLeft, currentVelocity, targetVelocity);
        
        // Apply jerk limiting using exponential smoothing
        double limitedAcceleration = applyJerkLimit(desiredAcceleration, lastAcceleration);
        
        // Clamp acceleration to max values
        limitedAcceleration = MathUtil.clamp(desiredAcceleration, -maxAcceleration, maxAcceleration);
        
        // Calculate next velocity
        double nextVelocity = currentVelocity + limitedAcceleration * CYCLE_DT;
        
        // Clamp velocity to max values
        nextVelocity = MathUtil.clamp(nextVelocity, -maxVelocity , maxVelocity);
        
        // Ensure we don't overshoot the target velocity when decelerating
        if (Math.abs(nextVelocity - targetVelocity) < Math.abs(currentVelocity - targetVelocity)) {
            nextVelocity = Math.max(MathUtil.clamp(nextVelocity, targetVelocity, currentVelocity), Math.min(currentVelocity, targetVelocity));
        }
        
        if (debug) {
            System.out.println(String.format(
                "Trap: distLeft=%.3f curV=%.3f nextV=%.3f accel=%.3f",
                distanceLeft, currentVelocity, nextVelocity, limitedAcceleration));
        }
        
        // Update state
        lastVelocity = nextVelocity;
        lastAcceleration = limitedAcceleration;
        lastTime = time;
        
        return nextVelocity;
    }
    
    /**
     * Calculate desired acceleration based on distance remaining
     * Uses physics equations to determine optimal acceleration
     */
    private double calculateDesiredAcceleration(double distanceLeft, 
                                                double currentVelocity, 
                                                double targetVelocity) {
        // Calculate distance needed to decelerate to target velocity
        double decelerationDistance = distanceToVelocity(
            currentVelocity, targetVelocity, maxAcceleration);
        
        // Account for one cycle of motion
        double futureDistance = distanceLeft - estimateCycleDistance(
            currentVelocity, lastAcceleration);
        
        if (futureDistance <= decelerationDistance) {
            // Need to decelerate - use kinematic equation: v² = u² + 2as
            // Rearranged: a = (v² - u²) / (2s)
            double distanceForDecel = Math.max(distanceLeft, 0.001); // Avoid divide by zero
            double accel = (targetVelocity * targetVelocity - 
                           currentVelocity * currentVelocity) / (2 * distanceForDecel);
            return accel;
        } else if (currentVelocity < maxVelocity) {
            // Can accelerate - check if we have room
            double accelDistance = distanceToVelocity(
                currentVelocity, maxVelocity, maxAcceleration);
            
            if (futureDistance > decelerationDistance + accelDistance) {
                // Room to accelerate to max velocity
                return maxAcceleration;
            } else {
                // Limited acceleration - calculate cruise velocity
                double cruiseVelocity = calculateCruiseVelocity(
                    distanceLeft, currentVelocity, targetVelocity);
                
                if (cruiseVelocity > currentVelocity) {
                    return maxAcceleration;
                } else {
                    return 0; // Maintain current velocity
                }
            }
        } else {
            // At max velocity, maintain it
            return 0;
        }
    }
    
    /**
     * Calculate optimal cruise velocity for given distance
     */
    private double calculateCruiseVelocity(double distance, 
                                          double currentVelocity, 
                                          double targetVelocity) {
        // Use triangular profile if distance is short
        // v_cruise² = v_current² + a*d - (v_cruise² - v_target²)/(2a) * a
        // Simplified: v_cruise = sqrt((v_current² + v_target² + 2*a*d) / 2)
        double vSquared = (currentVelocity * currentVelocity + 
                          targetVelocity * targetVelocity + 
                          2 * maxAcceleration * distance) / 2;
        return Math.min(Math.sqrt(Math.max(0, vSquared)), maxVelocity);
    }
    
    /**
     * Apply jerk limiting using exponential smoothing
     * This creates a smooth S-curve acceleration profile
     */
    private double applyJerkLimit(double desiredAcceleration, double currentAcceleration) {
        double maxAccelChange = maxJerk * CYCLE_DT;
        double accelError = desiredAcceleration - currentAcceleration;
        
        // Exponential smoothing factor based on jerk limit
        // Higher jerk = faster response (less smoothing)
        double alpha = Math.min(1.0, maxAccelChange / (Math.abs(accelError) + 0.001));
        
        // Exponential filter: a_new = a_old + alpha * (a_desired - a_old)
        double smoothedAcceleration = currentAcceleration + alpha * accelError;
        
        // Hard limit on acceleration change per cycle
        double deltaAccel = smoothedAcceleration - currentAcceleration;
        deltaAccel = Math.max(-maxAccelChange, Math.min(maxAccelChange, deltaAccel));
        
        return currentAcceleration + deltaAccel;
    }
    
    /**
     * Calculate distance needed to change from current velocity to target velocity
     * Using kinematic equation: s = (v² - u²) / (2a)
     */
    private double distanceToVelocity(double currentVelocity, 
                                     double targetVelocity, 
                                     double acceleration) {
        double velocityDiff = targetVelocity - currentVelocity;
        double time = Math.abs(velocityDiff) / acceleration;
        double avgVelocity = (currentVelocity + targetVelocity) / 2;
        return Math.abs(avgVelocity * time);
    }
    
    /**
     * Estimate distance traveled in one cycle given current velocity and acceleration
     */
    private double estimateCycleDistance(double velocity, double acceleration) {
        return velocity * CYCLE_DT + 0.5 * acceleration * CYCLE_DT * CYCLE_DT;
    }
    
    /**
     * Reset the profile state (call when starting a new motion)
     */
    public void reset() {
        lastVelocity = 0;
        lastAcceleration = 0;
        lastTime = 0;
    }
    
    /**
     * Reset with initial velocity (for continuous motion)
     */
    public void reset(double initialVelocity) {
        lastVelocity = initialVelocity;
        lastAcceleration = 0;
        lastTime = 0;
    }
}