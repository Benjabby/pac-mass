package pacmass.render;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JRootPane;

import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Vec2;

import pacmass.DebugFlags;
import pacmass.Utils;
import pacmass.engine.Game;
import pacmass.entity.Entity;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
/** 
 * @author Ben Tilbury
 */
public class GameWindow extends JComponent
{
	private static final long serialVersionUID = -91686678398222642L;

	@Deprecated
	public static final boolean FULLSCREEN = false;
	
	@Deprecated
	public static final float SKIN_FUDGE_FACTOR = 1.05f;
	
	private static final Path2D.Float GHOST_PATH;
	static
	{
		GHOST_PATH = new Path2D.Float();
		GHOST_PATH.moveTo(-1, 0);
		GHOST_PATH.lineTo(-1, -1.35);
		GHOST_PATH.lineTo(-0.5, -0.75);
		GHOST_PATH.lineTo(0, -1.35);
		GHOST_PATH.lineTo(0.5, -0.75);
		GHOST_PATH.lineTo(1, -1.35);
		GHOST_PATH.lineTo(1, 0);
		GHOST_PATH.lineTo(-1, 0);
		GHOST_PATH.closePath();
	}
	
	private final Dimension squareSize;
	
	private Dimension offsetOrigin;
	
	private final JFrame gameFrame;
	private final Game game;
	private InputManager input;
	
	private boolean fixHeight;
	private double worldScaleFactor;
	
	private AffineTransform savedTransform;
	private Stroke savedStroke;
	
//	private Graphics2D g;
	
	public GameWindow(Game game)
	{
		this.game = game;
		
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		int smaller = Math.min(bounds.height - 32, bounds.width);
		this.fixHeight = (smaller==(bounds.height - 32));
		this.squareSize = new Dimension(smaller,smaller);
		
		this.gameFrame = new JFrame("Pac-Mass");		
		gameFrame.addComponentListener(new ComponentAdapter() {
		    public void componentResized(ComponentEvent componentEvent) {
		        updateDimensions(getWidth(), getHeight());
		    }
		});
		gameFrame.getContentPane().add(this,BorderLayout.CENTER);
		gameFrame.pack();
		gameFrame.setLocationRelativeTo(null);
		gameFrame.setVisible(true);
		gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		gameFrame.repaint();
		
		this.input = new InputManager();
		gameFrame.addKeyListener(input);
		
	}
	
	@Override
	public void paintComponent(Graphics gSimple)
	{
		Game game;
		List<Entity> drawables;
		synchronized(this)
		{
			game = this.game;
			drawables = Collections.synchronizedList(game.getDrawOrder());
			game.setDrawing(true);
		}
		
//		synchronized(game)
//		{
//			
//		}

		Graphics2D g = (Graphics2D) gSimple;
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		if(DebugFlags.ROTATE_GRAV_METHOD)
		{
			g.translate(getWidth()/2, getHeight()/2);
			g.rotate(Maze.psuedoAngle);
			g.translate(-getWidth()/2, -getHeight()/2);
		}
		
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		for(Entity e : drawables) e.draw(g, this);
		
		game.test(g, this);
		
//		g.setColor(Color.WHITE);
//		int a = Math.min(getScreenHeight(), getScreenWidth());
//		g.fillRect((getScreenWidth()-a)/2, (getScreenHeight()-a)/2, a, a);
//		
//		int s = worldScaletoScreenScale(10);
//		
//		g.setColor(Color.RED);
//		g.fillRect(worldXtoScreenX(-5)-s/2,worldYtoScreenY(-5)-s/2,s,s);
//
//		g.setColor(Color.BLUE);
//		g.fillRect(worldXtoScreenX(-5)-s/2,worldYtoScreenY(5)-s/2,s,s);
//		
//		g.setColor(Color.GREEN);
//		g.fillRect(worldXtoScreenX(5)-s/2,worldYtoScreenY(-5)-s/2,s,s);
		synchronized(this){game.setDrawing(false);}
	}
	
	private void store(Graphics2D g)
	{
		savedTransform = g.getTransform();
		savedStroke = g.getStroke();
	}
	
	private void restore(Graphics2D g)
	{
		g.setTransform(savedTransform);
		g.setStroke(savedStroke);
	}

	public void drawPac(Graphics2D g, Color col, Vec2 worldPos, float angle, float radius) 
	{
		drawPacAnimated(g, col, worldPos, angle, radius, 1);
	}
	
	public void drawPacAnimated(Graphics2D g, Color col, Vec2 worldPos, float angle, float radius, float frame) 
	{
		int extent = (int)(frame*90)/2;
		g.setColor(col);
		int r = worldScaletoScreenScale(radius);
		g.fillArc(worldXtoScreenX(worldPos.x)-r, worldYtoScreenY(worldPos.y)-r, r*2, r*2, (int)Math.toDegrees(angle)+extent, (int)360-extent*2);
	}
	
	public void drawGhost(Graphics2D g, Color col1, Color col2, Color col3, Vec2 worldPos, float radius, float angleA, float angleB, float eyeDir)
	{
		store(g);
		g.setColor(col1);
		g.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		g.scale(worldScaleFactor, -worldScaleFactor);
		g.scale(radius, radius);
		g.rotate(angleB);
		g.fill(GHOST_PATH);
		g.rotate(angleA-angleB);
		g.fillOval(-1, -1, 2, 2);
		
		g.scale(0.125, 0.125);
		g.setColor(col2);
		g.fillOval(-6, -3, 4, 6);
		g.fillOval(2, -3, 4, 6);
		g.setColor(col3);
		g.translate(1,-1.5);
		g.translate(MathUtils.cos(eyeDir-angleA),1.5*MathUtils.sin(eyeDir-angleA));
		g.fillOval(-6, 0, 2, 3);
		g.fillOval(2, 0, 2, 3);
		restore(g);
	}
	
	
	
	public void drawArea(Graphics2D g, Color col, Vec2 worldPos, float angle, Area area)
	{
		g.setColor(col);
		AffineTransform af = new AffineTransform();
		af.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		af.scale(worldScaleFactor, -worldScaleFactor);
		af.rotate(angle);
//		Area p = area.createTransformedArea(af);
		Path2D.Float p = new Path2D.Float(area, af);
		g.fill(p);
	}
	
	/**
	 * If using a combined complicated path, use drawArea, otherwise this will be slower than even drawPathBatch.
	 * If just doing a single simple path this should be faster. Maybe. who knows.
	 */
	public void drawPath(Graphics2D g, Color col, Vec2 worldPos, float angle, Path2D.Float path)
	{
		g.setColor(col);
		AffineTransform af = new AffineTransform();
		af.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		af.scale(worldScaleFactor, -worldScaleFactor);
		af.rotate(angle); 
		Path2D.Float p = new Path2D.Float (path, af);
		g.fill(p);
	}
	
	/**
	 * @deprecated Using area is faster
	 */
	@Deprecated
	public void drawPathBatched(Graphics2D g, Color col, Vec2 worldPos, float angle, List<Path2D.Float> paths)
	{
		g.setColor(col);
		AffineTransform af = new AffineTransform();
		af.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		af.scale(worldScaleFactor, -worldScaleFactor);
		af.rotate(angle); 
		for(Path2D.Float path : paths) g.fill(new Path2D.Float (path, af));
	}
	
	public void debugDrawPathBatched(Graphics2D g, Vec2 worldPos, float angle, Map<Path2D.Float,Color> paths)
	{
		AffineTransform af = new AffineTransform();
		af.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		af.scale(worldScaleFactor, -worldScaleFactor);
		af.rotate(angle); 
		for(Map.Entry<Path2D.Float, Color> path : paths.entrySet())
		{
			g.setColor(path.getValue());
			g.fill(new Path2D.Float (path.getKey(), af));
		}
	}
	
	public void strokePath(Graphics2D g, Color colour, Vec2 worldPos, float angle, Path2D.Float path, float thickness)
	{
		g.setColor(colour);
		AffineTransform af = new AffineTransform();
		af.translate(worldXtoScreenX(worldPos.x), worldYtoScreenY(worldPos.y));
		af.scale(worldScaleFactor, -worldScaleFactor);
		af.rotate(angle); 
		Path2D.Float p = new Path2D.Float (path, af);
		if(thickness!=1)
		{
			store(g);
			BasicStroke stroke = new BasicStroke(thickness);
			g.setStroke(stroke);
			g.draw(p);
			restore(g);
		}
		else
		{
			g.draw(p);
		}
	}
	
	public void strokeWorldLine(Graphics2D g, Color colour, Vec2 worldPosA, Vec2 worldPosB, float thickness)
	{
		g.setColor(colour);
		if(thickness!=1)
		{
			Stroke reset = g.getStroke();
			BasicStroke stroke = new BasicStroke(thickness);
			g.setStroke(stroke);
			g.drawLine(worldXtoScreenX(worldPosA.x), worldYtoScreenY(worldPosA.y), worldXtoScreenX(worldPosB.x), worldYtoScreenY(worldPosB.y));
			g.setStroke(reset);
		}
		else
		{
			g.drawLine(worldXtoScreenX(worldPosA.x), worldYtoScreenY(worldPosA.y), worldXtoScreenX(worldPosB.x), worldYtoScreenY(worldPosB.y));
		}
	}
	
	public void drawRect(Graphics2D g, Color colour, float worldX, float worldY, float worldScale)
	{
		g.setColor(colour);
		g.fillRect(worldXtoScreenX(worldX), worldYtoScreenY(worldY), worldScaletoScreenScale(worldScale), worldScaletoScreenScale(worldScale));
	}
	

	public void drawCircle(Graphics2D g, Color colour, float worldX, float worldY, float radius) 
	{
		g.setColor(colour);
		g.fillOval(worldXtoScreenX(worldX-radius), worldYtoScreenY(worldY+radius), worldScaletoScreenScale(radius*2), worldScaletoScreenScale(radius*2));
	}
	
	@Override
	public Dimension getPreferredSize()
	{
		return squareSize;
	}
	
	public void updateDimensions(int width, int height)
	{
		this.fixHeight = height<=width;
		
		this.offsetOrigin = new Dimension(fixHeight?(width-height)/2:0,fixHeight?0:(height-width)/2);
		this.worldScaleFactor = 1.0/Game.WORLD_MIN_SIZE * (fixHeight ? height : width);
	}
	
	public int getScreenHeight()
	{
		return this.getHeight();
	}
	
	public int getScreenWidth()
	{
		return this.getWidth();
	}
	
	public int worldXtoScreenX(float worldX)
	{
		return worldScaletoScreenScale(worldX+Game.WORLD_HALF_MIN_SIZE) + offsetOrigin.width;
	}
	
	public int worldYtoScreenY(float worldY)
	{
		return getScreenHeight() - (worldScaletoScreenScale(worldY+Game.WORLD_HALF_MIN_SIZE) + offsetOrigin.height);
	}
	
	public float screenXtoWorldX(int screenX)
	{
		return screenScaletoWorldScale(screenX - offsetOrigin.width) - Game.WORLD_HALF_MIN_SIZE;
	}
	
	public float screenYtoWorldY(int screenY)
	{ 
		return screenScaletoWorldScale(getScreenHeight() - (screenY + offsetOrigin.height)) - Game.WORLD_HALF_MIN_SIZE;
	}

	public int worldScaletoScreenScale(float worldScale)
	{
		return (int)Math.floor(worldScale*worldScaleFactor);
	}
	
	public float screenScaletoWorldScale(int screenScale)
	{
		return (float)(screenScale/worldScaleFactor);
	}

	public InputManager getInputManager() 
	{
		return input;
	}

}
