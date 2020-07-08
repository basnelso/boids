
public class Configuration {
	boolean activateCohesion, activateAlignment, activateSeparation;
	boolean showCohesion, showAlignment, showSeparation;
	boolean wrapAround;
	
	final int BOID_SIZE = 5;
	final float BOID_TIP_ANGLE = 30.0f;
	final int BOID_VELOCITY_TAIL = 25;
	
	final float VISION_RADIUS = 250.0f;
	final float SEPERATION_RADIUS = 60.0f;
	
	final float COHESION_CONSTANT = 0.01f;
	final float ALIGNMENT_CONSTANT = 0.125f;
	final float SEPARATION_CONSTANT = 0.01f; // Prob good
	
	public float MAX_SPEED = 7;
	final float MIN_SPEED = 2.5f;
	final int THINK_TIME = 5;
	
	Configuration() {
		activateCohesion = true;
		activateAlignment = true;
		activateSeparation = true;
		showCohesion = false;
		showAlignment = false;
		showSeparation = false;
		wrapAround = true;
	}
}
