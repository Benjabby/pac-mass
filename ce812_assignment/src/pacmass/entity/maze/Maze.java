package pacmass.entity.maze;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.MathUtils;
import org.jbox2d.common.Rot;
import org.jbox2d.common.Vec2;
import org.jbox2d.common.Vec3;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.Filter;
import org.jbox2d.dynamics.Fixture;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.Contact;
import org.jbox2d.dynamics.joints.DistanceJoint;
import org.jbox2d.dynamics.joints.DistanceJointDef;
import org.jbox2d.dynamics.joints.Joint;
import org.jbox2d.dynamics.joints.JointEdge;
import org.jbox2d.dynamics.joints.JointType;
import org.jbox2d.dynamics.joints.PrismaticJoint;
import org.jbox2d.dynamics.joints.PrismaticJointDef;
import org.jbox2d.dynamics.joints.PulleyJoint;
import org.jbox2d.dynamics.joints.PulleyJointDef;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;
import org.jbox2d.dynamics.joints.WarpJoint;
import org.jbox2d.dynamics.joints.WarpJointDef;
import org.jbox2d.dynamics.joints.WeldJoint;
import org.jbox2d.dynamics.joints.WeldJointDef;
import org.jbox2d.particle.ModifiedParticleSystem;

import pacmass.DebugFlags;
import pacmass.Utils;
import pacmass.engine.Game;
import pacmass.entity.Entity;
import pacmass.entity.Ghost;
import pacmass.entity.Pellets;
import pacmass.entity.Player;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;
/** 
 * @author Ben Tilbury
 */
public class Maze extends Entity 
{	
	private static final float TURN_ACCELERATION = 3f;
	private static final float ANGULAR_DRAG = 0.02f;
	private static final float WALL_DENSITY = 1f;
	private static final float WALL_RESTITUTION = 0.2f;
	private static final float WALL_FRICTION = 0.2f;
	private static final Color WALL_COLOUR =  new Color(111, 65, 204);
	
	private final MazeContactListener testContact = new MazeContactListener();
	
	private final MapInfo map;
	private final List<WarpPoint> warps;
	private final Player player;
	
	private final Pellets pelletManager;
	
	private final float unitScale;
	private final float worldWidth;
	private final float worldHeight;
	
	private final int unitWidth;
	private final int unitHeight;
	
//	private final List<Path2D.Float> wallPaths;
//	private final Path2D.Float wallTest;
	//private final Path2D.Float playPath;
	
	// Render stuff
	private final Area wallArea;
	private final Area playArea;
	private final Area voidArea;
	private final Path2D.Float bounds;
	private List<Path2D.Float> debugWarpSideHelpers;
//	private final boolean voidOverlay;
	///

	private final Set<Entity> entities;
//	private final Filter fixtureFilter;
	
	final Vec2 bottomLeft, topLeft, topRight, bottomRight;

	private HashMap<Path2D.Float,Color> debugShapes;
	
	private int angularAcc;
	private float angularVel;
	

	// KLUDGE
	public static float psuedoAngle = 0;

//	public static float unitDebug;
	
	public Maze(Game game, MapInfo map) 
	{
		super(game, 2, 2);
		this.map = map;
		this.angularVel = 0f;
		this.debugShapes = DebugFlags.DRAW_MAZE_SUBSHAPES ? new HashMap<Path2D.Float,Color>() : null;
		this.unitWidth = map.width;
		this.unitHeight = map.height;
		this.unitScale = (float) ((Game.WORLD_MIN_SIZE/Math.sqrt(2.0))/Math.max(unitWidth, unitHeight));
		
		this.airResistance = 0;
		this.rotationFriction = 0;

//		unitDebug = (float) Math.sqrt(unitScale);
		
		this.worldWidth = map.width*unitScale;
		this.worldHeight = map.height*unitScale;
		
		Game.MAX_VEL_SQUARED = 1000*unitScale*unitScale;

		// PHYSICS
		World w = game.getWorld();
		BodyDef bodyDef = new BodyDef();
//		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.type = BodyType.KINEMATIC;
		bodyDef.position.set(0, 0);
		bodyDef.linearVelocity.set(0, 0);
		this.body = w.createBody(bodyDef);

		bottomLeft = new Vec2(-worldWidth/2.0f,-worldHeight/2.0f);
		topLeft = new Vec2(-worldWidth/2.0f,worldHeight/2.0f);
		topRight = new Vec2(worldWidth/2.0f,worldHeight/2.0f);
		bottomRight = new Vec2(worldWidth/2.0f,-worldHeight/2.0f);
		

		this.entities = new HashSet<Entity>();
		
		// Demo KLUDGE
		if(DebugFlags.WARP_DEMO_MAP) 
		{
			Player antiPlayer = new Player(game,0,6.5f*unitScale,unitScale);
			antiPlayer.setPersonalGravity(Game.STANDARD_GRAVITY.negate());
			antiPlayer.copyTest = true;
			entities.add(antiPlayer);
		}

		createWalls();
		this.warps = createWarps();
		
		if(!DebugFlags.IGNORE_NO_PLAYER)
		{
			MapInfo.MapNode playerNode = map.player;
			this.player = new Player(game,mapXtoWorldX(playerNode.x+0.5f),mapYtoWorldY(playerNode.y+0.5f),unitScale);
		}
		else
		{
			player = new Player(game,-100,-100,unitScale);
			player.setPersonalGravity(new Vec2());
		}
		entities.add(player);

		createGhosts();
		
		this.pelletManager = createPellets();
		
		w.setContactListener(testContact);
		
		
		// Create rendering objects
		Path2D.Float wallPath = new Path2D.Float();
		Path2D.Float playPath = new Path2D.Float();
		
		for(int i = 0; i<unitHeight; i++)
		{
			for(int j = 0; j<unitWidth; j++)
			{
				float x = mapXtoWorldX(j);
				float y = mapYtoWorldY(i);
				if(map.wallAt(j, i)) wallPath.append(makeSquarePath(x,y,unitScale),false);
				if(map.playAt(j, i)) 
				{
					playPath.append(makeSquarePath(x,y,unitScale),false);
				}
			}
		}
		
		this.wallArea = new Area(wallPath);
		this.playArea = new Area(playPath);
		this.voidArea = new Area(makeRectPath(-worldWidth, -worldHeight, 2*worldWidth, 2*worldHeight));
		this.voidArea.subtract(wallArea);
		this.voidArea.subtract(playArea);
		this.bounds = makeRectPath(-worldWidth/2f, -worldHeight/2f, worldWidth, worldHeight);
		
		//entities.add(new Pellets(game,this));
		
		
//		System.out.print
				
//		entities.add(new BasicParticle(game, 0f, 1.1f, 0, 0, 0.4f*unitScale, Color.PINK, 1f, 0f));
//		entities.add(new BasicParticle(game, 0.1f, 1.1f, 0, 0, 0.4f*unitScale, Color.ORANGE, 1f, 0.9f));
//
//		entities.add(new BasicParticle(game, -0.1f, 1.1f, 0, 0, 0.2f, Color.PINK, 1f, 9.8f));
//
//		entities.add(new BasicParticle(game, 0.2f, 1.1f, 0, 0, 0.2f, Color.PINK, 1f, 9.8f));
//
//		entities.add(new BasicParticle(game, -0.2f, 1.1f, 0, 0, 0.2f, Color.PINK, 1f, 9.8f));

//		entities.add(new BasicParticle(game, 0f, 1f, 0, 0, 0.2f, Color.PINK, 1f, 9.8f));
		
		
		//entities.add(new BasicParticle(game, -2.5f, 0f, 0, 0, 0.4f, Color.CYAN, 1, 9.8f));
		//entities.add(new BasicParticle(game, 2.5f, 0f, 0, 0, 0.4f, Color.RED, 1, 9.8f));
		
		game.addAllEntities(entities);
		
		// KLUDGE
		System.out.printf("%d Lives\n",player.getLives());
		
		// KLUDGE
		psuedoAngle = 0;
		
		// KLUDGE
//		game.testScale = 2*unitScale;
	}
	
	public float mapXtoWorldX(float mapX)
	{
		return (mapX*unitScale) - worldWidth/2.0f;
	}
	
	public float mapYtoWorldY(float mapY)
	{
		return (mapY*unitScale) - worldHeight/2.0f;
	}
	
	private void createGhosts()
	{
		for(MapInfo.MapNode ghost : map.createList(MapInfo.GHOST))
		{
			entities.add(new Ghost(game,mapXtoWorldX(ghost.x+0.5f),mapYtoWorldY(ghost.y+0.5f),unitScale,ghost.properties));
		}
	}
	
	private Pellets createPellets()
	{
		Map<Vec2,Integer> pelletData = new HashMap<Vec2,Integer>();
//		Random r = new Random();
		for(MapInfo.MapNode pellet : map.createList(MapInfo.PELLET))
		{
			if(DebugFlags.ROTATE_GRAV_METHOD && map.typeAt(pellet.x,pellet.y,MapInfo.GHOST)) continue;
//			pellets.put(new Vec2(mapXtoWorldX(pellet.x+0.5f),mapYtoWorldY(pellet.y+0.5f)), r.nextInt());
			pelletData.put(new Vec2(mapXtoWorldX(pellet.x+0.5f),mapYtoWorldY(pellet.y+0.5f)), pellet.properties);
		}
		Pellets pellets = new Pellets(game,this,pelletData);
		entities.add(pellets);
		return pellets;
	}
	
	private void createWalls()
	{
		for(MapInfo.MergedRectNode wall : map.createMergedList(MapInfo.WALL))
		{
			float x = mapXtoWorldX(wall.x);
			float y = mapYtoWorldY(wall.y);
			float length = wall.length*unitScale;
			if(DebugFlags.DRAW_MAZE_SUBSHAPES) debugShapes.put(makeRectPath(x,y,wall.vertical?unitScale:length,wall.vertical?length:unitScale),Utils.randomColour());
			PolygonShape shape = new PolygonShape();
			Vec2[] vertices = makeRect(x,y,wall.vertical?unitScale:length,wall.vertical?length:unitScale);
			shape.set(vertices, 4);
			FixtureDef fixtureDef = new FixtureDef();
			fixtureDef.shape = shape;
			fixtureDef.density = WALL_DENSITY;
			fixtureDef.friction = WALL_FRICTION;
			fixtureDef.restitution = WALL_RESTITUTION;
			body.createFixture(fixtureDef);
//			System.out.println(sequence);
		}
	}
	
	private void applyRotationEffect()
	{
//		Body b;
//		float dt = game.getElapsedSeconds();
//		float idt = 1f/dt;
//
//		Rot r = new Rot(angularVel*dt);
		for(Entity entity : entities) entity.applyRotationFriction(this);
	}
	
	// Lots of old code for different attempts at this
//			if(entity.getBody()==null) continue;
//			if(entity.rotateFriction==0) continue;
//			
//			b = entity.getBody();
//			b.setTransform([], []);
			
//			Vec2 n = new Vec2(b.getPosition());
//			n.normalize();
//			Vec2 t = n.skew();
//			float vN = Vec2.dot(b.m_linearVelocity, n);
//			float vT = Vec2.dot(b.m_linearVelocity, t);
//			
////			vN*=(1-entity.rotateFriction);
////			vT*=(1-entity.rotateFriction);
//			
//			b.setLinearVelocity(n.mul(vN).add(t.mul(vT)));
		
			// Assuming the maze is always centred and rotating around (0,0) which it always will be.
//			Vec2 force = body.getLinearVelocityFromWorldPoint(b.getPosition());
			
//			Vec2 out = new Vec2();
//			Rot.mulToOut(r, Game.STANDARD_GRAVITY, out);
//			entity.setPersonalGravity(out);
//			
////			b.setLinearVelocity(out.mul(entity.rotateFriction).add);
//			Vec2 force2 = Vec2.cross(angularVel, b.getPosition());
			
//			Vec2 force3 = new Vec2();
//			Rot.mulToOut(r, b.getPosition(), force3);
//			force3.subLocal(b.getPosition());
			
//			b.setTransform(b.getPosition().add(force2.mul(dt)), b.getAngle());
////			
//////			force.set(force.y, -force.x);
//			b.applyForceToCenter(force.mul(b.m_mass).sub(entity.mazeRotationForce).mul(idt*entity.rotateFriction));
//			entity.mazeRotationForce.set(force.mul(b.m_mass));

//////			System.out.println(b.getLinearVelocity());
//			Vec2 rotateFrictionForce=new Vec2(b.getLinearVelocity());
//			rotateFrictionForce=rotateFrictionForce.mul(-1*b.m_mass*entity.rotateFriction*force.length());
//			b.applyForceToCenter(rotateFrictionForce);
			
//			b.applyLinearImpulse(force3.mul(b.m_mass),b.getWorldCenter(),true);
//		}
//	}
	
	@Override
	public void receiveEvent(Event eventType)
	{
		if(eventType==Event.LIFE_LOST)
		{
			angularAcc = 0;
			angularVel = 0;
			for(WarpPoint warp : warps) warp.reset();
			body.setTransform(body.getPosition(), 0);
			body.synchronizeTransform();
			psuedoAngle=0;
			
			// KLUDGE
			System.out.printf("%d Lives remaining\n",player.getLives()-1); // -1 since this will update before the player.
			
		}
	}
	
	private	List<WarpPoint> createWarps()
	{
		if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) debugWarpSideHelpers = new ArrayList<Path2D.Float>();
		List<WarpPoint> warps = new ArrayList<WarpPoint>();
		for(MapInfo.MergedRectNode warp : map.createMergedList(MapInfo.WARP)) warps.add(new WarpPoint(warp));
		
		return warps;
	}
	
	
	public float getAngle()
	{
		return body.getAngle();
	}
	
	public float getUnitScale()
	{
		return unitScale;
	}
	public float getWorldWidth()
	{
		return worldWidth;
	}
	public float getWorldHeight()
	{
		return worldHeight;
	}
	
	
	@Override
	public void updateSelf(InputManager input) 
	{
//		boolean rightOn = input.getTriggered(InputManager.RIGHT);
//		boolean leftOn = input.getTriggered(InputManager.LEFT);
		
		if(input.getTriggered(InputManager.RIGHT)) angularAcc = -1;
		if(input.getTriggered(InputManager.LEFT)) angularAcc = 1;
		
		if(input.getReleased(InputManager.RIGHT))
		{
			if(input.getHeld(InputManager.LEFT)) angularAcc = 1;
			else angularAcc = 0;
		}
		
		if(input.getReleased(InputManager.LEFT))
		{
			if(input.getHeld(InputManager.RIGHT)) angularAcc = -1;
			else angularAcc = 0;
		}
		
		if(DebugFlags.ALLOW_SKIPPING && input.getTriggered(InputManager.SKIP)) game.broadcastImmediateEvent(Event.NEXT_LEVEL, true);
		
//		if(input.getReleased(InputManager.SPACE)) game.broadcastImmediateEvent(Event.LIFE_LOST, true);
		
		this.angularVel += angularAcc*game.getElapsedSeconds()*TURN_ACCELERATION;
		this.angularVel -= angularVel*ANGULAR_DRAG;

//		System.out.println(angularVel);
		
		if(DebugFlags.ROTATE_GRAV_METHOD)
		{
			psuedoAngle+=this.angularVel*game.getElapsedSeconds();
			Rot r = new Rot(-this.angularVel*game.getElapsedSeconds());
			for(Entity entity : entities)
			{
				Vec2 g = entity.getPersonalGravity();
				if(g!=null)	Rot.mulToOut(r, g, g);
			}
		}
		else
		{
			body.setAngularVelocity(angularVel);

			applyRotationEffect();

		}
		
		for(WarpPoint warp : warps) warp.update();
		
	}

	@Override
	public void drawSelf(Graphics2D g, GameWindow window) 
	{
//		window.drawPath(g, null, personalGravity, angularAcc, playPath);
		window.drawPath(g, Color.BLACK, body.getPosition(), body.getAngle(), bounds);
		//window.drawArea(g, Color.DARK_GRAY, body.getPosition(), body.getAngle(), playArea);
		
		if(DebugFlags.DRAW_MAZE_SUBSHAPES) window.debugDrawPathBatched(g, body.getPosition(), body.getAngle(), debugShapes);
		else window.drawArea(g, WALL_COLOUR, body.getPosition(), body.getAngle(), wallArea);
		
		
//		window.drawLine(g, Color.WHITE, body.getWorldPoint(new Vec2(-0.5f,-(l/2.0f))), body.getWorldPoint(new Vec2(0.5f,-(l/2.0f))), 1.5f);		
	
		
	}

	// KLUDGEYEST KLUDGE
	public void test(Graphics2D g, GameWindow window) 
	{
		if(!(DebugFlags.HIDE_VOID_COVER||DebugFlags.WARP_DEMO_MAP)) window.drawArea(g, Color.DARK_GRAY, body.getPosition(), body.getAngle(), voidArea);;
		
		if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) window.drawPathBatched(g, Color.WHITE, body.getPosition(), body.getAngle(), debugWarpSideHelpers);

		for(WarpPoint warp : warps) warp.draw(g, window);
	}
	
	static Path2D.Float makeSquarePath(float x, float y)
	{
		return makeSquarePath(x,y,1);
	}
	
	static Path2D.Float makeSquarePath(float x, float y, float size)
	{
		Path2D.Float path = new Path2D.Float();
		path.moveTo(x,y);
		path.lineTo(x+size, y);
		path.lineTo(x+size, y+size);
		path.lineTo(x, y+size);
		path.lineTo(x, y);
		path.closePath();
		return path;
	}
	
	static Path2D.Float makeRectPath(float x, float y, float width, float height)
	{
		Path2D.Float path = new Path2D.Float();
		path.moveTo(x,y);
		path.lineTo(x+width, y);
		path.lineTo(x+width, y+height);
		path.lineTo(x, y+height);
		path.lineTo(x, y);
		path.closePath();
		return path;
	}
	
	static Vec2[] makeSquare(float x, float y) 
	{
		return makeSquare(x,y,1);
	}
	
	static Vec2[] makeSquare(float x, float y, float size) 
	{
		Vec2[] values = {new Vec2(x,y),new Vec2(x+size,y),new Vec2(x+size,y+size),new Vec2(x,y+size)};
		return values;
	}
	
	static Vec2[] makeRect(float x, float y, float width, float height)
	{
		Vec2[] values = {new Vec2(x,y),new Vec2(x+width,y),new Vec2(x+width,y+height),new Vec2(x,y+height)};
		return values;
	}
	
	@Override
	public Entity clone() 
	{
		return new Maze(game, map);
	}
	
	@Override
	// KLUDGE
	public Entity clone(float newX, float newY) 
	{
		throw new RuntimeException("Mazes cannot be cloned with a position");
	}
	
	// KLUDGE
	public Player getPlayer() 
	{
		return player;
	}

	
	class MazeContactListener implements ContactListener
	{
		// An ugly hack for java's lack of referencing
//		Entity[] entityRef = new Entity[2];
//		WarpPoint[] warpRef = new WarpPoint[1];
//		Fixture[] sensorRef = new Fixture[1];
//		

		// KINDA KLUDGE
		private boolean entityWarpContact(Contact contact, boolean begin)
		{
			Fixture A = contact.m_fixtureA;
			Fixture B = contact.m_fixtureB;
			
			boolean sensorA = A.isSensor();
			boolean sensorB = B.isSensor();
			
			if(!(sensorA ^ sensorB)) return false;
			
			Entity entity;
			WarpPoint warp;
			Fixture sensor;
			
			if(sensorA)
			{
				
				entity = (Entity) B.m_body.getUserData();
				warp = (WarpPoint) A.getUserData();
				sensor = A;
			}
			else
			{
				entity = (Entity) A.m_body.getUserData();
				warp = (WarpPoint) B.getUserData();
				sensor = B;
			}
		
			warp.queueContact(begin, entity, sensor);
			
			return true;
		}
		
		// KINDA KLUDGE
		private boolean playerGhostContact(Contact contact)
		{
			Body A = contact.m_fixtureA.m_body;
			Body B = contact.m_fixtureB.m_body;
			
			boolean ghostA = A.getUserData() instanceof Ghost;
			boolean ghostB = B.getUserData() instanceof Ghost;
			
			boolean playerA = A.getUserData() instanceof Player;
			boolean playerB = B.getUserData() instanceof Player;
			
			if(!((ghostA^ghostB)&&(playerA^playerB))) return false;
			
			Ghost ghost;
			Player player;
			
			if(ghostA)
			{
				ghost = (Ghost)A.getUserData();
				player = (Player)B.getUserData();
			}
			else
			{
				ghost = (Ghost)B.getUserData();
				player = (Player)A.getUserData();
			}
			
			if(ghost.isDangerous()) 
			{
				if(player.lastLife()) game.broadcastImmediateEvent(Event.GAME_OVER);
				else game.queueEvent(Event.LIFE_LOST);
				
			}
			else ghost.setEaten();
			
			return true;
		}
		
		@Override
		public void beginContact(Contact contact) 
		{
			
			if(entityWarpContact(contact,true))
			{
				//anything else 
			}

		}

		@Override
		public void endContact(Contact contact)
		{
			
			if(entityWarpContact(contact,false))
			{
				//anything else
			}
			
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) 
		{
			if(!DebugFlags.WARP_DEMO_MAP && playerGhostContact(contact))
			{
				contact.setEnabled(false);
			}
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	/**
	 *  Since Box2D can be weird about manually moving objects during a contact callback (and probably other things like creating new bodies).
	 *  it's safer to just queue all contacts and execute them when this warp point updates 
	 * @author Ben
	 *
	 */
	class WarpPoint
	{
//		private final MazeMap.Warp mapData;
		private final Fixture sensorA, sensorB;
		// Could in future be replaced with a normal vector;
		private final boolean vertical;
	
		// KLUDGE
		private Path2D.Float testA, testB, testARect, testBRect;

		private Queue<ContactQueueElement> queuedContacts;

		private Map<Integer, EntityCopyPair> entityPairs;
		// for quick looking up of whether a given entity is being managed by this warp point to avoid looping through the entity set.
		// Sidenote I tried to find a way to have a fast hashmap/other set that can access an entity pair from either the original or the copy but failed.
		private Map<Entity, Integer> originals, copies; 

		// Note this isn't good. I doubt there will ever be the amount of interactions that will overflow the int limit
		// but really this should assign to the lowest unused ID
		// KLUDGE
		private int currentID = 0;
		
		Vec2 helperA1, helperB1, helperA2, helperB2, outA1, outB1, outA2, outB2;
		
		public WarpPoint(MapInfo.MergedRectNode mapData)
		{
			// MergedRectNode's definition of vertical (the shape of the rectangle) is perpendicular to the direction the warp goes in
			this.vertical = !mapData.vertical;
//			this.mapData = mapData;
			this.queuedContacts = new LinkedList<ContactQueueElement>();
			this.entityPairs = new HashMap<Integer,EntityCopyPair>();
			this.originals = new HashMap<Entity,Integer>();
			this.copies = new HashMap<Entity,Integer>();
			
			float length = mapData.length*unitScale;
			
			{
				float x = mapXtoWorldX(mapData.x);
				float y = mapYtoWorldY(mapData.y);
				// Square Sensor
				PolygonShape shape = new PolygonShape();
				shape.set(makeRect(x, y, vertical?length:unitScale, vertical?unitScale:length),4);
				// Line Sensor
//				EdgeShape shape = new EdgeShape();
//				shape.set(new Vec2(x,y+off), new Vec2(vertical?x+mapData.length:x, (vertical?y:y+mapData.length) + off));
				
				testARect = makeRectPath(x,y,vertical?length:length, vertical?unitScale:length);
				testA = Utils.makeLinePath(x,y, vertical?x+length:x, (vertical?y:y+length));
				
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0;
				fixtureDef.isSensor = true;
				this.sensorA = body.createFixture(fixtureDef);
				this.sensorA.setUserData(this);
//				
				PolygonShape sideHelper = new PolygonShape();
				helperA1 = new Vec2(x-unitScale,y-unitScale);
				outA1 = helperA1.add(new Vec2(vertical?0:2*unitScale,vertical?2*unitScale:0));
				sideHelper.set(makeSquare(x-unitScale,y-unitScale, unitScale), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) debugWarpSideHelpers.add(makeSquarePath(x-unitScale,y-unitScale, unitScale));
				
				sideHelper = new PolygonShape();
				helperA2 = new Vec2(vertical?x+length+unitScale:x-unitScale,vertical?y-unitScale:y+length+unitScale);
				outA2 = helperA2.add(new Vec2(vertical?0:2*unitScale,vertical?2*unitScale:0));
				sideHelper.set(makeSquare(vertical?x+length:x-unitScale,vertical?y-unitScale:y+length, unitScale), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density = 0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) debugWarpSideHelpers.add(makeSquarePath(vertical?x+length:x-unitScale,vertical?y-unitScale:y+length, unitScale));
				
			}
			{
				float x = mapXtoWorldX(mapData.x)*(vertical?1:-1) - (vertical?0:unitScale);
				float y = mapYtoWorldY(mapData.y)*(vertical?-1:1) - (vertical?unitScale:0);
				//float off = (vertical?0:unitScale);
//				// Square Sensor
				PolygonShape shape = new PolygonShape();
				shape.set(makeRect(x, y, vertical?length:unitScale, vertical?unitScale:length),4);
				// Line Sensor
//				EdgeShape shape = new EdgeShape();
//				shape.set(new Vec2(x+off,y), new Vec2((vertical?x+length:x) + off, vertical?y:y+length));
				
				testBRect = makeRectPath(x,y,vertical?length:unitScale, vertical?unitScale:length);
				testB = Utils.makeLinePath(x+(vertical?0:unitScale),y+(vertical?unitScale:0), (vertical?x+length:x+unitScale), vertical?y+unitScale:y+length);
				
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0;
				fixtureDef.isSensor = true;
				this.sensorB = body.createFixture(fixtureDef);
				this.sensorB.setUserData(this);
				
				PolygonShape sideHelper = new PolygonShape();
				helperB1 = new Vec2(vertical?x-unitScale:x+2*unitScale,vertical?y+2*unitScale:y-unitScale);
				outB1 = helperB1.sub(new Vec2(vertical?0:2*unitScale,vertical?2*unitScale:0));
				sideHelper.set(makeSquare(vertical?x-unitScale:x+unitScale,vertical?y+unitScale:y-unitScale, unitScale), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) debugWarpSideHelpers.add(makeSquarePath(vertical?x-unitScale:x+unitScale,vertical?y+unitScale:y-unitScale, unitScale));
				
				sideHelper = new PolygonShape();
				helperB2 = new Vec2(vertical?x+length+unitScale:x+2*unitScale,vertical?y+2*unitScale:y+length+unitScale);
				outB2 = helperB2.sub(new Vec2(vertical?0:2*unitScale,vertical?2*unitScale:0));
				sideHelper.set(makeSquare(vertical?x+length:x+unitScale,vertical?y+unitScale:y+length, unitScale), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS||DebugFlags.WARP_DEMO_MAP) debugWarpSideHelpers.add(makeSquarePath(vertical?x+length:x+unitScale,vertical?y+unitScale:y+length, unitScale));
				
			}
			
			
		}
		
		public void reset()
		{
			queuedContacts.clear();
			entityPairs.clear();
			originals.clear();
			game.removeAllEntities(copies.keySet());
			copies.clear();
			currentID = 0;
		}

		public void queueContact(boolean begin, Entity entity, Fixture sensor) 
		{
			boolean fromA = sensor.equals(sensorA);
			assert(fromA || sensor.equals(sensorB)) : "Warp point has had contact with an unknown sensor. Something has really gone wrong.";
			
			queuedContacts.offer(new ContactQueueElement(begin, entity, fromA));
			
//			System.out.println("Contact queued");
			
		}
		
		private void beginContact(Entity entity, boolean fromA)
		{
//			System.out.println("Contact executed");
			
			boolean isOriginal = originals.containsKey(entity);
			boolean isCopy = copies.containsKey(entity);
			boolean isBrandNew = !(isOriginal||isCopy);
			
			assert(!(isOriginal&&isCopy)) : "Entity has somehow ended up in both the original and copy set for a WarpPoint";
			
			if(isBrandNew && entity!=null && !entity.copyTest)
			{
				EntityCopyPair newPair = new EntityCopyPair(entity,fromA);
			}
		}
		
		private void endContact(Entity entity, boolean fromA)
		{
			boolean isOriginal = originals.containsKey(entity);
			boolean isCopy = copies.containsKey(entity);
			
			assert(!(isOriginal&&isCopy)) : "Entity has somehow ended up in both the original set and copy set for a WarpPoint";
			//assert(isOriginal||isCopy) : "endContact called for an entity not registered as an original or copy for a WarpPoint";
			
			if(isCopy)
			{
//				entity.visible = true;
			}
			
			if(isOriginal)
			{
				int ID = originals.get(entity);
				EntityCopyPair pair = entityPairs.get(ID);
				
				if(fromA==pair.fromA)
				{
				
					Vec2 pos = entity.getBody().getPosition();
					double angle = body.getAngle();
					if(vertical) angle -= Math.PI/2;
					Vec2 warpNormal = new Vec2((float)Math.cos(angle),(float)Math.sin(angle));
					
					if(Math.abs(Vec2.dot(pos, warpNormal))>((vertical?worldHeight:worldWidth)/2.0))
					{
						pair.swap();
					}

					pair.remove();
					
				}
				else
				{
					pair.remove();
				}
			}
		}
		
		public void update()
		{
			for(ContactQueueElement contact; (contact = queuedContacts.poll()) != null;)
			{
				if(contact.begin) beginContact(contact.entity, contact.fromA);
				else endContact(contact.entity, contact.fromA);
			}
			
			for(EntityCopyPair ecp : entityPairs.values())
			{
				if(ecp!=null) ecp.syncPositions();
			}
		}

		public void draw(Graphics2D g, GameWindow window)
		{
			if(DebugFlags.DRAW_WARP_POINTS)
			{
				window.drawPath(g, Color.MAGENTA, body.getPosition(), body.getAngle(), testARect);
				window.drawPath(g, Color.CYAN, body.getPosition(), body.getAngle(), testBRect);
	
				window.strokePath(g, Color.WHITE, body.getPosition(), body.getAngle(), testA, 2f);
				window.strokePath(g, Color.WHITE, body.getPosition(), body.getAngle(), testB, 2f);
			}
			if(DebugFlags.DRAW_WARP_PULLEYS||DebugFlags.WARP_DEMO_MAP)
			{
				for(EntityCopyPair ecp : entityPairs.values())
				{
					{
						Vec2 A1 = ecp.warpJoint1.getGroundAnchorA();
						Vec2 A2 = new Vec2();
						ecp.warpJoint1.getAnchorA(A2);
		
						Vec2 B1 = ecp.warpJoint1.getGroundAnchorB();
						Vec2 B2 = new Vec2();
						ecp.warpJoint1.getAnchorB(B2);
						
						window.strokeWorldLine(g, Color.CYAN, A1, A2, 2);
						window.strokeWorldLine(g, Color.CYAN, B1, B2, 2);
					}
					{
						Vec2 A1 = ecp.warpJoint2.getGroundAnchorA();
						Vec2 A2 = new Vec2();
						ecp.warpJoint2.getAnchorA(A2);
	
						Vec2 B1 = ecp.warpJoint2.getGroundAnchorB();
						Vec2 B2 = new Vec2();
						ecp.warpJoint2.getAnchorB(B2);
						
						window.strokeWorldLine(g, Color.MAGENTA, A1, A2, 2);
						window.strokeWorldLine(g, Color.MAGENTA, B1, B2, 2);
					}
				}
			}
		}
		
		
		private class EntityCopyPair
		{
			private final Entity original;
			private final Entity copy;
			
			// KLUDGE
			private final WarpJoint warpJoint1;
			// KLUDGE
			private final WarpJoint warpJoint2;
			
			private final boolean fromA;
			private int ID;
			
//			float prevAirResistance;
//			float prevRotationFriction;
			
			public EntityCopyPair(Entity original, boolean fromA)
			{
				this.original = original;
				
				Vec2 pos = original.getBody().getPosition();
				double angle = body.getAngle();
				if(vertical) angle += Math.PI/2;
				Vec2 warpNormal = new Vec2((float)Math.cos(angle),(float)Math.sin(angle));
				Vec2 offset = warpNormal.mul((fromA?-1:1)*(vertical?worldHeight:worldWidth));
				Vec2 newPos = pos.sub(offset);
				
//				this.copy = null;
				this.copy = original.createClone(newPos.x,newPos.y);
				this.fromA = fromA;
				
				// Note this isn't good. I doubt there will ever be the amount of interactions that will overflow the int limit
				// but really this should assign to the lowest unused ID
				this.ID = currentID++;
				entityPairs.put(ID, this);
				originals.put(original, ID);
				copies.put(copy, ID);
				
//				prevAirResistance = original.airResistance;
//				prevRotationFriction = original.rotationFriction;


				// Avoids stupid warp bugs. This pulley warp thing is very very bad.
				// Update. No. No it doesn't.
//				original.airResistance = 0f;
//				original.rotationFriction = 0f;
//				copy.airResistance = 0f;
//				copy.rotationFriction = 0f;

				game.addEntity(copy);
//				copy.visible = false;
				
				/////////////////
				
//				Fixture f = copy.getBody().getFixtureList();
//				while(f!=null)
//				{
//					f.setDensity(0);
////					f.setSensor(true);
//					f = f.getNext();
//				}
//				copy.setPersonalGravity(null);
				//////////////////////
				
				// Trying with regular joints
//				WeldJointDef wjd = new WeldJointDef();
//				wjd.bodyA = original.getBody();
//				wjd.bodyB = copy.getBody();
//				wjd.localAnchorB = warpNormal.mul((fromA?-1:1)*(vertical?unitHeight:unitWidth)).negate()
//				
//				testJoint = (WeldJoint) game.getWorld().createJoint(wjd);
//				
//				DistanceJointDef djd = new DistanceJointDef();
//				djd.bodyA = original.getBody();
//				djd.bodyB = copy.getBody();
//				djd.length = vertical?unitHeight:unitWidth;
//				distanceJoint = (DistanceJoint) game.getWorld().createJoint(djd);
//				
//				djd = new DistanceJointDef();
//				djd.bodyA = body;
//				djd.bodyB = copy.getBody();
//				
//				float x = fromA?mapData.bX-(unitWidth/2.0f):mapData.aX-(unitWidth/2.0f);
//				float y = fromA?mapData.bX-(unitWidth/2.0f):mapData.aY-(unitHeight/2.0f);
//				Vec2 t = new Vec2(x,y);
//				djd.localAnchorA.set(t);
//				
//				djd.length = body.getWorldPoint(t).sub(copy.getBody().getPosition()).length();
//				
//				testDistance = (DistanceJoint) game.getWorld().createJoint(djd);
				
//				PrismaticJointDef pjd = new PrismaticJointDef();
//				
//				pjd.initialize(original.getBody(), copy.getBody(), original.getBody().getPosition(), warpNormal);
//				pjd.upperTranslation = vertical?unitHeight:unitWidth;
//				pjd.lowerTranslation = vertical?unitHeight:unitWidth;
//				pjd.enableLimit = true;
//				pjd.referenceAngle = (float)angle;
//				prismaticJoint = (PrismaticJoint) game.getWorld().createJoint(pjd);
				
		
				// Can't get it to work.
				// Trying custom joint
				
				{
				WarpJointDef wjd = new WarpJointDef(Maze.this);
				wjd.bodyA = original.getBody();
				wjd.bodyB = copy.getBody();
			
				wjd.mazeLocalGroundAnchorA = fromA?helperA1:helperB1;
				wjd.mazeLocalGroundAnchorB = fromA?outB1:outA1;
				
				warpJoint1 = (WarpJoint) game.getWorld().createJoint(wjd);
				}
				
				{
				WarpJointDef wjd = new WarpJointDef(Maze.this);
				wjd.bodyA = original.getBody();
				wjd.bodyB = copy.getBody();
				
				wjd.mazeLocalGroundAnchorA = fromA?helperA2:helperB2;
				wjd.mazeLocalGroundAnchorB = fromA?outB2:outA2;
				
				warpJoint2 = (WarpJoint) game.getWorld().createJoint(wjd);
				}
			}
			
			public void swap() 
			{
				Vec2 temp = new Vec2();
				temp.set(original.getBody().getPosition());
				original.getBody().setTransform(copy.getBody().getPosition(), original.getBody().getAngle());
//				original.getBody().setTransform(copy.getBody().getPosition(), original.getBody().getAngle());
				copy.getBody().setTransform(temp, original.getBody().getAngle());
				
				original.getBody().synchronizeTransform();
				copy.getBody().synchronizeTransform();
				
			}

			public void remove()
			{
				copy.visible = false;
				entityPairs.remove(ID);
				originals.remove(original);
				copies.remove(copy);
				
				game.removeEntity(copy);
				
//				original.airResistance = prevAirResistance;
//				original.rotationFriction = prevRotationFriction;
			}
			
			public void syncPositions()
			{
				
				// Forces from contacts aren't stored in m_force -_- so this attempt didnt work
//				Vec2 syncedForce = copy.getBody().m_force.add(original.getBody().m_force);
//				copy.getBody().m_force.set(syncedForce);
//				original.getBody().m_force.set(syncedForce);
//				copy.getBody().m_force.setZero();
//				
//				float syncedTorque = copy.getBody().m_torque + original.getBody().m_torque;
//				copy.getBody().m_torque = syncedTorque;
//				original.getBody().m_torque = syncedTorque;
//				copy.getBody().m_torque = 0;
//				
////				copy.getBody().m_linearVelocity.set(original.getBody().getLinearVelocity());
////				copy.getBody().m_angularVelocity = original.getBody().getAngularVelocity();
//		
////				
//				/////////////////
				Vec2 pos = original.getBody().getPosition();
				double angle = body.getAngle();
				if(vertical) angle += Math.PI/2;
				Vec2 warpNormal = new Vec2((float)Math.cos(angle),(float)Math.sin(angle));
//				
				Vec2 offset = warpNormal.mul((fromA?-1:1)*(vertical?worldHeight:worldWidth));
				Vec2 newPos = pos.sub(offset);
//				
				copy.getBody().setTransform(newPos, original.getBody().getAngle());
//				copy.getBody().setTransform(copy.getBody().getPosition(), original.getBody().getAngle());
				copy.getBody().synchronizeTransform();
			}
			
//			public void drawCopies(Graphics2D g, GameWindow window)
//			{
//				
//			}
		}
		
		private class ContactQueueElement
		{
			public final boolean begin;
			public final Entity entity;
			public final boolean fromA;
			
			public ContactQueueElement(boolean begin, Entity entity, boolean fromA)
			{
				this.begin = begin;
				this.entity = entity;
				this.fromA = fromA;
			}
		}
		
	}

	public float getAngularVel() 
	{
		return angularVel;
	}

}
