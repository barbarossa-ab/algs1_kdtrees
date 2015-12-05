
import java.util.Iterator;

public class PointSET {

    private SET <Point2D> pset;
    
    public PointSET() {
        pset = new SET <Point2D> ();
    }
    
    public boolean isEmpty() {
        return pset.isEmpty();
    }
    
    public int size() {
        return pset.size();
    }
    
    public void insert(Point2D p) {
        if(!pset.contains(p)) {
            pset.add(p);
        }
    }
    
    
    public boolean contains(Point2D p) {
        return pset.contains(p);
    }
    
    
    public void draw() {
        Iterator it = pset.iterator();
        
        while(it.hasNext()) {
            ((Point2D)(it.next())).draw();
        }
    }
    
    
    public Iterable<Point2D> range(RectHV rect) {
        Queue <Point2D> insidePoints = new Queue <Point2D> ();
        Iterator it = pset.iterator();
        
        while(it.hasNext()) {
            Point2D cPoint = (Point2D)it.next();
            
            if(rect.contains(cPoint)) {
                insidePoints.enqueue(cPoint);
            }
        }
        
        return insidePoints;
    }

    
    public Point2D nearest(Point2D p) {
        Point2D nearest = null;
        double minDist = Double.MAX_VALUE;
        Iterator it = pset.iterator();
        
        while(it.hasNext()) {
            Point2D cPoint = (Point2D)it.next();
            double cDist = p.distanceTo(cPoint);
            
            if(p.distanceTo(cPoint) < minDist) {
                nearest = cPoint;
                minDist = cDist;
            }
        }
        
        return nearest;
    }
    
}
