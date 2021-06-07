package pacmass.unused;

import java.util.LinkedList;
import java.util.List;

import pacmass.Utils;
import pacmass.engine.Game;
@Deprecated
public class MazeMap 
{
//	static final byte O = 0;
//	static final byte B = 1;
//	static final byte P

	
	static final boolean[][] MAP_TEST =
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
	
	static final boolean[][] MAP_TEST2 =
		{
				{false,	true,	true,	true,	false,	true,	true,	true,	false,	false,	false,	true,	true,	true,	true,	true,	true,	true,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{false,	false,	true,	true,	false,	true,	true,	true,	false,	false,	false,	true,	true,	true,	true,	true,	true,	true,	true},
		};
	
	static final boolean[][] MAP_TEST3 = 
		{
				{true	,true	,true	,true	,true	,true	,true	,true	,true	,false	,true	,true	,true	,true	,true	,true	,true	,true	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,true	,true	,true	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,false	,false	,false	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,true	,true	,true	,true	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,true	,false	,true	,false	,true	,false	,true	,false	,true	,true	,true	,true	,true	,true	,true	,true	,true	},
				{false	,false	,true	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,false	,false	,false	},
				{true	,true	,true	,true	,true	,true	,true	,false	,true	,false	,true	,true	,true	,true	,true	,true	,true	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,false	,false	,false	,false	,false	,false	,false	,true	,false	,true	,false	,false	,false	,false	,false	,false	,false	,true	},
				{true	,true	,true	,true	,true	,true	,true	,true	,true	,false	,true	,true	,true	,true	,true	,true	,true	,true	,true	}
		};
	
	static final boolean[][] EMPTY_MAP = 
		{
//				1		2		3		4		5		6		7		8		9		10		11		12		13		14		15		16		17		18		19
				{true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	false,	true},
				{true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true,	true},
		};
	
	public static final MazeMap MAP1 = new MazeMap(MAP_TEST, 9, 11);
	public static final MazeMap WARP_TEST = new MazeMap(MAP_TEST2, 3, 17);
	public static final MazeMap WINDING = new MazeMap(MAP_TEST3, 14, 14);
	public static final MazeMap EMPTY = new MazeMap(EMPTY_MAP,9,9);
	
	
	public static boolean[][] getContiguousArea(boolean[][] map, int pointX, int pointY)
	{
		int height =  map.length;
		int width = map[0].length;
		
		// flip y
		pointY = height-pointY-1;
		assert(!map[pointY][pointX]) : "Starting point for contiguous region must be empty (false)";
		boolean[][] contig = new boolean[height][width];
		LinkedList<MapNode> q = new LinkedList<MapNode>();
		q.add(new MapNode(pointX,pointY, map[pointY][pointX]));
		MapNode n;
		while(!q.isEmpty())
		{
			n = q.pop();
			if(!n.value)
			{
				contig[n.y][n.x] = true;
				
				int x = Utils.wrapMod(n.x-1, width);
				int y = n.y;
				if(!contig[y][x]) q.addLast(new MapNode(x,y,map[y][x]));
				
				x = Utils.wrapMod(n.x+1, width);
				if(!contig[y][x]) q.addLast(new MapNode(x,y,map[y][x]));
				
				x = n.x;
				y = Utils.wrapMod(n.y-1, height);
				if(!contig[y][x]) q.addLast(new MapNode(x,y,map[y][x]));
				
				y = Utils.wrapMod(n.y+1, height);
				if(!contig[y][x]) q.addLast(new MapNode(x,y,map[y][x]));
			}
		}
		return contig;
	}
	
	public static void printMap(boolean[][] map)
	{
		int height =  map.length;
		int width = map[0].length;
		for(int i = height-1; i >= 0; i--)
		{
			for(int j = 0; j < width; j++)
			{
				System.out.printf("%2d ", map[i][j]?1:0);
			}
	      System.out.println();
	    }
		System.out.println();
	}
	
	private final boolean[][] wallMap;
	private final boolean[][] voidMap;
	private final boolean[][] playMap;
	
	private final int startX, startY;
	protected final int height, width;
	protected final int centreX, centreY;
	protected final List<Warp> warps;
	
	
	public MazeMap(boolean[][] layout, int startX, int startY)
	{
		this.wallMap = layout;
		this.startX = startX;
		this.startY = startY;
		this.height =  layout.length;
		this.width = layout[0].length;
		
		assert(width%2==1) : "Map width must be odd";
		this.centreX = (width-1)/2;
		assert(height%2==1) : "Map height must be odd";
		this.centreY = (height-1)/2;
		
		this.voidMap = new boolean[height][width];
		
		this.warps = new LinkedList<Warp>();
		
		this.playMap = getContiguousArea(wallMap,startX,startY);
		
		//printMap(layout);
		validateMap();
		//printMap(layout);
	}
	
	static class MapNode
	{
		public final int x, y;
		public final boolean value;
		public MapNode(int x, int y, boolean value)
		{
			this.x = x;
			this.y = y;
			this.value = value;
		}
	}
	
//	static class WallRect
//	{
//		public final int x, y, width, height;
//		public WallRect(int x, int y, int width, int height)
//		{
//			this.x = x;
//			this.y = y;
//			this.width = width;
//			this.height = height;
//		}
//	}
//	
	public class Warp
	{
		public final int aX, aY, bX, bY, length;
		public final boolean vertical;
		public Warp(int aX, int aY, int bX, int bY, boolean vertical, int length) 
		{
			this.length = length;
			this.aX = aX;
//			this.aY = aY;
			this.aY = height-aY-1;
//			this.bX = bX;
//			this.bY = height-bY-1;
			this.bX = (vertical?aX:(2*(centreX)-aX));
			this.bY = height-1-(vertical?(2*(centreY)-aY):aY);
			this.vertical = vertical;
		}
	}
	
//	public class 

	private void validateMap()
	{	
		boolean[] warpHorizontal= new boolean[height];
		for(int i = 1; i < height-1; i++)
		{
			// if one open-edge is in the play area but the other is not, fill it
			if(playMap[i][0]^playMap[i][width-1])
			{
				wallMap[i][0] = true;
				wallMap[i][width-1] = true;

				playMap[i][0] = false;
				playMap[i][width-1] = false;
			}
			else if(playMap[i][0] && playMap[i][width-1])
			{
				warpHorizontal[i] = true;
				//warps.add(new Warp(0,i,width-1,i,false));
			}
		}
		
		boolean[] warpVertical = new boolean[width];
		for(int j = 1; j < width-1; j++)
		{
			// if one open-edge is in the play area but the other is not, fill it
			if(playMap[0][j]^playMap[height-1][j])
			{
				wallMap[0][j] = true;
				wallMap[height-1][j] = true;
				
				playMap[0][j] = true;
				playMap[height-1][j] = true;
			}
			else if(playMap[0][j] && playMap[height-1][j])
			{
				warpVertical[j] = true;
				//warps.add(new Warp(j,0,j,height-1,true));
			}
		}
		
		for(int i = 0; i < height; i++)
		{
			for(int j = 0; j < width; j++)
			{
				// If corners are part of the play-area they should be filled. Corner warps would be too messy.
				if((i%(height-1))+(j%(width-1))==0 && playMap[i][j]) // find corners
				{
					wallMap[i][j] = true;
					playMap[i][j] = false;
				}
				
				voidMap[i][j] = !(playMap[i][j] || wallMap[i][j]);
			}
		}
		
		// Merge connected warp points
		int sequence = 0;
		for(int i = 0; i < height; i++)
		{
			if(warpHorizontal[i]) sequence++;
			else if(sequence>0)
			{
				warps.add(new Warp(0,i-sequence,width-1,i-sequence,false,sequence));
				sequence = 0;
			}
		}
		
		if(sequence>0) warps.add(new Warp(0,height-sequence,width-1,height-sequence,false,sequence));
		
		sequence = 0;
		
		for(int j = 0; j < width; j++)
		{
			if(warpVertical[j]) sequence++;
			else if(sequence>0)
			{
				warps.add(new Warp(j-sequence,0,j-sequence,height-1,true,sequence));
				sequence = 0;
			}
		}
		
		if(sequence>0) warps.add(new Warp(width-sequence,0,width-sequence,height-1,true,sequence));
	}
	
	public boolean[][] emptyLike()
	{
		return new boolean[height][width];
	}
	
	public boolean wallAt(int x, int y)
	{
		return wallMap[y][x];
	}
	
	public boolean playAt(int x, int y)
	{
		return playMap[y][x];
	}
	
	public boolean voidAt(int x, int y)
	{
		return voidMap[y][x];
	}
	
}
