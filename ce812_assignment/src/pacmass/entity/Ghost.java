package pacmass.entity;

import java.awt.Color;
import java.awt.Graphics2D;

import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.MathUtils;
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
import pacmass.entity.maze.MapInfo;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;
/** 
 * @author Ben Tilbury
 */
public class Ghost extends Entity 
{
	public static final float UNSCALED_RADIUS = 0.4f;
	private static final Color EDIBLE_COLOUR = Color.BLUE;
	private static final Color EDIBLE_COLOUR_ACCENT = new Color(245, 235, 200);
	private static final Color INVISI_COLOUR = new Color(0,0,0,0);
	
//	private static final Vec2 RED_GRAVITY = Game.STANDARD_GRAVITY.negate();//#.mul(0.5f);
//	private static final Vec2 ORANGE_GRAVITY = Game.STANDARD_GRAVITY;
//	private static final Vec2 PINK_GRAVITY = Game.STANDARD_GRAVITY.negate().mul(0.4f);
//	private static final Vec2 CYAN_GRAVITY = Game.STANDARD_GRAVITY.mul(0.4f);
	
//	static final int RED_GHOST = 0;
//	static final int ORANGE_GHOST = 1;
//	static final int CYAN_GHOST = 2;
//	static final int PINK_GHOST = 3;
//	
//	private static final byte PROP_MASK_COLOUR = 0b00000011;
//	

	protected final float radius;
	
	protected final Vec2 startingPosition; 
	private final int properties;
	private final Color col;
	
	private float eyeDir;
	private float bottomAngle;
	private float bottomAngleVel;
	
	private float edibleTimer;
	// Currently KLUDGE would be used for more things in the future
	private Float actionTimer;
	private boolean actionSwitch;
	
	private Vec2 normalGravity;
	
	/** -1 = eaten	
	 *   0 = normal
	 *   1 = edible
	 */
	private byte state;

	public Ghost(Game game, float x, float y, float scale, int props) 
	{
		this(game,x,y,UNSCALED_RADIUS*scale,new Vec2(), 0, 0, props);
	}
	
	public Ghost(Game game, float x, float y, float radius, Vec2 velocity, float angle, float angularVelocity, int props) 
	{
		super(game);
		World world = game.getWorld();
		this.startingPosition = new Vec2(x,y);
		BodyDef bodyDef = new BodyDef();
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
		
		this.properties = props&MapInfo.GHOST_PROP_MASK;
		
		fixtureDef.shape = circleShape;
		fixtureDef.friction = 0.2f;
		fixtureDef.restitution = 0.4f;
		fixtureDef.density = (float) (1/(Math.PI*radius*radius));
		
	
		switch(properties)
		{
			case MapInfo.GHOST_COL_ORANGE:
				this.col = new Color(255,150,0);
				this.normalGravity = Game.STANDARD_GRAVITY.mul(1.5f);
				fixtureDef.density = (float) (2/(Math.PI*radius*radius));
				this.airResistance = 0f;
				fixtureDef.restitution = 0.2f;
				fixtureDef.friction = 0.05f;
				break;
			case MapInfo.GHOST_COL_CYAN:
				this.col = Color.CYAN;
				this.normalGravity = Game.STANDARD_GRAVITY.negate();
				fixtureDef.restitution = 1f;
				this.airResistance = 0.8f;
				fixtureDef.friction = 1f;
				break;
			case MapInfo.GHOST_COL_PINK:
				this.col = new Color(255,150,180);
				this.normalGravity = Game.STANDARD_GRAVITY.negate().mul(0.2f);
				this.airResistance = 0f;
				this.rotationFriction = 1f;
				fixtureDef.restitution = 0.2f;
				fixtureDef.density = (float) (0.2/(Math.PI*radius*radius));
				break;
			case MapInfo.GHOST_COL_WHITE:
				this.col = Color.WHITE;
				// KLUDGE
				this.normalGravity = game.getPlayer().body.getPosition().sub(this.startingPosition);
				this.normalGravity.normalize();
				this.normalGravity.mulLocal(9.8f);
				this.actionTimer = 4+(float)Math.random()*2;
				break;
			default:
				this.col = Color.RED;
				this.normalGravity = new Vec2();
				Rot a = new Rot((float)Math.random()*MathUtils.TWOPI);
				Rot.mulToOut(a, Game.STANDARD_GRAVITY, normalGravity);
				this.actionTimer = 3+(float)Math.random()*3;
		}
		
		Filter filter = new Filter();
		fixtureDef.filter = filter;
		if(!DebugFlags.ROTATE_GRAV_METHOD) filter.categoryBits = ModifiedParticleSystem.IGNORE_PARTICLE_CATEGORY;
		body.createFixture(fixtureDef);
//		this.rotateFriction=1f;
		this.personalGravity = new Vec2();
		this.personalGravity.set(normalGravity);
	}
	

	public void drawSelf(Graphics2D g, GameWindow window)
	{
		switch(state)
		{
			case -1:
				window.drawGhost(g, INVISI_COLOUR, Color.BLACK, EDIBLE_COLOUR_ACCENT,  body.getPosition(), radius, body.getAngle(), bottomAngle, eyeDir);
				break;
			case 0:
				if(properties==MapInfo.GHOST_COL_WHITE) window.drawGhost(g, actionSwitch?Color.LIGHT_GRAY:col,  actionSwitch?Color.WHITE:Color.BLACK, actionSwitch?Color.BLACK:Color.WHITE,  body.getPosition(), radius, body.getAngle(), bottomAngle, eyeDir);
				else window.drawGhost(g, col,  Color.WHITE, Color.BLACK,  body.getPosition(), radius, body.getAngle(), bottomAngle, eyeDir);
				break;
			case 1:
				window.drawGhost(g, EDIBLE_COLOUR, Color.BLACK, EDIBLE_COLOUR_ACCENT,  body.getPosition(), radius, body.getAngle(), bottomAngle, eyeDir);
				break;
		}
	}
	
	@Override
	public void receiveEvent(Event eventType) 
	{
		switch(eventType)
		{
		case POWER_PELLET_PICKUP:
			setEdible(true);
			break;
		case LIFE_LOST:
			body.m_angularVelocity = 0;
			body.m_linearVelocity.setZero();
			body.setTransform(startingPosition, 0);
			body.synchronizeTransform();
			setEdible(false);
			bottomAngle = 0;
			bottomAngleVel = 0;

//			Utils.removeAllContacts(game.getWorld(), body.getContactList());
			break;
		default:
			break;
		}
	}

	@Override
	protected void updateSelf(InputManager input) 
	{
		switch(state)
		{
			case -1:
				this.eyeDir -= game.getElapsedSeconds()*Math.PI*6;

				// KLUDGE
				if(Vec2.cross(body.getPosition(), body.getLinearVelocity())!=0) body.setLinearVelocity(body.getPosition().negate());
				// KLUDGE
				if(body.getLinearVelocity().lengthSquared()<1) body.setLinearVelocity(body.getLinearVelocity().mul(2));
				
				
				if(body.getPosition().lengthSquared()<0.1f) respawn();
				
				break;
			case 0:
				Vec2 diff = game.getPlayer().body.getPosition().sub(body.getPosition());
				this.eyeDir = (float) (MathUtils.fastAtan2(diff.y, diff.x));
				this.bottomAngleVel -=  MathUtils.sin(bottomAngle);
				this.bottomAngle += (bottomAngleVel+body.m_angularVelocity)*game.getElapsedSeconds();
				this.bottomAngle -= this.bottomAngle*0.1;
				if(actionTimer!=null)
				{
					// KLUDGE
					if(actionTimer<=0)
					{
						if(properties==MapInfo.GHOST_COL_WHITE)
						{
							actionSwitch = !actionSwitch;
							actionTimer = 4+(float)Math.random()*2;
							this.body.setAwake(true);
						}
						else
						{
							Rot a = new Rot((float)Math.random()*MathUtils.TWOPI);
							Rot.mulToOut(a, Game.STANDARD_GRAVITY, normalGravity);
							this.personalGravity.set(normalGravity);
							actionTimer = 3+(float)Math.random()*3;
							this.body.setAwake(true);
						}
						
					}
					else actionTimer-=game.getElapsedSeconds();
				}
				
				if(properties==MapInfo.GHOST_COL_WHITE)
				{
					Rot a = new Rot(actionSwitch?-eyeDir:eyeDir);
					Rot.mulToOut(a, Game.STANDARD_GRAVITY.skew(), normalGravity);
					this.personalGravity.set(normalGravity);
				}
				
				break;
			case 1:
				if(edibleTimer<=0) setEdible(false);
				else
				{
					this.eyeDir += game.getElapsedSeconds()*Math.PI*3;
					this.bottomAngle = Utils.normalizeAngle(bottomAngle-game.getElapsedSeconds()*Math.PI);
					
					edibleTimer-=game.getElapsedSeconds();
				}	
				break;
		}
		
		if(input.getReleased(InputManager.SPACE)) setEdible(true);
	}

	private void setEdible(boolean edible)
	{
		if(state==-1) return;
		state = (byte) (edible?1:0);
		if(edible)
		{
			edibleTimer = 5;
			personalGravity.setZero();
		}
		else
		{
			edibleTimer = 0;
			personalGravity.set(normalGravity);
		}
	}
	
	public boolean isDangerous() 
	{
		return state==0;
	}

	private void respawn()
	{
		state = 0;
		edibleTimer = 0;
		personalGravity.set(normalGravity);
		Fixture f = body.m_fixtureList;
		while(f!=null)
		{
			if(!DebugFlags.ROTATE_GRAV_METHOD) f.m_filter.categoryBits = ModifiedParticleSystem.IGNORE_PARTICLE_CATEGORY;
			f.m_filter.maskBits = 65535;//Default
			f = f.getNext();
		}
		body.setTransform(new Vec2(), body.getAngle());
		
		// KLUDGE
		if(actionTimer!=null) actionTimer = 3+(float)Math.random()*3;
	}
	
	public void setEaten()
	{
		if(state==-1) return;
		state=-1;
		Utils.removeAllContacts(game.getWorld(), body.m_contactList);

		Fixture f = body.m_fixtureList;
		while(f!=null)
		{
			f.m_filter.categoryBits = 0;
			f.m_filter.maskBits = 0;
			f = f.getNext();
		}
		
		body.setLinearVelocity(body.getPosition().negate());
	}

	@Override
	public Entity clone(float newX, float newY) 
	{
		Ghost copy = new Ghost(this.game,newX,newY,radius,body.getLinearVelocity(),body.getAngle(),body.getAngularVelocity(),properties);
		copy.state = this.state;
		copy.bottomAngle = this.bottomAngle;
		copy.bottomAngleVel = this.bottomAngleVel;
		copy.edibleTimer = this.edibleTimer;
		return copy;
	}

}
