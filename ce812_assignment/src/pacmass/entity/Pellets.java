package pacmass.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Settings;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.particle.ParticleBodyContact;
import org.jbox2d.particle.ParticleColor;
import org.jbox2d.particle.ParticleContact;
import org.jbox2d.particle.ParticleDef;
import org.jbox2d.particle.ParticleGroup;
import org.jbox2d.particle.ParticleGroupDef;
import org.jbox2d.particle.ParticleType;

import pacmass.DebugFlags;
import pacmass.engine.Game;
import pacmass.entity.Entity.Event;
import pacmass.entity.maze.MapInfo;
import pacmass.entity.maze.Maze;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;
/** 
 * @author Ben Tilbury
 */
public class Pellets extends Entity 
{
	// Unfortunately this has to be the same for each and every particle.
	public static final float DEFAULT_PHYSICS_RADIUS = 0.3f; 
	public static final float DEFAULT_TRUE_RADIUS = 0.12f;	
	
	public static final Color POWER_PELLET_COLOUR = new Color(242, 230, 108);
	public static final float POWER_PELLET_RADIUS = 0.25f;
	
	private static final int OVERDRIVE_COUNT = 5;
	
	private final World world;
	private final Maze maze;
	
	private final int initialCount;
	private int currentCount;
	private float particleMass;
		
	private final float minMazeSq, width, halfWidth, height, halfHeight, radius;
	
//	private Map<Integer,Integer> properties;
	
	//Map<Vec2, Integer> test;
	
	// KLUDGE
	private boolean shrunk;
		
	public Pellets(Game game, Maze maze, Map<Vec2, Integer> particles) 
	{
		super(game,8,10);
		this.world = game.getWorld();
		this.maze = maze;
//		this.properties = new HashMap<Integer,Integer>();
		world.setParticleRadius(DEFAULT_PHYSICS_RADIUS*maze.getUnitScale());
		world.setParticleDensity(2*maze.getUnitScale());
		
		float stride = Settings.particleStride*world.getParticleRadius()*2;
	    particleMass = world.getParticleDensity() * stride * stride;
	    
//		test = particles;
		ParticleDef particleDef = new ParticleDef();
		particleDef.flags = ParticleType.b2_powderParticle;
		
		
		int tempCount = 0;
		for(Map.Entry<Vec2, Integer> p : particles.entrySet())
		{
			particleDef.position.set(p.getKey());
			
			// KLUDGE
			particleDef.userData = p;
			
			if((p.getValue()&MapInfo.PELLET_POWER)==0 && DebugFlags.PELLET_OVERDRIVE)
			{
				for(int i=0;i<OVERDRIVE_COUNT;i++)
				{
					int index = world.createParticle(particleDef);
					++tempCount;
				}
			}
			else
			{
				int index = world.createParticle(particleDef);
				++tempCount;
			}
		}

		 initialCount = tempCount;
		currentCount = initialCount;
		
		width = maze.getWorldWidth();
		height = maze.getWorldHeight();
		
		float temp = Math.min(width, height)/2;
		minMazeSq = temp*temp;
		
		halfWidth = width/2f;
		halfHeight = height/2f;
		
		radius = DEFAULT_TRUE_RADIUS*maze.getUnitScale();
		
//		airResistance = 100;
	}
	
	@Override
	protected void applyPersonalGravity() 
	{
		// Could do improved Euler method for particles but currently they're only weightless
	}
	
	@Override
	protected void applyAirResistance() 
	{
		int count = world.getParticleCount();
		Vec2[] velocities = world.getParticleVelocityBuffer();
		for(int i = 0; i<count; i++)
		{
			velocities[i].subLocal(velocities[i].mul(game.getElapsedSeconds()*airResistance));
		}
	}
	
	@Override
	protected void updateSelf(InputManager input) 
	{
		// KLUDGE
		if(shrunk)
		{
			world.setParticleRadius(DEFAULT_PHYSICS_RADIUS*maze.getUnitScale());
		}
		
		int count = world.getParticleBodyContactCount();
//		w.p
		ParticleBodyContact[] contacts = world.getParticleBodyContacts();
		Vec2[] positions = world.getParticlePositionBuffer();
//		Vec2[] velocities = world.getParticleVelocityBuffer();
		int[] flags = world.getParticleFlagsBuffer();
		Object[] properties = world.getParticleUserDataBuffer();
		
		for(int i = 0; i<count; i++)
		{
			ParticleBodyContact c = contacts[i];
			if(c.body.getUserData() instanceof Player)
			{
				// Because the physics radius is larger than the actual radius, do another check.
				// however because the particle will start to move away before reaching this...
//				Player p = (Player)c.body.getUserData();
//				Vec2 diff = p.body.getPosition().sub(positions[c.index]);
//				velocities[c.index].set(diff.mul(2));
//				
//				float comboRadius = radius+p.radius;
//				if(diff.lengthSquared()<comboRadius*comboRadius && (flags[c.index]&ParticleType.b2_zombieParticle)==0)
//				{
					--currentCount;
					flags[c.index] |= ParticleType.b2_zombieParticle;
					// KLUDGE
					if(((((Map.Entry<Vec2, Integer>)properties[c.index]).getValue())&MapInfo.PELLET_POWER)!=0) game.queueEvent(Event.POWER_PELLET_PICKUP);
//				}
			}
//			if(contacts[i].)
		}
		
//		if(currentCount==0) System.out.println("Woo");
		
		// Simple warp. Immediately jumps when it crosses the edge 
		float a = maze.getAngle();
		
		count = world.getParticleCount();
		Vec2 mazeT = new Vec2((float)Math.cos(a),(float)Math.sin(a));
		Vec2 mazeN = new Vec2(-mazeT.y,mazeT.x);
		
		for(int i = 0; i<count; i++)
		{
			
			Vec2 pos = positions[i];
			
			// Quick check to rule out most particles too far from any possible edge.
			if(pos.lengthSquared()<minMazeSq) continue;
			float rT = Vec2.dot(mazeT, pos);
			float rN = Vec2.dot(mazeN, pos);
			rT = (rT+width+halfWidth)%width - halfWidth;
			rN = (rN+height+halfHeight)%height - halfHeight;
			pos = mazeT.mul(rT).add(mazeN.mul(rN));
			positions[i].set(pos);
		
		}
		
		if(currentCount<=0 && initialCount>0 && !DebugFlags.IGNORE_PELLET_COMPLETION)
		{
			game.broadcastImmediateEvent(Event.NEXT_LEVEL, true);
		}
	}
	
	@Override
	public void destroy() 
	{
		int count = world.getParticleCount();
		for(int i = 0; i<count; i++) world.destroyParticle(i);
	}

	@Override
	protected void drawSelf(Graphics2D g, GameWindow window) 
	{
//		window.drawCircle(g, Color.WHITE, body.getPosition().x,body.getPosition().y, RADIUS);
		int count = world.getParticleCount();
		Vec2[] positions = world.getParticlePositionBuffer();
		Object[] properties = world.getParticleUserDataBuffer();
		
		for(int i = 0; i<count; i++)
		{
			if(DebugFlags.SHOW_PELLET_COLLISION_RINGS) 
			{	
				// This seems to be the radius at which Bodies have an impact.
				window.drawCircle(g, new Color(1f,1f,1f,0.4f), positions[i].x, positions[i].y, 2*DEFAULT_PHYSICS_RADIUS*maze.getUnitScale());
				// It's not until half this radius that other particles have the same impact
				// I've tried messing with the values in ParticleSystem but I can't seem to find what is the cause of it. I'm sure it's intended behaviour but I'd like to un-intend it.
				window.drawCircle(g, new Color(1f,1f,1f,0.4f), positions[i].x, positions[i].y, DEFAULT_PHYSICS_RADIUS*maze.getUnitScale());
			}
			
			// KLUDGE
			boolean power = (((Map.Entry<Vec2, Integer>)properties[i]).getValue()&MapInfo.PELLET_POWER)!=0;
			
			window.drawCircle(g, power?POWER_PELLET_COLOUR:Color.WHITE, positions[i].x, positions[i].y,  power?POWER_PELLET_RADIUS*maze.getUnitScale():radius);

		}
//		
//		for(Vec2 v : test.keySet())
//		{
//			window.drawCircle(g, Color.WHITE, v.x, v.y, radius);
//		}

//		window.drawCircle(g, Color.WHITE, halfWidth, 0, 0.2f);
//		window.drawCircle(g, Color.GREEN, -halfWidth, 0, 0.2f);
//		window.drawCircle(g, Color.WHITE,0, 0, (float) Math.sqrt(minSq));
	}
	
	@Override
	public void receiveEvent(Event eventType) 
	{
		switch(eventType)
		{
		case LIFE_LOST:
//			float a = maze.getLastAngle();
			
			Vec2[] positions = world.getParticlePositionBuffer();
			Vec2[] velocities = world.getParticleVelocityBuffer();
			Object[] properties = world.getParticleUserDataBuffer();
			
			int count = world.getParticleCount();
//			Rot rotation = new Rot(-a);
			for(int i = 0; i<count; i++)
			{
				//Rot.mulToOut(rotation, positions[i], positions[i]);
				positions[i].set(((Map.Entry<Vec2, Integer>)properties[i]).getKey());
				velocities[i].setZero();
			}
			world.setParticleRadius(0.01f);
		
			//KLUDGE
			shrunk = true;
			
//			break;
		}
	}

	@Override
	public Entity clone(float newX, float newY) 
	{
		throw new RuntimeException("Pellets cannot be cloned");
	}
	
	
}
