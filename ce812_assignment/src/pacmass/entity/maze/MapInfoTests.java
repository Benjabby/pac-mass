package pacmass.entity.maze;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static pacmass.entity.maze.MapInfo.*;

import java.util.List;

import org.junit.Test;
/** 
 * @author Ben Tilbury
 */
public class MapInfoTests 
{
	@Test
	public void MapFixTest() 
	{
		int[][] test_map = new int[][]
		{
			{NONE, NONE, NONE, WALL, WALL, WALL, NONE, WALL},
			{WALL, NONE, NONE, NONE, NONE, NONE, NONE, WALL},
			{WALL, NONE, NONE, NONE, PLAYER, NONE, NONE, WALL},
			{WALL, NONE, NONE, NONE, WALL, WALL, NONE, WALL},
			{WALL, NONE, NONE, NONE, WALL, NONE, NONE, WALL},
			{NONE, WALL, NONE, NONE, WALL, WALL, NONE, WALL},
		};
		MapInfo map = new MapInfo(test_map,false);
		List<MapInfo.MergedRectNode> warps = map.createMergedList(MapInfo.WARP);
		assertEquals(warps.size(),2);
		List<MapInfo.MergedRectNode> walls = map.createMergedList(MapInfo.WALL);
		assertEquals(walls.size(),8);
	}
}
