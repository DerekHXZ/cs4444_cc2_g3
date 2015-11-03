package cc2.g3;

import cc2.sim.Point;
import cc2.sim.Shape;

public class ourDough
{
	// the array
	public boolean[][] dough;

	// number of cuts
	public int n_cuts;
	
	//create new dough
	public ourDough (int height, int width)
	{
		if (width <= 0 || height <= 0)
			throw new IllegalArgumentException();
		dough = new boolean [height][width];
		n_cuts = 0;
	}
	
	// return width
	public int width()
	{
		return dough.length;
	}
	
	//return height
	public int height()
	{
		return dough[0].length;
	}
	
	// check if specific position can be cut
		public boolean uncut(int i, int j)
		{
			return i >= 0 && i < dough.length &&
			       j >= 0 && j < dough[i].length &&
			       dough[i][j] == false;
		}

		// check if shape can cut dough
		public boolean cuts(Shape shape, Point q)
		{
			for (Point p : shape)
				if (!uncut(p.i + q.i, p.j + q.j))
					return false;
			return true;
		}

		// perform cut using shape (simulator calls this)
		public boolean cut(Shape shape, Point q)
		{
			if (!cuts(shape, q))
				return false;
			for (Point p : shape) {
				dough[p.i + q.i][p.j + q.j] = true;
				n_cuts++;
			}
			return true;
		}
}
