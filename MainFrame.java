//MainFrame.java

import com.mxgraph.layout.mxCircleLayout;
import com.mxgraph.layout.mxParallelEdgeLayout;
import com.mxgraph.swing.mxGraphComponent;
import com.mxgraph.util.mxConstants;
import org.jgrapht.ext.JGraphXAdapter;
import org.jgrapht.graph.AbstractBaseGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.graph.SimpleWeightedGraph;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.NoSuchElementException;

/**
 * Frame user interface untuk program utama.
 * @author Arno Alexander
 */
public class MainFrame extends JFrame implements Runnable{

  /*Lokasi file teks yang berisi input*/
  private static final String INPUT_PATH = "input.txt";
  /*Konstanta kode algoritma pilihan*/
  private static final int ALGORITHM_NOT_SET = 0;
  private static final int ALGORITHM_REDUCED_COST_MATRIX = 1;
  private static final int ALGORITHM_BOBOT_TUR_LENGKAP = 2;
  /*Konstanta string untuk tombol*/
  private static final String BUTTON_REDUCED_COST_MATRIX
      = "Reduced Cost Matrix (Graf Berarah)";
  private static final String BUTTON_BOBOT_TUR_LENGKAP
      = "Bobot Tur Lengkap (Graf Tak Berarah)";
  private static final String BUTTON_SOLVE = "SOLVE";

  /*Algoritma yang digunakan*/
  private int usedAlgorithm;
  /*Graf permasalahan*/
  private AbstractBaseGraph<String,CustomWeightedEdge> graph;
  /*Matriks yang merepresentasikan graf permasalahan*/
  private WeightMatrix inputMatrix;
  /*Adapter dari graf permasalahan*/
  private JGraphXAdapter<String, CustomWeightedEdge> graphAdapter;


  /*Panel untuk memilih algoritma*/
  private JPanel menuButtonPanel;
  /*Panel untuk menampilkan graf*/
  private JPanel graphPanel;
  /*Panel untuk menampilkan keterangan*/
  private JPanel informationPanel;


  /**
   * Konstruktor.
   * @param mainDisplayTitle judul frame
   */
  public MainFrame(String mainDisplayTitle) {
    super(mainDisplayTitle);
    setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

    usedAlgorithm = ALGORITHM_NOT_SET;
    graph = null;
    inputMatrix = null;

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.Y_AXIS));
    menuButtonPanel = new JPanel();
    graphPanel = new JPanel();
    informationPanel = new JPanel();

    setContentPane(mainPanel);
    mainPanel.add(menuButtonPanel);
    mainPanel.add(graphPanel);
    mainPanel.add(informationPanel);
  }

  @Override
  public void run() {
    initialize();
  }

  /**
   * Menampilkan pilihan menu algoritma.
   */
  private void initialize() {
    menuButtonPanel.setLayout(new BoxLayout(menuButtonPanel,BoxLayout.Y_AXIS));
    informationPanel.setLayout(new BoxLayout(informationPanel,BoxLayout.Y_AXIS));

    JLabel buttonLabel = new JLabel("Pilih algoritma sesuai jenis graf");
    buttonLabel.setAlignmentX(CENTER_ALIGNMENT);
    JButton buttonRCM = new JButton(BUTTON_REDUCED_COST_MATRIX);
    buttonRCM.addActionListener(new ButtonClickListener());
    buttonRCM.setAlignmentX(CENTER_ALIGNMENT);
    JButton buttonBTL = new JButton(BUTTON_BOBOT_TUR_LENGKAP);
    buttonBTL.addActionListener(new ButtonClickListener());
    buttonBTL.setAlignmentX(CENTER_ALIGNMENT);

    menuButtonPanel.add(buttonLabel);
    menuButtonPanel.add(buttonRCM);
    menuButtonPanel.add(buttonBTL);

    pack();
    setResizable(false);
    setVisible(true);
  }

  /**
   * Membaca graf dari file eksternal.
   */
  private void retrieveGraph() {
    menuButtonPanel.removeAll();
    try {
      inputMatrix = new WeightMatrix(INPUT_PATH);
      displayRetrievedGraph();
    } catch (FileNotFoundException exception) {
      informationPanel.add(new JLabel
          ("Error : File input tidak ditemukan"));
      pack();
    } catch (NoSuchElementException exception) {
      informationPanel.add(new JLabel
          ("Error : Isi file input salah"));
      pack();
    }
  }

  /**
   * Menampilkan graf yang sudah dibaca dari file eksternal.
   */
  private void displayRetrievedGraph() {
    if (usedAlgorithm == ALGORITHM_REDUCED_COST_MATRIX) {
      graph = new SimpleDirectedWeightedGraph<>(CustomWeightedEdge.class);
    } else {
      graph = new SimpleWeightedGraph<>(CustomWeightedEdge.class);
    }

    for (int i = 0; i < inputMatrix.getNumberOfVertex(); ++i) {
      graph.addVertex(Integer.toString(i));
    }

    int numberOfSmallToBigEdge = 0;
    CustomWeightedEdge[] smallToBigEdge = new CustomWeightedEdge
        [inputMatrix.getNumberOfVertex() * (inputMatrix.getNumberOfVertex()+1) / 2];

    if (usedAlgorithm == ALGORITHM_REDUCED_COST_MATRIX) {
      for (int i = 0; i < inputMatrix.getNumberOfVertex(); ++i) {
        for (int j = 0; j < inputMatrix.getNumberOfVertex(); ++j) {
          double edgeWeight = inputMatrix.getWeight(i,j);
          if (edgeWeight != WeightMatrix.INVALID_WEIGHT) {
            CustomWeightedEdge edge = new CustomWeightedEdge();
            graph.addEdge(Integer.toString(i),Integer.toString(j), edge);
            graph.setEdgeWeight(edge,edgeWeight);
            if (i < j) {
              smallToBigEdge[numberOfSmallToBigEdge] = edge;
              numberOfSmallToBigEdge++;
            }
          }
        }
      }
    } else {
      for (int i = 1; i < inputMatrix.getNumberOfVertex(); ++i) {
        for (int j = 0; j < i; ++j) {
          double edgeWeight = inputMatrix.getWeight(i,j);
          if (edgeWeight != WeightMatrix.INVALID_WEIGHT) {
            CustomWeightedEdge edge = new CustomWeightedEdge();
            graph.addEdge(Integer.toString(i),Integer.toString(j), edge);
            graph.setEdgeWeight(edge, edgeWeight);
          }
        }
      }
    }

    graphAdapter = new JGraphXAdapter<>(graph);
    graphAdapter.getStylesheet().getDefaultEdgeStyle()
        .put(mxConstants.STYLE_FONTCOLOR,"#334455");
    graphAdapter.getStylesheet().getDefaultEdgeStyle()
        .put(mxConstants.STYLE_STROKECOLOR,"#333333");
    graphAdapter.getStylesheet().getDefaultVertexStyle()
        .put(mxConstants.STYLE_FONTCOLOR,"#334455");
    graphAdapter.getStylesheet().getDefaultVertexStyle()
        .put(mxConstants.STYLE_ROUNDED,"1");
    graphAdapter.getStylesheet().getDefaultVertexStyle()
        .put(mxConstants.STYLE_FONTSTYLE,mxConstants.FONT_BOLD);
    if (usedAlgorithm == ALGORITHM_REDUCED_COST_MATRIX) {
      graphAdapter.getStylesheet().getDefaultEdgeStyle()
          .put(mxConstants.STYLE_ENDARROW,mxConstants.ARROW_CLASSIC);
    } else {
      graphAdapter.getStylesheet().getDefaultEdgeStyle()
          .put(mxConstants.STYLE_ENDARROW,mxConstants.NONE);
    }

    HashMap<CustomWeightedEdge,com.mxgraph.model.mxICell> edgeToCellMap
        = graphAdapter.getEdgeToCellMap();
    com.mxgraph.model.mxICell[] smallToBigEdgeCell
        = new com.mxgraph.model.mxICell[numberOfSmallToBigEdge];
    for (int i = 0; i < numberOfSmallToBigEdge; ++i) {
      smallToBigEdgeCell[i] = edgeToCellMap.get(smallToBigEdge[i]);
    }
    graphAdapter.setCellStyles("strokeColor","#666666",smallToBigEdgeCell);
    graphAdapter.setCellStyles("fontColor","#667788",smallToBigEdgeCell);

    mxCircleLayout circleLayout = new mxCircleLayout(graphAdapter);
    circleLayout.setRadius(275);
    circleLayout.execute(graphAdapter.getDefaultParent());
    mxParallelEdgeLayout parallelEdgeLayout
        = new mxParallelEdgeLayout(graphAdapter,35);
    parallelEdgeLayout.execute(graphAdapter.getDefaultParent());

    mxGraphComponent graphComponent = new mxGraphComponent(graphAdapter);
    JButton solveButton = new JButton(BUTTON_SOLVE);
    solveButton.addActionListener(new ButtonClickListener());
    solveButton.setAlignmentX(CENTER_ALIGNMENT);

    graphPanel.removeAll();
    graphPanel.add(graphComponent);
    informationPanel.add(solveButton);
    pack();
  }

  /**
   * Menyelesaikan TSP.
   */
  private void solveGraph() {
    long time0, time1;
    int[] solution;
    time0 = System.currentTimeMillis();
    if (usedAlgorithm == ALGORITHM_REDUCED_COST_MATRIX) {
      solution = inputMatrix.solveReducedCostMatrix();
    } else {
      solution = inputMatrix.solveBobotTurLengkap();
    }
    time1 = System.currentTimeMillis();

    HashMap<CustomWeightedEdge,com.mxgraph.model.mxICell> edgeToCellMap
        = graphAdapter.getEdgeToCellMap();
    com.mxgraph.model.mxICell[] solutionEdgeCell = new com.mxgraph.model.mxICell[solution.length];
    for (int i = 0; i < solution.length; i++) {
      solutionEdgeCell[i] = edgeToCellMap.get(graph.getEdge(Integer.toString(solution[i])
          ,Integer.toString(solution[(i+1)%solution.length])));
    }
    graphAdapter.setCellStyles("strokeColor","#CA1155",solutionEdgeCell);
    graphAdapter.setCellStyles("fontColor","#CA2266",solutionEdgeCell);

    JLabel tourLabel = new JLabel("Lintasan terpendek (kemudian kembali ke titik semula) = "
        +Arrays.toString(solution));
    JLabel weightLabel = new JLabel("Bobot tur terpendek = "+inputMatrix.shortestTourWeight);
    JLabel numberOfSimpulLabel = new JLabel("Banyak simpul yang dibangkitkan = "
        +inputMatrix.numberOfSimpulToSolve);
    JLabel timeLabel = new JLabel("Waktu eksekusi = "
        +(double)(time1-time0)/1000+" detik");
    tourLabel.setAlignmentX(CENTER_ALIGNMENT);
    weightLabel.setAlignmentX(CENTER_ALIGNMENT);
    numberOfSimpulLabel.setAlignmentX(CENTER_ALIGNMENT);
    timeLabel.setAlignmentX(CENTER_ALIGNMENT);

    informationPanel.removeAll();
    informationPanel.add(tourLabel);
    informationPanel.add(weightLabel);
    informationPanel.add(numberOfSimpulLabel);
    informationPanel.add(timeLabel);
    pack();
  }

  /*Kelas yang merepresentasikan sisi dengan label berupa bobot*/
  private class CustomWeightedEdge extends DefaultWeightedEdge {
    @Override
    public String toString() {
      return Double.toString(getWeight());
    }
  }

  /*Kelas untuk respon terhadap klik pada tombol*/
  private class ButtonClickListener implements ActionListener {
    @Override
    public void actionPerformed(ActionEvent e) {
      String command = e.getActionCommand();
      switch(command) {
        case BUTTON_REDUCED_COST_MATRIX :
          usedAlgorithm = ALGORITHM_REDUCED_COST_MATRIX;
          retrieveGraph();
          break;
        case BUTTON_BOBOT_TUR_LENGKAP :
          usedAlgorithm = ALGORITHM_BOBOT_TUR_LENGKAP;
          retrieveGraph();
          break;
        case BUTTON_SOLVE:
          solveGraph();
          break;
        default:
          break;
      }
    }
  }
}
