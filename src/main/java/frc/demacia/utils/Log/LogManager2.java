// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.demacia.utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.math.Pair;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Alert.AlertType;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.demacia.utils.Data;

public class LogManager2 extends SubsystemBase {

  public static LogManager2 logManager;

  DataLog log;
  NetworkTable table = NetworkTableInstance.getDefault().getTable("Log");

  private static ArrayList<ConsoleAlert> activeConsole;
  
  private static int SkipedCycles = 0;
  private static int SKIP_CYCLES = 1;
  private static boolean isLoggingEnabled = true;

  ArrayList<LogEntry2<?>> logEntries = new ArrayList<>();
  
  LogEntry2<?>[] finalLogEntries = new LogEntry2<?>[16];

  private Map<String, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> entryLocationMap = new HashMap<>();

  public LogManager2() {
    logManager = this;

    DataLogManager.start();
    DataLogManager.logNetworkTables(false);
    log = DataLogManager.getLog();
    DriverStation.startDataLog(log);
    
    activeConsole = new ArrayList<>();
    log("log manager is ready");
  }

  public static <T> LogEntryBuilder<T> addEntry(String name, StatusSignal<T> ... statusSignals) {
      return new LogEntryBuilder<T>(name, statusSignals);
  }

  public static <T> LogEntryBuilder<T> addEntry(String name, Supplier<T> ... suppliers) {
    return new LogEntryBuilder<T>(name, suppliers);
  }

  public static void removeInComp() {
    for (int i = 0; i < logManager.logEntries.size(); i++) {
      logManager.logEntries.get(i).removeInComp();
      if (logManager.logEntries.get(i).logLevel == 1) {
        logManager.logEntries.remove(logManager.logEntries.get(i));
        i--;
      }
    }

    for (int i = 0; i < 4; i++) {
      if (logManager.finalLogEntries[i] != null && logManager.finalLogEntries[i] != null) {
        logManager.finalLogEntries[i].removeInComp();
        if (logManager.finalLogEntries[i].logLevel == 1) {
          logManager.finalLogEntries[i] = null;
          int index = i;
          logManager.entryLocationMap.entrySet().removeIf(entry -> entry.getValue().getFirst().getFirst() == index);
        }
      }
    }
  }
  
  public static void clearEntries() {
    if (logManager != null) {
      logManager.logEntries.clear();
      for (int i = 0; i < logManager.finalLogEntries.length; i++) {
        if (logManager.finalLogEntries[i] != null) {
          logManager.finalLogEntries[i] = null;
        }
      }
      logManager.entryLocationMap.clear();
    }
  }
  
  public static int getEntryCount() {
    if (logManager == null) return 0;

    int count = logManager.logEntries.size();
    for (LogEntry2<?> entry2 : logManager.finalLogEntries) {
      if (entry2 != null) {
        count++;
      }
    }
    return count;
  }

  public static LogEntry2<?> findEntry(String name) {
    if (logManager == null) return null;
    
    Pair< Pair<Integer, Integer>, Pair<Integer, Integer>> location = logManager.entryLocationMap.get(name);
    if (location == null) return null;
    
    int categoryIndex = location.getFirst().getFirst();
    
    if (categoryIndex == -1) {
      int index = location.getFirst().getSecond();
      if (index >= 0 && index < logManager.logEntries.size()) {
        return logManager.logEntries.get(index);
      }
    } else {
      return logManager.finalLogEntries[categoryIndex];
    }
    
    return null;
  }

  public static boolean removeEntry(String name) {
      if (logManager == null) return false;
      
      Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> location = logManager.entryLocationMap.get(name);
      if (location == null) return false;
      
      int categoryIndex = location.getFirst().getFirst();
      
      if (categoryIndex == -1) {
          int index = location.getFirst().getSecond();
          if (index >= 0 && index < logManager.logEntries.size()) {
              logManager.logEntries.remove(index);
              logManager.entryLocationMap.remove(name);
              
              logManager.updateIndicesAfterRemoval(index);
              return true;
          }
      } else {
          int subIndex = location.getFirst().getSecond();
          int dataIndex = location.getSecond().getFirst();
          int dataCount = location.getSecond().getSecond();
          
          logManager.entryLocationMap.remove(name);
          
          logManager.finalLogEntries[categoryIndex].removeData(subIndex, dataIndex, dataCount);
          
          logManager.updateSubIndicesAfterRemoval(categoryIndex, subIndex);
          logManager.updateDataIndicesAfterRemoval(categoryIndex, dataIndex, dataCount);
          
          if (logManager.finalLogEntries[categoryIndex].data == null || 
              logManager.finalLogEntries[categoryIndex].name == null || 
              logManager.finalLogEntries[categoryIndex].name.trim().isEmpty()) {
              logManager.finalLogEntries[categoryIndex] = null;
              final int catIndex = categoryIndex;
              logManager.entryLocationMap.entrySet().removeIf(
                  entry -> entry.getValue().getFirst().getFirst() == catIndex
              );
          }
          
          return true;
      }
      
      return false;
  }

  public static ConsoleAlert log(Object message, AlertType alertType) {
    DataLogManager.log(String.valueOf(message));
    
    ConsoleAlert alert = new ConsoleAlert(String.valueOf(message), alertType);
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

  public static void setStaticSkipCycles(int cycles) {
    SKIP_CYCLES = Math.max(1, cycles);
  }

  public static int getStaticSkipCycles() {
    return SKIP_CYCLES;
  }
  
  public static void setLoggingEnabled(boolean isenabled) {
    isLoggingEnabled = isenabled;
  }

  public static boolean getLoggingEnabled() {
    return isLoggingEnabled;
  }

  @Override
  public void periodic() {
    SkipedCycles++;
    if (SkipedCycles < SKIP_CYCLES || !isLoggingEnabled) {
      return;
    }
    SkipedCycles = 0;

    for (LogEntry2<?> e : logEntries) {
      e.log();
    }
    for (LogEntry2<?> e : finalLogEntries) {
      if (e != null){
        e.log();
      }
    }
  }

  public <T> LogEntry2<T> add(String name, Data<T> data, int logLevel, String metaData) {
    LogEntry2<T> entry = null;

    int categoryIndex = getCategoryIndex(data, logLevel, metaData);

    if (categoryIndex == -1){
      entry = new LogEntry2<T>(name, data, logLevel, metaData);
      logEntries.add(entry);
      int index = logEntries.size() - 1;
      entryLocationMap.put(name, new Pair<>(new Pair<>(-1, index), new Pair<>(null, null)));
    } else{
      entry = addToEntryArray(categoryIndex, name, data);
    }

    return entry;
  }

  @SuppressWarnings("unchecked")
  private <T> LogEntry2<T> addToEntryArray(int i, String name, Data<T> data) {
    int subIndex;
    int dataIndex = 0;
    
    if (finalLogEntries[i] == null || finalLogEntries[i].data == null) {
        finalLogEntries[i] = new LogEntry2<>(name, data, i/4 + 1, "");
        subIndex = 1;
        dataIndex = 0;
    } else {
        String[] parts = finalLogEntries[i].name.split(" \\| ");
        subIndex = parts.length + 1;

        if (finalLogEntries[i].data.getSignals() != null) {
          dataIndex = finalLogEntries[i].data.getSignals().length;
        } else if (finalLogEntries[i].data.getSuppliers() != null) {
          dataIndex = finalLogEntries[i].data.getSuppliers().length;
        }

        try {
            ((LogEntry2<T>) finalLogEntries[i]).addData(name, data);
        } catch (Exception e) {
            LogManager2.log("Error combining log entries: " + e.getMessage(), AlertType.kError);
        }
    }

    int dataLength = 0;
    if (data.getSignals() != null) {
      dataLength = data.getSignals().length;
    } else if (data.getSuppliers() != null) {
      dataLength = data.getSuppliers().length;
    }


    entryLocationMap.put(name, new Pair<>(new Pair<>(i, subIndex), new Pair<>(dataIndex, dataLength)));
    log("added:" + name + 
    " with: " + i + 
    " and: " + subIndex + 
    " and: " + dataIndex + 
    " and: " + dataLength);
    
    return (LogEntry2<T>) finalLogEntries[i];
}

  private int getCategoryIndex(Data<?> data, int logLevel, String metaData) {
    boolean isSignal = data.getSignals() != null;
    boolean isSupplier = data.getSuppliers() != null;
    boolean isDouble = data.isDouble();
    boolean isBoolean = data.isBoolean();
    
    if (!(isDouble || isBoolean) || !(isSignal || isSupplier) || metaData != "") {
      return -1;
    }
    
    int baseIndex = (isSignal ? 0 : 2) + (isDouble ? 0 : 1);
    int levelOffset = (logLevel - 1) * 4;
    
    return baseIndex + levelOffset;
  }

  private void updateIndicesAfterRemoval(int removedIndex) {
      for (Map.Entry<String, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> entry : entryLocationMap.entrySet()) {
          Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> location = entry.getValue();
          if (location.getFirst().getFirst() == -1 && location.getFirst().getSecond() > removedIndex) {
              entry.setValue(
                  new Pair<>(
                      new Pair<>(-1, location.getFirst().getSecond() - 1), 
                      new Pair<>(location.getSecond().getFirst(), location.getSecond().getSecond())
                  )
              );
          }
      }
  }

  private void updateSubIndicesAfterRemoval(int categoryIndex, int removedSubIndex) {
      for (Map.Entry<String, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> entry : entryLocationMap.entrySet()) {
          Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> location = entry.getValue();
          if (location.getFirst().getFirst() == categoryIndex && location.getFirst().getSecond() > removedSubIndex) {
              entry.setValue(
                  new Pair<>(
                      new Pair<>(categoryIndex, location.getFirst().getSecond() - 1),
                      new Pair<>(location.getSecond().getFirst(), location.getSecond().getSecond())
                  )
              );
          }
      }
  }

  private void updateDataIndicesAfterRemoval(int categoryIndex, int removedDataIndex, int removedCount) {
      for (Map.Entry<String, Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>> entry : entryLocationMap.entrySet()) {
          Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> location = entry.getValue();
          if (location.getFirst().getFirst() == categoryIndex && 
              location.getSecond().getFirst() >= removedDataIndex + removedCount) {
              entry.setValue(
                  new Pair<>(
                      new Pair<>(location.getFirst().getFirst(), location.getFirst().getSecond()),
                      new Pair<>(location.getSecond().getFirst() - removedCount, location.getSecond().getSecond())
                  )
              );
          }
      }
  }


  private LogEntry2<?> get(String name) {
    LogEntry2<?> e = find(name);
    return e != null 
    ?e 
    :new LogEntry2<>(name, null, 1, "");
  }

  private LogEntry2<?> find(String name) {
    for (LogEntry2<?> entry : logEntries) {
      if (entry.name.equals(name)) {
        return entry;
      }
    }
    return null;
  }
}