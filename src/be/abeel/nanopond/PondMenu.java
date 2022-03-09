/*
 * PondMenu.java 
 * -----------------------
 * Copyright (C) 2005-2007  Thomas Abeel
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * 
 * Author: Thomas Abeel
 */
package be.abeel.nanopond;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

public class PondMenu extends JMenuBar {

	MTRandom rg = new MTRandom();

	class EradicateAction extends AbstractAction {
		private EvolutionManager nano;

		public EradicateAction(EvolutionManager data) {
			super("Eradicate organism");
			this.nano = data;

		}

		public void actionPerformed(ActionEvent arg0) {
			try {
				String lin = JOptionPane.showInputDialog("Please enter the lineage of the organism to destroy");
				long lineage = Long.parseLong(lin);
				for (int i = 0; i < EvolutionManager.POND_SIZE_X; i++) {
					for (int j = 0; j < EvolutionManager.POND_SIZE_Y; j++) {
						if (nano.pond[i][j].lineage == lineage) {
							nano.pond[i][j].genome[0] = (byte) 15;
							nano.pond[i][j].parentID = 0;
							nano.pond[i][j].generation = 0;
						}
					}
				}
				JOptionPane.showMessageDialog(null, "Lineage " + lineage + " has been eradicated as best we could.");
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}
	}

	class SeedAction extends AbstractAction {

		/**
         * 
         */
		private static final long serialVersionUID = 444113025460236623L;

		private EvolutionManager nano;

		public SeedAction(EvolutionManager data) {
			super("Create new organism");
			this.nano = data;
		}

		public void actionPerformed(ActionEvent arg0) {
			/*
			 * Virtually no error checking is done, just catch any exception and
			 * pretend nothing happened
			 */
			byte[] genome = new byte[EvolutionManager.POND_DEPTH];
			for (int i = 0; i < EvolutionManager.POND_DEPTH; i++)
				genome[i] = (byte) 15;
			try {
				String input = JOptionPane
						.showInputDialog("Input the genome of the new organism in hexadecimal notation");
				if (input.startsWith("0x"))
					input = input.substring(2);
				for (int i = 0; i < input.length(); i++) {
					int instruction = Integer.parseInt("" + input.charAt(i), 16);
					genome[i] = (byte) instruction;
				}
				int x = rg.nextInt(EvolutionManager.POND_SIZE_X);
				int y = rg.nextInt(EvolutionManager.POND_SIZE_Y);
				if (nano.seed(x, y, genome)) {
					JOptionPane.showMessageDialog(null, "We have seeded location [" + x + "," + y + "] with\n" + input);
				}
			} catch (Exception e) {
				System.err.println(e.getMessage());
			}

		}

	}

	public PondMenu(EvolutionManager data) {
		super();
		JMenu creator = new JMenu("God mode");
		creator.add(new JMenuItem(new SeedAction(data)));
		creator.add(new JMenuItem(new EradicateAction(data)));
		this.add(creator);
	}

	/**
     * 
     */
	private static final long serialVersionUID = 1L;

}
