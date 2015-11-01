package cc2.g3;

import cc2.sim.Point;
import cc2.sim.Shape;
import cc2.sim.Dough;
import cc2.sim.Move;

import java.util.*;

public class Player implements cc2.sim.Player {

    private static final int SIDE = 50;

    private boolean[] row_2 = new boolean [0];

    private Random gen = new Random();

    private Dough opponent, self;
    //    private Dough padded;

    public Player() {
	opponent = new Dough(SIDE);
	self = new Dough(SIDE);
	//	padded = new Dough(SIDE);
    }

    public Shape cutter(int length, Shape[] shapes, Shape[] opponent_shapes)
    {
	// check if first try of given cutter length
	Point[] cutter = new Point [length];
	if (row_2.length != cutter.length - 1) {
	    // save cutter length to check for retries
	    row_2 = new boolean [cutter.length - 1];
	    for (int i = 0 ; i != cutter.length ; ++i)
		cutter[i] = new Point(i, 0);
	} else {
	    // pick a random cell from 2nd row but not same
	    int i;
	    do {
		i = gen.nextInt(cutter.length - 1);
	    } while (row_2[i]);
	    row_2[i] = true;
	    cutter[cutter.length - 1] = new Point(i, 1);
	    for (i = 0 ; i != cutter.length - 1 ; ++i)
		cutter[i] = new Point(i, 0);
	}
	return new Shape(cutter);
    }

    private int getMinWidth(Shape cutter) {
	int minI = Integer.MAX_VALUE;
	int minJ = Integer.MAX_VALUE;
	int maxI = Integer.MIN_VALUE;
	int maxJ = Integer.MIN_VALUE;
	Iterator<Point> pointsInShape = cutter.iterator();
	while (pointsInShape.hasNext()) {
	    Point p = pointsInShape.next();
	    minI = Math.min(minI, p.i);
	    maxI = Math.max(maxI, p.i);
	    minJ = Math.min(minJ, p.j);
	    maxJ = Math.max(maxJ, p.j);
	}
	return Math.min( maxJ-minJ, maxI-minI)+1;
    }

    // function that will be called multiple times in real_cut with different parameters. set searchDough to opponent for behavior from last submission
    public Move find_cut(Dough dough, Dough searchDough, Shape[] shapes, Shape[] opponent_shapes, int maxCutterIndex) { 
	ArrayList <ComparableMove> moves = new ArrayList <ComparableMove> ();
	for (int i = 0 ; i != searchDough.side() ; ++i)
	    for (int j = 0 ; j != searchDough.side() ; ++j) {
		Point p = new Point(i, j);
		for (int si = 0 ; si <= maxCutterIndex ; ++si) {
		    if (shapes[si] == null) continue;
		    Shape[] rotations = shapes[si].rotations();
		    for (int ri = 0 ; ri != rotations.length ; ++ri) {
			Shape s = rotations[ri];
			if (dough.cuts(s,p) && searchDough.cuts(s,p)) {
			    moves.add(new ComparableMove(new Move(si, ri, p), touched_edges(s,p,searchDough), s.size()));
			}
		    }
		}
	    }
	if (moves.size() >= 1) {
	    Collections.sort(moves);
	    //System.out.println(moves.get(moves.size() - 1).key);
	    return moves.get(moves.size() - 1).move;
	}
	else {
	    return null;
	}
    }
    
    // computes the cut to be made
    public Move real_cut(Dough dough, Shape[] shapes, Shape[] opponent_shapes) {
	// prune larger shapes if initial move	
	if (dough.uncut()) {
	    int min = Integer.MAX_VALUE;
	    for (Shape s : shapes)
		if (min > s.size())
		    min = s.size();
	    for (int s = 0 ; s != shapes.length ; ++s)
		if (shapes[s].size() != min)
		    shapes[s] = null;
	}
	int minWidth = getMinWidth(opponent_shapes[0]);	
	Move A = find_cut(dough, createPaddedBoard(dough, minWidth, minWidth, true, true), shapes, opponent_shapes, 0);
	//Move A = find_cut(dough, opponent, shapes, opponent_shapes, 0);
	if (A != null) {
	    System.out.println("Move A");
	    return A;
	}
	else {
	    Move B1 = find_cut(dough, createPaddedBoard(dough, minWidth / 2 + 1, minWidth, false, true), shapes, opponent_shapes, 0);
	    Move B2 = find_cut(dough, createPaddedBoard(dough, minWidth / 2 + 1, minWidth, true, false), shapes, opponent_shapes, 0);
	    if (B1 != null) {
		System.out.println("Move B1");
		return B1;
	    }
	    else if (B2 != null) {
		System.out.println("Move B2");
		return B2;
	    }
	    else {
		System.out.println("Move C");
		return find_cut(dough, opponent, shapes, opponent_shapes, 2);
	    }
	}
    }

    private Dough createPaddedBoard(Dough dough, int width, int minWidth, boolean vertical, boolean horizontal) {
	Dough padded = new Dough(SIDE);
	for (int i=0; i<SIDE; i++) {
	    for (int j=0; j<SIDE; j++) {
		if (!dough.uncut(i,j)) {
		    cutPadding(padded, i,j,width,vertical, horizontal);
		}
	    }
	}
	cutBorder(padded, minWidth);
	return padded;
    }

    private void cutPadding(Dough padded, int i, int j, int minWidth, boolean vertical, boolean horizontal) {
	int x,y;
	if (!horizontal) {
	    x = i;
	}
	else {
	    for (x = Math.max(0,i-minWidth+1); x<Math.min(SIDE-1,i+minWidth-1); x++) {
		if (!vertical) {
		    y = j;
		    padded.cut(new Shape(new Point[] {new Point(0,0)}), new Point(x,y));
		}
		else {
		    for (y=Math.max(0,j-minWidth+1); y<Math.min(SIDE-1,j+minWidth-1); y++) {
			padded.cut(new Shape(new Point[] {new Point(0, 0)}), new Point(x, y));
		    }
		}
	    }
	}
    }

    private void cutBorder(Dough padded, int minWidth) {
	for (int i=0; i<SIDE; i++) {cutPadding(padded, i,0,minWidth,true,true);}
	for (int i=0; i<SIDE; i++) {cutPadding(padded, i,SIDE-1,minWidth,true,true);}
	for (int j=0; j<SIDE; j++) {cutPadding(padded, 0,j,minWidth,true,true);}
	for (int j=0; j<SIDE; j++) {cutPadding(padded, SIDE-1,j,minWidth,true,true);}
    }

    // function called by simulator
    public Move cut(Dough dough, Shape[] shapes, Shape[] opponent_shapes)
    {
	// Get cut done by opponent
	for (int i = 0; i < SIDE; i++) {
	    for (int j = 0; j < SIDE; j++) {
		if (!dough.uncut(i, j) && opponent.uncut(i, j) && self.uncut(i, j)) {
		    opponent.cut(new Shape(new Point[] {new Point(0, 0)}), new Point(i, j));
		}
	    }
	}
	Move move = real_cut(dough, shapes, opponent_shapes);
	// Get cut done by ourselves
	if (move != null) 
	    self.cut(shapes[move.shape].rotations()[move.rotation], move.point);
	return move;
    }

    private long touched_edges(Shape s, Point p, Dough d) {
	long sum = 0;
	for (Point q : s) {
	    if (cut(d, p.i + q.i + 1, p.j + q.j)) sum += 1;
	    if (cut(d, p.i + q.i - 1, p.j + q.j)) sum += 1;
	    if (cut(d, p.i + q.i, p.j + q.j + 1)) sum += 1;
	    if (cut(d, p.i + q.i, p.j + q.j - 1)) sum += 1;
	}
	return sum;
    }
    
    private boolean cut(Dough d, int i, int j) {
	return  i >= 0 && i < d.side() && j >= 0 && j < d.side() && !d.uncut(i, j);
    }

    private class ComparableMove implements Comparable<ComparableMove> {

	public Move move;
	public long key1;
	public long key2;
	public int randomized;

	public ComparableMove(Move move, long key1, long key2) {
	    this.move = move;
	    this.key1 = key1;
	    this.key2 = key2;
	    this.randomized = gen.nextInt();
	}

	@Override
	public int compareTo(ComparableMove o) {
	    int c = Long.compare(this.key2, o.key2);
	    if (c != 0) {
		return c;
	    }
	    c = Long.compare(this.key1, o.key1);
	    if (c != 0) {
		return c;
	    }
	    return Integer.compare(this.randomized, o.randomized);
	}
    }
}
