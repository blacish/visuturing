package VisuTuring.diagram;

import VisuTuring.core.State;
import VisuTuring.core.Transition;
import VisuTuring.core.TuringMachine;
import VisuTuring.gui.panels.DiagramPrinter;
import VisuTuring.mousetools.MouseTool;
import VisuTuring.mousetools.SelectTool;
import VisuTuring.persistence.Persistence;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.QuadCurve2D;
import java.awt.geom.Ellipse2D.Double;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Vector;
import javax.swing.JPanel;

public class DiagramEditor extends JPanel {
  public static double BORDER = 20.0D;
  public static final double STATE_RADIUS = 20.0D;
  public static final double LOOP_LENGTH = 60.0D;
  public static final Color STATE_COLOUR = new Color(255, 255, 255);
  public static final Color HALTING_STATE_COLOUR = new Color(255, 255, 255);
  public static final Color SELECTED_STATE_COLOUR = new Color(255, 255, 204);
  public static final Color SELECTED_TRANSITION_COLOUR;
  public static final int TRANSITION_MARGIN = 10;
  public static final int ARROW_LENGTH = 7;
  public boolean gridOn;
  private TuringMachine currentMachine;
  private State selectedState;
  private Transition selectedTransition;
  private DiagramEditor.Mousey m;
  private MouseTool tool;
  protected AffineTransform transformation;
  private double[] zoomList;
  private int zoomIndex;
  private Vector transformWatchers;
  private boolean showDescription;

  public DiagramEditor(TuringMachine var1) {
    this.currentMachine = var1;
    this.gridOn = true;
    this.transformWatchers = new Vector();
    this.selectedState = null;
    this.tool = new SelectTool(this);
    this.transformation = new AffineTransform();
    this.m = new DiagramEditor.Mousey();
    this.addMouseListener(this.m);
    this.addMouseMotionListener(this.m);
    this.zoomList = new double[9];
    this.zoomList[0] = 0.25D;
    this.zoomList[1] = 0.5D;
    this.zoomList[2] = 1.0D;
    this.zoomList[3] = 1.25D;
    this.zoomList[4] = 1.5D;
    this.zoomList[5] = 1.75D;
    this.zoomList[6] = 2.0D;
    this.zoomList[7] = 3.0D;
    this.zoomList[8] = 4.0D;
    this.zoomIndex = 2;
    this.setBackground(Color.white);
    this.showDescription = false;
  }

  public void revertToSelect() {
    this.setTool(new SelectTool(this));
  }

  void setUp(Graphics2D var1) {
    FontMetrics var2 = var1.getFontMetrics();
    var1.setFont(new Font("Helvetica", 0, 14));
  }

  public void print() {
    DiagramPrinter var1 = new DiagramPrinter(this);
    var1.setVisible(true);
    if (!var1.wasCancelled()) {
      var1.printIt();
    }

  }

  public void drawState(State var1, Graphics2D var2) {
    double var3 = var1.getLocation().getX();
    double var5 = var1.getLocation().getY();
    Double var7 = new Double(var3 - 20.0D, var5 - 20.0D, 40.0D, 40.0D);
    if (var1.getName().equals("h")) {
      var2.setColor(HALTING_STATE_COLOUR);
    } else if (this.selectedState == var1) {
      var2.setColor(SELECTED_STATE_COLOUR);
    } else {
      var2.setColor(STATE_COLOUR);
    }

    var2.fill(var7);
    var2.setColor(Color.BLACK);
    var2.draw(var7);
    if (var1.getName().equals("h")) {
      var2.draw(new Double(var3 - 20.0D + 5.0D, var5 - 20.0D + 5.0D, 30.0D, 30.0D));
    }

    var2.setColor(Color.BLACK);
    int var8 = var2.getFontMetrics().getAscent();
    double var9 = var2.getFontMetrics().getStringBounds(var1.getName(), var2).getWidth();
    if (var9 < 30.0D) {
      var2.drawString(var1.getName(), (float)(var3 - var9 / 2.0D), (float)(var5 + (double)(var8 / 2)));
    } else {
      var2.drawString(var1.getName(), (float)(var3 - var9 / 2.0D), (float)(var5 + 40.0D));
    }

    if (var1.getName().equals("s")) {
      var2.draw(new java.awt.geom.Line2D.Double(var3 - 20.0D - 12.0D, var5 - 12.0D, var3 - 20.0D, var5));
      var2.draw(new java.awt.geom.Line2D.Double(var3 - 20.0D - 12.0D, var5 + 12.0D, var3 - 20.0D, var5));
    }

  }

  public void drawTransition(Transition var1, Graphics2D var2) {
    Point2D var3 = var1.getP1();
    Point2D var4 = var1.getP2();
    Point2D var5 = var1.getControlPoint();
    if (var1 == this.selectedTransition) {
      var2.setColor(SELECTED_TRANSITION_COLOUR);
      var2.setStroke(new BasicStroke(2.0F));
    } else {
      var2.setColor(Color.BLACK);
      var2.setStroke(new BasicStroke(1.0F));
    }

    Point2D var6;
    Point2D var7;
    String var9;
    Point var12;
    int var13;
    int var14;
    if (!var1.getCurrentState().equals(var1.getNextState())) {
      QuadCurve2D var8 = (QuadCurve2D)this.getGraphicEdge(var1);
      var2.draw(var8);
      var6 = var8.getCtrlPt();
      var7 = var8.getP2();
      var9 = var1.getCurrentSymbol() + " / " + var1.getTask();
      java.awt.geom.QuadCurve2D.Double var10 = new java.awt.geom.QuadCurve2D.Double();
      java.awt.geom.QuadCurve2D.Double var11 = new java.awt.geom.QuadCurve2D.Double();
      var8.subdivide(var10, var11);
      var12 = new Point((int)var10.getP2().getX(), (int)var10.getP2().getY());
      var13 = (int)var2.getFontMetrics().getStringBounds(var9, var2).getWidth();
      var14 = 10;
      if (var10.getY1() < var10.getY2()) {
        var14 = -(var14 + var2.getFontMetrics().getAscent());
      }

      var2.drawString(var9, (int)(var12.getX() - (double)(var13 / 2)), (int)var12.getY() - var14);
    } else {
      CubicCurve2D var15 = (CubicCurve2D)this.getGraphicEdge(var1);
      var2.draw(var15);
      var6 = var15.getCtrlP2();
      var7 = var15.getP2();
      var9 = var1.getCurrentSymbol() + " / " + var1.getTask();
      java.awt.geom.CubicCurve2D.Double var17 = new java.awt.geom.CubicCurve2D.Double();
      java.awt.geom.CubicCurve2D.Double var19 = new java.awt.geom.CubicCurve2D.Double();
      var15.subdivide(var17, var19);
      var12 = new Point((int)var17.getP2().getX(), (int)var17.getP2().getY());
      var13 = (int)var2.getFontMetrics().getStringBounds(var9, var2).getWidth();
      var14 = 10;
      if (var17.getY1() < var17.getY2()) {
        var14 = -(var14 + var2.getFontMetrics().getAscent());
      }

      var2.drawString(var9, (int)(var12.getX() - (double)(var13 / 2)), (int)var12.getY() - var14);
    }

    double var16 = var6.getX() - var7.getX();
    double var18 = var6.getY() - var7.getY();
    double var20 = Math.sqrt(var16 * var16 + var18 * var18);
    var16 = var16 / var20 * 7.0D;
    var18 = var18 / var20 * 7.0D;
    java.awt.geom.Line2D.Double var21 = new java.awt.geom.Line2D.Double();
    var21.setLine(var7.getX(), var7.getY(), var7.getX() - (-var16 - var18), var7.getY() - (-var18 + var16));
    var2.draw(var21);
    var21.setLine(var7.getX(), var7.getY(), var7.getX() - (-var16 + var18), var7.getY() - (-var18 - var16));
    var2.draw(var21);
  }

  public void setGrid(boolean var1) {
    this.gridOn = var1;
  }

  public void paintComponent(Graphics var1) {
    super.paintComponent(var1);
    Graphics2D var2 = (Graphics2D)var1;
    if (this.gridOn) {
      var2.setColor(new Color(230, 230, 230));
      int var3 = this.getWidth();
      int var4 = this.getHeight();

      int var5;
      for(var5 = 0; var5 < var3 / 20; ++var5) {
        var2.drawLine(var5 * 20, 0, var5 * 20, var4);
      }

      for(var5 = 0; var5 < var3 / 20; ++var5) {
        var2.drawLine(0, var5 * 20, var3, var5 * 20);
      }
    }

    var2.scale(this.transformation.getScaleX(), this.transformation.getScaleY());
    this.tool.preDraw(var2);
    this.render(var2);
    this.tool.postDraw(var2);
  }

  public void render(Graphics2D var1) {
    FontMetrics var2 = var1.getFontMetrics();
    var1.setFont(new Font("Helvetica", 0, 14));
    var1.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    var1.setColor(Color.BLACK);
    Vector var3 = this.currentMachine.getTransitions();

    for(int var4 = 0; var4 < var3.size(); ++var4) {
      Transition var5 = (Transition)var3.get(var4);
      this.drawTransition(var5, var1);
    }

    var1.setStroke(new BasicStroke(1.0F));
    Vector var7 = this.currentMachine.getStates();

    for(int var8 = 0; var8 < var7.size(); ++var8) {
      State var6 = (State)var7.get(var8);
      this.drawState(var6, var1);
    }

  }

  public void setTool(MouseTool var1) {
    this.tool = var1;
    this.setCursor(var1.getCursor());
  }

  public void setCurrentMachine(TuringMachine var1) {
    this.currentMachine = var1;
  }

  public void setSelectedState(State var1) {
    this.selectedState = var1;
  }

  public void setSelectedTransition(Transition var1) {
    this.selectedTransition = var1;
  }

  public MouseTool getTool() {
    return this.tool;
  }

  public TuringMachine getCurrentMachine() {
    return this.currentMachine;
  }

  public State getSelectedState() {
    return this.selectedState;
  }

  public Transition getSelectedTransition() {
    return this.selectedTransition;
  }

  public void setTransformation(AffineTransform var1) {
    this.transformation = var1;
  }

  public void zoomIn() {
    ++this.zoomIndex;
    if (this.zoomIndex >= this.zoomList.length) {
      this.zoomIndex = this.zoomList.length - 1;
    }

    this.transformation = new AffineTransform();
    this.transformation.scale(this.zoomList[this.zoomIndex], this.zoomList[this.zoomIndex]);
    this.repaint();
  }

  public void zoomOut() {
    --this.zoomIndex;
    if (this.zoomIndex < 0) {
      this.zoomIndex = 0;
    }

    this.transformation = new AffineTransform();
    this.transformation.scale(this.zoomList[this.zoomIndex], this.zoomList[this.zoomIndex]);
    this.repaint();
  }

  public void setZoom(double var1) {
    this.transformation = new AffineTransform();
    this.transformation.scale(var1, var1);
    this.repaint();
  }

  public Point2D toWorld(Point2D var1) {
    java.awt.geom.Point2D.Double var2 = new java.awt.geom.Point2D.Double();
    this.transformation.transform(var1, var2);
    return var2;
  }

  public Point2D toUser(Point2D var1) {
    java.awt.geom.Point2D.Double var2 = new java.awt.geom.Point2D.Double();

    try {
      this.transformation.inverseTransform(var1, var2);
    } catch (NoninvertibleTransformException var4) {
    } catch (Exception var5) {
    }

    return var2;
  }

  public BufferedImage makeBuffer(int var1, int var2) {
    BufferedImage var3 = new BufferedImage(var1, var2, 1);
    Graphics2D var4 = var3.createGraphics();
    var4.setColor(Color.WHITE);
    var4.fillRect(0, 0, var1, var2);
    this.render(var4);
    var4.dispose();
    return var3;
  }

  public boolean showDescription() {
    return this.showDescription;
  }

  public void setDescriptionShown(boolean var1) {
    this.showDescription = var1;
  }

  public Dimension getExtents() {
    Dimension var1 = new Dimension();
    double var2 = 200.0D;
    double var4 = 200.0D;
    Vector var6 = this.currentMachine.getStates();

    for(int var7 = 0; var7 < var6.size(); ++var7) {
      State var8 = (State)var6.get(var7);
      double var9 = var8.getLocation().getX();
      double var11 = var8.getLocation().getY();
      if (var2 < var9 + 20.0D + BORDER) {
        var2 = var9 + 20.0D + BORDER;
      }

      if (var4 < var11 + 20.0D + BORDER) {
        var4 = var11 + 20.0D + BORDER;
      }
    }

    Vector var14 = this.currentMachine.getTransitions();
    BasicStroke var15 = new BasicStroke(2.0F);

    for(int var16 = 0; var16 < var14.size(); ++var16) {
      Transition var10 = (Transition)var14.get(var16);
      Shape var13 = this.getGraphicEdge(var10);
      Rectangle var12 = var15.createStrokedShape(var13).getBounds();
      if (var2 < var12.getX() + var12.getWidth() + BORDER) {
        var2 = var12.getX() + var12.getWidth() + BORDER;
      }

      if (var4 < var12.getY() + var12.getHeight() + BORDER) {
        var4 = var12.getY() + var12.getHeight() + BORDER;
      }
    }

    var1.setSize(var2 + 2.0D * BORDER, var4 + 2.0D * BORDER);
    return var1;
  }

  public void exportToJPEG(File var1) {
    this.setSelectedState((State)null);
    this.setSelectedTransition((Transition)null);
    Dimension var2 = this.getExtents();
    BufferedImage var3 = this.makeBuffer((int)var2.getWidth(), (int)var2.getHeight());
    Persistence.saveJPEG(var3, var1.toString(), 1.0F);
  }

  public Shape getGraphicEdge(Transition var1) {
    Point2D var2 = var1.getP1();
    Point2D var3 = var1.getP2();
    Point2D var4 = var1.getControlPoint();
    if (var1.getCurrentState().equals(var1.getNextState())) {
      Point2D var25 = this.currentMachine.getState(var1.getCurrentState()).getLocation();
      double var6 = Math.sqrt((var4.getX() - var25.getX()) * (var4.getX() - var25.getX()) + (var4.getY() - var25.getY()) * (var4.getY() - var25.getY()));
      double var8 = Math.sqrt(3600.0D + var6 * var6);
      double var10 = Math.atan(60.0D / var6);
      double var12 = var6 * (var4.getX() - var25.getX());
      double var14 = Math.acos(var12 / (var6 * var6));
      if (var25.getY() < var4.getY()) {
        var14 = -var14;
      }

      java.awt.geom.Point2D.Double var16 = new java.awt.geom.Point2D.Double(var25.getX() + var8 * Math.cos(-var10 - var14), var25.getY() + var8 * Math.sin(-var10 - var14));
      java.awt.geom.Point2D.Double var17 = new java.awt.geom.Point2D.Double(var25.getX() + var8 * Math.cos(var10 - var14), var25.getY() + var8 * Math.sin(var10 - var14));
      double var18 = var16.getX() - var3.getX();
      double var20 = var16.getY() - var3.getY();
      double var22 = 20.0D / Math.sqrt(var18 * var18 + var20 * var20);
      var3.setLocation(var3.getX() + var18 * var22, var3.getY() + var20 * var22);
      var18 = var17.getX() - var2.getX();
      var20 = var17.getY() - var2.getY();
      var22 = 20.0D / Math.sqrt(var18 * var18 + var20 * var20);
      var2.setLocation(var2.getX() + var18 * var22, var2.getY() + var20 * var22);
      java.awt.geom.CubicCurve2D.Double var24 = new java.awt.geom.CubicCurve2D.Double();
      var24.setCurve(var2, var17, var16, var3);
      return var24;
    } else {
      double var5 = var4.getX() - var3.getX();
      double var7 = var4.getY() - var3.getY();
      double var9 = 20.0D / Math.sqrt(var5 * var5 + var7 * var7);
      var3.setLocation(var3.getX() + var5 * var9, var3.getY() + var7 * var9);
      var5 = var4.getX() - var2.getX();
      var7 = var4.getY() - var2.getY();
      var9 = 20.0D / Math.sqrt(var5 * var5 + var7 * var7);
      var2.setLocation(var2.getX() + var5 * var9, var2.getY() + var7 * var9);
      java.awt.geom.QuadCurve2D.Double var11 = new java.awt.geom.QuadCurve2D.Double();
      var11.setCurve(var2, var4, var3);
      return var11;
    }
  }

  static {
    SELECTED_TRANSITION_COLOUR = Color.BLACK;
  }

  public class Mousey extends MouseAdapter implements MouseMotionListener {
    public Mousey() {
    }

    public void mousePressed(MouseEvent var1) {
      DiagramEditor.this.tool.mousePressed(var1);
    }

    public void mouseReleased(MouseEvent var1) {
      DiagramEditor.this.tool.mouseReleased(var1);
    }

    public void mouseMoved(MouseEvent var1) {
      DiagramEditor.this.tool.mouseMoved(var1);
    }

    public void mouseDragged(MouseEvent var1) {
      DiagramEditor.this.tool.mouseDragged(var1);
    }
  }
}