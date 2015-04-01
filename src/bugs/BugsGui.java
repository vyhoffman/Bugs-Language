package bugs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tree.Tree;


/**
 * GUI for Bugs language.
 * @author Dave Matuszek
 * @author Nicki Hoffman
 * @version 2015
 */
public class BugsGui extends JFrame {
    private static final long serialVersionUID = 1L;
    View display;
    JSlider speedControl;
    int speed;
    JButton stepButton;
    JButton runButton;
    JButton pauseButton;
    JButton resetButton;
    Tree<Token> program;
    Interpreter in = new Interpreter();
    Timer timer;
    JFrame gui;
    
    /**
     * GUI constructor.
     */
    public BugsGui() {
        super();
        setSize(600, 600);
        setLayout(new BorderLayout());
        createAndInstallMenus();
        createDisplayPanel();
        createControlPanel();
        initializeButtons();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gui = this;
    }
    
    private void createAndInstallMenus() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");        
        JMenuItem quitMenuItem = new JMenuItem("Quit");
        JMenuItem loadMenuItem = new JMenuItem("Load...");
        
        menuBar.add(fileMenu);
        fileMenu.add(loadMenuItem);
        loadMenuItem.addActionListener(new ActionListener() {
        	@Override
        	public void actionPerformed(ActionEvent arg0) {
        		try { load(); }
        		catch (IOException e) { 
        			JOptionPane.showMessageDialog(gui, "Failed to load file");
        		}
        	}});
        fileMenu.add(quitMenuItem);
        quitMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent arg0) {
                quit();
            }});
        
        this.setJMenuBar(menuBar);
    }

    private void createDisplayPanel() {
        display = new View(); // update this when an interpreter is created
        display.setSize(getWidth()-2, getHeight()-100);
        add(display, BorderLayout.CENTER);
    }
    
    private void createDisplayPanel(Interpreter in) {
        display = new View(in);
        display.setSize(getWidth()-2, getHeight()-100);
        add(display, BorderLayout.CENTER);
    }

    private void createControlPanel() {
        JPanel controlPanel = new JPanel();
        
        addSpeedLabel(controlPanel);       
        addSpeedControl(controlPanel);
        addStepButton(controlPanel);
        addRunButton(controlPanel);
        addPauseButton(controlPanel);
        addResetButton(controlPanel);
        
        add(controlPanel, BorderLayout.SOUTH);
    }

    private void addSpeedLabel(JPanel controlPanel) {
        controlPanel.add(new JLabel("Speed:"));
    }

    private void addSpeedControl(JPanel controlPanel) {
        speedControl = new JSlider(SwingConstants.HORIZONTAL, 0, 100, 50);
        speed = 50;
        speedControl.setMajorTickSpacing(10);
        speedControl.setMinorTickSpacing(5);
        speedControl.setPaintTicks(true);
        speedControl.setPaintLabels(true);
        speedControl.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent arg0) {
                resetSpeed(speedControl.getValue());
            }
        });
        controlPanel.add(speedControl);
    }

    private void addStepButton(JPanel controlPanel) {
        stepButton = new JButton("Step");
        stepButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stepAnimation();
            }
        });
        controlPanel.add(stepButton);
    }

    private void addRunButton(JPanel controlPanel) {
        runButton = new JButton("Run");
        runButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                runAnimation();
            }
        });
        controlPanel.add(runButton);
    }

    private void addPauseButton(JPanel controlPanel) {
        pauseButton = new JButton("Pause");
        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                pauseAnimation();
            }
        });
        controlPanel.add(pauseButton);
    }

    private void addResetButton(JPanel controlPanel) {
        resetButton = new JButton("Reset");
        resetButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                resetAnimation();
            }
        });
        controlPanel.add(resetButton);
    }
    
    private void initializeButtons() {
        stepButton.setEnabled(false);
        runButton.setEnabled(false);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
    	
    	timer = new Timer(40, new ActionListener() {
    		@Override
        	public void actionPerformed(ActionEvent a) {
        		display.repaint();
        		if (in.bugs.size()==0) timer.stop();
        	}
        });
    }
	
	/**Tries to load a file into a string; reader from Dave's SimpleIO in
	 * flash cards assignment from 591.
	 * @throws IOException
	 */
	private void load() throws IOException {
		JFileChooser fc = new JFileChooser();
		File f;
		String contents = "";
		int val = fc.showOpenDialog(this);
		if (val == JFileChooser.APPROVE_OPTION) {
			f = fc.getSelectedFile();
			if (f != null) {
				String filename = f.getCanonicalPath();
				BufferedReader reader = new BufferedReader(new FileReader(filename));
				String line;
				while ((line = reader.readLine()) != null) {
					contents += line + " \n ";
				}
				reader.close();
			}
//			System.out.println("Got contents");
			Parser p = new Parser(contents);
//			System.out.println("Parsed contents");
			if (p.isProgram()) {
//				System.out.println("Is program");
				program = p.stack.pop();
				newAnimation();				
			} else {
				JOptionPane.showMessageDialog(gui, "Invalid program");
			}

		}
	}

    private void resetSpeed(int value) {
        speed = value;
        in.setDelay(1000 - 10*speed);
    }

    protected void newAnimation() {
        stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(false);
        
    	in = new Interpreter(program);
    	resetSpeed(speed);
    	remove(display);
    	createDisplayPanel(in);
        timer.start();
    	in.start();
    }
    
    protected void stepAnimation() {
    	in.step();
    	stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(true);
        
    }
    
    protected void runAnimation() {
        stepButton.setEnabled(false);
        runButton.setEnabled(false);
        pauseButton.setEnabled(true);
        resetButton.setEnabled(true);
        
    	in.running = true;
    }
    
    protected void pauseAnimation() {
    	stepButton.setEnabled(true);
        runButton.setEnabled(true);
        pauseButton.setEnabled(false);
        resetButton.setEnabled(true);

        in.running = false;
    }
    
    protected void resetAnimation() {
    	in.running = false;
    	timer.stop();
    	if (in != null) in.quit();
    	newAnimation();
    }

    protected void quit() {
    	if (in != null) in.quit();
        System.exit(0);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        new BugsGui();
    }

    
}
