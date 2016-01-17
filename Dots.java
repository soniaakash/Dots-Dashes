
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Polygon;
import java.awt.Color;

import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

class Points {

	/*
	 *	
	 *	Points is the basic object that is drawn onto the screen.
	 *
	 */

    Polygon shape;	//	The shape that is to be drawn
    Color color;	//	The color of the shape
    int width;		//	Width of the Points
    int height;		//	Height of the Points
    int x;			//	Horizontal coordinate of the center of the Points
    int y;			//	Vertical coordinate of the center of the Points
    
    public Points() {
    	//	Initialize all the fields
        shape=new Polygon();
        width=0;
        height=0;
        x=0;
        y=0;
        color=Color.BLACK;
    }
    
    public void render(Graphics g) {
    	//	The render method is responsible for positioning the Points
    	//	at the proper location
    	
        g.setColor(color);
        
        Polygon renderedShape=new Polygon();
        for(int i=0; i<shape.npoints; i++) {
            int renderedx=shape.xpoints[i] + x + width / 2;
            int renderedy=shape.ypoints[i] + y + height / 2;
            renderedShape.addPoint(renderedx, renderedy);
        }
        g.fillPolygon(renderedShape);
    }
    
    public boolean containsPoint(int x, int y) {
    	//	This returns true only if the point (x, y)
    	//	is contained within the visible shape of the Points
    	
    	return shape.contains(x - this.x - width /2, y - this.y - height /2);
    }
}

class Connection extends Points {

	/*
	 *
	 *	There are two types of connections: vertical & horizontal.
	 *
	 */

    public static final int HORZ_CONN=1;
    public static final int VERT_CONN=2;
    
    boolean connectionMade;	// Tracks wether the Connection has been clicked on
    
    public Connection() {
    	// Initialize all the fields
        super();
        
        connectionMade=false;
        color=Color.WHITE;
    }
    
    public static Connection createConnection(int type, int x, int y) {
    	Connection conn=new Connection();
    	
        if(type==Connection.HORZ_CONN) {
        	conn.width=Dots.DOT_GAP;
        	conn.height=Dots.DOT_SIZE;
        } else if(type==Connection.VERT_CONN) {
        	conn.width=Dots.DOT_SIZE;
        	conn.height=Dots.DOT_GAP;
        } else {
        	return null;
        }
        
        conn.x=x;
        conn.y=y;
        
        conn.shape.addPoint(-conn.width/2, -conn.height/2);
        conn.shape.addPoint(-conn.width/2, conn.height/2);
        conn.shape.addPoint(conn.width/2, conn.height/2);
        conn.shape.addPoint(conn.width/2, -conn.height/2);
        
        return conn;
    }
}

class Box extends Points {

	/*
	 *
	 *	Box represent the actual boxes made up by the Dot i.e points & connections.
	 *
	 */

	Connection[] horizontalConnections;	//	The Connections that are the top and bottom borders of the box
	Connection[] verticalConnections;		//	The Connections that are the left and right borders of the box

	int player;	//	Tracks the player that closed the box

	public Box() {
		super();

		color=Color.WHITE;	//	Initially the box should be the same color as the background

		horizontalConnections=new Connection[2];
		verticalConnections=new Connection[2];

		width=Dots.DOT_GAP;
		height=Dots.DOT_GAP;

		shape.addPoint(-width/2, -height/2);
        shape.addPoint(-width/2, height/2);
        shape.addPoint(width/2, height/2);
        shape.addPoint(width/2, -height/2);
	}	

	public boolean isBoxed() {
		boolean boxed=true;

		for(int i=0; i<2; i++) {
			if(!horizontalConnections[i].connectionMade || !verticalConnections[i].connectionMade) {
				boxed=false;
			}
		}

		return boxed;
	}

	public static Box createBox(int x, int y, Connection[] horizontalConnections, Connection[] verticalConnections) {
		Box box=new Box();
		box.player=0;
		box.x=x;
		box.y=y;
		box.horizontalConnections=horizontalConnections;
		box.verticalConnections=verticalConnections;
		return box;
	}
}

public class Dots extends JFrame implements MouseMotionListener, MouseListener {
    
    public static final int DOT_NUMBER=5;	//	The number of dots on each side of the square game board
    public static final int DOT_GAP=24;		//	The space between each dot					
    public static final int DOT_SIZE=4;		//	The length of the sides of the square dot
    
    public static final int PLAYER_ONE=1;
    public static final int PLAYER_TWO=2;
    
    public static final Color PLAYER_ONE_COLOR=Color.ORANGE;	//	The color of player1's boxes
    public static final Color PLAYER_TWO_COLOR=Color.GREEN;		// 	The color of player2's boxes
    
    private Connection[] horizontalConnections;	//	Array for all the Connections that horizontally connect dots
    private Connection[] verticalConnections;		//	Array for all the Connections that vertically connect dots
    private Box[] boxes;	//	Array for all the Boxs
    private Points[] dots;		//	Array for all the dots
    
    private Dimension dim;		//	Window dimensions
    
    private int clickx;		//	Holds the x coordinate of mouse click
    private int clicky;		// 	Holds the y coordinate of mouse click
    
    private int mousex;		// 	Holds the x coordinate of the mouse location
    private int mousey; 	// 	Holds the y coordinate of the mouse location
    	
    private int centerx;	//	x coordinate of the center of the gameboard
    private int centery; 	// 	y coordinate of the center of the gameborad 
    	
    private int side;	//	Length of the sides of the square gameboard
    private int space;	// Length of 1 dot + 1 connection
    	
    private int activePlayer;	// 	Holds the current player

    public Dots() {
        super("Connect the Dots");
        setSize(500, 600);
        setResizable(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addMouseListener(this);
        addMouseMotionListener(this);
        
        loadProperties();
        loadDots();
        
        startNewGame();
        
        setVisible(true);
    }
    
    private void loadProperties() {
    	//	Initialize fields
    	
        clickx=0;
        clicky=0;
        mousex=0;
        mousey=0;
        
        dim=getSize();					// Get the size set by setSize(500, 600);
        centerx=dim.width/2;
        centery=(dim.height - 100) /2;
        
        side=DOT_NUMBER * DOT_SIZE + (DOT_NUMBER - 1) * DOT_GAP;	//	There is one less connection than dot per side
    	space=DOT_SIZE + DOT_GAP;
    }
    
    private void loadConnections() {
    	
        horizontalConnections=new Connection[(DOT_NUMBER-1) * DOT_NUMBER];
        verticalConnections=new Connection[(DOT_NUMBER-1) * DOT_NUMBER];
        
        /*
         *
         *	colsy and rowsy track the columns and rows for the vertical connections. 
         *   
         *	colsy=rowsx and rowsy=colsx will put the vertical connections on the correct place on the screen. 
         *	
         *
         */
        
        for(int i=0; i<horizontalConnections.length; i++) {
        	int colsx=i % (DOT_NUMBER-1);
        	int rowsx=i / (DOT_NUMBER-1);
        	int horx=centerx - side / 2 + DOT_SIZE + colsx * space;
        	int hory=centery - side / 2 + rowsx * space;
        	horizontalConnections[i]=Connection.createConnection(Connection.HORZ_CONN, horx, hory);
        	
        	int colsy=i % DOT_NUMBER;
        	int rowsy=i / DOT_NUMBER;
        	int vertx=centerx - side / 2 + colsy * space;
        	int verty=centery - side / 2 + DOT_SIZE + rowsy * space;
        	verticalConnections[i]=Connection.createConnection(Connection.VERT_CONN, vertx, verty);
        }
    } 
    	
    private void loadBoxes() {
    	
    	/*
    	 *
    	 *	loadBoxes cycles through the box grid the way loadConnection does. There is one less box per side
    	 *	than dot per side.
    	 *
    	 */
    	
    	boxes=new Box[(DOT_NUMBER-1) * (DOT_NUMBER-1)];
    	
    	for(int i=0; i<boxes.length; i++) {
    		int cols=i % (DOT_NUMBER-1);
    		int rows=i / (DOT_NUMBER-1);
    		
    		int boxx=centerx - side / 2 + DOT_SIZE + cols * space;
    		int boxy=centery - side / 2 + DOT_SIZE + rows * space;
    		
    		Connection[] horConn=new Connection[2];
    		horConn[0]=horizontalConnections[i];
    		horConn[1]=horizontalConnections[i + (DOT_NUMBER - 1)];
    		
    		Connection[] verConn=new Connection[2];		//	This only works if the verticalConnections were put into the array rows then columns
    		verConn[0]=verticalConnections[i + rows];
    		verConn[1]=verticalConnections[i + rows + 1]; 		
    		
    		boxes[i]=Box.createBox(boxx, boxy, horConn, verConn);
    	}
    }
    
    private void loadDots() {

        dots=new Points[DOT_NUMBER * DOT_NUMBER];
        for(int rows=0; rows<DOT_NUMBER; rows++) {
            for(int cols=0; cols<DOT_NUMBER; cols++) {
                Points dot=new Points();
                dot.width=DOT_SIZE;
                dot.height=DOT_SIZE;
                dot.x=centerx - side/2 + cols * space;
                dot.y=centery - side/2 + rows * space;
                dot.shape.addPoint(-DOT_SIZE, -DOT_SIZE);
                dot.shape.addPoint(-DOT_SIZE, DOT_SIZE);
                dot.shape.addPoint(DOT_SIZE, DOT_SIZE);
                dot.shape.addPoint(DOT_SIZE, -DOT_SIZE);
                int index=rows * DOT_NUMBER + cols;
                dots[index]=dot;
            }
        }
    }
    
    private void startNewGame() {
    	activePlayer=PLAYER_ONE;
    	loadConnections();
        loadBoxes();
    }
    
    private Connection getConnection(int x, int y) {
    	
    	// Get the connection that encloses point (x, y) or return null if there isn't one
    	
    	for(int i=0; i<horizontalConnections.length; i++) {
    		if(horizontalConnections[i].containsPoint(x, y)) {
    			return horizontalConnections[i];			
    		}
    	}
    	
    	for(int i=0; i<verticalConnections.length; i++) {
    		if(verticalConnections[i].containsPoint(x, y)) {
    			return verticalConnections[i];
    		}
    	}
    	
    	return null;
    }
    
    private boolean[] getBoxStatus() {
    	boolean[] status=new boolean[boxes.length];
    	
    	for(int i=0; i<status.length; i++) {
    		status[i]=boxes[i].isBoxed();
    	}
    	
    	return status;
    }
    
    private int[] calculateScores() {
    	int[] scores={0, 0};
    	
    	for(int i=0; i<boxes.length; i++) {
    		if(boxes[i].isBoxed() && boxes[i].player!=0) {
    			scores[boxes[i].player - 1]++;
    		}
    	}
    	
    	return scores;
    }
    
    private boolean makeConnection(Connection connection) {
    	boolean newBox=false;
    	
    	boolean[] boxStatusBeforeConnection=getBoxStatus();	//	The two boolean arrays are used to see if a new box was created after the connection was made
    	
    	connection.connectionMade=true;
    	
    	boolean[] boxStatusAfterConnection=getBoxStatus();
    	
    	for(int i=0; i<boxes.length; i++) {
    		if(boxStatusAfterConnection[i]!=boxStatusBeforeConnection[i]) {
    			newBox=true;
    			boxes[i].player=activePlayer;
    		}
    	}
    	
    	if(!newBox) {	//	Allow the current player to go again if he made a box
    		if(activePlayer==PLAYER_ONE)
    			activePlayer=PLAYER_TWO;
    		else
    			activePlayer=PLAYER_ONE;
    	} 	
    	
    	checkForGameOver();
    	
    	return newBox;
    }
    
    private void checkForGameOver() {
    	int[] scores=calculateScores();
    	if((scores[0] + scores[1])==((DOT_NUMBER - 1) * (DOT_NUMBER - 1))) {
    		JOptionPane.showMessageDialog(this, "Player1: " + scores[0] + "\nPlayer2: " + scores[1], "Game Over", JOptionPane.PLAIN_MESSAGE);
    		startNewGame();
    		repaint();
    	}
    }
    
    private void handleClick() {
    	Connection connection=getConnection(clickx, clicky);
    	if(connection==null)
    		return;
    	
    	if(!connection.connectionMade) {
    		makeConnection(connection);
    		
    	}    		
    	
    	repaint();
    }
    
    public void mouseMoved(MouseEvent event) {
    	mousex=event.getX();
    	mousey=event.getY();
    	repaint();
    }
    
    public void mouseDragged(MouseEvent event) {
    	mouseMoved(event);
    }
    
    public void mouseClicked(MouseEvent event) {
    	clickx=event.getX();
    	clicky=event.getY();
    	
    	handleClick();
    }
    
    public void mouseEntered(MouseEvent event) {	
    }
    
    public void mouseExited(MouseEvent event) {	
    }
    
    public void mousePressed(MouseEvent event) {
    }
    
    public void mouseReleased(MouseEvent event) {
    }
    
    private void paintBackground(Graphics g) {
    	g.setColor(Color.WHITE);
    	g.fillRect(0, 0, dim.width, dim.height);
    }
    
    private void paintDots(Graphics g) {
    	for(int i=0; i<dots.length; i++) {
    		dots[i].render(g);
    	}
    }
    
    private void paintConnections(Graphics g) {
    	for(int i=0; i<horizontalConnections.length; i++) {
    		
    		if(!horizontalConnections[i].connectionMade) {
    			if(horizontalConnections[i].containsPoint(mousex, mousey)) {
    				horizontalConnections[i].color=Color.RED;
    			} else {
    				horizontalConnections[i].color=Color.WHITE;
    			}
    		} else {
    			horizontalConnections[i].color=Color.BLUE;
    		}
    		
    		horizontalConnections[i].render(g);
    	}
    	
    	for(int i=0; i<verticalConnections.length; i++) {
    		
    		if(!verticalConnections[i].connectionMade) {
    			if(verticalConnections[i].containsPoint(mousex, mousey)) {
    				verticalConnections[i].color=Color.RED;
    			} else {
    				verticalConnections[i].color=Color.WHITE;
    			}
    		} else {
    			verticalConnections[i].color=Color.BLUE;
    		}
    		
    		verticalConnections[i].render(g);
    	}
    }
    
    public void paintBoxes(Graphics g) {
    	for(int i=0; i<boxes.length; i++) {
    		if(boxes[i].isBoxed()) {
    			if(boxes[i].player==PLAYER_ONE) {
    				boxes[i].color=PLAYER_ONE_COLOR;
    			} else if(boxes[i].player==PLAYER_TWO) {
    				boxes[i].color=PLAYER_TWO_COLOR;
    			}
    		} else {
    			boxes[i].color=Color.WHITE;
    		}
    		
    		boxes[i].render(g);
    	}
    }
    
    public void paintStatus(Graphics g) {
    	int[] scores=calculateScores();
    	String status="It is player" + activePlayer + "'s turn";
    	String status2="Player 1: " + scores[0];
    	String status3="Player 2: " + scores[1];
    	
    	//Color currentColor=(activePlayer==PLAYER_ONE) ? PLAYER_ONE_COLOR : PLAYER_TWO_COLOR ;
    	//g.setColor(currentColor);
    	g.setColor(Color.BLACK);
    	g.drawString(status, 10, dim.height-50);
    	
    	g.setColor(PLAYER_ONE_COLOR);
    	g.drawString(status2, 10, dim.height-35);
    	
    	g.setColor(PLAYER_TWO_COLOR);
    	g.drawString(status3, 10, dim.height-20);
    }
    
    public void update(Graphics g) {
    	paint(g);
    }
    
    public void paint(Graphics g) {
    	
    	Image bufferImage=createImage(dim.width, dim.height);
    	Graphics bufferGraphics=bufferImage.getGraphics();
    	
    	paintBackground(bufferGraphics);    	
    	paintDots(bufferGraphics);   	
    	paintConnections(bufferGraphics);
    	paintBoxes(bufferGraphics);
    	paintStatus(bufferGraphics);
    	
    	g.drawImage(bufferImage, 0, 0, null);
    }
    
    public static void main(String[] args) {
    	new Dots();
    }
}
