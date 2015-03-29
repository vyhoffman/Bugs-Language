package bugs;

import java.util.ArrayList;
import java.util.HashMap;
import tree.Tree;

/**
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
	
	Interpreter() {
		functions = new HashMap<String, Tree<Token>>();
		variables = new HashMap<String, Double>();
		bugs = new HashMap<String, Bug>();
		lines = new ArrayList<Command>();
		started = false;
		running = false;
	} //hack to allow bugtest to keep working
	
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
	
	public void interpret(Tree<Token> tree) throws RuntimeException {
		switch (tree.getValue().value) {
		case "program":
			interpret(tree.getChild(0));
			interpret(tree.getChild(1));
			break;
		case "Allbugs":
			Tree<Token> t = tree.getChild(0); // var declaration list
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				variables.put(t.getChild(i).getValue().value, 0.0);
			}
			t = tree.getChild(1); // fn declaration list
			for (int i = 0; i < t.getNumberOfChildren(); i++) {
				Tree<Token> fn = t.getChild(i);
				functions.put(fn.getChild(0).getValue().value, fn);
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
	
	@Override
	public void run() {
		if (bugs.size() == 0) return;
		if (!started) { started = true; startBugs(); }
		while (bugs.size() > 0 && running) {
			//TODO
			try { sleep(delay); } catch (InterruptedException e) { }
			unblockAllBugs();
			System.out.println(bugs.size() + " bugs remain");
		}
	}
	
	public void setDelay(int d) {
		delay = d;
	}
	
	public void step() {
		if (bugs.size() == 0) return;
		if (!started) { started = true; startBugs(); }
		unblockAllBugs();
	}
	
	synchronized void getActionPermit(Bug b) {
		while (b.blocked) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}
	}
	
	synchronized void completeAction(Bug b) {
		b.blocked = true;
		notifyAll();
	}
	
	synchronized void unblockAllBugs() {
		while (countBlockedBugs() < bugs.size()) {
			try { wait(); } catch (InterruptedException e) { }
		}
		for (String name : bugs.keySet()) {
			bugs.get(name).blocked = false;
		}
		notifyAll();
	}
	
	private int countBlockedBugs() {
		int count = 0;
		for (String name : bugs.keySet()) if (bugs.get(name).blocked) count++;
		return count;
	}
	
	synchronized void killBug(Bug b) {
		bugs.remove(b.name);
		notifyAll();
	}
	
	synchronized void quit() {
		for (String bug : bugs.keySet()) killBug(bugs.get(bug));
		notifyAll();
	}
}
