import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;
import processing.core.PGraphics;

public class Flock {
	ArrayList<Boid> boids;
	Set<Boid> tracked_boids;
	final PApplet p; // Gives access to processing functions
	
	final int BOID_SIZE = 5;
	final float BOID_TIP_ANGLE = 30.0f;
	
	final float VISION_RADIUS = 250.0f;
	final float SEPERATION_RADIUS = 40.0f;
	
	final float COHESION_CONSTANT = 0.05f;
	final float ALIGNMENT_CONSTANT = 0.125f;
	final float SEPARATION_CONSTANT = 0.05f; // Prob good
	
	final float MAX_SPEED = 7;
	final float MIN_SPEED = 2;
	final int THINK_TIME = 5;
	
	// Flock Constructor
	Flock(int num, final PApplet p) {
		boids = new ArrayList<Boid>(num);
		// Add {num} boids in randomly selected positions
		for (int i = 0; i < num; i++) {
			boids.add(new Boid(p.random(0, p.width), p.random(0, p.height)));
		}
		// Pickup the PApplet to have access to processing functions
		this.p = p;
		
		// Manually add some tracked boids
		boids.get(0).tracked = true;
		boids.get(1).tracked = true;
	}

	// Method that iterates through the set of boids and draws each one
	public void drawBoids() {
		// Draw each boid in the flock
		for (Boid b : this.boids) {
			if (b.tracked) {
				b.drawBoid(p.color(0, 0, 255), p.color(0, 0, 255, 10));
			} else {
				b.drawBoid(p.color(0, 0, 0), p.color(255, 255, 255, 10));
			}
		}
	}

	// Method that iterates through the set of boids and updates the position of
	// each one based on its velocity and angle
	public void updateBoids() {
		// Update each boid in the flock
		for (Boid b : this.boids) {
			b.updateBoid();
		}
	}

	class Boid {
		PVector pos, posOld, velocity;
		Set<Boid> seen;
		int thinkTimer;
		float angle;
		boolean tracked, seenByTracked;
		
		Boid(float x, float y) {
			pos = new PVector(x, y);
			posOld = new PVector(0, 0);
			velocity = PVector.random2D().mult((3*MAX_SPEED)/4); //new PVector(0, 0);
			seen = new HashSet<Boid>();
			thinkTimer = (int) Math.random()*THINK_TIME;
			angle = 90;
			tracked = false;
			seenByTracked = false;
		}

		private void updateBoid() {
			// Reset seen status
			this.seenByTracked = false;

			// Only make decisions every couple instances
			if (thinkTimer == 0) {
				thinkTimer = THINK_TIME;
				
				// Get all seen boids
				this.vision();

				// Calculate velocity change for each rule
				PVector cohesionV = this.cohesion();
				PVector alignmentV = this.alignment();
				PVector separationV = this.separation();

				// Calculate new velocity based on the selected rules
				//this.velocity.add(cohesionV.mult(COHESION_CONSTANT));
				//this.velocity.add(alignmentV.mult(ALIGNMENT_CONSTANT));
				this.velocity.add(separationV.mult(SEPARATION_CONSTANT));

				// Upper and lower limit checks on velocity
				float mag = this.velocity.mag();
				mag = Math.min(Math.max(mag, MIN_SPEED), MAX_SPEED);
				this.velocity.setMag(mag);
				
				// Check if boid went off the screen. If so, encourage it to come back.
				if (this.pos.x > p.width) {
					this.velocity.x += -1;
				} else if (this.pos.x < 0) {
					this.velocity.x += 1;
				}

				if (this.pos.y > p.height) {
					this.velocity.y += -1;
				} else if (this.pos.y < 0) {
					this.velocity.y += 1;
				}
			} else {
				thinkTimer--;
			}
			
			// Save previous position and update position based on velocity.
			this.posOld.x = this.pos.x;
			this.posOld.y = this.pos.y;
			this.pos.add(this.velocity);
			
			// Update the movement angle based on the new position
			this.angle = this.calculateAngle(this.pos, this.posOld);
		}

		// Private method that computes the vertices and draws a single boid based on movement angle
		// First input is boid color and second input is vision color
		private void drawBoid(int boid_c, int vison_c) {
			
			// If this boid is in the vision range of a tracked boid, highlight it.
			if (this.seenByTracked) {
				boid_c = p.color(0, 255, 0);				
			} else {
				p.fill(boid_c);
			}
			
			// If tracking this boid, draw lines to boids in its vision radius
			if (this.tracked) {
				for (Boid b : this.seen) {
					p.stroke(255,0,0);
					p.line(this.pos.x, this.pos.y, b.pos.x, b.pos.y);
					p.noStroke();
				}
			}
			
			// Draw the actual boid
		    p.pushMatrix();
		    p.translate(this.pos.x, this.pos.y);
		    p.rotate((float) Math.toRadians(this.angle));
		    p.beginShape();
		    p.vertex(2*BOID_SIZE, 0);
		    p.vertex(-BOID_SIZE, BOID_SIZE);
		    p.vertex(-BOID_SIZE, -BOID_SIZE);
		    p.endShape(PConstants.CLOSE);
		    p.fill(vison_c);
		    p.arc(0, 0, VISION_RADIUS, VISION_RADIUS, (float) Math.toRadians(-135), (float) Math.toRadians(135));
		    p.popMatrix();
			
			
		}
		
		// Use the old position and new position of a boid to calculate the angle it is facing.
		private float calculateAngle(PVector p1, PVector p2) {
			float dy = p1.y - p2.y;
			float dx = p1.x - p2.x;
			double radians = Math.atan2(dy, dx);
			float angle = (float) Math.toDegrees(radians);
			return angle;
		}

		// This method grabs all the boids within the selected boids vision.
		// These are the only boids that will affect its flight path.
		private void vision() {
			this.seen = new HashSet<Boid>();
			for (Boid b : boids) {
				// Calculate the distance between the 2 boids
				PVector p1 = b.pos;
				PVector p2 = this.pos;
				double dist = PVector.dist(p1, p2);

				// If distance is less then vision radius then this boid can see other boid.
				if (dist < VISION_RADIUS/2 && dist > 0) {
					// Because we don't want this boid to see boids that are behind it. We will
					// check angles to determine if this boid can actually see the other boid.		
					
					// Get the angle opposite this boids movement.
					float reverseAngle = this.angle;
					// Normalize reversed angle
					if (reverseAngle > 180) {
						reverseAngle = -180 + (reverseAngle - 180);
					} else if (reverseAngle < -180) {
						reverseAngle = -(reverseAngle % 180);
					}
								
					// Calculate angle between this boid and the seen one.
					float angleBetween = this.calculateAngle(this.pos, b.pos);

					// Check if this angle is within 45 degrees of the reversed angle in either direction.
					if (Math.abs(angleBetween - reverseAngle) > 45) {
						if (reverseAngle > 135) {
							if (angleBetween < -135 - (180 - reverseAngle)) {
								// dont do anything
							} else {
								this.seen.add(b);
							}
						}
						else if (reverseAngle < -135) {
							if (angleBetween > 135 + (180 + reverseAngle)) {
								//dont do anything
							} else {
								this.seen.add(b);

							}
						} else {
							this.seen.add(b);
						}
					}
				}
			}
		}

		// Method that calculates and returns a velocity change that would steer
		// the boid towards the average position of seen boids.
		private PVector cohesion() {
			// Calculate average position of seen boids.
			PVector avgPos = new PVector(0, 0);
			for (Boid b : this.seen) {
				avgPos.add(b.pos);
			}
			avgPos.div(this.seen.size());

			// Calculate the acceleration the boid would require to move to the avg position
			// in 1 time interval.
			PVector acelRequired = PVector.sub(avgPos, this.pos);
			return acelRequired;
		}

		// Method that returns a velocity that will make the boid move more similarly to
		// its flockmates.
		private PVector alignment() {
			PVector avgVel = new PVector(0, 0);
			for (Boid b : this.seen) {
				avgVel.add(b.velocity.copy());
			}
			avgVel.div(this.seen.size());

			// Calculate the acceleration needed for current boid to reach the velocity of
			// flockmates in 1 time instance.s
			PVector acelRequired = PVector.sub(avgVel, this.velocity.copy());
			return acelRequired;
		}

		// Method that calculates a velocity to move the boid away from other boids that
		// are too close.
		private PVector separation() {
			PVector velocityRequired = new PVector(0, 0);
			// Look at each boid in vision
			for (Boid b : this.seen) {
				// Need to move away from any boids that are too close.
				if (PVector.dist(b.pos, this.pos) < SEPERATION_RADIUS) {
					// Calculate best position to move to
					PVector diff = PVector.sub(b.pos, this.pos);
					velocityRequired.sub(diff);
				}
				
			}
			return velocityRequired;
		}
	}
}