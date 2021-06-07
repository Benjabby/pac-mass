package pacmass.unused;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import org.jbox2d.collision.shapes.PolygonShape;
import org.jbox2d.common.Transform;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.Body;
import org.jbox2d.dynamics.BodyDef;
import org.jbox2d.dynamics.BodyType;
import org.jbox2d.dynamics.FixtureDef;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.joints.RevoluteJoint;
import org.jbox2d.dynamics.joints.RevoluteJointDef;

import pacmass.engine.Game;
import pacmass.entity.Entity;
import pacmass.input.InputManager;
import pacmass.render.GameWindow;

@Deprecated
public class MazeNaivePeg extends Entity 
{
	private final boolean[][] mapTest =
		{
//				1		2		3		4		5		6		7		8		9		10		11		12		13		14		15		16		17		18		19
				{true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true},	
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	true,	false,	false,	false,	false,	false,	false,	false,	false,	true},	
				{true,	false,	true,	true,	false,	true,	true,	true,	false,	true,	false,	true,	true,	true,	false,	true,	true,	false,	true},	
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},	
				{true,	false,	true,	true,	false,	true,	false,	true,	true,	true,	true,	true,	false,	true,	false,	true,	true,	false,	true},	
				{true,	false,	false,	false,	false,	true,	false,	false,	false,	true,	false,	false,	false,	true,	false,	false,	false,	false,	true},	
				{true,	true,	true,	true,	false,	true,	true,	true,	false,	true,	false,	true,	true,	true,	false,	true,	true,	true,	true},	
				{false,	false,	false,	true,	false,	true,	false,	false,	false,	false,	false,	false,	false,	true,	false,	true,	false,	false,	false},	
				{true,	true,	true,	true,	false,	true,	false,	false,	false,	false,	false,	false,	false,	true,	false,	true,	true,	true,	true},	
				{false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false},	
				{true,	true,	true,	true,	false,	true,	false,	false,	false,	false,	false,	false,	false,	true,	false,	true,	true,	true,	true},	
				{false,	false,	false,	true,	false,	true,	false,	false,	false,	false,	false,	false,	false,	true,	false,	true,	false,	false,	false},	
				{true,	true,	true,	true,	false,	true,	true,	true,	false,	true,	false,	true,	true,	true,	false,	true,	true,	true,	true},	
				{true,	false,	false,	false,	false,	true,	false,	false,	false,	true,	false,	false,	false,	true,	false,	false,	false,	false,	true},	
				{true,	false,	true,	true,	false,	true,	false,	true,	true,	true,	true,	true,	false,	true,	false,	true,	true,	false,	true},	
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},	
				{true,	false,	true,	true,	false,	true,	true,	true,	false,	true,	false,	true,	true,	true,	false,	true,	true,	false,	true},	
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	true,	false,	false,	false,	false,	false,	false,	false,	false,	true},	
				{true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true},	
		};

	private final int l =  mapTest.length;
	//private 
	
	private final List<Path2D.Float> paths;
	
	private RevoluteJointDef 	testJointDef;
	private RevoluteJoint 		testJoint;

	private int turningState;
	
	public MazeNaivePeg(Game game) 
	{
		super(game);
		World w = game.getWorld();
		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyType.DYNAMIC;
		bodyDef.position.set(0, 0);
		bodyDef.linearVelocity.set(0, 0);
		//bodyDef.angularVelocity = 0.1f;
		
		//bodyDef.angularDamping = 0.0f;
		this.body = w.createBody(bodyDef);
		paths = new ArrayList<Path2D.Float>();
		
		for(int i = 0; i<l; i++)
		{
			for(int j = 0; j<l; j++)
			{
				if(mapTest[i][j])
				{
					float x = j-(l/2.0f);
					float y = i-(l/2.0f);
				
					Vec2[] vertices = mkSquare(x,y);
					
					paths.add(mkSquarePath(x,y));
					
					PolygonShape shape = new PolygonShape();
					shape.set(vertices, 4);
					FixtureDef fixtureDef = new FixtureDef();// This class is from Box2D
					fixtureDef.shape = shape;
					fixtureDef.density = 1;//(float) (12/((float) 4)/2f*(4*4)*Math.sin(2*Math.PI/4));
					fixtureDef.friction = 0.2f;// this is surface friction;
					fixtureDef.restitution = 0.5f;
					body.createFixture(fixtureDef);
				}
			}
		}
		
		this.personalGravity = Game.STANDARD_GRAVITY;
		
		testJointDef = new RevoluteJointDef();
		testJointDef.bodyA = body;
		testJointDef.bodyB = game.test.getBody();
		testJointDef.maxMotorTorque = 10000f;
		
//		testJoint = null;
	}

	@Override
	public void updateSelf(InputManager input) 
	{

//		boolean rightOn = input.getTriggered(InputManager.RIGHT);
//		boolean leftOn = input.getTriggered(InputManager.LEFT);
		
		if(input.getTriggered(InputManager.RIGHT))
		{
			if(turningState==0) startTurn();
			turningState = 1;
		}
		if(input.getTriggered(InputManager.LEFT))
		{
			if(turningState==0) startTurn();
			turningState = -1;
		}
		
		if(input.getReleased(InputManager.RIGHT))
		{
			if(input.getHeld(InputManager.LEFT)) turningState = -1;
			else stopTurn();
		}
		
		if(input.getReleased(InputManager.LEFT))
		{
			if(input.getHeld(InputManager.RIGHT)) turningState = 1;
			else stopTurn();
		}
		
		if(turningState!=0)
		{
			assert testJoint!=null;
			testJoint.enableMotor(true);
			testJoint.setMotorSpeed(turningState);
		}
	}
	
	private void startTurn()
	{
		testJointDef.localAnchorA = body.getLocalPoint(new Vec2(0,0));
		testJoint = (RevoluteJoint)game.getWorld().createJoint(testJointDef);
	}
	
	private void stopTurn()
	{
		game.getWorld().destroyJoint(testJoint);
		turningState = 0;
	}

	@Override
	public void drawSelf(Graphics2D g, GameWindow window) 
	{
		window.drawPathBatched(g, Color.BLUE, body.getPosition(), body.getAngle(), paths);
	}

	public static Path2D.Float mkSquarePath(float x, float y)
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
	
	public static Vec2[] mkSquare(float x, float y) 
	{
		Vec2[] values = {new Vec2(x,y),new Vec2(x+1,y),new Vec2(x+1,y+1),new Vec2(x,y+1)};
		return values;
	}

	@Override
	public Entity clone() {
		// TODO Auto-generated method stub
		return null;
	}

}
