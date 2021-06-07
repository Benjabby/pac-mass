package pacmass.unused;

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
import java.util.Set;

import org.jbox2d.callbacks.ContactImpulse;
import org.jbox2d.callbacks.ContactListener;
import org.jbox2d.collision.Manifold;
import org.jbox2d.collision.shapes.EdgeShape;
import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Vec2;
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

import pacmass.DebugFlags;
import pacmass.Utils;
import pacmass.engine.Game;
import pacmass.entity.BasicParticle;
import pacmass.entity.Entity;
import pacmass.entity.Pellets;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;
import pacmass.unused.MazeMap.Warp;
@Deprecated
public class MazeOldMethod extends Entity 
{	
	private static final float TURN_ACCELERATION = 2f;
	private static final float ANGULAR_DRAG = 0.02f;
	private static final float WALL_DENSITY = 1f;
	private static final float WALL_RESTITUTION = 0.5f;
	private static final float WALL_FRICTION = 0.2f;
	
	private static final TestContactListener testContact = new TestContactListener();
	
	private final MazeMap map;
	private final List<WarpPoint> warps;
	
	/// TODO Currently unused 
	private final float unitScale = 1f;
	private final float worldWidth;
	private final float worldHeight;
	
	public final int unitWidth;
	public final int unitHeight;
	
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
	
	public MazeOldMethod(Game game, MazeMap map) 
	{
		super(game,2, 20);
		this.map = map;
		this.angularVel = 0f;
		this.debugShapes = DebugFlags.DRAW_MAZE_SUBSHAPES ? new HashMap<Path2D.Float,Color>() : null;
		this.unitWidth = map.width;
		this.unitHeight = map.height;
		
		this.worldWidth = map.width*unitScale;
		this.worldHeight = map.height*unitScale;
		
//		this.fixtureFilter = new Filter();
//		fixtureFilter.
		// PHYSICS
		World w = game.getWorld();
		BodyDef bodyDef = new BodyDef();
//		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.type = BodyType.KINEMATIC;
		bodyDef.position.set(0, 0);
		bodyDef.linearVelocity.set(0, 0);
		//bodyDef.angularVelocity = 0.1f;
		bodyDef.angularDamping = 0.0f;
		this.body = w.createBody(bodyDef);
		
//		bodyDef = new BodyDef();
//		bodyDef.type = BodyType.STATIC;
//		bodyDef.position.set(0, 0);
//		bodyDef.linearVelocity.set(0, 0);
//		Body staticPivot = w.createBody(bodyDef);
//		
//		RevoluteJointDef rjd = new RevoluteJointDef();
//		rjd.bodyA = staticPivot;
//		rjd.bodyB = body;
//		w.createJoint(rjd);
//		
		
		
		//addWallFixturesNaive();
		initWallFixturesSmart();
		
		if(DebugFlags.DRAW_WARP_HELPERS) debugWarpSideHelpers = new ArrayList<Path2D.Float>();
		warps = new ArrayList<WarpPoint>();
		for(MazeMap.Warp warp : map.warps) warps.add(new WarpPoint(warp));
		
//		warps.add(new WarpPoint(map.new Warp(6, 9, 0, 0, false, 1)));
		
		bottomLeft = new Vec2(-unitWidth/2.0f,-unitHeight/2.0f);
		topLeft = new Vec2(-unitWidth/2.0f,unitHeight/2.0f);
		topRight = new Vec2(unitWidth/2.0f,unitHeight/2.0f);
		bottomRight = new Vec2(unitWidth/2.0f,-unitHeight/2.0f);
		
		w.setContactListener(testContact);
		// RENDER
//		this.wallPaths = makeNaiveWalls();		
//		this.playPath = new Path2D.Float();
		Path2D.Float wallPath = new Path2D.Float();
		Path2D.Float playPath = new Path2D.Float();
//		Path2D.Float voidPath = new Path2D.Float();
//		List<Path2D.Float> paths = new ArrayList<Path2D.Float>();
		
		entities = new HashSet<Entity>();

		
		for(int i = 0; i<unitHeight; i++)
		{
			for(int j = 0; j<unitWidth; j++)
			{
				float x = (j-(unitWidth/2.0f));
				float y = (i-(unitHeight/2.0f));
				if(map.wallAt(j, i)) wallPath.append(makeSquarePath(x,y),false);
				if(map.playAt(j, i)) 
				{
					playPath.append(makeSquarePath(x,y),false);
//					entities.add(new BasicParticle(game, x+0.5f, y+0.5f, 0, 0, 0.4f, Color.WHITE, 1f, 0f));
				}
//				if(map.voidAt(j, i)) voidPath.append(makeSquarePath(x,y),false);
			}
		}
//		for(int i=-1; i<=unitHeight; i++)
//		{
//			float y = (i-(unitHeight/2.0f));
//			voidPath.append(makeSquarePath(unitWidth/2.0f,y), false);
//			voidPath.append(makeSquarePath(-unitWidth/2.0f-1,y), false);
//		}
//		for(int j=0; j<unitWidth; j++)
//		{
//			float x = (j-(unitWidth/2.0f));
//			voidPath.append(makeSquarePath(x,unitHeight/2.0f), false);
//			voidPath.append(makeSquarePath(x,-unitHeight/2.0f-1), false);
//		}
		
		this.wallArea = new Area(wallPath);
		this.playArea = new Area(playPath);
		this.voidArea = new Area(makeRectPath(-unitWidth, -unitHeight, 2*unitWidth, 2*unitHeight));
		this.voidArea.subtract(wallArea);
		this.voidArea.subtract(playArea);
		
		this.bounds = makeRectPath(-unitWidth/2f, -unitHeight/2f, unitWidth, unitHeight);
		
		//entities.add(new Pellets(game,this));
		

//		System.out.print
				
		entities.add(new BasicParticle(game, 7f, -7f, 0, 0, 0.4f, Color.YELLOW, 1f, -9.8f));
//		entities.add(new BasicParticle(game, 0f, 1.1f, 0, 0, 0.4f, Color.PINK, 1f, 9.8f));
//
//		entities.add(new BasicParticle(game, 0.1f, 1.1f, 0, 0, 0.2f, Color.PINK, 1f, 9.8f));
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
	}

	
	private void initWallFixturesNaive()
	{
		for(int i = 0; i<unitHeight; i++)
		{
			for(int j = 0; j<unitWidth; j++)
			{
				if(map.wallAt(j, i))
				{
					float x = (j-(unitWidth/2.0f));
					float y = (i-(unitHeight/2.0f));
				
					if(DebugFlags.DRAW_MAZE_SUBSHAPES) debugShapes.put(makeSquarePath(x,y),Utils.randomColour());
					
					PolygonShape shape = new PolygonShape();
					Vec2[] vertices = makeSquare(x,y);
					shape.set(vertices, 4);
					FixtureDef fixtureDef = new FixtureDef();
					fixtureDef.shape = shape;
					fixtureDef.density = WALL_DENSITY;
					fixtureDef.friction = WALL_FRICTION;
					fixtureDef.restitution = WALL_RESTITUTION;
					body.createFixture(fixtureDef);
				}
			}
		}
	}
	
	
	private void initWallFixturesSmart()
	{
		int sequence = 0;
		boolean[][] singles = map.emptyLike();
		// Horizontal scan
		for(int i = 0; i<unitHeight; i++)
		{
			for(int j = 0; j<unitWidth; j++)
			{
				float x = (j-(unitWidth/2.0f));
				float y = (i-(unitHeight/2.0f));
				if(map.wallAt(j, i)) sequence++;
				
				if(!map.wallAt(j, i) || j==unitWidth-1)
				{
					int off = (map.wallAt(j, i)&&j==unitWidth-1)?0:1;
					if(sequence==1) singles[i][j-off] = true;
					else if(sequence>1)
					{
						if(DebugFlags.DRAW_MAZE_SUBSHAPES) debugShapes.put(makeRectPath(x-sequence+1-off,y,sequence,1),Utils.randomColour());
						PolygonShape shape = new PolygonShape();
						Vec2[] vertices = makeRect(x-sequence+1-off,y,sequence,1);
						shape.set(vertices, 4);
						FixtureDef fixtureDef = new FixtureDef();
						fixtureDef.shape = shape;
						fixtureDef.density = WALL_DENSITY;
						fixtureDef.friction = WALL_FRICTION;
						fixtureDef.restitution = WALL_RESTITUTION;
						body.createFixture(fixtureDef);
						
					}
					
					sequence = 0;
				}
			}
		}

		// Vertical scan
		for(int j = 0; j<unitWidth; j++)
		{
			for(int i = 0; i<unitHeight; i++)
			{
				float x = (j-(unitWidth/2.0f));
				float y = (i-(unitHeight/2.0f));
				if(singles[i][j]) sequence++;
				
				if(!singles[i][j] || i==unitHeight-1)
				{
					int off = (map.wallAt(j, i)&&i==unitHeight-1)?0:1;
					if(sequence==1)
					{
						singles[i-off][j] = true;
						if(DebugFlags.DRAW_MAZE_SUBSHAPES) debugShapes.put(makeSquarePath(x,y-off),Utils.randomColour());
						
						PolygonShape shape = new PolygonShape();
						Vec2[] vertices = makeSquare(x,y-off);
						shape.set(vertices, 4);
						FixtureDef fixtureDef = new FixtureDef();
						fixtureDef.shape = shape;
						fixtureDef.density = WALL_DENSITY;
						fixtureDef.friction = WALL_FRICTION;
						fixtureDef.restitution = WALL_RESTITUTION;
						body.createFixture(fixtureDef);
					}
					else if(sequence>1)
					{
						if(DebugFlags.DRAW_MAZE_SUBSHAPES) debugShapes.put(makeRectPath(x,y-sequence,1,sequence),Utils.randomColour());
						PolygonShape shape = new PolygonShape();
						Vec2[] vertices = makeRect(x,y-sequence,1,sequence);
						shape.set(vertices, 4);
						FixtureDef fixtureDef = new FixtureDef();
						fixtureDef.shape = shape;
						fixtureDef.density = WALL_DENSITY;
						fixtureDef.friction = WALL_FRICTION;
						fixtureDef.restitution = WALL_RESTITUTION;
						body.createFixture(fixtureDef);
//						System.out.println(sequence);
					}
					
					sequence = 0;
				}
				
				singles[i][j] = false;
			}
		}
//		MazeMap.printMap(singles);
		
		assert(debugShapes.size() == body.m_fixtureCount);

	}
	
	public float getAngle()
	{
		return body.getAngle();
	}
	
	@Override
	public void updateSelf(InputManager input) 
	{
//		boolean rightOn = input.getTriggered(InputManager.RIGHT);
//		boolean leftOn = input.getTriggered(InputManager.LEFT);
		
		if(input.getTriggered(InputManager.RIGHT)) angularAcc = 1;
		if(input.getTriggered(InputManager.LEFT)) angularAcc = -1;
		
		if(input.getReleased(InputManager.RIGHT))
		{
			if(input.getHeld(InputManager.LEFT)) angularAcc = -1;
			else angularAcc = 0;
		}
		
		if(input.getReleased(InputManager.LEFT))
		{
			if(input.getHeld(InputManager.RIGHT)) angularAcc = 1;
			else angularAcc = 0;
		}
		
		this.angularVel += -angularAcc*Game.DELTA_T*TURN_ACCELERATION;
		this.angularVel -= angularVel*ANGULAR_DRAG;
		body.setAngularVelocity(angularVel);
		
		for(WarpPoint warp : warps) warp.update();
	}

	@Override
	public void drawSelf(Graphics2D g, GameWindow window) 
	{
//		window.drawPath(g, null, personalGravity, angularAcc, playPath);
		window.drawPath(g, Color.DARK_GRAY, body.getPosition(), body.getAngle(), bounds);
		//window.drawArea(g, Color.DARK_GRAY, body.getPosition(), body.getAngle(), playArea);
		
		if(DebugFlags.DRAW_MAZE_SUBSHAPES) window.debugDrawPathBatched(g, body.getPosition(), body.getAngle(), debugShapes);
		else window.drawArea(g, Color.BLUE, body.getPosition(), body.getAngle(), wallArea);
		
		
//		window.drawLine(g, Color.WHITE, body.getWorldPoint(new Vec2(-0.5f,-(l/2.0f))), body.getWorldPoint(new Vec2(0.5f,-(l/2.0f))), 1.5f);		
	
		
	}



	public void test(Graphics2D g, GameWindow window) 
	{
		if(!DebugFlags.HIDE_VOID_COVER) window.drawArea(g, Color.BLACK, body.getPosition(), body.getAngle(), voidArea);;
		
		for(WarpPoint warp : warps) warp.draw(g, window);
		
		if(DebugFlags.DRAW_WARP_HELPERS) window.drawPathBatched(g, Color.WHITE, body.getPosition(), body.getAngle(), debugWarpSideHelpers);
	}
	
	
	static Path2D.Float makeSquarePath(float x, float y)
	{
		Path2D.Float path = new Path2D.Float();
		path.moveTo(x,y);
		path.lineTo(x+1, y);
		path.lineTo(x+1, y+1);
		path.lineTo(x, y+1);
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
		Vec2[] values = {new Vec2(x,y),new Vec2(x+1,y),new Vec2(x+1,y+1),new Vec2(x,y+1)};
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
		throw new RuntimeException("Mazes cannot be cloned");
	}
	
	static class TestContactListener implements ContactListener
	{
		
		boolean getValues(Contact contact, Entity[] entity, WarpPoint[] warp, Fixture[] fixture)
		{
			Fixture A = contact.m_fixtureA;
			Fixture B = contact.m_fixtureB;
			
			boolean sensorA = A.isSensor();
			boolean sensorB = B.isSensor();
			
			if(!(sensorA ^ sensorB)) return false;
			
			if(sensorA)
			{
				entity[0] = (Entity) B.m_body.getUserData();
				warp[0] = (WarpPoint) A.getUserData();
				fixture[0] = A;
			}
			else
			{
				entity[0] = (Entity) A.m_body.getUserData();
				warp[0] = (WarpPoint) B.getUserData();
				fixture[0] = B;
			}
			return true;
		}
		
		@Override
		public void beginContact(Contact contact) 
		{
			// An ugly hack for java's lack of referencing
			Entity[] entityWrap = new Entity[1];
			WarpPoint[] warpWrap = new WarpPoint[1];
			Fixture[] sensorWrap = new Fixture[1];
			
			if(getValues(contact, entityWrap, warpWrap, sensorWrap))
			{
				Entity entity = entityWrap[0];
				WarpPoint warp = warpWrap[0];
				Fixture sensor = sensorWrap[0];
				
				warp.queueContact(true, entity, sensor);
			}
		}

		@Override
		public void endContact(Contact contact)
		{
			// An ugly hack for java's lack of referencing
			Entity[] entityWrap = new Entity[1];
			WarpPoint[] warpWrap = new WarpPoint[1];
			Fixture[] sensorWrap = new Fixture[1];
			
			if(getValues(contact, entityWrap, warpWrap, sensorWrap))
			{
				Entity entity = entityWrap[0];
				WarpPoint warp = warpWrap[0];
				Fixture sensor = sensorWrap[0];

				warp.queueContact(false, entity, sensor);
			}
			
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
			// TODO Auto-generated method stub
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
		private final MazeMap.Warp mapData;
		private final Fixture sensorA, sensorB;
		// Could in future be replaced with a normal vector;
		private final boolean vertical;
		
		private Path2D.Float testA, testB, testARect, testBRect;

		private Queue<ContactQueueElement> queuedContacts;

		private Map<Integer, EntityCopyPair> entityPairs;
		// for quick looking up of whether a given entity is being managed by this warp point to avoid looping through the entity set.
		// Sidenote I tried to find a way to have a fast hashmap/other set that can access an entity pair from either the original or the copy but failed.
		private Map<Entity, Integer> originals, copies; 

		// Note this isn't good. I doubt there will ever be the amount of interactions that will overflow the int limit
		// but really this should assign to the lowest unused ID
		private int currentID = 0;
		
		Vec2 helperA1, helperB1, helperA2, helperB2, outA1, outB1, outA2, outB2;
		
		public WarpPoint(MazeMap.Warp mapData)
		{
			this.vertical = mapData.vertical;
			this.mapData = mapData;
			this.queuedContacts = new LinkedList<ContactQueueElement>();
			this.entityPairs = new HashMap<Integer,EntityCopyPair>();
			this.originals = new HashMap<Entity,Integer>();
			this.copies = new HashMap<Entity,Integer>();
			
			{
				float x = mapData.aX-(unitWidth/2.0f);
				float y = mapData.aY-(unitHeight/2.0f);
				float off = (vertical?1:0);
				// Square Sensor
				PolygonShape shape = new PolygonShape();
				shape.set(makeRect(x, y, vertical?mapData.length:1, vertical?1:mapData.length),4);
				// Line Sensor
//				EdgeShape shape = new EdgeShape();
//				shape.set(new Vec2(x,y+off), new Vec2(vertical?x+mapData.length:x, (vertical?y:y+mapData.length) + off));
				
				testARect = makeRectPath(x,y,vertical?mapData.length:1, vertical?1:mapData.length);
				testA = Utils.makeLinePath(x,y+off, vertical?x+mapData.length:x, (vertical?y:y+mapData.length) + off);
				
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0;
				fixtureDef.isSensor = true;
				this.sensorA = body.createFixture(fixtureDef);
				this.sensorA.setUserData(this);
				
				PolygonShape sideHelper = new PolygonShape();
				helperA1 = new Vec2(x-1,vertical?y+2:y-1);
				outA1 = helperA1.sub(new Vec2(vertical?0:-2,vertical?2:0));
				sideHelper.set(makeSquare(x-1,vertical?y+1:y-1), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS) debugWarpSideHelpers.add(makeSquarePath(x-1,vertical?y+1:y-1));
				
				sideHelper = new PolygonShape();
				helperA2 = new Vec2(vertical?x+mapData.length+1:x-1,vertical?y+2:y+mapData.length+1);
				outA2 = helperA2.sub(new Vec2(vertical?0:-2,vertical?2:0));
				sideHelper.set(makeSquare(vertical?x+mapData.length:x-1,vertical?y+1:y+mapData.length), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density = 0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS) debugWarpSideHelpers.add(makeSquarePath(vertical?x+mapData.length:x-1,vertical?y+1:y+mapData.length));
				
			}
			{
				float x = mapData.bX-(unitWidth/2.0f);
				float y = mapData.bY-(unitHeight/2.0f);
				float off = (vertical?0:1);
//				// Square Sensor
				PolygonShape shape = new PolygonShape();
				shape.set(makeRect(x, y, vertical?mapData.length:1, vertical?1:mapData.length),4);
				// Line Sensor
//				EdgeShape shape = new EdgeShape();
//				shape.set(new Vec2(x+off,y), new Vec2((vertical?x+mapData.length:x) + off, vertical?y:y+mapData.length));
				
				testBRect = makeRectPath(x,y,vertical?mapData.length:1, vertical?1:mapData.length);
				testB = Utils.makeLinePath(x+off,y, (vertical?x+mapData.length:x) + off, vertical?y:y+mapData.length);
				
				FixtureDef fixtureDef = new FixtureDef();
				fixtureDef.shape = shape;
				fixtureDef.density = 0;
				fixtureDef.isSensor = true;
				this.sensorB = body.createFixture(fixtureDef);
				this.sensorB.setUserData(this);
				
				PolygonShape sideHelper = new PolygonShape();
				helperB1 = new Vec2(vertical?x-1:x+2,vertical?y-1:y-1);
				outB1 = helperB1.add(new Vec2(vertical?0:-2,vertical?2:0));
				sideHelper.set(makeSquare(vertical?x-1:x+1,y-1), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS) debugWarpSideHelpers.add(makeSquarePath(vertical?x-1:x+1,y-1));
				
				sideHelper = new PolygonShape();
				helperB2 = new Vec2(vertical?x+mapData.length+1:x+2,vertical?y-1:y+mapData.length+1);
				outB2 = helperB2.add(new Vec2(vertical?0:-2,vertical?2:0));
				sideHelper.set(makeSquare(vertical?x+mapData.length:x+1,vertical?y-1:y+mapData.length), 4);
				fixtureDef = new FixtureDef();
				fixtureDef.shape = sideHelper;
				fixtureDef.density =  0;
				fixtureDef.friction = WALL_FRICTION;
				fixtureDef.restitution = WALL_RESTITUTION;
				body.createFixture(fixtureDef);
				
				if(DebugFlags.DRAW_WARP_HELPERS) debugWarpSideHelpers.add(makeSquarePath(vertical?x+mapData.length:x+1,vertical?y-1:y+mapData.length));
				
			}
			
			
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
			
			if(isBrandNew)
			{
				EntityCopyPair newPair = new EntityCopyPair(entity,fromA);
			}
		}
		
		private void endContact(Entity entity, boolean fromA)
		{
			boolean isOriginal = originals.containsKey(entity);
			boolean isCopy = copies.containsKey(entity);
			
			assert(!(isOriginal&&isCopy)) : "Entity has somehow ended up in both the original set and copy set for a WarpPoint";
			assert(isOriginal||isCopy) : "endContact called for an entity not registered as an original or copy for a WarpPoint";
			
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
					
					if(Math.abs(Vec2.dot(pos, warpNormal))>((vertical?unitHeight:unitWidth)/2.0))
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
				if(ecp!=null) ecp.syncForces();
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
			if(DebugFlags.DRAW_WARP_PULLEYS)
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
			
			private final WarpJoint warpJoint1;
			private final WarpJoint warpJoint2;
			
			private final boolean fromA;
			private int ID;
			
			public EntityCopyPair(Entity original, boolean fromA)
			{
				this.original = original;
//				this.copy = null;
				this.copy = original.clone();
				this.fromA = fromA;
				
				// Note this isn't good. I doubt there will ever be the amount of interactions that will overflow the int limit
				// but really this should assign to the lowest unused ID
				this.ID = currentID++;
				entityPairs.put(ID, this);
				originals.put(original, ID);
				copies.put(copy, ID);

				game.addEntity(copy);
//				copy.visible = false;
				
				/////////////////
				Vec2 pos = original.getBody().getPosition();
				double angle = body.getAngle();
				if(vertical) angle -= Math.PI/2;
				Vec2 warpNormal = new Vec2((float)Math.cos(angle),(float)Math.sin(angle));
				Vec2 offset = warpNormal.mul((fromA?-1:1)*(vertical?unitHeight:unitWidth));
				Vec2 newPos = pos.sub(offset);
				
				copy.getBody().setTransform(newPos, original.getBody().getAngle());
				Fixture f = original.getBody().getFixtureList();
				while(f!=null)
				{
					f.setDensity(0);
//					f.setSensor(true);
					f = f.getNext();
				}
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
				WarpJointDef wjd = new WarpJointDef(MazeOldMethod.this);
				wjd.bodyA = original.getBody();
				wjd.bodyB = copy.getBody();
			
				wjd.mazeLocalGroundAnchorA = fromA?helperA1:helperB1;
				wjd.mazeLocalGroundAnchorB = fromA?outB1:outA1;
				
				warpJoint1 = (WarpJoint) game.getWorld().createJoint(wjd);
				}
				{
				WarpJointDef wjd = new WarpJointDef(MazeOldMethod.this);
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
				
				if(DebugFlags.DRAG_ENTITIES)
				{
					//Remove the mouse joint
					JointEdge j = original.getBody().getJointList();
					while(j!=null)
					{
						if(j.joint.getType()==JointType.MOUSE) game.getWorld().destroyJoint(j.joint);
						j = j.next;
					}
				}
			}

			public void remove()
			{
				copy.visible = false;
				entityPairs.remove(ID);
				originals.remove(original);
				copies.remove(copy);
				
				game.removeEntity(copy);
			}
			
			public void syncForces()
			{
				
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
//				Vec2 pos = original.getBody().getPosition();
//				double angle = body.getAngle();
//				if(vertical) angle -= Math.PI/2;
//				Vec2 warpNormal = new Vec2((float)Math.cos(angle),(float)Math.sin(angle));
//				
//				Vec2 offset = warpNormal.mul((fromA?-1:1)*(vertical?unitHeight:unitWidth));
//				Vec2 newPos = pos.sub(offset);
//				
//				copy.getBody().setTransform(newPos, original.getBody().getAngle());
				copy.getBody().setTransform(copy.getBody().getPosition(), original.getBody().getAngle());
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

}
