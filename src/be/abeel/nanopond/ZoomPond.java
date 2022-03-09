/*
 * ZoomPond.java
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

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

class ZoomPond extends JLabel implements Observer {

    int x, y;

    int multiplier = 15;

    class Mouse extends MouseAdapter {
        private ZoomPond zoom;

        Mouse(ZoomPond zoom) {
            this.zoom = zoom;
        }

        public void mouseClicked(MouseEvent e) {
            int x = e.getX() / multiplier;
            int y = e.getY() / multiplier;
            text.setText("Lineage: " + nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].lineage + "\n");
            text.append("Energy: " + nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].energy + "\n");
            text.append("Id: " + nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].ID + "\n");
            text.append("ParentID: " + nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].parentID + "\n");
            text.append("Generation: " + nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].generation + "\n");
            text.append("Hexa: "+hexa(nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].genome)+"\n");
            text.append(disassemble(nanoPond.pond[x + this.zoom.x - 10][y + this.zoom.y - 10].genome, nanoPond));
            text.setCaretPosition(0);
        }

        private String hexa(byte[] genome) {
            StringBuffer out=new StringBuffer();
            for(int i=0;i<genome.length;i++)
                out.append(Integer.toHexString(genome[i]));
            return out.substring(0,out.indexOf("ff")+1);
        }

    }

    static String disassemble(byte[] genome, EvolutionManager nano) {
        String out = "";
        for (int i = 0; i < genome.length; i++) {
            out += i + "\t" + nano.names[genome[i]] + "\n";
        }
        return out;
    }

    private static final long serialVersionUID = 6246402986760030953L;

    private final EvolutionManager nanoPond;

    private JTextArea text = new JTextArea();

    public ZoomPond(EvolutionManager nanoPond) {
        super();
        this.nanoPond = nanoPond;
        nanoPond.addObserver(this);
        this.addMouseListener(new Mouse(this));
        setPreferredSize(new Dimension(21 * multiplier, 21 * multiplier));
        JFrame window = new JFrame("Genome Disassembler");
        window.setResizable(false);
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(300, 480));
        window.getContentPane().add(pane);
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setLocation(650, 0);
        window.pack();
        window.setVisible(true);

        JFrame own = new JFrame("Zoom view");
        own.getContentPane().add(this);
        own.setLocation(650, 510);
        own.setResizable(false);
        own.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        own.pack();
        own.setVisible(true);

    }

    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int i = -10; i < 11; i++) {
            for (int j = -10; j < 11; j++) {
                int tmpX = this.x + i;
                int tmpY = this.y + j;
                if (tmpX < 0)
                    tmpX += EvolutionManager.POND_SIZE_X;
                if (tmpY < 0)
                    tmpY += EvolutionManager.POND_SIZE_Y;
                if (tmpX >= EvolutionManager.POND_SIZE_X)
                    tmpX -= EvolutionManager.POND_SIZE_X;
                if (tmpY >= EvolutionManager.POND_SIZE_Y)
                    tmpY -= EvolutionManager.POND_SIZE_Y;
                g.setColor(Pond.getColor(this.nanoPond.pond[tmpX][tmpY]));
                g.fillRect((i + 10) * multiplier, (j + 10) * multiplier, multiplier + 1, multiplier + 1);
            }
        }
    }

    

   

    public void update(Observable o, Object arg) {
        this.repaint();
    }

    public void setZoom(int x, int y) {
        this.x = x;
        this.y = y;
        this.repaint();

    }
}