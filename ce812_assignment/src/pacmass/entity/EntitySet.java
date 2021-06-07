package pacmass.entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import org.jbox2d.dynamics.World;

/** 
 * @author Ben Tilbury
 */
public class EntitySet extends HashSet<Entity> 
{
	private static final long serialVersionUID = 1921431292883795463L;

	// Tried various fancy stuff with data structures to avoid having to sort entities... nothing worked well.
//	private HashMap<Entity,Float> trueDraw;
//	private TreeMap<Integer, ArrayList<Entity>> drawQueue;
//	private List<Entity> flattenedList;
//	private TreeMap<Integer, ArrayList<Entity>> updateQueue;
	
	private List<Entity> drawOrder;
	private List<Entity> updateOrder;
	
	private Set<Entity> pendRemoves;
	private Set<Entity> pendAdds;
	
	private final World world;
	
	private LinkedHashSet<Entity.Event> events;
	private boolean eventPause;
	
	public EntitySet(World world) 
	{
		super();
		this.world = world;
		
		this.drawOrder = new CopyOnWriteArrayList<Entity>();
		this.updateOrder = new CopyOnWriteArrayList<Entity>();
		
		this.pendRemoves = new HashSet<Entity>();
		this.pendAdds = new HashSet<Entity>();
		
		this.events = new LinkedHashSet<Entity.Event>();
	}


	public void refreshDrawPriority()
	{
		drawOrder.sort(Entity.drawOrder);
	}
	
	public void refreshUpdatePriority()
	{
		updateOrder.sort(Entity.updateOrder);
	}
	
	public boolean add(Entity e) 
	{
		boolean result = super.add(e);
		if(result)
		{
			if(e.body!=null && e.body.getUserData()==null) e.body.setUserData(e);
			
			drawOrder.add(e);
			updateOrder.add(e);
			
			drawOrder.sort(Entity.drawOrder);
			updateOrder.sort(Entity.updateOrder);
		}
		return result;
	}
	
	private boolean addPartial(Entity e) 
	{
		boolean result = super.add(e);
		if(result)
		{
			if(e.body!=null && e.body.getUserData()==null) e.body.setUserData(e);
			
			drawOrder.add(e);
			updateOrder.add(e);
		}
		return result;
	}


	@Override
	public boolean addAll(Collection<? extends Entity> c) 
	{
		boolean result = false;
		for (Entity e : c)
		{
			if (addPartial(e)) result = true;
		}
		drawOrder.sort(Entity.drawOrder);
		updateOrder.sort(Entity.updateOrder);
		return result;
	}
	
	
	@Override
	public boolean remove(Object o)
	{
		boolean result =  super.remove(o);
		if(result)
		{
			drawOrder.remove(o);
			updateOrder.remove(o);
			Entity e = (Entity)o;
			world.destroyBody(e.body);
		}
		return result;
	}

	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean result =  super.removeAll(c);
		if(result)
		{
			drawOrder.removeAll(c);
			updateOrder.removeAll(c);
		}
		return result;
	}
	
	public void burn()
	{
		for(Entity e : updateOrder)
		{
			if(e.body!=null) world.destroyBody(e.body);
			e.destroy();
		}
		super.clear();
		updateOrder.clear();
		drawOrder.clear();
		pendRemoves.clear();
		pendAdds.clear();
		events.clear();
	}
	
	public void executePending()
	{
		if(!pendAdds.isEmpty())
		{
			addAll(pendAdds);
			pendAdds.clear();
		}
		
		if(!pendRemoves.isEmpty())
		{
			removeAll(pendRemoves);
			pendRemoves.clear();
		}
	}
	
	public void queueEvent(Entity.Event eventType)
	{
		if(!eventPause) events.add(eventType);
	}
	

	public void broadcastEvents() 
	{
		Iterator<Entity.Event> it = events.iterator();
		Entity.Event event;
		while(it.hasNext())
		{
			event = it.next();
			for(Entity e : getUpdateOrder()) e.receiveEvent(event);
		}
		
		events.clear();
		eventPause = false;
	}
	
	// KLUDGE
	public void pauseEvents() 
	{
		eventPause=true;
		events.clear();
	}
	
	public void pendAdd(Entity entity)
	{
		pendAdds.add(entity);
	}
	
	public void pendAddAll(Collection<? extends Entity> entities)
	{
		pendAdds.addAll(entities);
	}
	
	public void pendRemove(Entity entity)
	{
		pendRemoves.add(entity);
	}
	
	public void pendRemoveAll(Collection<? extends Entity> entities)
	{
		pendRemoves.addAll(entities);
	}

	public List<Entity> getDrawOrder()
	{
		return Collections.unmodifiableList(Collections.synchronizedList(drawOrder));
	}
	
	public List<Entity> getUpdateOrder()
	{
		return Collections.unmodifiableList(updateOrder);
	}


	
	
}
