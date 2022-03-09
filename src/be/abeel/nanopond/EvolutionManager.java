package be.abeel.nanopond;

import java.util.Observable;

public class EvolutionManager extends Observable implements Runnable {

	/**
	 * Constructor of the world : fill all genomes with 512 STOP's
	 */
	public EvolutionManager(Reporter report) {
		this.report=report;
		for (int i = 0; i < POND_DEPTH; i++)
			startBuffer[i] = (byte) 15;
		for (int i = 0; i < POND_SIZE_X; i++) {
			for (int j = 0; j < POND_SIZE_Y; j++) {
				pond[i][j] = new Cell();
			}
		}
	
	}

	/* All available instructions */
	String[] names = { "ZERO", "FWD", "BACK", "INC", "DEC", "READG", "WRITEG", "READB", "WRITEB", "LOOP", "REP",
			"TURN", "XCHG", "KILL", "SHARE", "STOP" };

	/*
	 * Frequency of comprehensive reports-- lower values will provide more info
	 * while slowing down the simulation. Higher values will give less frequent
	 * updates. This is also the frequency of screen refreshes if SDL is
	 * enabled.
	 */
	public static final int REPORT_FREQUENCY = 100000;

	/*
	 * How frequently should random cells / energy be introduced? Making this
	 * too high makes things very chaotic. Making it too low might not introduce
	 * enough energy.
	 */

	public static final int INFLOW_FREQUENCY = 100;

	/* Base amount of energy to introduce per INFLOW_FREQUENCY ticks */
	public static final int INFLOW_RATE_BASE = 4000;

	/*
	 * A random amount of energy between 0 and this is added to INFLOW_RATE_BASE
	 * when energy is introduced. Comment this out for no variation in inflow
	 * rate.
	 */
	public static final int INFLOW_RATE_VARIATION = 8000;

	public static final int POND_SIZE_X = 640;

	public static final int POND_SIZE_Y = 480;

	/*
	 * Maximum genome size.
	 */
	public static final int POND_DEPTH = 512;

	/*
	 * This is the divisor that determines how much energy is taken from cells
	 * when they try to KILL a viable cell neighbor and fail. Higher numbers
	 * mean lower penalties.
	 */
	public static final int FAILED_KILL_PENALTY = 2;

	byte[] startBuffer = new byte[POND_DEPTH];

	enum Direction {
		LEFT, RIGHT, UP, DOWN;

		public static Direction getDirection(int i) {
			switch (i) {
			case 0:
				return LEFT;
			case 1:
				return RIGHT;
			case 2:
				return UP;
			case 3:
				return DOWN;
			default:
				throw new RuntimeException("Unknown direction requested: " + i);
			}
		}
	}

	class Cell {
		public Cell() {
			this.ID = 0;
			this.parentID = 0;
			this.lineage = 0;
			this.generation = 0;
			this.energy = 0;
			this.genome = new byte[POND_DEPTH];
			System.arraycopy(startBuffer, 0, genome, 0, POND_DEPTH);
		}

		long generation;

		long ID;

		long parentID;

		long lineage;

		int energy;

		byte[] genome;

		/* Fill genome with random instruction */
		public void setRandomGenome() {
			for (int i = 0; i < POND_DEPTH; i++)
				genome[i] = (byte) rg.nextInt(16);
		}

	}

	/* The pond is a 2D array of cells */
	Cell[][] pond = new Cell[POND_SIZE_X][POND_SIZE_Y];

	MTRandom rg = new MTRandom();

	/**
	 * Class for keeping some running tally type statistics
	 */
	class PerReportStatCounters {
		long[] instructionExecutions = new long[16];

		long cellExecutions = 0;

		long viableCellsReplaced = 0;

		long viableCellsKilled = 0;

		long viableCellShares = 0;

		public void reset() {
			instructionExecutions = new long[16];
			cellExecutions = 0;
			viableCellsReplaced = 0;
			viableCellsKilled = 0;
			viableCellShares = 0;
		}
	}

	/* Global statistics counters */
	PerReportStatCounters statCounters = new PerReportStatCounters();
 final Reporter report;
	boolean replicatorMessage = false;

	public void doReport(final long clock) {

		new Thread(new Runnable() {

			public void run() {
				report.year = clock;
				long totalActiveCells = 0;
				long totalEnergy = 0;
				long totalViableReplicators = 0;
				long maxGeneration = 0;
				for (int i = 0; i < POND_SIZE_X; i++) {
					for (int j = 0; j < POND_SIZE_Y; j++) {
						Cell c = pond[i][j];
						if (c.energy > 0) {
							totalActiveCells++;
							totalEnergy += c.energy;
							if (c.generation > 2) {
								totalViableReplicators++;

							}

							if (c.generation > maxGeneration)
								maxGeneration = c.generation;
						}
					}
				}
				if (maxGeneration > 2 && !replicatorMessage) {
					replicatorMessage = true;
					System.out.println("[EVENT] Replicators have evolved in the year " + clock);
				}
				if (maxGeneration <= 2 && replicatorMessage) {
					replicatorMessage = false;
					System.out.println("[EVENT] Replicators have gone extinct in the year " + clock);
				}
				report.energy = totalEnergy;
				report.maxGeneration = maxGeneration;
				report.activeCells = totalActiveCells;
				report.viableReplicators = totalViableReplicators;
				report.kills = statCounters.viableCellsKilled;
				report.replaced = statCounters.viableCellsReplaced;
				report.shares = statCounters.viableCellShares;
				report.update();
				statCounters.reset();
			}

		}).start();

	}

	/**
	 * Get a neighbor in the pond
	 * 
	 * @param x
	 *            Starting X position
	 * @param y
	 *            Starting Y position
	 * @param dir
	 *            Direction to get neighbor from
	 * @return neighboring cell
	 */
	public Cell getNeighbor(int x, int y, Direction dir) {
		switch (dir) {
		case LEFT:
			return x != 0 ? pond[x - 1][y] : pond[POND_SIZE_X - 1][y];
		case RIGHT:
			return (x < (POND_SIZE_X - 1)) ? pond[x + 1][y] : pond[0][y];
		case UP:
			return y != 0 ? pond[x][y - 1] : pond[x][POND_SIZE_Y - 1];
		case DOWN:
			return (y < (POND_SIZE_Y - 1)) ? pond[x][y + 1] : pond[x][0];
		default:
			throw new RuntimeException("Unknown direction!");
		}

	}

	int[] BITS = { 0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4 };

	/**
	 * Determines whether neighbor cell is accessable by the cell with the
	 * register value.
	 * 
	 * @param reg
	 *            register value of the attacking cell
	 * @param positiveInteraction
	 *            kill & replace : false / share : true
	 * @param neighbor
	 *            the neighboring cell
	 * @return true if access to the neighbor is allowed, false in other cases
	 */
	private boolean accessAllowed(Cell neighbor, byte reg, boolean positiveInteraction) {
		if (neighbor.parentID == 0)
			return true;
		if (positiveInteraction)
			return BITS[neighbor.genome[0] ^ reg] <= rg.nextInt(4);
		else
			return BITS[neighbor.genome[0] ^ reg] >= rg.nextInt(4);

	}

	//static Reporter report;

	public void run() {
		/* Buffer used for execution output of candidate offspring */
		byte[] outputBuf = new byte[POND_DEPTH];
		System.arraycopy(startBuffer, 0, outputBuf, 0, POND_DEPTH);

		/* Reset per-report stat counters */
		statCounters.reset();

		/* Clock is incremented on each core loop */
		long clock = 0;

		/* This is used to generate unique cell IDs */
		long cellIdCounter = 0;

		/* The main "register" */
		byte reg = 0;

		/* Virtual machine loop/rep stack */
		int loopStackPointer[] = new int[POND_DEPTH];
		int loopStackPtr = 0;

		/*
		 * If this is nonzero, we're skipping to matching REP It is incremented
		 * to track the depth of a nested set of LOOP/REP pairs in false state.
		 */
		int falseLoopDepth = 0;

		final double MUTATION_RATE = 0.000005;
		Direction facing = Direction.getDirection(rg.nextInt(4));
		/* Main loop */
		while (true) {
			clock++;

			/* Increment clock and run reports periodically */

			if (clock % REPORT_FREQUENCY == 0) {
				doReport(clock);
				setChanged();
				notifyObservers();
			}

			/*
			 * Introduce a random cell somewhere with a given energy level. This
			 * is called seeding and introduces both energy and entropy into the
			 * substrate. This happens every INFLOW_FREQUENCY clock ticks.
			 */
			if (clock % INFLOW_FREQUENCY == 0) {
				int x = rg.nextInt(POND_SIZE_X);
				int y = rg.nextInt(POND_SIZE_Y);

				pond[x][y].ID = cellIdCounter;
				pond[x][y].parentID = 0;
				pond[x][y].lineage = cellIdCounter;
				pond[x][y].generation = 0;
				pond[x][y].energy = INFLOW_RATE_BASE + (int) (rg.nextDouble() * INFLOW_RATE_VARIATION);

				pond[x][y].setRandomGenome();

				cellIdCounter++;
			}
			/* Pick a random cell to execute */
			int x = rg.nextInt(POND_SIZE_X);
			int y = rg.nextInt(POND_SIZE_Y);
			Cell c = pond[x][y];

			/* Reset VM */
			System.arraycopy(startBuffer, 0, outputBuf, 0, POND_DEPTH);
			reg = 0;
			int pointer = 0;
			loopStackPtr = 0;
			falseLoopDepth = 0;
			boolean stop = false;

			/* Keep track of how many cells have been executed */
			statCounters.cellExecutions++;

			/* Core execution loop */
			int instructionIndex = 0;// the current instruction index
			while (c.energy > 0 && !stop) {
				/*
				 * Randomly frob either the instruction or the register with a
				 * probability defined by MUTATION_RATE. This introduces
				 * variation, and since the variation is introduced into the
				 * state of the VM it can have all manner of different effects
				 * on the end result.
				 */
				// ===================================== //
				// ********** START TO CHANGE ********** //
				// ===================================== //
				// Implement faulty execution by duplicating or skipping
				// instructions
				if (rg.nextDouble() < MUTATION_RATE) {
					int type = rg.nextInt(4);
					switch (type) {
					/* replacement */
					case 0:
						c.genome[instructionIndex] = (byte) rg.nextInt(16);
						break;
					/* change register */
					case 1:
						reg = (byte) rg.nextInt(16);
						break;
					/* duplicate instruction execution */
					case 2:
						// TODO
						break;
					/* skip instruction */
					case 3:
						// TODO
						break;
					}
				}
				// =================================== //
				// ********** END TO CHANGE ********** //
				// =================================== //

				/* Each instruction processed costs one unit of energy */
				c.energy--;
				/* Execute the instruction */
				if (falseLoopDepth > 0) {
					/*
					 * Skip forward to matching REP if we're in a false loop.
					 */
					if (c.genome[instructionIndex] == 9)
						falseLoopDepth++;
					/*
					 * Decrement on REP
					 */
					else if (c.genome[instructionIndex] == 10)
						falseLoopDepth--;
				} else {
					/*
					 * Keep track of execution frequencies for each instruction
					 */
					statCounters.instructionExecutions[c.genome[instructionIndex]]++;

					switch (c.genome[instructionIndex]) {
					case 0x0: /* ZERO: Zero VM state registers */
						reg = 0;
						pointer = 0;
						break;
					case 0x1: /* FWD: Increment the pointer (wrap at end) */
						pointer++;
						pointer %= POND_DEPTH;
						break;
					case 0x2: /* BACK: Decrement the pointer (wrap at beginning) */
						if (pointer == 0)
							pointer = POND_DEPTH;
						pointer--;
						break;
					case 0x3: /* INC: Increment the register */
						reg++;
						reg %= 16;
						break;
					case 0x4: /* DEC: Decrement the register */
						if (reg == 0)
							reg = 16;
						reg--;
						break;
					case 0x5: /* READG: Read into the register from genome */
						reg = c.genome[pointer];
						break;
					case 0x6: /* WRITEG: Write out from the register to genome */
						c.genome[pointer] = reg;
						break;
					case 0x7: /* READB: Read into the register from buffer */
						reg = outputBuf[pointer];
						break;
					case 0x8: /* WRITEB: Write out from the register to buffer */
						outputBuf[pointer] = reg;
						break;
					case 0x9: /*
							 * LOOP: Jump forward to matching REP if register is
							 * zero
							 */
						if (reg > 0) {
							if (loopStackPtr >= POND_DEPTH)
								/* Stack overflow ends execution */
								stop = true;
							else {
								loopStackPointer[loopStackPtr] = instructionIndex;
								loopStackPtr++;
							}
						} else
							falseLoopDepth = 1;

						break;
					case 0xa: /*
							 * REP: Jump back to matching LOOP if register is
							 * nonzero
							 */
						if (loopStackPtr > 0) {
							loopStackPtr--;
							if (reg > 0) {
								instructionIndex = loopStackPointer[loopStackPtr];
								/*
								 * This ensures that the LOOP is rerun and that
								 * the instruction pointer has not yet changed.
								 */
								continue;
							}
						}
						break;
					case 0xb: /*
							 * TURN: Turn in the direction specified by register
							 */
						facing = Direction.getDirection(Math.abs(reg) % 4);
						break;
					case 0xc: /*
							 * XCHG: Skip next instruction and exchange value of
							 * reg with it
							 */
						instructionIndex++;
						instructionIndex %= POND_DEPTH;
						byte tmp = reg;
						reg = c.genome[instructionIndex];
						c.genome[instructionIndex] = tmp;

						break;
					case 0xd: /*
							 * KILL: Blow away neighboring cell if allowed with
							 * penalty on failure
							 */
						Cell neighborKill = getNeighbor(x, y, facing);
						if (accessAllowed(neighborKill, reg, false)) {
							if (neighborKill.generation > 2)
								statCounters.viableCellsKilled++;
							/*
							 * putting a STOP instruction as first instruction
							 * will kill the neighboring cell
							 */
							neighborKill.genome[0] = 15;
							neighborKill.ID = cellIdCounter;
							neighborKill.parentID = 0;
							neighborKill.lineage = cellIdCounter;
							neighborKill.generation = 0;

							cellIdCounter++;

						} else if (neighborKill.generation > 2) {
							c.energy /= FAILED_KILL_PENALTY;
						}
						break;
					case 0xe: /*
							 * SHARE: Equalize energy between self and neighbor
							 * if allowed
							 */
						Cell neighborShare = getNeighbor(x, y, facing);
						if (accessAllowed(neighborShare, reg, true)) {
							if (neighborShare.generation > 2)
								statCounters.viableCellShares++;
							int newEnergy = (c.energy + neighborShare.energy) / 2;
							c.energy = newEnergy;
							neighborShare.energy = newEnergy;
						}
						break;
					case 0xf: /* STOP: End execution */
						stop = true;
						break;
					}
				}

				/*
				 * Increase instruction pointer and loop at the end of the
				 * genome
				 */
				instructionIndex++;
				instructionIndex %= POND_DEPTH;
			}
			/*
			 * Copy outputBuf into neighbor if access is permitted and there is
			 * energy there to make something happen. There is no need to copy
			 * to a cell with no energy, since anything copied there would never
			 * be executed and then would be replaced with random junk
			 * eventually. See the seeding code in the main loop above.
			 */
			if (outputBuf[0] != 15) {
				Cell neighbor = getNeighbor(x, y, facing);
				if (neighbor.energy > 0 && accessAllowed(neighbor, reg, false)) {
					/* Log it if we're replacing a viable cell */
					if (neighbor.generation > 0)
						statCounters.viableCellsReplaced++;
					neighbor.ID = cellIdCounter++;
					neighbor.parentID = c.ID;
					/*
					 * Lineage is copied in offspring
					 */
					neighbor.lineage = c.lineage;
					neighbor.generation = c.generation + 1;
					// ===================================== //
					// ********** START TO CHANGE ********** //
					// ===================================== //
					// Implement a 'faulty' copy mechanism that allow the minor
					// mutations to enter when copying the cell
					System.arraycopy(outputBuf, 0, neighbor.genome, 0, POND_DEPTH);
					// =================================== //
					// ********** END TO CHANGE ********** //
					// =================================== //
				}

			}

		}
	}

	/* Used for unique seed ids */
	long seedingID = -1;

	public boolean seed(int x, int y, byte[] genome) {
		Cell c = pond[x][y];
		c.generation = 5;
		c.energy = 10000;
		c.parentID = seedingID;
		c.ID = seedingID;
		c.lineage = seedingID;
		seedingID--;
		System.arraycopy(genome, 0, c.genome, 0, POND_DEPTH);
		return true;

	}
}
