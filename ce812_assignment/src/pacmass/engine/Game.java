package pacmass.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.TreeSet;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ModifiedParticleSystem;

import pacmass.DebugFlags;
import pacmass.entity.Entity;
import pacmass.entity.EntitySet;
import pacmass.entity.Player;
import pacmass.entity.maze.MapInfo;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;

/** 
 * The main manager class for the game
 * @author Ben Tilbury
 */
public class Game 
{
	public static final int STATE_RESET = -1;
	public static final int STATE_PLAY = 0;
	public static final int STATE_NEXT = 1;
	
//	// Timing Constants
	private static final int TARGET_FPS = 60;
	
	// Physics Constants
	public static final int PHYS_ITERATIONS = 10;
	public static final float WORLD_MIN_SIZE = 10;
	public static final float WORLD_HALF_MIN_SIZE = WORLD_MIN_SIZE/2;
	public static final Vec2 STANDARD_GRAVITY = new Vec2(0,-9.8f);
	
	public static float MAX_VEL_SQUARED = 1000f;
	
	private final EntitySet allEntities;
	private final World world;
	
	private Maze currentLevel;
	
	private boolean inUpdateLoop;
	private boolean inDrawLoop;
	
	// KLUDGE
	private int state = STATE_PLAY;
	// KLUDGE
	int level = 0;
	
	// KLUDGE
//	public float testScale = 1f;
	
	public Game()
	{
		world = new World(new Vec2(0, 0),true);
		world.setContinuousPhysics(true);

		allEntities = new EntitySet(world);
		
//		currentLevel = new Maze(this, MapInfo.EMPTY);
		// KLUDGE
		currentLevel = new Maze(this, DebugFlags.WARP_DEMO_MAP?MapInfo.WARP_TEST:MapInfo.LEVELS[level]);
		
		allEntities.add(currentLevel);
	}
	
	/**
	 * Runs one update
	 * @param input
	 */
	private void update(InputManager input)
	{
		if(state==STATE_RESET||state==STATE_NEXT)
		{
			if(state==STATE_RESET) 
			{
				level = 0;
				System.out.println("GAME OVER");
			}
			else if(++level>=MapInfo.LEVELS.length) System.exit(0);
			allEntities.burn();
			currentLevel = new Maze(this,MapInfo.LEVELS[level]);
			allEntities.add(currentLevel);
			state = STATE_PLAY;
		}
		
		tryPending();
		allEntities.broadcastEvents();
		
		inUpdateLoop = true;
		
		world.step(1f/frameRate, PHYS_ITERATIONS, PHYS_ITERATIONS);
		for(Entity e : allEntities.getUpdateOrder()) e.update(input);

		input.resetTriggers();
		inUpdateLoop = false;
	}
	
	public void addEntity(Entity entity)
	{
		if(!(inUpdateLoop || inDrawLoop)) allEntities.add(entity);
		else allEntities.pendAdd(entity);
	}
	
	public void addAllEntities(Set<Entity> entities)
	{
		if(!(inUpdateLoop || inDrawLoop)) allEntities.addAll(entities);
		else allEntities.pendAddAll(entities);
	}
	
	public void removeEntity(Entity entity)
	{
		if(!(inUpdateLoop || inDrawLoop)) allEntities.remove(entity);
		else allEntities.pendRemove(entity);
	}
	
	public void removeAllEntities(Set<Entity> entities)
	{
		if(!(inUpdateLoop || inDrawLoop)) allEntities.removeAll(entities);
		else allEntities.pendRemoveAll(entities);
	}
	
	public List<Entity> getDrawOrder()
	{
		return allEntities.getDrawOrder();
	}
	
	public List<Entity> getUpdateOrder()
	{
		return allEntities.getUpdateOrder();
	}
	
	public void refreshDrawPriority()
	{
		allEntities.refreshDrawPriority();
	}
	
	public void refreshUpdatePriority()
	{
		allEntities.refreshUpdatePriority();
	}
	
	public void queueEvent(Entity.Event eventType)
	{
		allEntities.queueEvent(eventType);
	}
	
	// KLUDGE
	public void broadcastImmediateEvent(Entity.Event eventType)
	{
		broadcastImmediateEvent(eventType, false);
	}
	// KLUDGE
	public void broadcastImmediateEvent(Entity.Event eventType, boolean invalidateAll)
	{
		if(eventType==Entity.Event.GAME_OVER)
		{
			state=STATE_RESET;
		}
		else if(eventType==Entity.Event.NEXT_LEVEL)
		{
			state=STATE_NEXT;
		}
		else
		{
			if(invalidateAll) allEntities.pauseEvents();
			for(Entity e : allEntities.getUpdateOrder()) e.receiveEvent(eventType);
		}
	}
	
	private long startTime;
	private float frameRate = TARGET_FPS;
	private float timeInSecs  = 1f/TARGET_FPS;

	public float getElapsedSeconds()
	{
		return timeInSecs;
	}
	
	public void setDrawing(boolean drawing)
	{
		inDrawLoop = drawing;
	}
	
	public void startThread(GameWindow window, InputManager input) throws InterruptedException
	{	
		/*
		 * Game loop modified from JBox2D testbed by Daniel Murphy
		 */
		long beforeTime, afterTime,timeDiff, updateTime, sleepTime, timeSpent;
	    beforeTime = startTime = updateTime = System.nanoTime();
	    sleepTime = 0;

		final Game game = this;
		while(true)
		{

			
			timeSpent = beforeTime - updateTime;
			if (timeSpent > 0)
			{
			    timeInSecs = timeSpent * 1.0f / 1000000000.0f;
			    updateTime = System.nanoTime();
			    frameRate = (frameRate * 0.9f) + (1.0f / timeInSecs) * 0.1f;
			} 
			else
			{
				updateTime = System.nanoTime();
			}
			
			
			game.update(input);
			
			
//			inDrawLoop = true;
			window.repaint();
			Toolkit.getDefaultToolkit().sync();
//			inDrawLoop = false;
			
			afterTime = System.nanoTime();
			
			timeDiff = afterTime - beforeTime;
			sleepTime = (1000000000 / TARGET_FPS - timeDiff) / 1000000;
			if (sleepTime > 0) 
			{
				try { Thread.sleep(sleepTime); } 
				catch (InterruptedException ex) {}
			}
			beforeTime = System.nanoTime();
			
		}
	}
	
	// could be done way better.
	// edit: doesn't even work properly anyway
	public void tryPending()
	{
		if(!(inDrawLoop || inUpdateLoop)) allEntities.executePending();
	}
	
	public boolean isUpdating()
	{
		return inUpdateLoop;
	}
	
	public boolean isDrawing()
	{
		return inDrawLoop;
	}

	public World getWorld()
	{
		return world;
	}

	public static void main(String[] args) throws Exception
	{
		final Game game = new Game();
		final GameWindow window = new GameWindow(game);
		
		game.startThread(window, window.getInputManager());
	}

	public void test(Graphics2D g, GameWindow gameWindow) {
		currentLevel.test(g, gameWindow);
	}

	// KLUDGE
	public Player getPlayer() 
	{
		return currentLevel.getPlayer();
	}
	
}
