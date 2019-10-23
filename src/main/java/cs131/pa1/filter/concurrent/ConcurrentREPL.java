package cs131.pa1.filter.concurrent;

import cs131.pa1.filter.Message;
import java.util.*;

/**
 * The main implementation of the REPL loop (read-eval-print loop).
 * It reads commands from the user, parses them, executes them and displays the result.
 * @author ruiyuzhang
 *
 */
public class ConcurrentREPL {
	/**
	 * the path of the current working directory
	 */
	static String currentWorkingDirectory;
	/**
	 * The main method that will execute the REPL loop
	 * @param args not used
	 */
	public static void main(String[] args){
		currentWorkingDirectory = System.getProperty("user.dir");
		Scanner s = new Scanner(System.in);
		System.out.print(Message.WELCOME);
		String command;
		int numberJob = 0;
		Set<String> backgroundCmd = new LinkedHashSet<String>();
		Map<String, Thread> threadMap = new HashMap<String, Thread>();
		Map<String, Integer> numberMap = new HashMap<String, Integer>();
		LinkedList<Thread> threadList = new LinkedList<Thread>();
		
		while(true) {
			//obtaining the command from the user
			System.out.print(Message.NEWCOMMAND);
			command = s.nextLine();
			if(command.equals("exit")) {
				break;
			}
			else if(command.endsWith("&")) {
				// does not wait for current command to complete
				// directly print ">" and accept new command
				command = command.substring(0, command.length()-1);
				ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
				if(filterlist!=null ) {
					// add to the thread linked list and start the thread
					Thread thread = new Thread(filterlist, command);
					thread.start();
					threadList.add(thread);
					backgroundCmd.add(thread.getName());
					threadMap.put(thread.getName(), thread);
					numberJob ++;
					numberMap.put(thread.getName(), numberJob);
					filterlist = (ConcurrentFilter) filterlist.getNext();
				}		
			}
			else if(command.equals("repl_jobs")) {		
				for(Thread thread: threadList) {
					if(thread.isAlive()) {
						//print out the commands that are still alive
						System.out.println("\t" + numberMap.get(thread.getName()) + ". " + thread.getName() + "&");
					}
				}
			}
			else if(command.startsWith("kill")) {	
				String[] commandArr = command.split("\\s+");
				if(commandArr.length==1) {
					System.out.print(Message.REQUIRES_PARAMETER.with_parameter(command));
				}
				if(commandArr.length==2) {
					try {
						//interrupt the command given its number
						int numberKill = Integer.parseInt(commandArr[1]);
						String[] arr = backgroundCmd.toArray(new String[backgroundCmd.size()]);
						String killString = arr[numberKill-1];
						Thread killThread = threadMap.get(killString);
						killThread.interrupt();
					}catch (NumberFormatException | NullPointerException nfe){
						System.out.print(Message.INVALID_PARAMETER.with_parameter(command));
					}
				}
			}
			else if(!command.trim().equals("")) {
				//building the filters list from the command
				ConcurrentFilter filterlist = ConcurrentCommandBuilder.createFiltersFromCommand(command);
				while(filterlist != null) {
					filterlist.process();
					filterlist = (ConcurrentFilter) filterlist.getNext();
				}
			}
		}
		s.close();
		System.out.print(Message.GOODBYE);
	}

}
