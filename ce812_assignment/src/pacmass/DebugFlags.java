package pacmass;

/**
 * I encourage you to try these out
 * @author Ben Tilbury
 *
 */
public final class DebugFlags 
{
	private DebugFlags() {}
	
	/** Loads the warp demo map showing the warp pulleys */
	public static final boolean WARP_DEMO_MAP = false;
	
	/** Randomly colours maze sub-shapes to see how they are divided / merged*/
	public static final boolean DRAW_MAZE_SUBSHAPES = false;

	/** Draws the pulleys that help in the */
	public static final boolean DRAW_WARP_PULLEYS = false;
	
	/** Draws the sensors that make up warp points*/
	public static final boolean DRAW_WARP_POINTS = false;
	
	/** Draws the extra blocks put outside of warps to help guide entity movement */
	public static final boolean DRAW_WARP_HELPERS = false;
	
	/** Does not draw over the outside of the map so that entities part-way inside warps are visible */
	public static final boolean HIDE_VOID_COVER = false;
	
	/** Experimental method that shows off less physics but plays better. Instead of rotating the maze, each entity's gravity vector is rotated */
	public static final boolean ROTATE_GRAV_METHOD = false;
	
	/** Draws the two collision radii (body-particle and particle-particle) of particles  */
	public static final boolean SHOW_PELLET_COLLISION_RINGS = false;
	
	/** Adds more than one pellet in each pellet space. Looks cool. */
	public static final boolean PELLET_OVERDRIVE = false;
	
	/** Lets you skip levels by pressing 'N' */
	public static final boolean ALLOW_SKIPPING = true;
	
	/** Doesn't cause an event to trigger when the number of pellets equals zero*/
	public static final boolean IGNORE_PELLET_COMPLETION = false;
	/** Ignore the assertion that a map/maze must contain a player entity. For testing properties that don't involve the player*/
	public static final boolean IGNORE_NO_PLAYER = false;
	
	// No longer relevant
	public static final boolean DRAW_CIRCLE_LINES = false;
	
}
