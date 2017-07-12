/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.IOException;
import java.net.URL;
import lev.gui.LAreaChart;
import lev.gui.LImagePane;

/**
 *
 * @author Justin Swanson
 */
public class HeightVarChart extends LAreaChart {

    double max = 0;
    double min = 0;
    double left = 0;
    double right = 0;
    final static int peak = 66;
    final static int floor = 155;
    final static int height = floor - peak;
    final static int offsetX = 4;
    final static int width = 125;
    final static int smallPicX = 80;
    final static int largePicX = 235;

    URL pic = HeightVarChart.class.getResource("heightFalmer.png");
    LImagePane small;
    LImagePane norm;
    LImagePane large;

    public HeightVarChart(String title_, Dimension size_, Color titleColor, Color seriesColor,
	    String XLabel, String YLabel) {
	super(title_, size_, titleColor, seriesColor, XLabel, YLabel);
	try {
	    small = new LImagePane(pic);
	    norm = new LImagePane(pic);
	    norm.setMaxSize(0, height);
	    norm.setLocation(125, peak);
	    large = new LImagePane(pic);

	    add(small, 0);
	    add(norm, 0);
	    add(large, 0);
	} catch (IOException ex) {
	}
    }

    @Override
    public void paint(Graphics g) {
	super.paint(g);
	g.setColor(AV.blue);

	// Normal line
	g.drawLine(this.getSize().width / 2 + offsetX - width, peak,
		this.getSize().width / 2 + offsetX + width, peak);


	// Min Height
	int minHeight = peak - (int) Math.round(min * height);
	g.drawLine(this.getSize().width / 2 + offsetX - width, minHeight,
		this.getSize().width / 2 + offsetX + width, minHeight);

	// Max Height
	int maxHeight = peak - (int) Math.round(max * height);
	g.drawLine(this.getSize().width / 2 + offsetX - width, maxHeight,
		this.getSize().width / 2 + offsetX + width, maxHeight);
	try {
	    small.setImage(pic);
	    small.setMaxSize(0, floor - minHeight);
	    small.setLocation(smallPicX - small.getWidth() / 2, floor - small.getHeight());

	    large.setImage(pic);
	    large.setMaxSize(0, floor - maxHeight);
	    large.setLocation(largePicX - large.getWidth() / 2, floor - large.getHeight());

	} catch (IOException ex) {
	}
    }
}
