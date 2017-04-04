//WeightMatrix.java

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * Matriks yang menggambarkan bobot dari edge.
 * @author Arno Alexander
 */
public class WeightMatrix {

  /*Bobot yang di-assign ke dalam matriks jika tidak ada sisi*/
  public final static double INVALID_WEIGHT = -1;
  /*Indeks simpul awal dari TSP*/
  public final static int START_VERTEX = 0;

  /*Banyak vertex dari graf yang sama dengan banyak baris dan banyak kolom matriks masukan*/
  private int numberOfVertex;
  /*Matriks bobot*/
  private double weight[][];
  /*Banyaknya simpul yang di-generate untuk memecahkan masalah*/
  public int numberOfSimpulToSolve;
  /*Bobot tur terpendek*/
  public double shortestTourWeight;

  /**
   * Konstruktor.
   * Prekondisi : inputPath berisi dari n baris, masing-masing baris terdiri dari n buah integer, n>0.
   * @param inputPath lokasi relatif file teks input dalam folder.
   */
  public WeightMatrix(String inputPath) throws FileNotFoundException, NoSuchElementException {
    Scanner scanner = new Scanner(new File(inputPath));
    String[] firstLine = scanner.nextLine().split("(\\s)+");
    numberOfVertex = firstLine.length;
    weight = new double[numberOfVertex][numberOfVertex];
    for (int i = 0; i < numberOfVertex; i++) {
      try {
        double parsedWeight = Double.parseDouble(firstLine[i]);
        if (parsedWeight > 0 && i != 0) {
          weight[0][i] = parsedWeight;
        } else {
          weight[0][i] = INVALID_WEIGHT;
        }
      } catch(NumberFormatException nfe) {
        weight[0][i] = INVALID_WEIGHT;
      }
    }
    for (int i = 1; i < numberOfVertex; i++) {
      for (int j = 0; j < numberOfVertex; j++) {
        try {
          double parsedWeight = Double.parseDouble(scanner.next());
          if (parsedWeight > 0 && i != j) {
            weight[i][j] = parsedWeight;
          } else {
            weight[i][j] = INVALID_WEIGHT;
          }
        } catch(NumberFormatException nfe) {
          weight[i][j] = INVALID_WEIGHT;
        }
      }
    }
  }

  /**
   * Copy Constructor.
   * @param weightMatrix matriks yang ingin di-copy
   */
  public WeightMatrix(WeightMatrix weightMatrix) {
    numberOfVertex = weightMatrix.numberOfVertex;
    weight = new double[numberOfVertex][numberOfVertex];
    for (int i = 0; i < numberOfVertex; i++) {
      for (int j = 0; j < numberOfVertex; j++) {
        weight[i][j] = weightMatrix.weight[i][j];
      }
    }
  }

  /**
   * Getter numberOfVertex.
   * @return banyaknya node dalam graf
   */
  public int getNumberOfVertex() {
    return numberOfVertex;
  }

  /**
   * Getter weight.
   * @param beginNode node asal
   * @param endNode node tujuan
   * @return bobot dari node asal ke node tujuan
   */
  public double getWeight(int beginNode, int endNode) {
    return weight[beginNode][endNode];
  }

  /**
   * Mencetak matriks ke terminal.
   */
  public void print() {
    for (int i = 0; i < numberOfVertex; i++) {
      for (int j = 0; j < numberOfVertex; j++) {
        System.out.print(weight[i][j]+" ");
      }
      System.out.println();
    }
    System.out.println();
  }

  /**
   * Menyelesaikan TSP dengan Bobot Tur Lengkap.
   * @return array berisi urutan nomor vertex yang harus dilewati
   */
  public int[] solveBobotTurLengkap() {
    numberOfSimpulToSolve = 0;
    PriorityQueue<SimpulBobotTurLengkap> queue
        = new PriorityQueue<>(new SimpulBobotTurLengkapComparator());
    SimpulBobotTurLengkap head = new SimpulBobotTurLengkap();
    queue.add(head);
    numberOfSimpulToSolve++;
    while (!queue.isEmpty()) {
      head = queue.poll();
      shortestTourWeight = head.cost;
      if (head.level < numberOfVertex) { //bukan daun
        for (int i = 0; i < numberOfVertex; ++i) {
          if (!head.isVisited[i]) {
            SimpulBobotTurLengkap simpul = new SimpulBobotTurLengkap(head, i);
            queue.add(simpul);
            numberOfSimpulToSolve++;
          }
        }
      } else { //daun, bunuh simpul yang pasti bukan solusi
        PriorityQueue<SimpulBobotTurLengkap> temporaryQueue
            = new PriorityQueue<>(new SimpulBobotTurLengkapComparator());
        while (!queue.isEmpty()) {
          SimpulBobotTurLengkap otherThanHead = queue.poll();
          if (otherThanHead.cost < head.cost) {
            temporaryQueue.add(otherThanHead);
          }
        }
        queue = temporaryQueue;
      }
    }
    int[] solution = new int[numberOfVertex];
    for (int i = numberOfVertex-1; i >= 0; --i) {
      solution[i] = head.vertex;
      head = head.parent;
    }
    return solution;
  }

  /**
   * Menyelesaikan TSP dengan Reduced Cost Matrix.
   * @return array berisi urutan nomor vertex yang harus dilewati
   */
  public int[] solveReducedCostMatrix() {
    numberOfSimpulToSolve = 0;
    PriorityQueue<SimpulReducedCostMatrix> queue
        = new PriorityQueue<>(new SimpulReducedCostMatrixComparator());
    SimpulReducedCostMatrix head = new SimpulReducedCostMatrix(this);
    queue.add(head);
    numberOfSimpulToSolve++;
    while (!queue.isEmpty()) {
      head = queue.poll();
      shortestTourWeight = head.cost;
      if (head.level < numberOfVertex) { //bukan daun
        for (int i = 0; i < numberOfVertex; ++i) {
          if (head.matrix.weight[head.vertex][i] != INVALID_WEIGHT) {
            SimpulReducedCostMatrix simpul = new SimpulReducedCostMatrix(head, i);
            queue.add(simpul);
            numberOfSimpulToSolve++;
          }
        }
      } else { //daun, bunuh simpul yang pasti bukan solusi
        PriorityQueue<SimpulReducedCostMatrix> temporaryQueue
            = new PriorityQueue<>(new SimpulReducedCostMatrixComparator());
        while (!queue.isEmpty()) {
          SimpulReducedCostMatrix otherThanHead = queue.poll();
          if (otherThanHead.cost < head.cost) {
            temporaryQueue.add(otherThanHead);
          }
        }
        queue = temporaryQueue;
      }
    }
    int[] solution = new int[numberOfVertex];
    for (int i = numberOfVertex-1; i >= 0; --i) {
      solution[i] = head.vertex;
      head = head.parent;
    }
    return solution;
  }

  /**
   * Kelas simpul untuk pohon ruang status Bobot Tur Lengkap.
   */
  private class SimpulBobotTurLengkap {

    public final SimpulBobotTurLengkap parent; //simpul parent
    public final boolean[] isVisited; //apakah vertex pada suatu indeks sudah dikunjungi
    public double cost; //nilai batas
    public final int level; //level simpul pada pohon ruang status
    public final int vertex; //vertex yang ditempati pada simpul ini

    public SimpulBobotTurLengkap() {
      parent = null;
      isVisited = new boolean[numberOfVertex];
      level = 1;
      vertex = START_VERTEX;
      isVisited[vertex] = true;
      computeCost();
    }

    public SimpulBobotTurLengkap(SimpulBobotTurLengkap parent, int nextVertex) {
      this.parent = parent;
      isVisited = parent.isVisited.clone();
      level = parent.level + 1;
      vertex = nextVertex;
      isVisited[vertex] = true;
      computeCost();
    }

    void computeCost() {
      cost = 0;
      int[][] indexOfMinimumNeighbor = new int[numberOfVertex][2];
      int[] numberOfMinimumNeighbor = new int[numberOfVertex];
      SimpulBobotTurLengkap scannedSimpul = this;
      while (scannedSimpul != null) {
        if (scannedSimpul.parent != null) {
          indexOfMinimumNeighbor[scannedSimpul.vertex][numberOfMinimumNeighbor
              [scannedSimpul.vertex]] = scannedSimpul.parent.vertex;
          numberOfMinimumNeighbor[scannedSimpul.vertex]++;
          indexOfMinimumNeighbor[scannedSimpul.parent.vertex][numberOfMinimumNeighbor
              [scannedSimpul.parent.vertex]] = scannedSimpul.vertex;
          numberOfMinimumNeighbor[scannedSimpul.parent.vertex]++;
        }
        scannedSimpul = scannedSimpul.parent;
      }
      for (int i = 0; i < numberOfVertex; i++) { //iterasi baris
        while (numberOfMinimumNeighbor[i] < 2) {
          int excludedIndex;
          if (numberOfMinimumNeighbor[i] == 1) {
            excludedIndex = indexOfMinimumNeighbor[i][0];
          } else {
            excludedIndex = -1;
          }
          double minimumWeight = Double.MAX_VALUE;
          int minimumWeightIndex = -1;
          int numberOfValidWeight = 0;
          for (int j = 0; j < numberOfVertex; j++) { //mencari bobot minimum
            if (j != excludedIndex) {
              if (weight[i][j] != INVALID_WEIGHT) {
                numberOfValidWeight++;
                if (weight[i][j] < minimumWeight) {
                  minimumWeight = weight[i][j];
                  minimumWeightIndex = j;
                }
              }
            }
          }
          if (numberOfValidWeight > 0) {
            indexOfMinimumNeighbor[i][numberOfMinimumNeighbor[i]] = minimumWeightIndex;
            numberOfMinimumNeighbor[i]++;
          }
        }
      }
      for (int i = 0; i < numberOfVertex; i++) {
        cost += weight[i][indexOfMinimumNeighbor[i][0]];
        cost += weight[i][indexOfMinimumNeighbor[i][1]];
      }
      cost /= 2;
    }

  }

  /**
   * Kelas simpul untuk pohon ruang status Reduced Cost Matrix.
   */
  private class SimpulReducedCostMatrix {

    public final SimpulReducedCostMatrix parent; //simpul parent
    public WeightMatrix matrix; //matriks bobot
    public double cost; //nilai batas
    public final int level; //level simpul pada pohon ruang status
    public final int vertex; //vertex yang ditempati pada simpul ini

    public SimpulReducedCostMatrix(WeightMatrix matrix) {
      parent = null;
      this.matrix = new WeightMatrix(matrix);
      cost = 0;
      level = 1;
      vertex = START_VERTEX;
      reduceCost();
    }

    public SimpulReducedCostMatrix(SimpulReducedCostMatrix parent, int nextVertex) {
      this.parent = parent;
      matrix = new WeightMatrix(parent.matrix);
      for (int j = 0; j < matrix.numberOfVertex; ++j) {
        matrix.weight[parent.vertex][j] = INVALID_WEIGHT;
      }
      for (int i = 0; i < matrix.numberOfVertex; ++i) {
        matrix.weight[i][nextVertex] = INVALID_WEIGHT;
      }
      matrix.weight[nextVertex][0] = INVALID_WEIGHT;
      cost = parent.cost + parent.matrix.weight[parent.vertex][nextVertex];
      level = parent.level+1;
      this.vertex = nextVertex;
      reduceCost();
    }

    public void reduceCost() {
      for (int i = 0; i < matrix.numberOfVertex; ++i) {
        boolean isZeroExist = false;
        double minimumCost = Double.MAX_VALUE;
        int numberOfInvalid = 0;
        for (int j = 0; j < matrix.numberOfVertex; ++j) {
          if (matrix.weight[i][j] == 0.0) {
            isZeroExist = true;
            break;
          } else if (matrix.weight[i][j] != INVALID_WEIGHT) {
            if (matrix.weight[i][j] < minimumCost) {
              minimumCost = matrix.weight[i][j];
            }
          } else {
            numberOfInvalid++;
          }
        }
        if (!isZeroExist) {
          if (numberOfInvalid < matrix.numberOfVertex) cost += minimumCost;
          for (int j = 0; j < matrix.numberOfVertex; ++j) {
            if (matrix.weight[i][j] != INVALID_WEIGHT) {
              matrix.weight[i][j] -= minimumCost;
            }
          }
        }
      }
      for (int j = 0; j < matrix.numberOfVertex; ++j) {
        boolean isZeroExist = false;
        double minimumCost = Double.MAX_VALUE;
        int numberOfInvalid = 0;
        for (int i = 0; i < matrix.numberOfVertex; ++i) {
          if (matrix.weight[i][j] == 0.0) {
            isZeroExist = true;
            break;
          } else if (matrix.weight[i][j] != INVALID_WEIGHT) {
            if (matrix.weight[i][j] < minimumCost) {
              minimumCost = matrix.weight[i][j];
            }
          } else {
            numberOfInvalid++;
          }
        }
        if (!isZeroExist) {
          if (numberOfInvalid < matrix.numberOfVertex) cost += minimumCost;
          for (int i = 0; i < matrix.numberOfVertex; ++i) {
            if (matrix.weight[i][j] != INVALID_WEIGHT) {
              matrix.weight[i][j] -= minimumCost;
            }
          }
        }
      }
    }
  }

  /**
   * Komparaor agar SimpulReducedCostMatrix dapat dimasukkan ke dalam priority queue.
   */
  private class SimpulReducedCostMatrixComparator implements Comparator<SimpulReducedCostMatrix> {
    @Override
    public int compare(SimpulReducedCostMatrix simpul1, SimpulReducedCostMatrix simpul2) {
      if (simpul1.cost < simpul2.cost) {
        return -1;
      } else if (simpul1.cost > simpul2.cost) {
        return 1;
      } else return 0;
    }
  }

  /**
   * Komparaor agar SimpulBobotTurLengkap dapat dimasukkan ke dalam priority queue.
   */
  private class SimpulBobotTurLengkapComparator implements Comparator<SimpulBobotTurLengkap> {
    @Override
    public int compare(SimpulBobotTurLengkap simpul1, SimpulBobotTurLengkap simpul2) {
      if (simpul1.cost < simpul2.cost) {
        return -1;
      } else if (simpul1.cost > simpul2.cost) {
        return 1;
      } else return 0;
    }
  }
}
