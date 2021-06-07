package pacmass.input;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
/** 
 * @author Ben Tilbury
 */
public class InputManager extends KeyAdapter
{
//	public enum Keys
//	{
//		UP,LEFT,DOWN,RIGHT
//	}
	
	
//	public static final int UP 		= 0;
//	public static final int LEFT 	= 1;
//	public static final int DOWN 	= 2;
//	public static final int RIGHT	= 3;
//	public static final int SPACE 	= 4;
	
	public static final int UP 		= 1;
	public static final int LEFT 	= 2;
	public static final int DOWN 	= 4;
	public static final int RIGHT	= 8;
	public static final int SPACE 	= 16;
	public static final int SKIP 	= 32;
	
	// Yes, using enums would alleviate the need to do stuff like this,
	// but using enums comes with a lot more complications so I'm just going to do it like this. It's only going to be used for debugging anyway
	public static final Map<Integer,String> keyNames;
	static
	{
		keyNames = new HashMap<Integer,String>();
		keyNames.put(UP, "UP");
		keyNames.put(LEFT, "LEFT");
		keyNames.put(DOWN, "DOWN");
		keyNames.put(RIGHT, "RIGHT");
		keyNames.put(SPACE, "SPACE");
		keyNames.put(SKIP, "SKIP");
	}
	
	private byte keyHeldMap;
	private byte keyTriggerPressedMap;
	private byte keyTriggerReleasedMap;
	
	@Override
	public void keyPressed(KeyEvent e) 
	{
		int keyCode = e.getKeyCode();
		switch(keyCode)
		{
			case KeyEvent.VK_UP:
				set(UP);
				break;
			case KeyEvent.VK_LEFT:
				set(LEFT);
				break;
			case KeyEvent.VK_DOWN:
				set(DOWN);
				break;
			case KeyEvent.VK_RIGHT:
				set(RIGHT);
				break;
			case KeyEvent.VK_SPACE:
				set(SPACE);
				break;
		}
		
		char keyChar = Character.toUpperCase(e.getKeyChar());
		switch(keyChar)
		{
			case 'W':
				set(UP);
				break;
			case 'A':
				set(LEFT);
				break;
			case 'S':
				set(DOWN);
				break;
			case 'D':
				set(RIGHT);
			case 'N':
				set(SKIP);
				break;
		}
	}
	
	@Override
	public void keyReleased(KeyEvent e)
	{
		int keyCode = e.getKeyCode();
		switch(keyCode)
		{
			
			case KeyEvent.VK_UP:
				unset(UP);
				break;
			case KeyEvent.VK_LEFT:
				unset(LEFT);
				break;
			case KeyEvent.VK_DOWN:
				unset(DOWN);
				break;
			case KeyEvent.VK_RIGHT:
				unset(RIGHT);
				break;
			case KeyEvent.VK_SPACE:
				unset(SPACE);
				break;
		}
		
		char keyChar = Character.toUpperCase(e.getKeyChar());
		switch(keyChar)
		{
			case 'W':
				unset(UP);
				break;
			case 'A':
				unset(LEFT);
				break;
			case 'S':
				unset(DOWN);
				break;
			case 'D':
				unset(RIGHT);
			case 'N':
				unset(SKIP);
				break;
		}
	}

	
	private void set(int key)
	{

		if(!getHeld(key)) 
		{
			keyTriggerPressedMap |= key;//1 << key;
//			System.out.println(keyNames.get(key) + ": Pressed");
		}
		
		//keyTriggerPressedMap |= (1 << key) & ~(keyHeldMap & (1 << key));
		
		keyHeldMap |= key;//1 << key;
		keyTriggerReleasedMap &= ~key;//~(1 << key);
		
		
	}
	
	private void unset(int key)
	{
		keyHeldMap &= ~key;//~(1 << key);
		keyTriggerPressedMap &= ~key;//~(1 << key);
		keyTriggerReleasedMap |= key;//1 << key;
		
//		System.out.println(keyNames.get(key) + ": Released");
	}
	
	public void resetTriggers()
	{
		keyTriggerPressedMap = 0;
		keyTriggerReleasedMap = 0;
	}
	
	public boolean getHeld(int key)
	{
		return (keyHeldMap & key) != 0;
	}
	
	public boolean getTriggered(int key)
	{
		return (keyTriggerPressedMap & key) != 0;
	}
	
	public boolean getReleased(int key)
	{
		return (keyTriggerReleasedMap & key) != 0;
	}
}
