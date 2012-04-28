package jogltest;

import java.awt.Dimension;

import javax.media.opengl.GL;
import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.awt.GLJPanel;
import javax.swing.JFrame;

public class Jogl2dTest {

	private GLJPanel canvas;
	
	public Jogl2dTest() {
		JFrame frame = new JFrame("Draw First 2D Graphics");
		frame.setSize(400, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas = new GLJPanel();
		canvas.addGLEventListener(new JOGLListener());
		canvas.setPreferredSize(new Dimension(512, 512));
		frame.add(canvas);
		frame.pack();
		frame.setVisible(true);

	}

	public static void main(String args[]) {
		new Jogl2dTest();
	}

	private class JOGLListener implements GLEventListener {

		@Override
		public void display(GLAutoDrawable drawable) {
			System.out.println("DISPLAY CALLED");
			GL2 gl = drawable.getGL().getGL2();
			// Projection mode is for setting camera
			gl.glMatrixMode(GL2.GL_PROJECTION);
			// This will set the camera for orthographic projection and allow 2D
			// view
			// Our projection will be on 400 X 400 screen
			gl.glLoadIdentity();
			gl.glOrtho(0, canvas.getWidth(), canvas.getHeight(), 0, 0, 1);
			// Modelview is for drawing
			gl.glMatrixMode(GL2.GL_MODELVIEW);
			// Depth is disabled because we are drawing in 2D
			gl.glDisable(GL.GL_DEPTH_TEST);
			// Setting the clear color (in this case black)
			// and clearing the buffer with this set clear color
			gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
			gl.glClear(GL.GL_COLOR_BUFFER_BIT);
			// This defines how to blend when a transparent graphics
			// is placed over another (here we have blended colors of
			// two consecutively overlapping graphic objects)
			gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
			gl.glEnable(GL.GL_BLEND);
			// After this we start the drawing of object
			// We want to draw a triangle which is a type of polygon
			gl.glBegin(GL2.GL_POLYGON);
			// We want to draw triangle in red color
			// So setting the gl color to red
			gl.glColor4f(1, 0, 0, 1);
			// Making vertices of the triangle
			gl.glVertex2d(100, 100);
			gl.glVertex2d(100, 200);
			gl.glVertex2d(200, 200);
			// Our polygon ends here
			gl.glEnd();
			gl.glFlush();
		}

		@Override
		public void init(GLAutoDrawable drawable) {
		}

		@Override
		public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
				int arg4) {

		}

		@Override
		public void dispose(GLAutoDrawable drawable) {
			// TODO Auto-generated method stub

		}
	}
}