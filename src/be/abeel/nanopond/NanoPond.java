/*
 * NanoPond.java
 *
 * Copyright (C) 2007 Thomas Abeel
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * This program was based on the Nanopond 1.9 C program by Adam Ierymenko
 * http://www.greythumb.org/wiki/Nanopond
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin St, Fifth Floor, Boston, MA 02110 USA
 *
 */
package be.abeel.nanopond;

import javax.swing.JFrame;

public class NanoPond {

	public static void main(String[] args) {
		
		Reporter report = new Reporter();
		EvolutionManager data = new EvolutionManager(report);
		JFrame window = new JFrame("Genetisch programmeren demo applicatie");
		Pond pond = new Pond(data);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setResizable(false);
		window.getContentPane().add(pond);
		window.setJMenuBar(new PondMenu(data));
		window.pack();
		window.setVisible(true);
		data.addObserver(pond);
		new Thread(data).start();

	}

}
