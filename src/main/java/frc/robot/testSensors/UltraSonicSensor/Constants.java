
package frc.robot.testSensors.UltraSonicSensor;
import edu.wpi.first.wpilibj.Ultrasonic;
import frc.demacia.utils.Motors.BaseMotorConfig.Canbus;
import frc.demacia.utils.Sensors.UltraSonicSensorConfig;
import frc.demacia.utils.Sensors.UltraSonicSensor;

public class Constants {
      private static final int channel=3;
      private static final int pingChannel=2;
      private static final String name="UltraSonicSensor";
      public static final UltraSonicSensorConfig ULTRA_SONIC_SENSOR_CONFIG = new UltraSonicSensorConfig(channel, pingChannel, name);
    
    }
   

