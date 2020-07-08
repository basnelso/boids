import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PVector;

public class Flock {
	ArrayList<Boid> boids;
	Set<Boid> seenByTrackedBoids;
	Configuration config;
	boolean showCohesion, showAlignment, showSeparation;
	
	final PApplet p; // Gives access to processing functions
	
	// Flock Constructor
	Flock(int num, final PApplet p, Configuration config) {
		boids = new ArrayList<Boid>(num);
		seenByTrackedBoids = new HashSet<Boid>();
		
		// Get configuration details
		this.config = config;
		
		// Add {num} boids in randomly selected positions
		for (int i = 0; i < num; i++) {
			boids.add(new Boid(p.random(0, p.width), p.random(0, p.height)));
		}
		// Pickup the PApplet to have access to processing functions
		this.p = p;

		
		// Manually set visual trackers
		showCohesion = true;
		showAlignment = true;
		showSeparation = true;
		
		// Manually add some tracked boids
		boids.get(0).isTracked = true;
		boids.get(1).isTracked = true;

	}

	// Method that iterates through the set of boids and draws each one
	public void drawBoids() {
		// Draw each boid in the flock
		for (Boid b : this.boids) {
			// Select the color scheme based on parameters of boid.
			if (b.isTracked) {
				b.drawBoid(p.color(0, 0, 255), p.color(0, 0, 255, 10), p.color(0, 0, 255, 100));
			} else if (this.seenByTrackedBoids.contains(b)) {
				b.drawBoid(p.color(0), null, p.color(0, 0, 0, 100));
			} else {
				b.drawBoid(p.color(150), null, null);
			}
		}
	}

	// Method that iterates through the set of boids and updates the position of
	// each one based on its velocity and angle
	public void updateBoids() {
		// Reset the boids that tracked boids can see
		this.seenByTrackedBoids = new HashSet<>();
		
		// Update each boid in the flock
		for (Boid b : this.boids) {
			b.updateBoid();
			
			// Gather all boids tracked boids saw
			if (b.isTracked) {
				this.seenByTrackedBoids.addAll(b.seen);
			}
		}
	}

	class Boid {
		PVector pos, posOld, velocity;
		Set<Boid> seen;
		int thinkTimer;
		float angle;
		boolean isTracked;
		
		Boid(float x, float y) {
			pos = new PVector(x, y);
			posOld = new PVector(0, 0);
			float startingVelocity = (3*config.MAX_SPEED)/4;
			velocity = PVector.random2D().mult(startingVelocity);
			seen = new HashSet<Boid>();
			thinkTimer = (int) Math.random()*config.THINK_TIME;
			angle = 0;
			isTracked = false;
		}

		private void updateBoid() {
			// Only make decisions every couple instances
			if (thinkTimer == 0) {
				thinkTimer = config.THINK_TIME;
				
				// Get all seen boids
				this.vision();

				// Calculate velocity change for each rule
				PVector cohesionV = this.cohesion();
				PVector alignmentV = this.alignment();
				PVector separationV = this.separation();

				// Calculate new velocity based on the selected rules
				this.velocity.add(cohesionV.mult(config.COHESION_CONSTANT));
				this.velocity.add(alignmentV.mult(config.ALIGNMENT_CONSTANT));
				this.velocity.add(separationV.mult(config.SEPARATION_CONSTANT));

				// Upper and lower limit checks on velocity
				float mag = this.velocity.mag();
				mag = Math.min(Math.max(mag, config.MIN_SPEED), config.MAX_SPEED);
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
		private void drawBoid(Integer boidC, Integer visC, Integer velC) {
			if (this.isTracked) {
				// Draw lines to all boids in vision radius
				if (config.showSeparation) {
					for (Boid b : this.seen) {
						p.stroke(255,0,0);
						p.strokeWeight(1);
						p.line(this.pos.x, this.pos.y, b.pos.x, b.pos.y);
						p.noStroke();
					}
				}

			}
			
			// Set up position and rotation of boid.
		    p.pushMatrix();
		    p.translate(this.pos.x, this.pos.y);
		    p.rotate((float) Math.toRadians(this.angle));
		    
		    // Draw vision radius.
		    if (visC != null) {
			    p.fill(visC);
			    p.arc(0, 0, config.VISION_RADIUS, config.VISION_RADIUS, (float) Math.toRadians(-135), (float) Math.toRadians(135));
		    }
		    // Draw velocity tail.
		    if (velC != null && config.showAlignment) {
		    	p.stroke(velC);
				p.strokeWeight(1);
		    	p.line(0, 0, config.BOID_VELOCITY_TAIL, 0);
		    	p.noStroke();
		    }
		    
		    // Draw actual boid body.
			p.fill(boidC);
		    p.beginShape();
		    p.vertex(2*config.BOID_SIZE, 0);
		    p.vertex(-config.BOID_SIZE, config.BOID_SIZE);
		    p.vertex(-config.BOID_SIZE, -config.BOID_SIZE);
		    p.endShape(PConstants.CLOSE);
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
				boolean seen = false;
				// If distance is less then vision radius then this boid can see other boid.
				if (dist < config.VISION_RADIUS/2 && b != this) {
					// Because we don't want this boid to see boids that are behind it. We will
					// check angles to determine if this boid can actually see the other boid.		
					
					// Calculate angle between this boid and the seen one.
					float angleBetween = this.calculateAngle(this.pos, b.pos);

					// Check that angle is further then 45 degrees away from movement angle.
					if (Math.abs(angleBetween - this.angle) > 45) {
						// Most likely other boid is within the sight angle of this boid.
						// Just need to check and two edge cases.
						if (this.angle > 135) {
							if (angleBetween > -135 - (180 - this.angle)) {
								seen = true; // Possible edge case but still in vision.
							}
						} else if (this.angle < -135) {
							if (angleBetween < 135 + (180 + this.angle)) {
								seen = true; // Possible edge case but still in vision.
							}
						} else { // Movement angle is not a part of an edge case
							seen = true;
						}
					}
				}
				
				if (seen) {
					this.seen.add(b);
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

			// Calculate movement required to reach average position in 1 time instance.
			if (this.seen.size() > 0) {
				avgPos.div(this.seen.size());
				
				if (this.isTracked && config.showCohesion) {
					p.fill(0);
					p.circle(avgPos.x, avgPos.y, 25);;
				}
				return PVector.sub(avgPos, this.pos);
			} else { // If no boids in vision radius rule doesn't apply.
				return new PVector(0,0);
			}
		}

		// Method that returns a velocity that will make the boid move more similarly to
		// its flockmates.
		private PVector alignment() {
			PVector velocityRequired = new PVector(0, 0);
			PVector avgVelocity = new PVector(0,0);
			
			// Look at each boid in this boid's vision
			for (Boid b : this.seen) {
				avgVelocity.add(b.velocity.copy());
			}
			
			// Calculate velocity change required to reach average velocity of group in 1 time instance.
			if (this.seen.size() > 0) {
				avgVelocity.div(this.seen.size());
				velocityRequired = PVector.sub(avgVelocity, this.velocity.copy());
				return velocityRequired;
			} else { // If no boids in vision radius rule doesn't apply.
				return new PVector(0, 0);
			}
		}

		// Method that calculates a velocity to move the boid away from other boids that
		// are too close.
		private PVector separation() {
			PVector velocityRequired = new PVector(0, 0);
			// Look at each boid in vision
			for (Boid b : this.seen) {
				// Need to move away from any boids that are too close.
				if (PVector.dist(b.pos, this.pos) < config.SEPERATION_RADIUS) {
					// Calculate best position to move to
					PVector diff = PVector.sub(b.pos, this.pos);
					velocityRequired.sub(diff);
				}
				
			}
			return velocityRequired;
		}
	}
}