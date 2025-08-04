package potatoclient.video;

/**
 * Shared NDC (Normalized Device Coordinates) converter for video streams.
 * Used by both Clojure mock tool and Kotlin video stream implementations.
 * 
 * NDC coordinates range from -1 to 1 in both axes, with (0,0) at the center.
 * Y-axis is inverted so that positive Y is up (matching OpenGL conventions).
 */
public class NDCConverter {
    
    /**
     * Represents a point in NDC space
     */
    public static class NDCPoint {
        public final double x;
        public final double y;
        
        public NDCPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return String.format("NDCPoint(x=%.3f, y=%.3f)", x, y);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            NDCPoint other = (NDCPoint) obj;
            return Double.compare(other.x, x) == 0 && Double.compare(other.y, y) == 0;
        }
        
        @Override
        public int hashCode() {
            return Double.hashCode(x) * 31 + Double.hashCode(y);
        }
    }
    
    /**
     * Represents a point in pixel space
     */
    public static class PixelPoint {
        public final int x;
        public final int y;
        
        public PixelPoint(int x, int y) {
            this.x = x;
            this.y = y;
        }
        
        @Override
        public String toString() {
            return String.format("PixelPoint(x=%d, y=%d)", x, y);
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            PixelPoint other = (PixelPoint) obj;
            return x == other.x && y == other.y;
        }
        
        @Override
        public int hashCode() {
            return x * 31 + y;
        }
    }
    
    /**
     * Convert pixel coordinates to NDC coordinates.
     * 
     * @param x Pixel X coordinate (0 to width-1)
     * @param y Pixel Y coordinate (0 to height-1)
     * @param width Canvas width in pixels
     * @param height Canvas height in pixels
     * @return NDC coordinates where x,y are in range [-1, 1]
     */
    public static NDCPoint pixelToNDC(int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        double ndcX = (x / (double)width) * 2.0 - 1.0;
        double ndcY = -((y / (double)height) * 2.0 - 1.0); // Invert Y
        
        // Clamp to valid NDC range to handle edge cases
        ndcX = Math.max(-1.0, Math.min(1.0, ndcX));
        ndcY = Math.max(-1.0, Math.min(1.0, ndcY));
        
        return new NDCPoint(ndcX, ndcY);
    }
    
    /**
     * Convert NDC coordinates to pixel coordinates.
     * 
     * @param ndcX NDC X coordinate (-1 to 1)
     * @param ndcY NDC Y coordinate (-1 to 1)
     * @param width Canvas width in pixels
     * @param height Canvas height in pixels
     * @return Pixel coordinates
     */
    public static PixelPoint ndcToPixel(double ndcX, double ndcY, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        // Clamp NDC coordinates to valid range
        ndcX = Math.max(-1.0, Math.min(1.0, ndcX));
        ndcY = Math.max(-1.0, Math.min(1.0, ndcY));
        
        int x = (int)Math.round((ndcX + 1.0) / 2.0 * width);
        int y = (int)Math.round(((-ndcY) + 1.0) / 2.0 * height); // Invert Y back
        
        // Clamp to valid pixel range
        x = Math.max(0, Math.min(width - 1, x));
        y = Math.max(0, Math.min(height - 1, y));
        
        return new PixelPoint(x, y);
    }
    
    /**
     * Convert pixel delta to NDC delta (for pan gestures).
     * Does not invert Y axis as this is a relative movement.
     * 
     * @param deltaX Pixel delta X
     * @param deltaY Pixel delta Y
     * @param width Canvas width in pixels
     * @param height Canvas height in pixels
     * @return NDC delta values
     */
    public static NDCPoint pixelDeltaToNDC(int deltaX, int deltaY, int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("Width and height must be positive");
        }
        
        double ndcDeltaX = (deltaX / (double)width) * 2.0;
        double ndcDeltaY = -(deltaY / (double)height) * 2.0; // Still invert for consistency
        
        return new NDCPoint(ndcDeltaX, ndcDeltaY);
    }
    
    /**
     * Apply aspect ratio correction to NDC X coordinate.
     * This is useful for maintaining circular motion in non-square viewports.
     * 
     * @param ndcX NDC X coordinate
     * @param aspectRatio Width/Height ratio
     * @return Aspect-corrected NDC X
     */
    public static double applyAspectRatio(double ndcX, double aspectRatio) {
        return ndcX * aspectRatio;
    }
    
    /**
     * Check if pixel coordinates are within canvas bounds.
     * 
     * @param x Pixel X coordinate
     * @param y Pixel Y coordinate
     * @param width Canvas width
     * @param height Canvas height
     * @return true if coordinates are within bounds
     */
    public static boolean isInBounds(int x, int y, int width, int height) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }
}