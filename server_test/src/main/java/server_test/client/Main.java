package server_test.client;

import server_test.client.gui.GUI;

public class Main {
	public static void main(String[] args) {
		javax.swing.SwingUtilities.invokeLater(GUI::createAndShowGUI);
	}
}
