import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.*;

public class Sketch extends PApplet {	
	Flock flock;
	GUI gui;
	Configuration config;
	
    public static void main(String[] args) {
        PApplet.main("Sketch");
    }

    public void settings(){
        size(1000, 1000);
    }

    public void setup(){
        // Generate new Flock of 50 boids
    	config = new Configuration();
    	gui = new GUI(config, this);
    	flock = new Flock(200, this, config);
    	noStroke();
    }

    public void draw(){
        // Draw each boid
    	background(255);
        flock.updateBoids();
        flock.drawBoids();
        gui.drawGUI();
    }
    
    public void mousePressed() {
    	gui.mousePressed();
    }
    
}