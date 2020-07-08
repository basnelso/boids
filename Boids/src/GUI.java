import processing.core.PApplet;
import processing.core.PConstants;

public class GUI {
	Configuration config;
	PApplet p;
	final int BUFFER = 10;
	
	
	GUI(Configuration config, PApplet p) {
		this.p = p;
		this.config = config;
	}
	
	public void drawGUI() {
		this.updateSelection();
		
		// Background
		p.fill(0);
	    p.beginShape();
	    p.vertex(0, 0);
	    p.vertex(p.width, 0);
	    p.vertex(p.width, p.height/10);
	    p.vertex(0, p.height/10);
	    p.endShape(PConstants.CLOSE);
	    
	    // Buttons
	    this.makeButton(0 + BUFFER, 0 + BUFFER, 100 - BUFFER, p.height/10 - BUFFER, 150);
	    this.makeButton(100 + BUFFER, 0 + BUFFER, 200 - BUFFER, p.height/10 - BUFFER, 150);
	    this.makeButton(200 + BUFFER, 0 + BUFFER, 300 - BUFFER, p.height/10 - BUFFER, 150);
	}
	
	public void mousePressed() {
		if (p.mouseY < p.height/10) {
			if (p.mouseX > 0 && p.mouseX < 100) {
				config.showAlignment = !config.showAlignment;
			} else if (p.mouseX > 100 && p.mouseX < 200) {
				config.showSeparation = !config.showSeparation;
			} else if (p.mouseX > 200 && p.mouseX < 300) {
				config.showCohesion = !config.showCohesion;
			}
		}
	}
	
	public void updateSelection() {
		
	}
	
	public void makeButton(int x1, int y1, int x2, int y2, int color) {
		p.fill(150);
	    p.beginShape();
	    p.vertex(x1, y1);
	    p.vertex(x2, y1);
	    p.vertex(x2, y2);
	    p.vertex(x1, y2);
	    p.endShape(PConstants.CLOSE);
	}
}
