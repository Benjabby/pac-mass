package pacmass;

import java.awt.Color;
import java.awt.geom.Path2D;
import java.util.Map;
import java.util.Random;

import org.jbox2d.common.MathUtils;
import org.jbox2d.dynamics.ContactManager;
import org.jbox2d.dynamics.World;
import org.jbox2d.dynamics.contacts.ContactEdge;
/** 
 * Some non-specific utility functions
 * @author Ben Tilbury
 */
public class Utils 
{
	static Random random = new Random(System.nanoTime());
	
	public static int roundAwayFromZero(double x)
	{
		return (int) ((x > 0) ? Math.ceil(x) : Math.floor(x));
	}
	
	public static int roundTowardZero(double x)
	{
		return (int) ((x < 0) ? Math.ceil(x) : Math.floor(x));
	}
	
	public static void removeAllContacts(World world, ContactEdge edge)
	{
		if(edge==null) return;
		ContactManager cm = world.getContactManager();
		while(edge!=null)
		{
			cm.destroy(edge.contact);
			edge = edge.next;
		}
	}
	
	public static int wrapMod(int v, int size)
	{
		int mod = v%size;
		if(mod<0) return size+mod;
		else return mod;
	}
	
	public static float normalizeAngle(double theta)
	{
		return (float) (theta - MathUtils.TWOPI * Math.floor((theta + Math.PI) / MathUtils.TWOPI));
	}

	public static Color randomColour()
	{
		return new Color(random.nextInt(255),random.nextInt(255),random.nextInt(255));
	}
	
	public static Path2D.Float makeLinePath(float x1, float y1, float x2, float y2)
	{
		Path2D.Float path = new Path2D.Float();
		path.moveTo(x1,y1);
		path.lineTo(x2, y2);
		return path;
	}
	
}
