/*
 * Reporter.java
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
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.NumberFormat;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import be.abeel.util.TimeInterval;

public class Reporter {
    
    long year;
    long energy;
    long maxGeneration;
    long activeCells;
    long viableReplicators;
    long kills;
    long replaced;
    long shares;

    JTextArea area;
    long startTime=0;
    public Reporter(){
        startTime=System.currentTimeMillis();
        JFrame window=new JFrame("World report");
        window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        window.setResizable(false);
        area=new JTextArea();
        JScrollPane pane=new JScrollPane(area);
        pane.setPreferredSize(new Dimension(320,180));
        window.setLocation(0,530);
        window.getContentPane().add(pane);
        window.pack();
        window.setVisible(true);
        nf.setMaximumFractionDigits(2);
  
        
    }
    NumberFormat nf=NumberFormat.getInstance();
    
    
    long time=System.currentTimeMillis();
    public void update(){
        StringWriter string=new StringWriter();
        PrintWriter out=new PrintWriter(string);
        out.println("Report for the epoch " + year/1000000.0);
        out.println("\tTotal energy = " + energy);
        out.println("\tMax generation = " + maxGeneration);
        out.println("\tActive cells = " + activeCells);
        out.println("\tViable replicators = " + viableReplicators);
        out.println("\tSuccesful kills = " + kills);
        out.println("\tSuccesful replaced = " + replaced);
        out.println("\tSuccesful shares = " + shares);
        out.println("Speed: "+(System.currentTimeMillis()-time)/100.0+" s/My");
        out.println("Average speed: "+nf.format(((System.currentTimeMillis()-startTime)/1000)/(year/1000000.0))+" s/My");
        out.println("Total time: "+(new TimeInterval(System.currentTimeMillis()-startTime)));
        time=System.currentTimeMillis();
        area.setText(string.toString());
        
    }

}
