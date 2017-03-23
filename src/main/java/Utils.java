import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.geometry.Bounds;
import java.nio.file.FileSystems;
import java.util.*;

public class Utils {
  public static void prl(Object printable) {
    System.out.println(printable);
  }

  public static double[] getNodeCoords(Node n, String corner) {
    double[] coords = new double[2];

    Bounds localBounds = n.localToScene(n.getBoundsInLocal());

    if(corner.equals("top-left")) {
      coords[0] = localBounds.getMinX();
      coords[1] = localBounds.getMinY();
    } else {
      coords[0] = localBounds.getMaxX();
      coords[1] = localBounds.getMaxY();
    }

    return coords;
  }

  public static double[] getNodeCoords(Node n) {
    return getNodeCoords(n, "bottom-right");
  }

  public static boolean withinRegion(Region n, double x, double y) {
    double[] nodeCoords = getNodeCoords(n, "top-left");
    if((x >= nodeCoords[0] && x <= nodeCoords[0] + n.getWidth()) &&
    (y >= nodeCoords[1] && y <= nodeCoords[1] + n.getHeight()))
    return true;
    return false;
  }

  public static boolean withinNode(Node n, double x, double y) {
    double[] nodeCoords = getNodeCoords(n, "top-left");
    if((x >= nodeCoords[0] && x <= nodeCoords[0] + n.getBoundsInParent().getWidth()) &&
    (y >= nodeCoords[1] && y <= nodeCoords[1] + n.getBoundsInParent().getHeight()))
    return true;
    return false;
  }

  public static String getUserHomePath() {
    return System.getProperty("user.home") + FileSystems.getDefault().getSeparator();
  }
}
