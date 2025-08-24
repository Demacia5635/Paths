package frc.demacia.SysID;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.wpi.first.util.datalog.DataLogReader;

public class LogReader {

    private static Map<Integer, EntryDescription> entries;

  private static class EntryDescription {
    int entryId;
    String name;
    String type;
    String metadata;
    List<DataPoint> data = new ArrayList<>();
    
    EntryDescription(int id, String name, String type, String meta) {
      this.entryId = id;
      this.name = name;
      this.type = type;
      this.metadata = meta;
    }
  }

  private static class DataPoint {
    long timestamp;
    double[] value;
    
    DataPoint(long timestamp, double[] value) {
      this.timestamp = timestamp;
      this.value = value.clone();
    }
  }

  public static class SysIDResults {
        public double kS;
        public double kV;
        public double kA;
        public double kP;
        public double error;
        public int dataPoints;
        
        public SysIDResults(double kS, double kV, double kA, double kP, double error, int points) {
            this.kS = kS;
            this.kV = kV; 
            this.kA = kA;
            this.kP = kP;
            this.error = error;
            this.dataPoints = points;
        }
        
        @Override
        public String toString() {
            return String.format("KS=%.4f, KV=%.4f, KA=%.6f, Error=%.4f, Points=%d", 
                               kS, kV, kA, error, dataPoints);
        }
    }

  public static Map<String, SysIDResults> getReasult(String fileName){
    entries = new HashMap<>();
    try {
      WpilogReader(fileName);
      return performAnalysis();
    } catch(IOException e){
      System.err.println("Error reading log file: " + e.getMessage());
      return new HashMap<>();
    }
  }
  private static void WpilogReader(String fileName) throws IOException {
    try(FileInputStream fileInputStream = new FileInputStream(fileName);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream)){
      if (!Arrays.equals(readHeader(dataInputStream), "WPILOG".getBytes())) {
        throw new IOException("Invalid WPILOG file format");
      }
      skipHeaderExtra(dataInputStream);
      readRecords(dataInputStream);
    }
  }
  private static byte[] readHeader(DataInputStream dataInputStream) throws IOException {
    byte[] signature = new byte[6];
    dataInputStream.readFully(signature);
    return signature;
  }
  private static void skipHeaderExtra(DataInputStream dataInputStream) throws IOException {
    Short.reverseBytes(dataInputStream.readShort());
    int extraLength = Integer.reverseBytes(dataInputStream.readInt());
    dataInputStream.skipBytes(extraLength);
  }
  private static void readRecords(DataInputStream dataInputStream) throws IOException {
    while (dataInputStream.available() > 0) {
      try {
        readRecord(dataInputStream);
      } catch (EOFException e) {
        break;
      }
    }
  }
  private static void readRecord(DataInputStream dataInputStream) throws IOException {
    int headerByte = dataInputStream.readUnsignedByte();
    
    int idLength = (headerByte & 0x3) + 1;
    int payloadLength = (headerByte >> 2 & 0x3) + 1;  
    int timestampLength = (headerByte >> 4 & 0x7) + 1;
    
    int recordId = readLittleEndianInt(dataInputStream, idLength);
    int payloadSize = readLittleEndianInt(dataInputStream, payloadLength);
    long timestamp = readLittleEndianLong(dataInputStream, timestampLength);
    
    if (recordId == 0) {
      addEntryFromControlRecord(dataInputStream, payloadSize);
    } else {//TODO
      EntryDescription entry = entries.get(recordId);
      if (entry != null && "double[]".equals(entry.type) && payloadSize % 8 == 0) {
        int count = payloadSize / 8;
        double[] value = new double[count];
        for (int i = 0; i < count; i++) {
            long raw = Long.reverseBytes(dataInputStream.readLong());
            value[i] = Double.longBitsToDouble(raw);
        }
        entry.data.add(new DataPoint(timestamp, value));
      } else {
        dataInputStream.skipBytes(payloadSize);
      }
    }
  }
  private static void addEntryFromControlRecord(DataInputStream dataInputStream, int payloadSize) throws IOException {
    int recordType = dataInputStream.readUnsignedByte();
    
    if (recordType == 0) { // Start record - new entry definition
      int entryId = Integer.reverseBytes(dataInputStream.readInt());
      
      int nameLength = Integer.reverseBytes(dataInputStream.readInt());
      String name = readString(dataInputStream, nameLength);
      
      int typeLength = Integer.reverseBytes(dataInputStream.readInt());
      String type = readString(dataInputStream, typeLength);
      
      int metaLength = Integer.reverseBytes(dataInputStream.readInt());
      String metadata = readString(dataInputStream, metaLength);
      
      if ("motor".equals(metadata)){
        entries.put(entryId, new EntryDescription(entryId, name, type, metadata));
      }
    } else {
      dataInputStream.skipBytes(payloadSize - 1);
    }
  }
  private static String readString(DataInputStream dataInputStream, int length) throws IOException {
    byte[] bytes = new byte[length];
    dataInputStream.readFully(bytes);
    return new String(bytes, "UTF-8");
  }
  private static int readLittleEndianInt(DataInputStream dis, int bytes) throws IOException {
    int result = 0;
    for (int i = 0; i < bytes; i++) {
      result |= (dis.readUnsignedByte() << (i * 8));
    }
    return result;
  }
  private static long readLittleEndianLong(DataInputStream dis, int bytes) throws IOException {
    long result = 0;
    for (int i = 0; i < bytes; i++) {
      result |= ((long)dis.readUnsignedByte() << (i * 8));
    }
    return result;
  }



  private static Map<String, SysIDResults> performAnalysis(){
    Map<String, SysIDResults> results = new HashMap<>();

    Set<String> groups = findGroups();

    for (String group : groups) {
      SysIDResults result = analyzeGroup(group);
      if (result != null) {
        results.put(group, result);
        System.out.println("Group " + group + ": " + result);
      }
    }

    return results;
  }
  private static Set<String> findGroups() {
    Set<String> groups = new HashSet<>();

    for (EntryDescription entry : entries.values()) {
      String name = entry.name;
      int lastSlash = name.lastIndexOf('/');
      if (lastSlash > 0) {
        String group = name.substring(0, lastSlash);
        groups.add(group);
      }
    }  
    return groups;
  }

  private static SysIDResults analyzeGroup(String groupName) {
    EntryDescription entry = findEntry(groupName + "/Position and Velocity and Acceleration and Voltage and Current and CloseLoopError and CloseLoopSP");

    if (entry == null || entry.data.size() == 0) {
      System.out.println("Missing data for group: " + groupName);
      return null;
    }
    
    List<SyncedDataPoint> syncedData = synchronizeData(entry.data);
    
    return performRegression(syncedData);
  }
  private static EntryDescription findEntry(String name) {
    for (EntryDescription entry : entries.values()) {
      if (entry.name.equals(name)) {
        return entry;
      }
    }
    return null;
  }
  private static class SyncedDataPoint {
    double velocity;
    double position;
    double acceleration;
    double voltage;
    long timestamp;
    
    SyncedDataPoint(double velocity, double position, double acceleration, double voltage, long timestamp) {
        this.velocity = velocity;
        this.position = position;
        this.acceleration = acceleration;
        this.voltage = voltage;
        this.timestamp = timestamp;
    }
  }
  private static List<SyncedDataPoint> synchronizeData(List<DataPoint> dataPointList) {
    
    
    List<SyncedDataPoint> result = new ArrayList<>();

    for(int index = 0; index < dataPointList.size(); index++) {
      DataPoint dataPoint = dataPointList.get(index);
        
      double velocity = dataPoint.value[1];
      double position = dataPoint.value[0];
      double acceleration = dataPoint.value[2];
      double voltage = dataPoint.value[3];

      long time = dataPoint.timestamp;

      result.add(new SyncedDataPoint(velocity, position, acceleration, voltage, time));
    }
    
    return result;
  }

  private static SysIDResults performRegression(List<SyncedDataPoint> data) {
    
  }
}
