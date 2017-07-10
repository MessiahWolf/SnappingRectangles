/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 *
 * @author Robert Cherry
 * Modifications and cleanups made by Hoang Tran
 * 
 * Very simple program that keeps rectangles apart.
 * If one rectangle completely contains another then the contained rectangle is destroyed.
 * If they merely intersect then they are separated out to the side that intersects the most.
 * If there is a center defined for all the rectangles, then the rest of the rectangles should maintain their initial
 * distance from the center rectangle and move along with the center.
 * 
 * Purpose is to be able to, when implemented, define hit-boxes that can easily be created as non-intersecting rectangles over
 * an image and saved.
 */
public class MainFrame extends javax.swing.JFrame {

    // Variable Declaration
    // Data Types
    private String msg;
    // Java Native Classes
    private Font font;
    private Point firstPoint;
    private Point secondPoint;
    private Point mouse = new Point(0, 0);
    private Rectangle sel;
    private Rectangle center;
    private boolean drawingNewRect = false;
    private ArrayList<Rectangle> list;
    private HashMap<Rectangle, Point> distances;
    // End of Variable Declaration

    public MainFrame() {
        initComponents();
        
        //
        init();
    }
    
    private void init() {
        
        list = new ArrayList<Rectangle>();
        font = new Font("TimesRoman", Font.PLAIN, 11);

        //
        final BufferedImage image = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        final Toolkit kit = Toolkit.getDefaultToolkit();

        // Our new blank cursor image
        final Cursor blank = kit.createCustomCursor(image, new Point(0, 0), "Blank");

        // Change the cursor to nothing so it's easier to tell where we're clicking.
        setCursor(blank);
        setSize(new Dimension(640, 480));
        setLocation(kit.getScreenSize().width / 2 - getWidth() / 2, kit.getScreenSize().height / 2 - getHeight() / 2);
        setTitle("No Intersection Complexion");
        setResizable(false);
    }

    private void addRectangle() {

        // Start point and size depends on which Point is greater;
        int height = firstPoint.y > secondPoint.y ? firstPoint.y - secondPoint.y : secondPoint.y - firstPoint.y;
        int width = firstPoint.x > secondPoint.x ? firstPoint.x - secondPoint.x : secondPoint.x - firstPoint.x;

        // Adding to the list of rectangles to draw.
        list.add(new Rectangle(firstPoint.x > secondPoint.x ? secondPoint.x : firstPoint.x,
                firstPoint.y > secondPoint.y ? secondPoint.y : firstPoint.y,
                width, height));
    }

    @Override
    public void paint(Graphics monet) {
        
        // Cast for easier shape filling
        final Graphics2D manet = (Graphics2D) monet;

        // Use a consistent seed for the RNG
        final Random generator = new Random(0);

        // Refresh the background each call.
        manet.setColor(new Color(251, 251, 251));
        manet.fillRect(0, 0, getWidth(), getHeight());

        for (Rectangle rect : list) {
            // Randomly determine the next rectangle's color
            manet.setColor(new Color((int) (generator.nextFloat() * 255), (int) (generator.nextFloat() * 255), (int) (generator.nextFloat() * 255)));

            // When the mouse is inside the current rect fill it in; otherwise outline it.
            if (rect.contains(mouse)) {
                manet.fill(rect);
                
                // Also draw where the user needs to click to move the rectangle.
                manet.setColor(Color.BLACK);
                monet.drawRect(rect.x + rect.width / 2 - (rect.width / 4), rect.y + rect.height / 2 - (rect.height / 4), rect.width / 2, rect.height / 2);
            } else {
                manet.draw(rect);
            }

            // Draw the outside grab squares.
            manet.setColor(Color.BLACK);
            monet.drawRect(rect.x - 3, rect.y - 3, 6, 6);
            monet.drawRect(rect.x + rect.width - 3, rect.y - 3, 6, 6);
            monet.drawRect(rect.x - 3, rect.y + rect.height - 3, 6, 6);
            monet.drawRect(rect.x + rect.width - 3, rect.y + rect.height - 3, 6, 6);
        }

        // Draw a black rectangle indicating the boundaries of the new rectangle that's about to be added
        if(drawingNewRect){
        	if(firstPoint.x > mouse.x){
        		if(firstPoint.y > mouse.y){
                	monet.drawRect(mouse.x, mouse.y, firstPoint.x - mouse.x, firstPoint.y - mouse.y);
        		}
        		else{
                	monet.drawRect(mouse.x, firstPoint.y, firstPoint.x - mouse.x, mouse.y - firstPoint.y);
        		}
        	} else{
        		if(firstPoint.y > mouse.y){
                	monet.drawRect(firstPoint.x, mouse.y, mouse.x - firstPoint.x, firstPoint.y - mouse.y);
        		}
        		else{
                	monet.drawRect(firstPoint.x, firstPoint.y, mouse.x - firstPoint.x, mouse.y - firstPoint.y);
        		}
        	}
        }
        
        // Mark where the mouse is.
        manet.setColor(Color.BLUE);
        manet.fillOval(mouse.x, mouse.y, 2, 2);

        // If we have a center rectangle draw a green circle in the center.
        if (center != null) {
            monet.setColor(Color.GREEN);
            monet.fillOval(center.x + center.width / 2 - 4, center.y + center.height / 2 - 4, 8, 8);
        }
        
        // Draw the instructions
        drawInstructions(manet);
    }
    
    private void drawInstructions(Graphics2D manet) {
        // In the top left corner
        manet.setFont(font);
        manet.setColor(Color.BLACK);
        
        // Define instructions
        manet.drawString("Left-Click (Hold) then Release: To create a rectangle when not inside other rectangles.", 16, 48);
        manet.drawString("Dragging (Slowly) inside the center of a rectangle will allow you to move it.", 32, 64);
        manet.drawString("Dragging (Slowly) inside the outside handles of the rectangle allows resizing.", 32, 80);
        manet.drawString("Right-Click inside the center of a rectangle to assign it as the Center Rectangle.", 16, 96);
        manet.drawString("This will allow you to move all other rectangles about the center of the Center rectangle.", 32, 112);
    }

    private void separateRectangles() {
        
        // Trim and Order the array by biggest squares to smallest before all this nonsense
        list.trimToSize();
        final Rectangle[] n = orderRectangles(list.toArray(new Rectangle[]{}));

        // We use array list for the dynamic length.
        final ArrayList<Rectangle> removeList = new ArrayList<Rectangle>();

        // Move rectangles away from each other
        for (int i = 0; i < n.length - 1; i++) {

            // Convienence variables.
            final Rectangle r = n[i];
            final int rxSize = r.x + r.width;
            final int rySize = r.y + r.height;
            
            // Stay a step in front of the previous index.
            for (int j = i + 1; j < n.length; j++) {

                // Convienence variables.
                final Rectangle s = n[j];
                final int sxSize = s.x + s.width;
                final int sySize = s.y + s.height;

                // If the current rectangle contains the smaller (Because these are in size order)
                // Mark it to be removed.
                if (r.contains(s)) {
                    removeList.add(s);

                    // Avoids unnessecary calculations below if removing.
                    continue;
                }

                // Otherwise bounce it off me if I'm the bigger rectangle.
                if (r.intersects(s)) {

                    // How much are they intersecting?
                    final int distX = sxSize > rxSize ? (s.x + s.width) - (r.x + r.width) : (r.x + r.width) - (s.x + s.width);
                    final int distY = sySize > rySize ? (s.y + s.height) - (r.y + r.height) : (r.y + r.height) - (s.y + s.height);

                    // If my width is more into it than height
                    if (distX > distY) {
                        // If the other rect is more than halfway into the current...move it out to the right
                        if (s.x >= rxSize / 2) {
                            s.x = rxSize + 1;
                        } else {
                            s.x = (r.x - 1) - s.width;
                        }
                    } else if (distX < distY) {
                        if (s.y >= rySize / 2) {
                            s.y = rySize + 1;
                        } else {
                            s.y = (r.y - 1) - s.height;
                        }
                    }
                }
            }
        }

        // Remove all the inner rectangles.
        list.removeAll(removeList);
    }

    private Rectangle[] orderRectangles(Rectangle[] arr) {

        // Keeps index of last known biggest rectangle
        int h;

        // Looping
        for (int i = 0; i < arr.length - 1; i++) {

            //
            h = i;

            //
            for (int j = i + 1; j < arr.length; j++) {
                if (arr[j].width + arr[j].height > arr[h].width + arr[h].height) {
                    h = j;
                }
            }

            // If the current iteration isn't the index of the biggest rect, then swap them.
            if (i != h) {
                swapRects(arr, h, i);
            }
        }

        // Output.
        return arr;
    }

    private void swapRects(Rectangle[] arr, int a, int b) {
        final Rectangle r = arr[a];
        arr[a] = arr[b];
        arr[b] = r;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
            public void mousePressed(java.awt.event.MouseEvent evt) {
                formMousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                formMouseReleased(evt);
            }
        });
        addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                formMouseDragged(evt);
            }
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                formMouseMoved(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        // Keep updating mouse position.      
        mouse = evt.getPoint();

        // Must have a selected rectangle to drag.
        if (sel != null) {
            // Determine where we're grabbing the rectangles grab points
            int tempX;
            int tempY;
            //@TOP LEFT
            if (mouse.x > sel.x - 3 && mouse.x < sel.x + 3 && mouse.y > sel.y - 3 && mouse.y < sel.y + 3) {
                tempX = sel.x;
                tempY = sel.y;
                sel.x = mouse.x;
                sel.width = tempX + sel.width - mouse.x;
                sel.y = mouse.y;
                sel.height = tempY + sel.height - mouse.y;
            } else if (mouse.x > sel.x + sel.width - 3 && mouse.x < sel.x + sel.width + 3 && mouse.y > sel.y - 3 && mouse.y < sel.y + 3) {
                // TOP RIGHT
                tempY = sel.y;
                sel.width = mouse.x - sel.x;
                sel.y = mouse.y;
                sel.height = tempY + sel.height - mouse.y;
            } else if (mouse.x > sel.x - 3 && mouse.x < sel.x + 3 && mouse.y > sel.y + sel.height - 3 && mouse.y < sel.y + sel.height + 3) {
                // BOTTOM LEFT
                tempX = sel.x;
                tempY = sel.y;
                sel.x = mouse.x;
                sel.width = tempX + sel.width - mouse.x;
                sel.height = mouse.y - tempY;
            } else if (mouse.x > sel.x + sel.width - 3 && mouse.x < sel.x + sel.width + 3 && mouse.y > sel.y + sel.height - 3 && mouse.y < sel.y + sel.height + 3) {
                // BOTTOM RIGHT
                sel.width = mouse.x - sel.x;
                sel.height = mouse.y - sel.y;
            } else if (mouse.x > sel.x + sel.width / 2 - (sel.width / 4) && mouse.x < sel.x + sel.width / 2 + (sel.width / 4) && mouse.y > sel.y + sel.height / 2 - (sel.height / 4) && mouse.y < sel.y + sel.height / 2 + (sel.height / 4)) {
                sel.x = mouse.x - sel.width / 2;
                sel.y = mouse.y - sel.height / 2;
            }// End of Grab Points code...
        }

        // If we have a center then keep all the other rectangles the same distance from the center as it moves.
        if (center != null) {

            // Determine distance from center and keep those distances consistent when dragging.
            for (Map.Entry<Rectangle, Point> m : distances.entrySet()) {
                final Rectangle r = m.getKey();
                
                // As long as the current isn't the center or the selected rectangle do the Auto-Snapping.
                if (r != center && r != sel) {
                    r.x = center.x + m.getValue().x;
                    r.y = center.y + m.getValue().y;
                }
            }
        }

        // Paint.
        repaint();
    }//GEN-LAST:event_formMouseDragged

    private void formMouseMoved(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
        // Keep updating the Mouse Position and repainting.
        mouse = evt.getPoint();
        // Paint.
        repaint();
        
    }//GEN-LAST:event_formMouseMoved

    private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
        // On User Left Click event.
        if (evt.getButton() == MouseEvent.BUTTON1) {
            // Order the rectangles up
            final Rectangle[] arr = orderRectangles(list.toArray(new Rectangle[]{}));

            // Determine if you pressed on of the small rectangles on the outside, or the center of a rectangle.
            for (Rectangle r : arr) {
                if (mouse.x > r.x - 6 && mouse.x < r.x + 6 && mouse.y > r.y - 6 && mouse.y < r.y + 6) {
                    // Top left
                    sel = r;
                    drawingNewRect = false;
                    break;
                } else if (mouse.x > r.x + r.width - 6 && mouse.x < r.x + r.width + 6 && mouse.y > r.y - 6 && mouse.y < r.y + 6) {
                    // Top right
                    sel = r;
                    drawingNewRect = false;
                    break;
                } else if (mouse.x > r.x - 6 && mouse.x < r.x + r.width - 6 && mouse.y > r.y + r.height - 6 && mouse.y < r.y + r.height + 6) {
                    // Bottom left
                    sel = r;
                    drawingNewRect = false;
                    break;
                } else if (mouse.x > r.x + r.width - 6 && mouse.x < r.x + r.width + 6 && mouse.y > r.y + r.height - 6 && mouse.y < r.y + r.height + 6) {
                    // Bottom right
                    sel = r;
                    drawingNewRect = false;
                    break;
                } else if (mouse.x > r.x + r.width / 2 - (r.width / 4) && mouse.x < r.x + r.width / 2 + (r.width / 4) && mouse.y > r.y + r.height / 2 - (r.height / 4) && mouse.y < r.y + r.height / 2 + (r.height / 4)) {
                    // Center
                    sel = r;
                    drawingNewRect = false;
                    break;
                }
            }

            // If we didn't click inside a rectangle then start a new one.
            if (sel == null) {
                firstPoint = evt.getPoint();
                drawingNewRect = true;
            }
        }
        // Paint.
        repaint();
    }//GEN-LAST:event_formMousePressed

    private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
        // On User Left click release.
        if (evt.getButton() == MouseEvent.BUTTON1) {

            // If there's no rectangle selected then allow one to be made.
            if (sel == null) {
                secondPoint = evt.getPoint();

                // Make the rectangle.
                addRectangle();
            }

            // Separate out all the rectangles and deselect the selected rectangle
            separateRectangles();
            sel = null;

            // If we have a center when the mouse is released. Capture those distances between the shapes as new.
            if (center != null) {
                
                // We need to store these before the drag event.
                distances = new HashMap<>(list.size());

                // Set all the distances
                for (Rectangle r : list) {

                    // Store the distance that they are apart.
                    distances.put(r, new Point(r.x - center.x, r.y - center.y));
                }
            }
        }
        drawingNewRect = false;
        // Paint.
        repaint();
    }

    private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
        // If you right click on the center of a rectangle, then call that one the New center.
        if (evt.getButton() == MouseEvent.BUTTON3) {
            // Find the one the mouse is inside
            for (Rectangle r : list) {

                // If you're inside the center of a rectangle
                if (mouse.x > r.x + r.width / 2 - (r.width / 4) && mouse.x < r.x + r.width / 2 + (r.width / 4) && mouse.y > r.y + r.height / 2 - (r.height / 4) && mouse.y < r.y + r.height / 2 + (r.height / 4)) {
                    // Call that the center
                    center = r;
                    break;
                }
            }

            // We need to store these before the drag event.
            distances = new HashMap<>(list.size());

            // Set all the distances
            for (Rectangle r : list) {

                // So we can keep them the same distance apart.
                int distX = r.x - center.x;
                int distY = r.y - center.y;

                //Store that
                distances.put(r, new Point(distX, distY));
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame().setVisible(true);
            }
        });
    }
}
