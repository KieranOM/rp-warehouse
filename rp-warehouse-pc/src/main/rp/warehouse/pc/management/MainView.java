package rp.warehouse.pc.management;

import rp.warehouse.pc.data.robot.Robot;
import rp.warehouse.pc.management.panels.main.InfoPanel;
import rp.warehouse.pc.management.panels.main.WarehouseMapPanel;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Main warehouse MI frame
 * @author dxj786
 */
public class MainView extends JFrame {

    /**
     * Creates the main gui for this application using a given list of robots
     * @param robots list of robots to visualise
     */
    public MainView(List<Robot> robots) {
        JPanel top = new JPanel();
        top.setLayout(new FlowLayout());
        top.add(new WarehouseMapPanel(robots));
        top.add(new InfoPanel(robots));

        this.add(top);
        this.setSize(top.getMinimumSize());
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setVisible(true);
    }
}
