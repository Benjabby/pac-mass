package pacmass.render;

import static org.junit.Assert.*;

//import java.util.ArrayList;
//import java.util.List;

import org.junit.Test;

import pacmass.engine.Game;
/** 
 * @author Ben Tilbury
 */
public class RenderTests {

	@Test
	public void windowScalingTests() 
	{
		final Game game = new Game();
		final GameWindow window = new GameWindow(game);
		
		
		assertEquals("Screen to World to Screen Y", 252, window.worldYtoScreenY(window.screenYtoWorldY(252)));
		assertEquals("Screen to World to Screen X", 252, window.worldXtoScreenX(window.screenXtoWorldX(252)));
		assertEquals("World to Screen to World Y", -1f, window.screenYtoWorldY(window.worldYtoScreenY(-1f)),1e-2);
		assertEquals("World to Screen to World X", -1f, window.screenXtoWorldX(window.worldXtoScreenX(-1f)),1e-2);
		
	}

}
