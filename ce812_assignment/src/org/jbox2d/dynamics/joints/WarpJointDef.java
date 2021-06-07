package org.jbox2d.dynamics.joints;

import org.jbox2d.common.Vec2;

import pacmass.entity.maze.Maze;

public class WarpJointDef extends JointDef 
{
	public Vec2 mazeLocalGroundAnchorA;
	public Vec2 mazeLocalGroundAnchorB;
	/**
	 * The local anchor point relative to body1's origin.
	 */
	public Vec2 localAnchorA;

	/**
	 * The local anchor point relative to body2's origin.
	 */
	public Vec2 localAnchorB;

	/**
	 * The body2 angle minus body1 angle in the reference state (radians).
	 */
	public float referenceAngle;

	/**
	 * The mass-spring-damper frequency in Hertz. Rotation only. Disable softness
	 * with a value of 0.
	 */
	public float frequencyHz;

	/**
	 * The damping ratio. 0 = no damping, 1 = critical damping.
	 */
	public float dampingRatio;
	
	public final float ratio;
	  
	public Maze maze;
	
	public WarpJointDef(Maze maze) 
	{
		super(JointType.WARP);
		localAnchorA = new Vec2();
		localAnchorB = new Vec2();
		ratio = -1f;
		this.maze = maze;
	}

}
