/* Drew Schuster */
import javax.swing.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import javax.swing.JApplet;
import java.awt.*;
import java.util.*;
import java.lang.*;

/* This class contains the entire game... most of the game logic is in the Board class but this
   creates the gui and captures mouse and keyboard input, as well as controls the game states */
public class Pacman extends JApplet implements MouseListener, KeyListener
{ 

  /* These timers are used to kill title, game over, and victory screens after a set idle period (5 seconds)*/
  long titleTimer = -1;
  long timer = -1;

  /* Create a new board */
  Board b=new Board(); 

  /* This timer is used to do request new frames be drawn*/
  javax.swing.Timer frameTimer;
 

  /* This constructor creates the entire game essentially */   
  public Pacman()
  {
    b.requestFocus();

    /* Create and set up window frame*/
    JFrame f=new JFrame(); 
    f.setSize(420,460);

    /* Add the board to the frame */
    f.add(b,BorderLayout.CENTER);

    /*Set listeners for mouse actions and button clicks*/
    b.addMouseListener(this);  
    b.addKeyListener(this);  

    /* Make frame visible, disable resizing */
    f.setVisible(true);
    f.setResizable(false);

    /* Set the New flag to 1 because this is a new game */
    b.New=1;

    /* Manually call the first frameStep to initialize the game. */
    stepFrame(true);

    /* Create a timer that calls stepFrame every 30 milliseconds */
    frameTimer = new javax.swing.Timer(30,new ActionListener()
      {
        public void actionPerformed(ActionEvent e)
        {
          stepFrame(false);
        }
      });

    /* Start the timer */
    frameTimer.start();

    b.requestFocus();
  }

  /* This repaint function repaints only the parts of the screen that may have changed.
     Namely the area around every player ghost and the menu bars
  */
  public void repaint()
  {
    if (b.player.teleport)
    {
      b.repaint(b.player.lastX-20,b.player.lastY-20,80,80);
      b.player.teleport=false;
    }
    b.repaint(0,0,600,20);
    b.repaint(0,420,600,40);
    b.repaint(b.player.x-20,b.player.y-20,80,80);
    b.repaint(b.ghost1.x-20,b.ghost1.y-20,80,80);
    b.repaint(b.ghost2.x-20,b.ghost2.y-20,80,80);
    b.repaint(b.ghost3.x-20,b.ghost3.y-20,80,80);
    b.repaint(b.ghost4.x-20,b.ghost4.y-20,80,80);
  }

  /* Steps the screen forward one frame */
  public void stepFrame(boolean New)
  {
    /* If we aren't on a special screen than the timers can be set to -1 to disable them */
    if (!b.titleScreen && !b.winScreen && !b.overScreen)
    {
      timer = -1;
      titleTimer = -1;
    }

    /* If we are playing the dying animation, keep advancing frames until the animation is complete */
    if (b.dying>0)
    {
      b.repaint();
      return;
    }

    /* New can either be specified by the New parameter in stepFrame function call or by the state
       of b.New.  Update New accordingly */ 
    New = New || (b.New !=0) ;

    /* If this is the title screen, make sure to only stay on the title screen for 5 seconds.
       If after 5 seconds the user hasn't started a game, start up demo mode */
    if (b.titleScreen)
    {
      if (titleTimer == -1)
      {
        titleTimer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - titleTimer >= 5000)
      {
        b.titleScreen = false;
        b.demo = true;
        titleTimer = -1;
      }
      b.repaint();
      return;
    }
 
    /* If this is the win screen or game over screen, make sure to only stay on the screen for 5 seconds.
       If after 5 seconds the user hasn't pressed a key, go to title screen */
    else if (b.winScreen || b.overScreen)
    {
      if (timer == -1)
      {
        timer = System.currentTimeMillis();
      }

      long currTime = System.currentTimeMillis();
      if (currTime - timer >= 5000)
      {
        b.winScreen = false;
        b.overScreen = false;
        b.titleScreen = true;
        timer = -1;
      }
      b.repaint();
      return;
    }


    /* If we have a normal game state, move all pieces and update pellet status */
    if (!New)
    {
      /* The pacman player has two functions, demoMove if we're in demo mode and move if we're in
         user playable mode.  Call the appropriate one here */
      if (b.demo)
      {
        b.player.demoMove();
      }
      else
      {
        b.player.move();
      }

      /* Also move the ghosts, and update the pellet states */
      b.ghost1.move(); 
      b.ghost2.move(); 
      b.ghost3.move(); 
      b.ghost4.move(); 
      b.player.updatePellet();
      b.ghost1.updatePellet();
      b.ghost2.updatePellet();
      b.ghost3.updatePellet();
      b.ghost4.updatePellet();
    }

    /* We either have a new game or the user has died, either way we have to reset the board */
    if (b.stopped || New)
    {
      /*Temporarily stop advancing frames */
      frameTimer.stop();

      /* If user is dying ... */
      while (b.dying >0)
      {
        /* Play dying animation. */
        stepFrame(false);
      }

      /* Move all game elements back to starting positions and orientations */
      b.player.currDirection='L';
      b.player.direction='L';
      b.player.desiredDirection='L';
      b.player.x = 200;
      b.player.y = 300;
      b.ghost1.x = 180;
      b.ghost1.y = 180;
      b.ghost2.x = 200;
      b.ghost2.y = 180;
      b.ghost3.x = 220;
      b.ghost3.y = 180;
      b.ghost4.x = 220;
      b.ghost4.y = 180;

      /* Advance a frame to display main state*/
      b.repaint(0,0,600,600);

      /*Start advancing frames once again*/
      b.stopped=false;
      frameTimer.start();
    }
    /* Otherwise we're in a normal state, advance one frame*/
    else
    {
      repaint(); 
    }
  }  

  /* Handles user key presses*/
  public void keyPressed(KeyEvent e) 
  {
    /* Pressing a key in the title screen starts a game */
    if (b.titleScreen)
    {
      b.titleScreen = false;
      return;
    }
    /* Pressing a key in the win screen or game over screen goes to the title screen */
    else if (b.winScreen || b.overScreen)
    {
      b.titleScreen = true;
      b.winScreen = false;
      b.overScreen = false;
      return;
    }
    /* Pressing a key during a demo kills the demo mode and starts a new game */
    else if (b.demo)
    {
      b.demo=false;
      /* Stop any pacman eating sounds */
      b.sounds.nomNomStop();
      b.New=1;
      return;
    }

    /* Otherwise, key presses control the player! */ 
    switch(e.getKeyCode())
    {
      case KeyEvent.VK_LEFT:
       b.player.desiredDirection='L';
       break;     
      case KeyEvent.VK_RIGHT:
       b.player.desiredDirection='R';
       break;     
      case KeyEvent.VK_UP:
       b.player.desiredDirection='U';
       break;     
      case KeyEvent.VK_DOWN:
       b.player.desiredDirection='D';
       break;     
    }

    repaint();
  }

  /* This function detects user clicks on the menu items on the bottom of the screen */
  public void mousePressed(MouseEvent e){
    if (b.titleScreen || b.winScreen || b.overScreen)
    {
      /* If we aren't in the game where a menu is showing, ignore clicks */
      return;
    }

    /* Get coordinates of click */
    int x = e.getX();
    int y = e.getY();
    if ( 400 <= y && y <= 460)
    {
      if ( 100 <= x && x <= 150)
      {
        /* New game has been clicked */
        b.New = 1;
      }
      else if (180 <= x && x <= 300)
      {
        /* Clear high scores has been clicked */
        b.clearHighScores();
      }
      else if (350 <= x && x <= 420)
      {
        /* Exit has been clicked */
        System.exit(0);
      }
    }
  }
  
 
  public void mouseEntered(MouseEvent e){}
  public void mouseExited(MouseEvent e){}
  public void mouseReleased(MouseEvent e){}
  public void mouseClicked(MouseEvent e){}
  public void keyReleased(KeyEvent e){}
  public void keyTyped(KeyEvent e){}
  
  /* Main function simply creates a new pacman instance*/
  public static void main(String [] args)
  {
      Pacman c = new Pacman();
  } 
}
