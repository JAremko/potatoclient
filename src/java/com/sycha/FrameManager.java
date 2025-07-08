package com.sycha;

import javax.swing.*;
import java.awt.*;
import com.sun.jna.Platform;
import java.net.URL;

/**
 * Manages the creation and lifecycle of the video frame and component.
 */
public class FrameManager {
    private final String streamId;
    private volatile JFrame frame;
    private volatile Component videoComponent;
    
    public interface FrameEventListener {
        void onFrameCreated(JFrame frame, Component videoComponent);
        void onFrameClosing();
    }
    
    private final FrameEventListener listener;
    
    public FrameManager(String streamId, FrameEventListener listener) {
        this.streamId = streamId;
        this.listener = listener;
    }
    
    public void createFrame() {
        SwingUtilities.invokeLater(() -> {
            frame = createJFrame();
            videoComponent = createVideoComponent();
            
            frame.add(videoComponent);
            frame.pack();
            frame.setLocationRelativeTo(null);
            
            if (listener != null) {
                listener.onFrameCreated(frame, videoComponent);
            }
        });
    }
    
    private JFrame createJFrame() {
        String title = streamId.equals(Constants.StreamConfig.HEAT_STREAM_ID) 
            ? Constants.StreamConfig.HEAT_STREAM_TITLE 
            : Constants.StreamConfig.DAY_STREAM_TITLE;
            
        JFrame newFrame = new JFrame(title);
        newFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        
        // Set window icon
        try {
            URL iconURL = getClass().getResource("/icon.png");
            if (iconURL != null) {
                newFrame.setIconImage(new ImageIcon(iconURL).getImage());
            }
        } catch (Exception e) {
            System.err.println("Failed to load window icon: " + e.getMessage());
        }
        
        // Add window close listener
        newFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                if (listener != null) {
                    listener.onFrameClosing();
                }
            }
        });
        
        return newFrame;
    }
    
    private Component createVideoComponent() {
        Component component;
        
        // Create video component based on platform
        if (Platform.isLinux()) {
            // Use Canvas for X11
            component = new Canvas();
        } else if (Platform.isWindows()) {
            // Use heavyweight component for Windows
            component = new Canvas();
        } else if (Platform.isMac()) {
            // Use Canvas for macOS (more reliable than JComponent)
            component = new Canvas();
            // Enable CALayer rendering
            System.setProperty("apple.awt.graphics.UseQuartz", "true");
        } else {
            // Default fallback
            component = new Canvas();
        }
        
        // Set preferred size based on stream
        if (streamId.equals(Constants.StreamConfig.HEAT_STREAM_ID)) {
            component.setPreferredSize(new Dimension(
                Constants.StreamConfig.HEAT_STREAM_WIDTH, 
                Constants.StreamConfig.HEAT_STREAM_HEIGHT
            ));
        } else {
            component.setPreferredSize(new Dimension(
                Constants.StreamConfig.DAY_STREAM_DISPLAY_WIDTH, 
                Constants.StreamConfig.DAY_STREAM_DISPLAY_HEIGHT
            ));
        }
        
        component.setBackground(Color.BLACK);
        
        return component;
    }
    
    public void showFrame() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setVisible(true);
            }
        });
    }
    
    public void hideFrame() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.setVisible(false);
            }
        });
    }
    
    public void disposeFrame() {
        SwingUtilities.invokeLater(() -> {
            if (frame != null) {
                frame.dispose();
                frame = null;
                videoComponent = null;
            }
        });
    }
    
    public JFrame getFrame() {
        return frame;
    }
    
    public Component getVideoComponent() {
        return videoComponent;
    }
    
    public boolean isFrameCreated() {
        return frame != null;
    }
    
    public boolean isFrameVisible() {
        return frame != null && frame.isVisible();
    }
}