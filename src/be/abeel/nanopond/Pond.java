/*
 * Pond.java
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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;

import be.abeel.nanopond.EvolutionManager.Cell;

class Pond extends JLabel implements Observer {

    class Mouse extends MouseAdapter {
        public void mouseClicked(MouseEvent e) {
            zoom.setZoom(e.getX(), e.getY());
        }
    }

    private static final long serialVersionUID = 6246402986760030953L;

    private final EvolutionManager nanoPond;

    ZoomPond zoom;

    public Pond(EvolutionManager nanoPond) {
        super();
        this.nanoPond = nanoPond;
        this.addMouseListener(new Mouse());
        setPreferredSize(new Dimension(EvolutionManager.POND_SIZE_X, EvolutionManager.POND_SIZE_Y));
        zoom = new ZoomPond(this.nanoPond);

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = 0; i < EvolutionManager.POND_SIZE_X; i++) {
            for (int j = 0; j < EvolutionManager.POND_SIZE_Y; j++) {
                // if (pond[i][j].generation > 2)
                g.setColor(getColor(this.nanoPond.pond[i][j]));
                g.fillRect(i, j, 2, 2);
            }
        }
    }

    static int cap(int i) {
        if (i > 255)
            return 255;
        else if (i < 0)
            return 0;
        else
            return i;
    }

    static Color[] artificial = { Color.white, Color.green, Color.cyan, Color.yellow, Color.red, Color.PINK };

    static Color getColor(Cell cell) {
        if (cell.generation > 2 && cell.energy > 0) {
            if (cell.lineage < 0)
                return artificial[(int) Math.abs(cell.lineage) % artificial.length];

            int red = (int) cell.lineage % 256;
            int green = (int) cell.lineage % (256 * 256) / 256;
            int blue = (int) cell.lineage % (256 * 256 * 256) / 256 / 256;
            return new Color(cap(red), cap(green), cap(blue));
        } else
            return Color.black;
    }

    public void update(Observable o, Object arg) {
        this.repaint();
    }
}