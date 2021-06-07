package pacmass.entity;

import java.awt.Graphics2D;
import java.util.Comparator;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;

import pacmass.DebugFlags;
import pacmass.engine.Game;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;

/** 
 * @author Ben Tilbury
 */
public abstract class Entity 
{
	public static int DEFAULT_PRIORITY = 10;
	
	// Really this should be scaled by maze's unitSize, as it's in place so that entities don't 'miss' the unitSize sized warp sensors. but whatever
//	private static float MAX_VEL_SQUARED = 1000f;
	
	static Comparator<Entity> drawOrder = new DrawOrder();
	static Comparator<Entity> updateOrder = new UpdateOrder();
	
	protected final Game game;
	private int drawPriority = DEFAULT_PRIORITY;
	private int updatePriority = DEFAULT_PRIORITY;
	
	protected Vec2 personalGravity;
	public float rotationFriction = 0f;
	public float airResistance = 1.4f;
	protected Body body;
	
	// KLUDGE for demo purposes.
	public boolean copyTest;
	
	public boolean visible = true;
//	private boolean enabled = true;
	
	public Vec2 prevRotationForce = new Vec2();
	
	public Entity(Game game)
	{
		this(game, DEFAULT_PRIORITY, DEFAULT_PRIORITY);
	}
	
	public Entity(Game game, int drawPriority, int updatePriority)
	{
		this.game = game;
		this.drawPriority = drawPriority;
		this.updatePriority = updatePriority;
	}
	
	public Vec2 getPersonalGravity() 
	{
		return personalGravity;
	}
	
	public final Game getGame() {return game;}

	
	public final void update(InputManager input)
	{
//		if(!enabled) return
		applyPersonalGravity();
		applyAirResistance();
		
		
		updateSelf(input);
	}
//	
//	public void setEnabled(boolean enabled)
//	{
//		this.enabled = enabled;
//		
//		Fixture f = body.getFixtureList()
//	}
//	
	public void receiveEvent(Event eventType) {};
		
	protected void applyAirResistance()
	{
		if(airResistance==0 || body==null || !body.isAwake()) return;
		body.applyForceToCenter(body.getLinearVelocity().mul(-airResistance*body.getMass()));
	}
	
	protected void applyPersonalGravity()
	{
		if(personalGravity==null || body==null || !body.isAwake() || body.getLinearVelocity().lengthSquared()>=Game.MAX_VEL_SQUARED) return;
		body.applyForceToCenter(personalGravity.mul(body.getMass()));
	}
	
	// Maze reference and having to be called by a maze is KLUDGE-y 
	/** Version 1 doesn't work properly because velocity is conserved.
	 *  Version 2 is incompatible with air resistance. */
	public void applyRotationFriction(Maze maze)
	{
		if(rotationFriction==0 || body==null || !body.isAwake() || DebugFlags.ROTATE_GRAV_METHOD) return;
		
		assert(airResistance==0) : "Air resistance cannot be used with rotation friction";
		
//		Vec2 force = maze.body.getLinearVelocityFromWorldPoint(this.body.getPosition());
		Vec2 force = Vec2.cross(maze.getAngularVel(), this.body.getPosition());
		
		float idt = 1f/game.getElapsedSeconds();
		force.mulLocal(body.getMass());
		this.body.applyForceToCenter(force.sub(prevRotationForce).mul(idt*rotationFriction));
		this.prevRotationForce.set(force);
		
	}
	
	protected abstract void updateSelf(InputManager input);
	
	public final void draw(Graphics2D g, GameWindow window)
	{
		if(visible) drawSelf(g,window);
	}
	
	protected abstract void drawSelf(Graphics2D g, GameWindow window);
	
	public Body getBody()
	{
		return body;
	}
	
	protected final void setDrawPriority(int priority, boolean refreshGame)
	{
		this.drawPriority = priority;
		if(refreshGame) game.refreshDrawPriority();
	}
	
	protected final void setUpdatePriority(int priority, boolean refreshGame)
	{
		this.updatePriority = priority;
		if(refreshGame) game.refreshUpdatePriority();
	}
	
	public void setPersonalGravity(Vec2 gravity) 
	{
		this.personalGravity = gravity;
	}
	
	public final int getDrawPriority() {return drawPriority;}
	public final int getUpdatePriority() {return updatePriority;}
	
	protected abstract Entity clone(float newX, float newY);
	
	public Entity createClone()
	{
		Entity clone = this.clone(body.getPosition().x,body.getPosition().y);
		clone.airResistance = this.airResistance;
		clone.personalGravity = this.personalGravity;
		clone.rotationFriction = this.rotationFriction;
		clone.drawPriority = this.drawPriority+1;
		clone.updatePriority = this.updatePriority+1;
		return clone;
	}
	
	public Entity createClone(float newX, float newY)
	{
		Entity clone = this.clone(newX, newY);
		clone.airResistance = this.airResistance;
		clone.personalGravity = this.personalGravity;
		clone.rotationFriction = this.rotationFriction;
		clone.drawPriority = this.drawPriority+1;
		clone.updatePriority = this.updatePriority+1;
		return clone;
	}
	
	public void destroy() {}
	
	public static class DrawOrder implements Comparator<Entity>
	{
		@Override
		public int compare(Entity a, Entity b)
		{
			return a.drawPriority - b.drawPriority;
		}	
	}
	
	
	public static class UpdateOrder implements Comparator<Entity>
	{
		@Override
		public int compare(Entity a, Entity b)
		{
			return a.updatePriority - b.updatePriority;
		}	
	}
	
	public static enum Event 
	{
		LIFE_LOST,
		GAME_OVER,
		POWER_PELLET_PICKUP,
		NEXT_LEVEL,
	}
	
//	public static enum Event 
//	{
//		LIFE_LOST(true),
//		GAME_OVER(true),
//		POWER_PELLET_PICKUP,
//		;
//		
//		public final boolean invalidateOthers;
//		Event() {this.invalidateOthers = false;}
//	    Event(boolean invalidateOthers){this.invalidateOthers = invalidateOthers;}
//		// something something something
//	}

}
