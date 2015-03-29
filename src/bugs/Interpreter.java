package bugs;

import java.util.ArrayList;
import java.util.HashMap;
import tree.Tree;

/**Bugs interpreter class.
 * @author Nicki Hoffman
 * @author Dave Matuszek
 *
 */
public class Interpreter extends Thread {
	HashMap<String, Tree<Token>> functions;
	HashMap<String, Double> variables;
	HashMap<String, Bug> bugs;
	//Tree<Token> allbugs; //why??
	boolean started;
	boolean running;
	ArrayList<Command> lines;
	int delay;
	
	/**Default Interpreter constructor.
	 * 
	 */
	Interpreter() {
		functions = new HashMap<String, Tree<Token>>();
		variables = new HashMap<String, Double>();
		bugs = new HashMap<String, Bug>();
		lines = new ArrayList<Command>();
		started = false;
		running = false;
	} //hack to allow bugtest to keep working
	
	/**Interpreter constructor that takes an AST to interpret.
	 * @param t tree to interpret
	 */
	Interpreter(Tree<Token> t) {
		this(); // I mean given the above I might as well
		interpret(t);
	}
	
//	public void main(String args[]) {
//		//TODO
//		String program = "";
//		Parser p = new Parser(program);
//		p.isProgram();
//		//other stuff
//	}
	
	/**Tries to interpret the tree passed in.
	 * @param tree to interpret
	 * @throws RuntimeException if an error is encountered?
	 */
	public void interpret(Tree<Token> tree) throws RuntimeException {
		switch (tree.getValue().value) {
		case "program":
			interpret(tree.getChild(0));
			interpret(tree.getChild(1));
			break;
		case "Allbugs":
			Tree<Token> t = tree.getChild(0); // var declaration list
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				interpret(t.getChild(i));
			}
			t = tree.getChild(1); // fn declaration list
			Tree<Token> fn;
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				fn = t.getChild(i);
				functions.put(fn.getChild(0).getValue().value, fn);
			}
			break;
		case "var":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) {
				variables.put(tree.getChild(i).getValue().value, 0.0);
			}
			break;
		case "list":
			for (int i = 0; i < tree.getNumberOfChildren(); i++) 
				interpret(tree.getChild(i));
			break;
		case "Bug":
			bugs.put(tree.getChild(0).getValue().value, new Bug(tree, this));
		}
	}
	
	/** Calls the start() method of each live bug. */
	private void startBugs() {
		for (String name : bugs.keySet()) {
			bugs.get(name).start();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		if (bugs.size() == 0) return;
		if (!started) { started = true; startBugs(); }
		while (bugs.size() > 0) {
			//TODO
			while (!running) {try { sleep(delay); } catch (InterruptedException e) { }}
			try { sleep(delay); } catch (InterruptedException e) { }
			unblockAllBugs();
//			System.out.println(bugs.size() + " bugs remain");
		}
	}
	
	/**Sets the Interpreter's delay (how long to wait before handing out a
	 * round of action permits)
	 * @param d delay to set
	 */
	public void setDelay(int d) {
		delay = d;
	}
	
	/**Hands out one round of action permits, allowing each Bug to take one
	 * action.
	 */
	public void step() {
		if (bugs.size() == 0) return;
		if (!started) { started = true; startBugs(); }
		unblockAllBugs();
	}
	
	/**Delays Bug b while it waits for an action permit.
	 * @param b Bug waiting for an action permit
	 */
	synchronized void getActionPermit(Bug b) {
		while (b.blocked) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}
	}
	
	/**Resets Bug b to blocked after being called/notified that it has
	 * performed an action.
	 * @param b Bug that needs to be blocked.
	 */
	synchronized void completeAction(Bug b) {
		b.blocked = true;
		notifyAll();
	}
	
	/**Waits for all Bugs to be blocked, then hands out a round of work permits.
	 * 
	 */
	synchronized void unblockAllBugs() {
		while (countBlockedBugs() < bugs.size()) {
			try { wait(); } catch (InterruptedException e) { }
		}
		for (String name : bugs.keySet()) {
			bugs.get(name).blocked = false;
		}
		notifyAll();
	}
	
	/**Counts the number of live Bugs that are currently blocked.
	 * @return Count of Bugs blocked
	 */
	private int countBlockedBugs() {
		int count = 0;
		for (String name : bugs.keySet()) if (bugs.get(name).blocked) count++;
		return count;
	}
	
	/**When called by Bug b, removes b from the hashMap of live Bugs.
	 * @param b Bug to remove
	 */
	synchronized void killBug(Bug b) {
		bugs.remove(b.name);
		notifyAll();
	}
	
	/**Kills all Bugs, clears the set of lines to draw, and stops running.
	 * 
	 */
	synchronized void quit() {
		while (countBlockedBugs() < bugs.size()) {
			try { wait(); } catch (InterruptedException e) { }
		}
		for (String bug : bugs.keySet()) killBug(bugs.get(bug));
		lines = new ArrayList<Command>();
		running = false;
		notifyAll();
	}
}
