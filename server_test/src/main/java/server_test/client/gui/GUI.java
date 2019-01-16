package server_test.client.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.data.xy.DefaultXYDataset;
import server_test.Messages.MeasureResponse;
import server_test.Messages.ServerType;
import server_test.client.MeasureLauncher;
import server_test.client.MeasureLauncher.ChangingVariables;

@SuppressWarnings("serial")
public class GUI extends JPanel {

	private final MeasureLauncher launcher = new MeasureLauncher();

	private final JTextArea ipAddress, requestsNumber;

	private final List<RangeFields> rangeFields = new ArrayList<>();

	private Thread measureThread;
	private final JButton startMeasureButton, cancelButton;

	private final JProgressBar progressBar;

	private final List<JButton> resultUsingButtons = new ArrayList<>();

	private volatile boolean measureThreadCancelled = false;
	private void cancelAction() {
		if (measureThread != null)
			measureThread.interrupt();
		launcher.closeSocket();
		measureThreadCancelled = true;
	}

	public GUI() {
		super(new GridBagLayout());
		int currGridY = 0;
		{
			ipAddress = new JTextArea("localhost");
			ipAddress.setPreferredSize(new Dimension(100, 20));
			JPanel panel = new JPanel(new FlowLayout());

			panel.add(new JLabel("server address"));
			panel.add(ipAddress);

			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			add(panel, c);
		}
		{
			ButtonGroup group = new ButtonGroup();
			JPanel radioPanel = new JPanel(new GridLayout(0, 1));

			radioPanel.add(new JLabel("Select server architecture", SwingConstants.CENTER));

			{
				JRadioButton button = new JRadioButton("Separate threads for all work with client");
				group.add(button);
				button.addActionListener((e) -> launcher.setServerType(ServerType.simpleBlocking));
				radioPanel.add(button);
				button.doClick();
			}
			{
				JRadioButton button = new JRadioButton("Separate threads for recieve/transmit, common thread pool");
				group.add(button);
				button.addActionListener((e) -> launcher.setServerType(ServerType.middleBlocking));
				radioPanel.add(button);
			}

			{
				JRadioButton button = new JRadioButton("Non blocking server");
				group.add(button);
				button.addActionListener((e) -> launcher.setServerType(ServerType.nonBlocking));
				radioPanel.add(button);
			}
			
			{
				JRadioButton button = new JRadioButton("Asynchronous");
				group.add(button);
				button.addActionListener((e) -> launcher.setServerType(ServerType.asynchronous));
				radioPanel.add(button);
			}

			radioPanel.add(new JSeparator());

			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(radioPanel, c);
		}	

		{

			ButtonGroup group = new ButtonGroup();
			JPanel radioPanel = new JPanel(new GridLayout(0, 1));


			radioPanel.add(new JLabel("Changing variable", SwingConstants.CENTER));

			RangeFields rf;

			rf = new RangeFields("Size of arrays", ChangingVariables.ArraySize, group, launcher);
			rangeFields.add(rf);
			radioPanel.add(rf);

			rf = new RangeFields("Number of working clients", ChangingVariables.NumberClients, group, launcher);
			rangeFields.add(rf);
			radioPanel.add(rf);

			rf = new RangeFields("Time delta between client's requests (ms)", ChangingVariables.TimeDelta, group, launcher);
			rangeFields.add(rf);
			radioPanel.add(rf);

			group.getElements().nextElement().setSelected(true);

			{
				requestsNumber = new JTextArea("10");
				requestsNumber.setPreferredSize(new Dimension(100, 20));
				JPanel panel = new JPanel(new FlowLayout());
				panel.add(new JLabel("number of requests"));
				panel.add(requestsNumber);
				radioPanel.add(panel);
			}

			radioPanel.add(new JSeparator());
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(radioPanel, c);
		}


		{
			startMeasureButton = new JButton("Make measurements");
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY;
			add(startMeasureButton, c);

			startMeasureButton.addActionListener(a -> startMeasureThread());
		}

		{
			cancelButton = new JButton("Cancel measurements");
			cancelButton.setEnabled(false);
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridx = 1;
			add(cancelButton, c);

			cancelButton.addActionListener(a -> {
				cancelAction();
			});
		}

		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(new JSeparator(), c);
		}

		{
			progressBar = new JProgressBar();
			progressBar.setPreferredSize(new Dimension(300, 20));
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(progressBar, c);
		}

		{
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(new JSeparator(), c);
		}

		{
			JPanel panel = new JPanel(new FlowLayout());
			panel.add(new JSeparator());
			{
				JButton b = new JButton("Plot sorting time");
				panel.add(b);
				resultUsingButtons.add(b);
				b.addActionListener(e -> {
					showPlot("Average sorting time", MeasureResponse::getAvgSortTime);
				});
			}
			{
				JButton b = new JButton("Plot process time");
				panel.add(b);
				resultUsingButtons.add(b);
				b.addActionListener(e -> {
					showPlot("Average on server process time", MeasureResponse::getAvgProcessTime);
				});
			}
			{
				JButton b = new JButton("Plot client time");
				panel.add(b);
				resultUsingButtons.add(b);
				b.addActionListener(e -> {
					showPlot("On client average time", MeasureResponse::getAvgOnClientTime);
				});
			}
			{
				JButton b = new JButton("save to txt");
				panel.add(b);
				resultUsingButtons.add(b);
				b.addActionListener(e -> {
					try {
						launcher.saveResultsToFile();
						JOptionPane.showMessageDialog(this, "Successfully saved", "Saved", JOptionPane.INFORMATION_MESSAGE);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(this, "Error while saving", "error", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
			resultUsingButtons.forEach(b -> b.setEnabled(false));
			GridBagConstraints c = new GridBagConstraints();
			c.gridy = currGridY++;
			c.gridwidth = 2;
			add(panel, c);
		}
	}

	private void showPlot(String yAxisTitle,
			Function<MeasureResponse, Integer> selector) {

		final int len = launcher.getResults().size();
		double[][] series = new double[2][len];

		int i = 0;
		for (Entry<Integer, MeasureResponse> ent : launcher.getResults().entrySet()) {
			series[0][i] = ent.getKey();
			series[1][i] = selector.apply(ent.getValue());
			i++;
		}

		String xAxisTitle;
		switch (launcher.getChangingVariable()) {
		case ArraySize:
			xAxisTitle = "array size";
			break;
		case NumberClients:
			xAxisTitle = "number of clients";
			break;
		case TimeDelta:
			xAxisTitle = "time delta";
			break;
		default:
			xAxisTitle = "";
		}

		DefaultXYDataset dataset = new DefaultXYDataset();
		dataset.addSeries(1, series);
		ChartPanel chartPanel = new ChartPanel(
				ChartFactory.createXYLineChart("Load testing", xAxisTitle, yAxisTitle, dataset));

		JFrame chartFrame = new JFrame();

		chartFrame.add(chartPanel);
		chartFrame.pack();
		chartFrame.setVisible(true);

	}

	private void startMeasureThread() {
		measureThread = new Thread(() -> {
			measureThreadCancelled = false;
			try {
				try {
					startMeasureButton.setEnabled(false);
					cancelButton.setEnabled(true);
					resultUsingButtons.forEach(b -> b.setEnabled(false));

					launcher.setServerAddress(ipAddress.getText());


					launcher.setNumberArrays(Integer.parseInt(requestsNumber.getText()));
					rangeFields.forEach(rf -> rf.setValuesToLauncher());
					launcher.makeMeasure(progressBar);
					resultUsingButtons.forEach(b -> b.setEnabled(true));
				} catch (Exception e) {
					if (!measureThreadCancelled)
						throw e;
				}
			} catch (NumberFormatException e){
				JOptionPane.showMessageDialog(this, "Some integer fields incorrect", "NumberFormatException", JOptionPane.ERROR_MESSAGE);
			} catch (IllegalArgumentException e) {
				JOptionPane.showMessageDialog(this, "Some integer values are incorrect", "NumberArgumentException", JOptionPane.ERROR_MESSAGE);
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(this, 
						"Unknown host",
						"An error occured", JOptionPane.ERROR_MESSAGE);
				//} catch (SocketException e){
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, 
						"IOException occured. " + e.getMessage() + "\nStopping",
						"An error occured", JOptionPane.ERROR_MESSAGE);
			} catch (IllegalStateException e){

				JOptionPane.showMessageDialog(this, 
						e.getMessage() + "\nStopping",
						"An error occured", JOptionPane.ERROR_MESSAGE);
			} catch (InterruptedException e) {}
			finally {
				startMeasureButton.setEnabled(true);
				cancelButton.setEnabled(false);
				progressBar.setValue(0);
			}
		});
		measureThread.setDaemon(true);
		measureThread.start();
	}

	public static void createAndShowGUI() {
		JFrame frame = new JFrame("Server Architecture Laboratory");

		frame.setSize(800, 600);

		GUI contentPane = new GUI();
		frame.setContentPane(contentPane);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.pack();
		frame.setVisible(true);
	}
}
