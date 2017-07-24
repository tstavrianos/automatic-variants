/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package automaticvariants.gui;

import automaticvariants.AV;
import automaticvariants.AVSaveFile.Settings;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import lev.gui.LCheckBox;
import lev.gui.LHelpPanel;
import lev.gui.LNumericSetting;
import skyproc.gui.SPMainMenuPanel;
import skyproc.gui.SPSettingPanel;
import skyproc.gui.SUMGUI;

/**
 *
 * @author Justin Swanson
 */
public class StatsPanel extends SPSettingPanel {

    LNumericSetting heightDifference;
    LNumericSetting healthDifference;
    LNumericSetting magicDifference;
    LNumericSetting staminaDifference;
    LNumericSetting speedDifference;
    LCheckBox tieDifferences;
    HeightVarChart chart;
    static int minStd = 3;
    static int maxStd = 40;
    static double cutoff = 2.5;

    public StatsPanel(SPMainMenuPanel parent_) {
	super(parent_, "Stat Variants", AV.orange);
    }

    @Override
    protected void initialize() {
	super.initialize();

	chart = new HeightVarChart("Current Height Settings", new Dimension(SUMGUI.helpPanel.getBottomSize().width, 190),
		AV.yellow, AV.orange, "Percent difference from normal height", "Probability / Height");
	chart.addSeries(AV.darkGreen);

	heightDifference = new LNumericSetting("Max Height Difference", AV.AVFont, AV.yellow,
		0, maxStd, 1);
	heightDifference.tie(Settings.STATS_HEIGHT_MAX, AV.save, SUMGUI.helpPanel, true);
	setPlacement(heightDifference);
	heightDifference.addChangeListener(new StatsPanel.UpdateChartChangeHandler());
	AddSetting(heightDifference);

	healthDifference = new LNumericSetting("Max Health Difference", AV.AVFont, AV.yellow,
		0, maxStd, 1);
	healthDifference.tie(Settings.STATS_HEALTH_MAX, AV.save, SUMGUI.helpPanel, true);
	setPlacement(healthDifference);
	AddSetting(healthDifference);

	magicDifference = new LNumericSetting("Max Mana Difference", AV.AVFont, AV.yellow,
		0, maxStd, 1);
	magicDifference.tie(Settings.STATS_MAGIC_MAX, AV.save, SUMGUI.helpPanel, true);
	setPlacement(magicDifference);
	AddSetting(magicDifference);

	staminaDifference = new LNumericSetting("Max Stamina Difference", AV.AVFont, AV.yellow,
		0, maxStd, 1);
	staminaDifference.tie(Settings.STATS_STAMINA_MAX, AV.save, SUMGUI.helpPanel, true);
	setPlacement(staminaDifference);
	AddSetting(staminaDifference);

	speedDifference = new LNumericSetting("Max Speed Difference", AV.AVFont, AV.yellow,
		0, maxStd, 1);
	speedDifference.tie(Settings.STATS_SPEED_MAX, AV.save, SUMGUI.helpPanel, true);
	setPlacement(speedDifference);
	AddSetting(speedDifference);

	tieDifferences = new LCheckBox("Bundled Differences", AV.AVFont, AV.yellow);
	tieDifferences.tie(Settings.STATS_TIE, AV.save, SUMGUI.helpPanel, true);
	tieDifferences.addShadow();
	tieDifferences.setOffset(2);
	setPlacement(tieDifferences);
	AddSetting(tieDifferences);

	alignRight();

	updateChart();

    }

    void updateChart() {
	AV.save.updateCurToGUI();
	chart.clear();
	double std = (AV.save.getInt(Settings.STATS_HEIGHT_MAX) + minStd) / 3.0;
	if (std == 0) {
	    return;
	}

	double scale = 1 / bellCurve(0, std);
	// only iterate over "accepted area"
	for (double i = cutoff * -std - 1; i <= cutoff * std + 1; i = i + .5) {
	    double value = bellCurve(i, std);
	    chart.putPoint(0, i, value * scale);
	    if (i >= cutoff * std) {
		chart.max = (i / 100);
		chart.putPoint(1, i, 1 + chart.max);
	    } else if (i <= -cutoff * std) {
		chart.min = (i / 100);
		chart.putPoint(1, i, 1 + chart.min);
	    }
	}
	chart.plot.getDomainAxis().setRange(cutoff * -std, cutoff * std);
	chart.plot.getRangeAxis().setRange(0, 1.5);
    }

    void boxMullerTest() {
	int[] array = new int[1000];
	for (int i = 0; i < array.length; i++) {
	    array[i] = 0;
	}
	int maxRange = 0;
	double min = 1000;
	double max = -1000;
	double average = 0;
	for (int i = 0; i < 999; i++) {
	    for (int j = 0; j < 999; j++) {
		double val = (Math.sqrt(-2 * Math.log((i + 1) / 1000.0)) * Math.cos(2 * Math.PI * (j + 1) / 1000.0));
		if (max < val) {
		    max = val;
		}

		if (min > val) {
		    min = val;
		}

		average += val;

		val = val * 100 + 500;
		array[(int) val]++;
		if (array[(int) val] > maxRange) {
		    maxRange = array[(int) val];
		}
	    }
	}

	System.out.println("Min " + min);
	System.out.println("Max " + max);
	System.out.println("Avg " + average / 1000 / 1000);

	for (int i = 0; i < array.length; i++) {
	    chart.putPoint(1, i, array[i]);
	}
	chart.plot.getDomainAxis().setRange(0, array.length);
	chart.plot.getRangeAxis().setRange(0, maxRange);
    }

    // Equation just taken from wikipedia bell curve page.
    double bellCurve(double pos, double stdDev) {
	double exponent = -(Math.pow(pos, 2)
		/ (2 * Math.pow(stdDev, 2)));
	return (1 / stdDev / Math.sqrt(2 * Math.PI))
		* Math.pow(Math.E, exponent);
    }

    double estimateSTD(double value, double target, double accuracy, double testSTD, double step, int direction) {

	double test = bellCurve(value, testSTD);
	if (Math.abs(target - test) < accuracy) {
	    // If test STD gave value within accuracy range, return it
	    return testSTD;

	} else {
	    // Need to adjust STD and test again
	    boolean passedTarget = false;
	    if (direction != 0) {
		passedTarget = direction > 0
			? test < target : // If direction up, then passed if test > target
			test > target; // If direction down, then passed if test < target
	    }

	    // If we passed, half the step
	    if (passedTarget) {
		step = step / 2;
	    }

	    // Set direction
	    if (target > test) {
		direction = -1;
	    } else {
		direction = 1;
	    }

	    // Select new test
	    testSTD = testSTD + (step * direction);

	    return estimateSTD(value, target, accuracy, testSTD, step, direction);
	}
    }

    double estimateSTD() {
	return estimateSTD(AV.save.getInt(Settings.STATS_HEIGHT_MAX), 1, .1, 1, .5, 0);
    }

    private class UpdateChartHandler implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent event) {
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
			    updateChart();
			}
		    });
		}
	    });

	}
    }

    private class UpdateChartChangeHandler implements ChangeListener {

	@Override
	public void stateChanged(ChangeEvent event) {
	    SwingUtilities.invokeLater(new Runnable() {

		@Override
		public void run() {
		    SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
			    updateChart();
			}
		    });
		}
	    });

	}
    }

    @Override
    public void onOpen(SPMainMenuPanel parent_) {
	SUMGUI.helpPanel.clearBottomArea();
	SUMGUI.helpPanel.addToBottomArea(chart);
	SUMGUI.helpPanel.setBottomAreaHeight(190);
    }

    @Override
    protected void update() {
	SwingUtilities.invokeLater(new Runnable() {

	    @Override
	    public void run() {
		updateChart();  // Double nest to ensure it's the last listener
	    }
	});
    }
}
