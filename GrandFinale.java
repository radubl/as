import uk.ac.warwick.dcs.maze.logic.*;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.*;
import java.awt.Point;
import java.lang.reflect.*;
import java.lang.Class;	
import java.util.*;
import javax.swing.*;

public class GrandFinale
{
	private Test2 test2 = new Test2();								// Input Panel
	private Maze maze;										// The Maze
	private HighScore highscore = new HighScore();						// Highscores
	private int pollRun = 0;									// moves Counter
 	private Point food;										// Food :)
 	private int fx;											
 	private int fy;											// Coordinates
	private Stack<Point> tail;									// Tail
	private boolean gamegoing = true;						
	private MazeData mazeData;
	private int errorCount = 0;
	private int countRuns = 0;
	private boolean notshown = true;
	private int option = -1;

 public void controlRobot(IRobot robot) {
  
  if(countRuns % 5 == 4 && notshown && option != 1){ 											//Choose run mode
	option = JOptionPane.showConfirmDialog(null, "Do you want to continue? It gets kind of boring.", "warning", JOptionPane.YES_NO_OPTION); 
	JOptionPane.showMessageDialog(null, "Enter the input box on the upper left corner of the screen and control with W A S D.");
	notshown = false; }

  if(option != 1)															//Maze solving
  {
	try{
	if(pollRun == 0 ){
		mazeData= new MazeData(robot); 												//Check for AStar, check if unsolvable
		pollRun++; 	
	}
	} catch( java.util.EmptyStackException e ){ 
		if(errorCount == 0){ 
			JOptionPane.showMessageDialog(null, "The maze is unsolvable");
  			EventBus.broadcast(new Event(100, mazeData.came_from.peek()));
			errorCount++; }
	} finally{
	if(errorCount == 0){
	if(mazeData.getCounter()>0) robot.setHeading(checkdirection(mazeData.came_from,mazeData.getCounter()));				// Move the robot according to stack counter
 	EventBus.broadcast(new Event(100, mazeData.came_from.elementAt(mazeData.getCounter())));
 	
	mazeData.setCounter(mazeData.getCounter()-1); }											// Increase counter		
	}
  }	
  else{																	// Snake mode
	if(pollRun == 0 ){														// if at the beginnig, reset parameters								
		maze = SnakeMaze(robot); 
		pollRun++; 
		tail = new Stack<Point>();
		tail.push(new Point(robot.getLocationX(),robot.getLocationY()));
	} 
	
	if(gamegoing)maze = changeMaze(robot, maze, tail);										// Move the snake
 	EventBus.broadcast(new Event(107, maze));
  }
 }
   
 public Maze changeMaze(IRobot robot, Maze maze, Stack<Point> tail){									// Changes the maze accordingly to snake location/tail
	
	Point location = new Point(robot.getLocationX(),robot.getLocationY());
	
	switch(robot.getHeading()){
	case 1000:	switch(test2.x){
			case 'w' :robot.setHeading(1000); if(location.y!=0)location.y--; else location.y=maze.getHeight()-1; break;
			case 'a' :robot.setHeading(1003); if(location.x!=0)location.x--; else location.x=maze.getWidth()-1; break;
			case 'd' :robot.setHeading(1001); if(location.x!=maze.getWidth()-1)location.x++; else location.x=0; break;
			default: robot.setHeading(1000); if(location.y!=0)location.y--; else location.y=maze.getHeight()-1; break;
			} break;
	case 1001:	switch(test2.x){
			case 'w' :robot.setHeading(1000); if(location.y!=0)location.y--; else location.y=maze.getHeight()-1; break;
			case 's' :robot.setHeading(1002); if(location.y!=maze.getHeight()-1)location.y++; else location.y=0; break;
			case 'd' :robot.setHeading(1001); if(location.x!=maze.getWidth()-1)location.x++; else location.x=0; break;
			default: robot.setHeading(1001); if(location.x!=maze.getWidth()-1)location.x++; else location.x=0; break;
			} break;
	case 1002:	switch(test2.x){
			case 'a' :robot.setHeading(1003); if(location.x!=0)location.x--; else location.x=maze.getWidth()-1; break;
			case 's' :robot.setHeading(1002); if(location.y!=maze.getHeight()-1)location.y++; else location.y=0; break;
			case 'd' :robot.setHeading(1001); if(location.x!=maze.getWidth()-1)location.x++; else location.x=0; break;
			default: robot.setHeading(1002); if(location.y!=maze.getHeight()-1)location.y++; else location.y=0; break;
			} break;
	case 1003:	switch(test2.x){
			case 'w' :robot.setHeading(1000); if(location.y!=0)location.y--; else location.y=maze.getHeight()-1; break;
			case 'a' :robot.setHeading(1003); if(location.x!=0)location.x--; else location.x=maze.getWidth()-1; break;
			case 's' :robot.setHeading(1002); if(location.y!=maze.getHeight()-1)location.y++; else location.y=0; break;
			default: robot.setHeading(1003); if(location.x!=0)location.x--; else location.x=maze.getWidth()-1; break;
			} break;
	}
 	if(contains(tail,location.x,location.y)){											// If hit tail, game over, display score / high score
		int score = pollRun*(pollRun+1)/2;
		if(score > highscore.getHighScore()){ JOptionPane.showMessageDialog(null, " New High Score: " + score);
						      highscore.setHighScore(score); }
		else JOptionPane.showMessageDialog(null, "Game over. Your score is " + score);
		gamegoing=false;
		}
	else																//If not hit tail, continue
		maze.setStart(location.x,location.y);
	maze.setFinish(501,501);													//get rid of the start

	if(maze.getCellType(location)==2){												//if no food available, create new food
		pollRun++;
		do{
			fy = (int)(Math.random()*(maze.getHeight()-1));
			fx = (int)(Math.random()*(maze.getWidth()-1));
		}while(contains(tail,fx,fy));
		
		if(gamegoing)maze.toggleCellType(fx,fy);
		
 		tail.push(location);
		}
	tail.add(0,location);														// if food eaten, add to tail
 	maze.setCellType(tail.peek().x,tail.peek().y,1); 
 	tail.pop();
	
 	for( Point i : tail ) maze.setCellType(i.x,i.y,2);										//Move tail
 	
	return maze;	
 }
	
  public boolean contains(Stack<Point> tail, int fx, int fy){										// return true if point is in stack
    boolean check = false;
    for( Point i : tail ) 
	    if(i.x==fx && i.y==fy) { check =true; break; }
    return check;
		  
  }
  
  public Maze SnakeMaze(IRobot robot){													// create the snake maze
  
    Maze maze = getnewMaze(robot);
    
    for(int i=0;i<maze.getHeight();i++)													// empty the maze
	    for(int j=0;j<maze.getWidth();j++)
		    if(maze.getCellType(j,i)==2) maze.toggleCellType(j,i);
		    
    maze.setStart(maze.getWidth()/2,maze.getHeight()/2);
    fy = (int)(Math.random()*maze.getHeight());
    fx = (int)(Math.random()*maze.getWidth());
    maze.toggleCellType(fx,fy);														// set initial food
    return maze;
  }
  
  public Maze getnewMaze(IRobot robot){													// get maze through Reflecsion at runtime
	  try {
	  Field mazeField = robot.getClass().getDeclaredField("maze");
	  mazeField.setAccessible(true);
	  maze = (Maze) mazeField.get(robot);
	  } catch ( IllegalAccessException e ) {  
	  } catch ( NoSuchFieldException e ) {
	  } finally{
		  return maze;
	  }
  }

  public int checkdirection(Stack<APoint> came_from, int i){									//check direction for maze-solving mode
	int direction = 0;
	if(came_from.elementAt(i).x == came_from.elementAt(i-1).x && came_from.elementAt(i).y == came_from.elementAt(i-1).y+1)
		direction = 1000+0;
	if(came_from.elementAt(i).x == came_from.elementAt(i-1).x && came_from.elementAt(i).y == came_from.elementAt(i-1).y-1)
		direction = 1000+2;
	if(came_from.elementAt(i).x == came_from.elementAt(i-1).x+1 && came_from.elementAt(i).y == came_from.elementAt(i-1).y)
		direction = 1000+3;
	if(came_from.elementAt(i).x == came_from.elementAt(i-1).x-1 && came_from.elementAt(i).y == came_from.elementAt(i-1).y)
		direction = 1000+1;
	return direction;
 }	
	
  public void reset(){ notshown=true; mazeData.resetCounter(); pollRun = 0; errorCount = 0; gamegoing = true; countRuns++;}		
																			// reset local parameters when moe is over
}

 class Test2 extends JFrame implements KeyListener											// create the input panel
{
	private char x = 'd';
	private JTextPane j;
	public Test2 ()
	{
		super ("title");
		setBounds (0, 0, 200, 200);
		setDefaultCloseOperation (EXIT_ON_CLOSE);
		j = new JTextPane ();
		j.setBackground(Color.BLACK);
		j.setForeground(Color.LIGHT_GRAY);
		j.setFont(new Font("courier", Font.BOLD, 15));
		j.addKeyListener (this);
		getContentPane ().add (j);
		setVisible (true);
    }
		public void keyPressed (KeyEvent arg0)  {   }
		public void keyReleased (KeyEvent arg0)  {  }
		public void keyTyped (KeyEvent arg0)
    {
		char c = arg0.getKeyChar ();
		x = (char) c ;
    }
		private void print (String s)
    {
		j.setText (j.getText () + s);
    }
		private void println (String s)
    {
		j.setText (j.getText () + s + "\n");
    }

		public static void createPanel ()
    {
		new Test2 ();
    }
}

class HighScore{													// Class for high scores storing
	private int highscore = 0;
	public HighScore(){ highscore = 0; }
	public int getHighScore() { return highscore; }
	public void setHighScore(int a){ highscore = a; }
}

class MazeData{														// MazeData for AStar implementing

	private int counter;
	private IRobot robot;
	private Stack<APoint> came_from;
	private Maze maze;
 
 public MazeData(IRobot robot){												// constructor				
	this.robot = robot;
	maze = getnewMaze(robot);
	came_from = Astar(maze);
	counter = came_from.indexOf(came_from.peek());
 }
    
 public Maze getnewMaze(IRobot robot){											// get maze
	try {
	    Field mazeField = robot.getClass().getDeclaredField("maze");
	    mazeField.setAccessible(true);
	    maze = (Maze) mazeField.get(robot);
	    } catch ( IllegalAccessException e ) {  
	    } catch ( NoSuchFieldException e ) {
	    } finally{
		    return maze;
	}
 }
	  
	public Stack<APoint> Astar(Maze maze){							// Search shortest path with AStar
	
		Stack<APoint> closedset = new Stack<APoint>();					// The set of nodes already evaluated.
		Stack<APoint> openset = new Stack<APoint>();					// The set of tentative nodes to be evaluated, initially containing the start node  
		APoint start = new APoint(maze.getStart().x, maze.getStart().y);
		APoint goal =  new APoint(maze.getFinish().x, maze.getFinish().y);
		openset.push(start);									// add start point to openset
		
		while (openset.capacity() != 0){
		APoint current = openset.elementAt(minfscore(maze, openset)); 		// check for the best node to consider at the moment
			if (current.x == goal.x && current.y == goal.y){			// if it is the goal, we are done, reconstruct the path
					APoint curr = current;
					Stack<APoint> ret = new Stack<APoint>();
					while(curr.parent != null) {
						ret.push(curr);
						curr = curr.parent;  }
					return ret;
			}
														//if not,
			openset.remove(current);								// remove current from openset
			closedset.push(current);								// add current to closedset
			
			for( APoint neighbor : neighbor_nodes(maze, current)){			// for all neighbours, check for the next viable step
				boolean neighborIsBetter = false;
				if(closedset.contains(neighbor))					
						continue;
						
				int g_score = current.g + 1;							// calculate current score
				if (!openset.contains(neighbor) ){ 	
					openset.add(neighbor);
					neighborIsBetter = true; 
					neighbor.h = Math.abs(neighbor.x-goal.x)+Math.abs(neighbor.y-goal.y);		// store heuristic
				}else if(g_score < neighbor.g) {
					neighborIsBetter = true; } 
				
				if (neighborIsBetter){								// store the best neighbour as next step
					neighbor.parent = current;
					neighbor.g = g_score;
					neighbor.f = neighbor.g + neighbor.h;
					}
				}
			}
		return closedset;
	}
	
 public static int minfscore(Maze maze, Stack<APoint> openset){					//Compute the Point with the minimum f_score in openset
	
	int aux = openset.indexOf(openset.peek());
	for( APoint i: openset)
		if( i.f <= openset.elementAt(aux).f)
			aux = openset.indexOf(i);
	return aux;
 }
	
 public static Stack<APoint> reverse(Stack<APoint> ret){						//reverse stack
		Stack<APoint> newret = new Stack<APoint>();
		while(ret.capacity() != 0)
			newret.push(ret.pop());
		return newret;
 }
	
 public static Stack<APoint> neighbor_nodes(Maze maze, APoint current){				// get neighbour nodes that are not walls
	
	Stack<APoint> neighbor_nodes = new Stack<APoint>(); 
	int x = current.x; 
	int y = current.y;
	APoint[] nb = new APoint[4];
	
	nb[0]=new APoint(x-1,y); nb[1]=new APoint(x+1,y);
	nb[2]=new APoint(x,y-1); nb[3]=new APoint(x,y+1);
	
	for(int i = 0;i<4;i++){
		Point iswall = new Point(nb[i].x, nb[i].y);
		if(maze.getCellType(iswall) == 1)
			neighbor_nodes.push(nb[i]);
			}
	return neighbor_nodes;
 }
 
 public void setCounter(int x){ counter = x; }
 public int getCounter(){ return counter; }
 public void resetCounter(){ counter = came_from.indexOf(came_from.peek()); }
}

class APoint extends Point{	
															// set a new Point class for Astar purposes
	int f = 0; int g = 0; int h = 0;
	APoint parent = null;
	
	public APoint(int x , int y) {
		super(x,y);
	}
}
