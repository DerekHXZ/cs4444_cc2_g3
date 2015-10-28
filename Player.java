package cc2.g3;

import cc2.sim.Point;
import cc2.sim.Shape;
import cc2.sim.Dough;
import cc2.sim.Move;

import java.util.*;

public class Player implements cc2.sim.Player {

	private boolean[] row_2 = new boolean [0];

	private Random gen = new Random();

	private static final int SIDE = 50;

	private Dough opponent, self;

	public Player() {
		opponent = new Dough(SIDE);
		self = new Dough(SIDE);
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
		// find all valid cuts
		ArrayList <Move> moves = new ArrayList <Move> ();
		for (int i = 0 ; i != dough.side() ; ++i)
			for (int j = 0 ; j != dough.side() ; ++j) {
				Point p = new Point(i, j);
				for (int si = 0 ; si != shapes.length ; ++si) {
					if (shapes[si] == null) continue;
					Shape[] rotations = shapes[si].rotations();
					for (int ri = 0 ; ri != rotations.length ; ++ri) {
						Shape s = rotations[ri];
						if (dough.cuts(s, p))
							moves.add(new Move(si, ri, p));
					}
				}
			}
		// return a cut randomly

		return moves.get(gen.nextInt(moves.size()));
	}

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
		self.cut(shapes[move.shape].rotations()[move.rotation], move.point);
		return move;
	}
}
