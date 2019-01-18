package server_test.client.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import server_test.client.MeasureLauncher;
import server_test.client.MeasureLauncher.ChangingVariables;

@SuppressWarnings("serial")
public class RangeFields extends JPanel {
	public final int lineHeight = 20;
	
	private final MeasureLauncher launcher;
	
	private final ChangingVariables varType;
	
	private final JRadioButton button;
	private final JTextArea minArea, stepArea, maxArea;
	
	public RangeFields(String variableName,
			ChangingVariables var,
			ButtonGroup group,
			MeasureLauncher launcher) {
		super(new FlowLayout());
		
		this.varType = var;
		this.launcher = launcher;
		
		button = new JRadioButton(variableName);
		button.setPreferredSize(new Dimension(350, lineHeight));
		
		group.add(button);

		add(button);

		add(new JLabel("Min"));

		minArea = new JTextArea();
		minArea.setPreferredSize(new Dimension(50, lineHeight));
		add(minArea);

		add(new JLabel("Step"));

		stepArea = new JTextArea();
		stepArea.setPreferredSize(new Dimension(50, lineHeight));
		stepArea.setEditable(false);
		stepArea.setBackground(Color.gray);
		add(stepArea);

		add(new JLabel("Max"));

		maxArea = new JTextArea();
		maxArea.setPreferredSize(new Dimension(50, lineHeight));
		maxArea.setEditable(false);
		maxArea.setBackground(Color.gray);
		add(maxArea);
		
		button.addChangeListener(new ChangeListener() {
			
			@Override
			public void stateChanged(ChangeEvent e) {
				boolean selected = ((JRadioButton) e.getSource()).isSelected();
				Color c = selected ? Color.WHITE : Color.GRAY;
				stepArea.setEditable(selected);
				maxArea.setEditable(selected);
				
				stepArea.setBackground(c);
				maxArea.setBackground(c);
			}
		});

	}
	
	public void setValuesToLauncher() throws NumberFormatException {
		if (button.isSelected()) {
			launcher.setChangingVariable(varType);
			launcher.setRange(
					Integer.parseInt(minArea.getText()),
					Integer.parseInt(stepArea.getText()),
					Integer.parseInt(maxArea.getText()));
		} else {
			switch (varType) {
			case ArraySize:
				launcher.setArraySize(Integer.parseInt(minArea.getText()));
				break;
			case NumberClients:
				launcher.setNumberClients(Integer.parseInt(minArea.getText()));
				break;
			case TimeDelta:
				launcher.setTimeDeltaMillis(Integer.parseInt(minArea.getText()));
			}
		}
	}
}
