import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.*;

public class Sketch extends PApplet {	
	Flock flock;
    public static void main(String[] args) {
        PApplet.main("Sketch");
    }

    public void settings(){
        size(1000, 1000);
    }

    
    
    
    public void setup(){
        // Generate new Flock of 50 boids
    	flock = new Flock(100, this);
    	noStroke();
    }

    public void draw(){
        // Draw each boid
    	background(255);
        flock.drawBoids();
        flock.updateBoids();
    }
    
}