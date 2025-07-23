// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.BooleanArrayPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.BooleanArrayLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DataLogEntry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import edu.wpi.first.util.datalog.StringLogEntry;
import edu.wpi.first.util.datalog.DoubleArrayLogEntry;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.RobotContainer;
import frc.robot.utils.constants.UtilsContants.ConsoleConstants;

public class LogManager extends SubsystemBase {

  public static LogManager logManager; // singleton reference

  private DataLog log;
  private NetworkTable table = NetworkTableInstance.getDefault().getTable("Log");

  private static ArrayList<ConsoleAlert> activeConsole;

  private static String currentLogFilePath = null;
  
  // Optimization: Configurable skip cycles for better performance
  private static int SkipedCycles1 = 0;
  private static int SKIP_CYCLES = 1; // Default: log every cycle, can be changed for optimization
  private static boolean issenabled = true;

  /*
   * class for a single data entry
   */
  public class LogEntry<T> {
    DataLogEntry entry;
    StatusSignal<T> phoenix6Status; // supplier of phoenix 6 status signal
    StatusSignal<T>[] phoenix6StatusArray;
    Supplier<T> getter;
    BiConsumer<T, Long> consumer = null;
    String name;
    String metaData;
    Publisher ntPublisher;
    T lastValue;
    private double precision = 0; // Configurable precision for change detection
    private int skipedCycles2 = 0;
    private int SkipCycle = 1; // Default: log every cycle, can be changed for optimization
    private final Class<T> classType;

    private boolean isDoubleType;
    private boolean isBooleanType;
    private boolean isDoubleArrayType;
    private boolean isBooleanArrayType;

    /*
     * the log levels are this:
     * 1 -> log if it is not in a compition
     * 2 -> only log
     * 3 -> log and add to network tables if not in a compition
     * 4 -> log and add to network tables
     */
    public int logLevel;

    /*
     * Constructor with the suppliers and boolean if add to network table
     */
    LogEntry(String name, StatusSignal<T> phoenix6Status, StatusSignal<T>[] phoenix6StatusArray, Supplier<T> getter, int logLevel, String metaData, Class<T> classType) {

      this.name = name;
      this.logLevel = logLevel;
      this.classType = classType;
      this.phoenix6Status = phoenix6Status;
      this.phoenix6StatusArray = phoenix6StatusArray;
      this.getter = getter;
      this.metaData = metaData;

      isDoubleType = classType == Double.class;
      isBooleanType = classType == Boolean.class;
      isDoubleArrayType = classType == double[].class;
      isBooleanArrayType = classType == boolean[].class;

      this.entry = createLogEntry(log, name, metaData);

      if (logLevel == 4 || (logLevel == 3 && !RobotContainer.isComp())) {
        this.ntPublisher = createPublisher(table, name);
      } else {
        this.ntPublisher = null;
      }
      
    }

    /*
     * perform a periodic log
     * get the data from the getters and call the actual log
     */
    void log() {
      skipedCycles2++;
      if (skipedCycles2 < SkipCycle) {
        return;
      }
      skipedCycles2 = 0;
      T value  = null;
      long time = 0;

      if (phoenix6Status != null) {
        var st  = phoenix6Status.refresh();
        if (st.getStatus() == StatusCode.OK) {
          value = (T) Double.valueOf(st.getValueAsDouble());
          time = (long) (st.getTimestamp().getTime() * 1000);
        } else {
          return;
        }
      } else if (phoenix6StatusArray != null) {
        StatusCode  st = StatusSignal.refreshAll(phoenix6StatusArray);
        if (st == StatusCode.OK) {
          double[] arrV = new double[phoenix6StatusArray.length];
          for (int i = 0; i < phoenix6StatusArray.length; i++) {
            arrV[i] = phoenix6StatusArray[i].getValueAsDouble();
            if (i == 0) {
              time = (long) (phoenix6StatusArray[i].getTimestamp().getTime() * 1000);
            }
          }
          value = (T) arrV;
        } else {
          return;
        }
      } else if (getter != null){
        value = getter.get();
        time = 0;
      } if (value != null) {
        log(value, time);
      }
  }

    /*
     * log a value use zero (current) time
     */
    public void log(T value) {
      log(value, 0);
    }

    private boolean hasSignificantChange(T value) {
      if (lastValue == null) {
        return true;
      }
      
      if (isDoubleType) {
        return Math.abs((Double) value - (Double) lastValue) >= precision;
      } else if (isDoubleArrayType) {
        double[] arrV = (double[]) value;
        double[] lastArrV = (double[]) lastValue;
        if (arrV.length != lastArrV.length) {
          return true;
        }
        for (int i = 0; i < arrV.length; i++) {
          if (Math.abs(arrV[i] - lastArrV[i]) >= precision) {
            return true;
          }
        }
        return false;
      } else {
        return !value.equals(lastValue);
      }
    }

    /*
     * Log data and time if data changed
     * also publish to network table (if required)
     * also call consumer if set
     */
    public void log(T value, long time) {
      if (!hasSignificantChange(value)) {
        return;
      }

      appendEntry(value, time);

      if (ntPublisher != null) {
        publishToNetworkTable(value);
      }
      
      // Call consumer if set
      if (consumer != null) {
        consumer.accept(value, time);
      }
      
      lastValue = value;
    }

    private DataLogEntry createLogEntry(DataLog log, String name, String metaData) {
      if (isDoubleType) {
        return new DoubleLogEntry(log, name, metaData);
      } else if (isBooleanType) {
        return new BooleanLogEntry(log, name, metaData);
      } else if (isDoubleArrayType) {
        return new DoubleArrayLogEntry(log, name, metaData);
      } else if (isBooleanArrayType) {
        return new BooleanArrayLogEntry(log, name, metaData);
      }
      throw new IllegalArgumentException("Unsupported type: " + classType);
    }

    private Publisher createPublisher(NetworkTable table, String name) {
      if (isDoubleType) {
        return table.getDoubleTopic(name).publish();
      } else if (isBooleanType) {
        return table.getBooleanTopic(name).publish();
      } else if (isDoubleArrayType) {
        return table.getDoubleArrayTopic(name).publish();
      } else if (isBooleanArrayType) {
        return table.getBooleanArrayTopic(name).publish();
      }
      throw new IllegalArgumentException("Unsupported type: " + classType);
    }

    private void appendEntry(T value, long time) {
      if (isDoubleType) {
        ((DoubleLogEntry) entry).append((Double) value, time);
      } else if (isBooleanType) {
        ((BooleanLogEntry) entry).append((Boolean) value, time);
      } else if (isDoubleArrayType) {
        ((DoubleArrayLogEntry) entry).append((double[]) value, time);
      } else if (isBooleanArrayType) {
        ((BooleanArrayLogEntry) entry).append((boolean[]) value, time);
      }
    }

    private void publishToNetworkTable(T value) {
      if (isDoubleType) {
        ((DoublePublisher) ntPublisher).set((Double) value);
      } else if (isBooleanType) {
        ((BooleanPublisher) ntPublisher).set((Boolean) value);
      } else if (isDoubleArrayType) {
        ((DoubleArrayPublisher) ntPublisher).set((double[]) value);
      } else if (isBooleanArrayType) {
        ((BooleanArrayPublisher) ntPublisher).set((boolean[]) value);
      }
    }

    public void setPrecision(double precision) {
      this.precision = precision;
    }

    public double getPrecision() {
      return precision;
    }

    /*
     * Set skip interval for periodic logging (1 = every cycle, 2 = every other cycle, etc.)
     */
    public void setSkipCycles(int interval) {
      SkipCycle = Math.max(1, interval);
    }
  
    /*
     * Get current skip interval
     */
    public int getSkipCycles() {
      return SkipCycle;
    }

    // set the consumer
    public void setConsumer(BiConsumer<T, Long> consumer) {
      this.consumer = consumer;
    }

    public void removeInComp() {
      if (logLevel == 3 && ntPublisher != null) {
        ntPublisher.close();
      }
    }

  }

  // array of log entries
  ArrayList<LogEntry<?>> logEntries = new ArrayList<>();

  // Log managerconstructor
  public LogManager() {
    logManager = this;

    DataLogManager.start();
    DataLogManager.logNetworkTables(false);
    log = DataLogManager.getLog();
    DriverStation.startDataLog(log);
    
    activeConsole = new ArrayList<>();
    log("log manager is ready");

    updateCurrentLogFilePath();
  }

  private void updateCurrentLogFilePath() {
    // המתן קצת למערכת להתחיל
    // Timer.delay(0.1);
    
    String logDir = DataLogManager.getLogDir();
    if (logDir != null && !logDir.isEmpty()) {
      File logsDir = new File(logDir);
      File[] files = logsDir.listFiles((d, name) -> name.endsWith(".wpilog"));
      if (files != null && files.length > 0) {
        Arrays.sort(files, Comparator.comparingLong(File::lastModified).reversed());
        currentLogFilePath = files[0].getAbsolutePath();
        log("Current log file: " + currentLogFilePath);
      }
    }
  }

  public static String getCurrentLogFilePath() {
    if (currentLogFilePath != null) {
      return currentLogFilePath;
    }
    
    // אם אין נתיב נוכחי, חפש את הקובץ האחרון
    return findLatestLogFile();
  }

  // **שינוי 1: פונקציה לחיפוש קובץ הלוג האחרון**
  private static String findLatestLogFile() {
    String[] possiblePaths = {
      DataLogManager.getLogDir(), // אם רץ על רובוט
      System.getProperty("user.home") + "/wpilib/logs", // Windows/Linux/Mac
      System.getProperty("user.dir") + "/logs", // תיקיית הפרויקט
      "./logs", // יחסי לתיקיה נוכחית
      "../logs" // תיקיה אחת למעלה
    };
    
    File latestFile = null;
    long latestTime = 0;
    
    for (String path : possiblePaths) {
      if (path == null || path.isEmpty()) continue;
      
      File dir = new File(path);
      if (!dir.exists() || !dir.isDirectory()) continue;
      
      File[] files = dir.listFiles((d, name) -> name.endsWith(".wpilog"));
      if (files == null) continue;
      
      for (File file : files) {
        if (file.lastModified() > latestTime) {
          latestTime = file.lastModified();
          latestFile = file;
        }
      }
    }
    
    return latestFile != null ? latestFile.getAbsolutePath() : null;
  }

  /*
   * add a log entry with all data
   */
  private <T> LogEntry<T> add(String name, StatusSignal<T> phoenix6Status, StatusSignal<T>[] phoenix6StatusArray, Supplier<T> getter, int logLevel, String metaData, Class<T> classType) {
    LogEntry<T> entry = new LogEntry<T>(name, phoenix6Status, phoenix6StatusArray, getter,  logLevel, metaData, classType);
    logEntries.add(entry);
    return entry;
  }

  /*
   * get a log entry - if not found, create one
   */
  private LogEntry<?> get(String name) {
    LogEntry<?> e = find(name);
    return e != null 
    ?e 
    :new LogEntry(name, null, null, null, 1, "", (Class<Double>) Double.class);
  }

  public static void removeInComp() {
    ArrayList<LogEntry<?>> entries = logManager.logEntries;
    for (int i = entries.size()-1; i >= 0; i--) {
      LogEntry<?> entry = entries.get(i);
      entry.removeInComp();
      if (entry.logLevel == 1) {
        entries.remove(entry);
      }
    }
  }

  /*
   * find a log entry by name
   */
  private LogEntry<?> find(String name) {
    for (LogEntry<?> entry : logEntries) {
      if (entry.name.equals(name)) {
        return entry;
      }
    }
    return null;
  }
  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenixStatus, int logLevel, String metaData) {
    Class<T> classType;
    try{
      phoenixStatus.getValueAsDouble();
      classType = (Class<T>) Double.class;
    } catch(Exception e){
      classType = (Class<T>) phoenixStatus.getValue().getClass();
    }
    return logManager.add(name, phoenixStatus, null, null, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status, int logLevel) {
    return addEntry(name, phoenix6Status, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status, String metaData) {
    return addEntry(name, phoenix6Status, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T> phoenix6Status) {
    return addEntry(name, phoenix6Status, 4, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, int logLevel, String metaData) {
    Class<T> classType;
    try {
      phoenixStatusArray[0].getValueAsDouble();
      classType = (Class<T>) double[].class;
    } catch (Exception e) {
      classType = (Class<T>) boolean[].class;
    }
    return logManager.add(name, null, phoenixStatusArray, null, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, int logLevel) {
    return addEntry(name, phoenixStatusArray, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray, String metaData) {
    return addEntry(name, phoenixStatusArray, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, StatusSignal<T>[] phoenixStatusArray) {
    return addEntry(name, phoenixStatusArray, 4, "");
  }

  /*
   * Static function - add log entry for double supplier with option to add to
   * network table
   */
  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, int logLevel, String metaData) {
    T value = getter.get();
    Class<T> classType = (Class<T>) value.getClass();
    return logManager.add(name, null, null, getter, logLevel, metaData, classType);
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, int logLevel) {
    return addEntry(name, getter, logLevel, "");
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter, String metaData) {
    return addEntry(name, getter, 4, metaData);
  }

  public static <T> LogEntry<T> addEntry(String name, Supplier<T> getter) {
    return addEntry(name, getter, 4, "");
  }

  /*
   * Static function - get an entry, create if not foune - will see network table
   * is crating new
   */
  public static LogEntry<?> getEntry(String name) {
    return logManager.get(name);
  }

  /*
   * Log text message - also will be sent System.out
   */
  public static ConsoleAlert log(Object message, AlertType alertType) {
    DataLogManager.log(String.valueOf(message));
    
    ConsoleAlert alert = new ConsoleAlert(String.valueOf(message.toString()), alertType);
    alert.set(true);
    if (activeConsole.size() > ConsoleConstants.CONSOLE_LIMIT) {
      activeConsole.get(0).close();
      activeConsole.remove(0);
    }
    activeConsole.add(alert);
    return alert;
  }

  public static ConsoleAlert log(Object meesage) {
    return log(meesage, AlertType.kInfo);
  }

  @Override
  public void periodic() {
    // Configurable cycle skipping for performance optimization
    SkipedCycles1++;
    if (SkipedCycles1 < SKIP_CYCLES || !issenabled) {
      return;
    }
    SkipedCycles1 = 0;

    for (LogEntry<?> e : logEntries) {
      e.log();
    }
  }

  /*
   * Set skip interval for periodic logging (1 = every cycle, 2 = every other cycle, etc.)
   */
  public static void setStaticSkipCycles(int cycles) {
    SKIP_CYCLES = Math.max(1, cycles);
  }

  /*
   * Get current skip interval
   */
  public static int getSStaticSkipCycles() {
    return SKIP_CYCLES;
  }
  
  /*
   * Enable/disable all logging for performance in competition
   */
  public static void setLoggingEnabled(boolean isenabled) {
    issenabled = isenabled;
  }

  public static boolean getLoggingEnabled() {
    return issenabled;
  }
  
  /*
   * Get number of active log entries
   */
  public static int getEntryCount() {
    return logManager != null ? logManager.logEntries.size() : 0;
  }
  
  
  /*
   * Clear all log entries (useful for testing)
   */
  public static void clearEntries() {
    if (logManager != null) {
      logManager.logEntries.clear();
    }
  }
}