package main;
/** [ChatClient.java]
 * Group chat client class
 * @author Josh Cai
 */


//Imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import data_structures.SimpleLinkedList;
import exceptions.DuplicateException;
import player.Player;
import player.builds.Assassin;
import player.builds.Guard;
import player.builds.Sniper;
import player.builds.Soldier;

public class Client {

	// init vars and gui
	private Socket socket;
	private BufferedReader input;
	private PrintWriter output;

	private Player player;
	private SimpleLinkedList<Player> players;
	private boolean running;
	private String build;

	private int mapSize;
	private int[][] map;

	//	private String address;
	//	private int port;
	private String name;

	private final static int KILL_CREDIT = 50;
	private final static int DIE_CREDIT = 10;

	/**
	 * ChatClient 
	 * constructor
	 * @throws IOException 
	 */
	public Client(String address, int port, String name, int build) throws DuplicateException, IOException {		
		running = true;
		//		this.address = address;
		//		this.port = port;
		this.name = name;

		// get address and port and connect, then get username
		players = new SimpleLinkedList<Player>();

		//		address = "localhost"; // JOptionPane.showInputDialog("Enter IP Address:");
		//		port = 5001; //Integer.parseInt(JOptionPane.showInputDialog("Enter port (enter a number or else the program will crash):"));
		//		name = JOptionPane.showInputDialog("Enter username:").replace(" ", "");

		connect(address, port);

		output.println(name);
		output.flush();

		// check duplicate username
		String msg = input.readLine();

		// if the current username is already taken
		if (msg.equals("duplicate")) {
			throw new DuplicateException();
		}

		player = new Player(name);
		// System.out.println("reach builds");
		if (build == 0) {
			player.setBuild(new Assassin());
		} else if (build == 1) {
			player.setBuild(new Guard());
		} else if (build == 2) {
			player.setBuild(new Sniper());
		} else if (build == 3) {
			player.setBuild(new Soldier());
		}

		// start communication with server
		go();
	}
	/**
	 * Main
	 * @param args parameters from command line
	 */
	//	public static void main(String[] args) {
	//		c = new Client();
	//	}

	// getters
	public Player getPlayer() {
		return player;
	}

	public String getName() {
		return name;
	}

	public String getBuild() {
		return build;
	}

	public SimpleLinkedList<Player> getPlayers(){
		return players;
	}

	public int[][] getMap(){
		return map;
	}

	public void update() {
		output.println("xy " + player.getName() + " " + player.getX() + " " + player.getY());
		output.println("score " + player.getName() + " " + player.getScore());
		output.flush();
	}

	public void println(String msg) {
		output.println(msg);
		output.flush();
	}

	public void disconnect() {
		output.println("exit");
		output.flush();
		try {
			socket.close();
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		output.close();
	}


	/* go
	 * communicate  with server (get messages)
	 */
	private void go() {
		// OutputThread ot = new OutputThread();

		// call a method that connects to the server
		// after connecting loop and keep appending[.append()] to the JTextArea

		Runnable runnable = new Runnable() {
			String msg;
			String[] arr;

			public void run(){
				try {
					while (running) {
						if (input.ready()) { // check for an incoming messge
							msg = input.readLine(); // read the message
							arr = msg.split(" ");

							String command = arr[0];
							String second = "";
							if (arr.length > 1) {
								second = arr[1];
							}

							// used for looping through list of players
							boolean found = false;
							int count = 0;

							if (command.equals("map")) { // if send map
								mapSize = Integer.parseInt(second);
								map = new int[mapSize][mapSize];
								for (int i = 0; i < mapSize; i++) {
									msg = input.readLine();
									arr = msg.split(" ");
									for (int j = 0; j < arr.length; j++) {
										map[i][j] = Integer.parseInt(arr[j]);
									}
								}
							} else if (command.equals("add")) { // if add user
								players.add(new Player(second));
								int score = Integer.parseInt(arr[2]);
								player.setScore(score);
								
							} else if (command.equals("delete")) { // if delete user
								while (!found && count < players.size()) {
									if (players.get(count).getName().equals(second)) {
										players.remove(count);
										found = true;
										// System.out.println("found and removed " + count);
									}
									count++;
								}
								
							} else if (command.equals("xy")) {
								double x = Double.parseDouble(arr[2]);
								double y = Double.parseDouble(arr[3]);
								
								if (!second.equals(player.getName()) && x == -1 && y == -1) {
									System.out.println(second + " is at " + x + " " + y);
								}

								while (!found && count < players.size()) {
									Player p = players.get(count);

									if (p.getName().equals(second)) {
										p.setX(x);
										p.setY(y);
										found = true;
									}
									count++;
								}
								
							} else if (command.equals("score")) {
								int newScore = Integer.parseInt(arr[2]);

								if (player.getName().equals(second)) {
									player.setScore(newScore);
								} else {
									while (!found && count < players.size()) {
										Player p = players.get(count);

										if (p.getName().equals(second)) {
											p.setScore(newScore);
											found = true;
										}
										count++;
									}
								}
							} else if (command.equals("hit")) {
								double damage = Double.parseDouble(arr[2]);
								player.getBuild().setHealth(player.getBuild().getHealth() - damage);

								// if the player is killed from this shot
								if (player.getBuild().getHealth() + damage > 0 && player.getBuild().getHealth() <= 0) {
									String killer = arr[3];
									
									int newScore = 0;
									// find killer's original score
									while (!found && count < players.size()) {
										Player p = players.get(count);

										if (p.getName().equals(killer)) {
											p.setScore(p.getScore() + KILL_CREDIT);
											newScore = p.getScore();
											found = true;
										}
										count++;
									}
									output.println("score " + killer + " " + newScore);
									output.flush();

									// decrement player score
									player.setScore(player.getScore() - DIE_CREDIT);
									player.setEliminator(killer);
								}
							}
						}
					}

				} catch (IOException e) {
					e.printStackTrace();
				}

				try { // after leaving the main loop we need to close all the sockets
					input.close();
					output.close();
					socket.close();
				} catch (Exception l) {}
			}
		};

		Thread thread = new Thread(runnable);

		thread.start();
	}

	/*
	 * connect
	 * Connect to server
	 */
	private void connect(String address, int port) {
		try {
			socket = new Socket(address, port);
			input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			output = new PrintWriter(socket.getOutputStream());

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}