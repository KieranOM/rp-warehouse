package rp.warehouse.pc.management;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * A frame with an indeterminate loading bar
 * @author dxj786
 */
public class LoadingView extends JFrame {
    private static LoadingView loadingView;

    /**
     * Creates a frame with an indeterminate loading bar, there should only be one loading view
     */
    public LoadingView() {
        super("Warehouse MI");

        // Create label and progress bar with correct layouts and borders
        JPanel loadingBar = new JPanel();
        loadingBar.setLayout(new BorderLayout(5, 5));
        JLabel loading = new JLabel("loading");
        loading.setBorder(new EmptyBorder(10, 10, 10, 10));
        JPanel progressBarPanel = new JPanel();
        JProgressBar progressBar = new JProgressBar();

        progressBar.setIndeterminate(true);
        progressBarPanel.setLayout(new BorderLayout());
        progressBarPanel.add(progressBar, BorderLayout.CENTER);
        progressBarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        loadingBar.add(loading, BorderLayout.NORTH);
        loadingBar.add(progressBarPanel, BorderLayout.CENTER);

        this.add(loadingBar);
        this.pack();
        this.setSize(600, 150);

        // Center frame
        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        loadingView = this;
    }

    /**
     * Closes frame when notified of loading finished
     */
    public static void finishedLoading() {
        loadingView.setVisible(false);
    }
}
