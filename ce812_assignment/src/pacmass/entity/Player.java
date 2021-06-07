package pacmass.entity;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.particle.ModifiedParticleSystem;

import pacmass.DebugFlags;
import pacmass.Utils;
import pacmass.engine.Game;
import pacmass.entity.Entity.Event;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;
/** 
 * @author Ben Tilbury
 */
public class Player extends Entity 
{

	private static final int STARTING_LIVES = 3;
	public static final float UNSCALED_RADIUS = 0.4f;
	
	protected final Vec2 startingPosition; 
	protected final float radius;
	private double animationFrame;
	private int currentLives;

	public Player(Game game, float x, float y, float scale) 
	{
		this(game,x,y,UNSCALED_RADIUS*scale,new Vec2(), 0, 0);
	}
	
	public Player(Game game, float x, float y, float radius, Vec2 velocity, float angle, float angularVelocity) 
	{
		super(game);
		World world = game.getWorld();
		BodyDef bodyDef = new BodyDef();
		this.startingPosition = new Vec2(x,y);
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(x, y);
		bodyDef.angle = angle;
		bodyDef.linearVelocity.set(velocity);
		bodyDef.angularVelocity = angularVelocity;
		this.body = world.createBody(bodyDef);
		this.radius = radius;
		CircleShape circleShape = new CircleShape();
		circleShape.m_radius = radius;
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = circleShape;
		fixtureDef.density = (float) (1/(Math.PI*radius*radius));
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.4f;
		Filter filter = new Filter();
		fixtureDef.filter = filter;
		body.createFixture(fixtureDef);
//		this.rotateFriction=0.1f;
		this.personalGravity = new Vec2();
		this.personalGravity.set(Game.STANDARD_GRAVITY);
		this.currentLives = STARTING_LIVES;

		
	}

	@Override
	public void receiveEvent(Event eventType) 
	{
		switch(eventType)
		{
		case LIFE_LOST:
			--currentLives;
			body.m_angularVelocity = 0;
			body.m_linearVelocity.setZero();
			body.setTransform(startingPosition, 0);
			body.synchronizeTransform();
			animationFrame = 0;
			this.personalGravity.set(Game.STANDARD_GRAVITY);
//			Utils.removeAllContacts(game.getWorld(), body.getContactList());
			break;
		}
	}

	public void drawSelf(Graphics2D g, GameWindow window)
	{
		// KLUDGE but only for demo purposes
		window.drawPacAnimated(g, this.copyTest?Color.RED:Color.YELLOW, body.getPosition(), body.getAngle(), radius, (float) (1+(Math.sin(animationFrame)))/2);
		// VERY VERY KLUDGE but only for demo purposes
		if(this.copyTest) window.drawPacAnimated(g, new Color(255,0,0,140), body.getPosition().sub(new Vec2(0,7.0710683f)), body.getAngle(), radius, (float) (1+(Math.sin(animationFrame)))/2);
		
	}
	

	@Override
	protected void updateSelf(InputManager input) 
	{
		animationFrame += game.getElapsedSeconds()*2*Math.PI;

	}

	@Override
	public Entity clone(float newX, float newY) 
	{
		Player copy = new Player(this.game,newX,newY,radius,body.getLinearVelocity(),body.getAngle(),body.getAngularVelocity());
		copy.animationFrame = this.animationFrame;
		return copy;
	}

	public boolean lastLife() 
	{
		return currentLives==1;
	}

	public int getLives() {
		return currentLives;
	}
}
