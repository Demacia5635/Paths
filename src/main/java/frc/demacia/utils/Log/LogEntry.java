// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.demacia.utils.Log;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.ctre.phoenix6.StatusCode;
import com.ctre.phoenix6.StatusSignal;

import edu.wpi.first.networktables.BooleanArrayPublisher;
import edu.wpi.first.networktables.BooleanPublisher;
import edu.wpi.first.networktables.DoubleArrayPublisher;
import edu.wpi.first.networktables.DoublePublisher;
import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.Publisher;
import edu.wpi.first.util.datalog.BooleanArrayLogEntry;
import edu.wpi.first.util.datalog.BooleanLogEntry;
import edu.wpi.first.util.datalog.DataLog;
import edu.wpi.first.util.datalog.DataLogEntry;
import edu.wpi.first.util.datalog.DoubleArrayLogEntry;
import edu.wpi.first.util.datalog.DoubleLogEntry;
import frc.robot.RobotContainer;

public class LogEntry<T> {

    private final LogManager logManager;

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

        logManager = LogManager.logManager;

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

        this.entry = createLogEntry(logManager.log, name, metaData);

        if (logLevel == 4 || (logLevel == 3 && !RobotContainer.isComp())) {
        this.ntPublisher = createPublisher(logManager.table, name);
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
