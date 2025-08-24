package frc.utils;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.wpi.first.units.measure.Velocity;
import edu.wpi.first.wpilibj.DataLogManager;
import frc.demacia.utils.Log.LogManager;

public class Sysid {

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
        public double error;
        public int dataPoints;
        
        public SysIDResults(double kS, double kV, double kA, double error, int points) {
            this.kS = kS;
            this.kV = kV; 
            this.kA = kA;
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
    double acceleration;
    double voltage;
    long timestamp;
    
    SyncedDataPoint(double v, double a, double volt, long time) {
        this.velocity = v;
        this.acceleration = a;
        this.voltage = volt;
        this.timestamp = time;
    }
  }
  private static List<SyncedDataPoint> synchronizeData(List<DataPoint> dataPointList) {
    
    
    List<SyncedDataPoint> result = new ArrayList<>();

    for(int index = 0; index < dataPointList.size(); index++) {
      DataPoint dataPoint = dataPointList.get(index);

      long time = dataPoint.timestamp;
        
      double velocity = dataPoint.value[1];
      double acceleration = dataPoint.value[2];
      double voltage = dataPoint.value[3];

      result.add(new SyncedDataPoint(velocity, acceleration, voltage, time));
    }
    
    return result;
  }
  private static SysIDResults performRegression(List<SyncedDataPoint> data) {
    int n = data.size();
    
    // Build matrices for least squares: A*x = b
    // where x = [KS, KV, KA]
    double[][] A = new double[n][3];
    double[] b = new double[n];
    
    for (int i = 0; i < n; i++) {
        SyncedDataPoint point = data.get(i);
        
        // A matrix: [sign(velocity), velocity, acceleration]
        A[i][0] = Math.signum(point.velocity); // KS term
        A[i][1] = point.velocity;              // KV term  
        A[i][2] = point.acceleration;          // KA term
        
        // b vector: voltage
        b[i] = point.voltage;
    }
    
    // Solve least squares using normal equations: (A^T * A) * x = A^T * b
    double[][] AtA = multiplyMatrices(transpose(A), A);
    double[] Atb = multiplyMatrixVector(transpose(A), b);
    
    // Solve the system (simplified - in practice you'd use proper matrix decomposition)
    double[] solution = solveLinearSystem(AtA, Atb);
    if (solution == null) {
      System.err.println("there is no solution");
      return null;
    }
    
    // Calculate error
    double error = calculateRMSError(A, solution, b);
    
    return new SysIDResults(solution[0], solution[1], solution[2], error, n);
  }
  private static double[][] transpose(double[][] matrix) {
    int rows = matrix.length;
    int cols = matrix[0].length;
    double[][] result = new double[cols][rows];
    
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < cols; j++) {
        result[j][i] = matrix[i][j];
      }
    }
    return result;
  }

  private static double[][] multiplyMatrices(double[][] a, double[][] b) {
    int aRows = a.length;
    int aCols = a[0].length;
    int bCols = b[0].length;
    double[][] result = new double[aRows][bCols];
    
    for (int i = 0; i < aRows; i++) {
      for (int j = 0; j < bCols; j++) {
        for (int k = 0; k < aCols; k++) {
          result[i][j] += a[i][k] * b[k][j];
        }
      }
    }
    return result;
  }

  private static double[] multiplyMatrixVector(double[][] matrix, double[] vector) {
    int rows = matrix.length;
    double[] result = new double[rows];
    
    for (int i = 0; i < rows; i++) {
      for (int j = 0; j < matrix[i].length; j++) {
        result[i] += matrix[i][j] * vector[j];
      }
    }
    return result;
  }

  private static double[] solveLinearSystem(double[][] A, double[] b) {
    // Simplified Gaussian elimination (in practice, use proper linear algebra library)
    int n = A.length;
    double[][] augmented = new double[n][n + 1];
    
    // Create augmented matrix
    for (int i = 0; i < n; i++) {
      System.arraycopy(A[i], 0, augmented[i], 0, n);
      augmented[i][n] = b[i];
    }
    
    // Forward elimination
    for (int i = 0; i < n; i++) {
      // Find pivot
      int maxRow = i;
      for (int k = i + 1; k < n; k++) {
        if (Math.abs(augmented[k][i]) > Math.abs(augmented[maxRow][i])) {
          maxRow = k;
        }
      }
      
      // Swap rows
      double[] temp = augmented[i];
      augmented[i] = augmented[maxRow];
      augmented[maxRow] = temp;
      
      // Check for singular matrix
      if (Math.abs(augmented[i][i]) < 1e-10) {
        return null;
      }
      
      // Eliminate
      for (int k = i + 1; k < n; k++) {
        double factor = augmented[k][i] / augmented[i][i];
        for (int j = i; j <= n; j++) {
          augmented[k][j] -= factor * augmented[i][j];
        }
      }
    }
    
    // Back substitution
    double[] solution = new double[n];
    for (int i = n - 1; i >= 0; i--) {
      solution[i] = augmented[i][n];
      for (int j = i + 1; j < n; j++) {
        solution[i] -= augmented[i][j] * solution[j];
      }
      solution[i] /= augmented[i][i];
    }
    
    return solution;
  }

  private static double calculateRMSError(double[][] A, double[] x, double[] b) {
    double sumSquaredError = 0;
    int n = b.length;
    
    for (int i = 0; i < n; i++) {
      double predicted = 0;
      for (int j = 0; j < x.length; j++) {
          predicted += A[i][j] * x[j];
      }
      double error = b[i] - predicted;
      sumSquaredError += error * error;
    }
    
    return Math.sqrt(sumSquaredError / n);
  }






}
