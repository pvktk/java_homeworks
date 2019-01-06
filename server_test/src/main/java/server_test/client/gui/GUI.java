package server_test.client.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import server_test.client.MeasureLauncher;
import server_test.client.MeasureLauncher.ChangingVariables;

@SuppressWarnings("serial")
public class GUI extends JPanel {
	
	private final MeasureLauncher launcher = new MeasureLauncher();
	
	private class RangeFields extends JPanel {
		public final int lineHeight = 20;
		public RangeFields(String variableName, ChangingVariables var, ButtonGroup group) {
			super(new FlowLayout());

			JRadioButton button = new JRadioButton(variableName);
			button.setPreferredSize(new Dimension(350, lineHeight));
			
			group.add(button);

			add(button);

			add(new JLabel("Min"));

			JTextArea minArea = new JTextArea();
			minArea.setPreferredSize(new Dimension(50, lineHeight));
			add(minArea);

			add(new JLabel("Step"));

			JTextArea stepArea = new JTextArea();
			stepArea.setPreferredSize(new Dimension(50, lineHeight));
			add(stepArea);

			add(new JLabel("Max"));

			JTextArea maxArea = new JTextArea();
			maxArea.setPreferredSize(new Dimension(50, lineHeight));
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
	}

	public GUI() {
		super(new GridLayout(0, 1));
		{
			ButtonGroup group = new ButtonGroup();
			JPanel radioPanel = new JPanel(new GridLayout(0, 1));

			radioPanel.add(new JLabel("Server architecture type", SwingConstants.CENTER));
			JRadioButton simpleBlockingServerButton = new JRadioButton("Separate threads for all work with client");
			group.add(simpleBlockingServerButton);
			radioPanel.add(simpleBlockingServerButton);


			JRadioButton middleBlockingServerButton = new JRadioButton("Separate threads for recieve/transmit, common thread pool");
			group.add(middleBlockingServerButton);
			radioPanel.add(middleBlockingServerButton);


			JRadioButton nonBlockingServerButton = new JRadioButton("Non blocking server");
			group.add(nonBlockingServerButton);
			radioPanel.add(nonBlockingServerButton);

			radioPanel.add(new JSeparator());

			add(radioPanel, BorderLayout.LINE_START);
		}	

		{

			ButtonGroup group = new ButtonGroup();
			JPanel radioPanel = new JPanel(new GridLayout(0, 1));

			radioPanel.add(new JLabel("Changing variable", SwingConstants.CENTER));


			radioPanel.add(new RangeFields("Size of arrays", ChangingVariables.ArraySize, group));

			radioPanel.add(new RangeFields("Number of working clients", ChangingVariables.NumberClients, group));

			radioPanel.add(new RangeFields("Time delta between client's requests (ms)", ChangingVariables.TimeDelta, group));

			add(radioPanel, BorderLayout.LINE_START);
		}
	}

	private static void createAndShowGUI() {
		JFrame frame = new JFrame("Server Architecture Laboratory");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 600);

		JComponent contentPane = new GUI();
		frame.setContentPane(contentPane);

		frame.pack();
		frame.setVisible(true);
	}

	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(GUI::createAndShowGUI);
	}
}
