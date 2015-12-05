
import java.util.Comparator;

public class KdTree_first {

    private static final int COMP_X = 0;
    private static final int COMP_Y = 1;
    private static final int NR_COMP = 2;
    private static final int DEFAULT_COMP = COMP_X;
    private static Comparator<Point2D> comparators[] = new Comparator[NR_COMP];

    static {
        comparators[COMP_X] = Point2D.X_ORDER;
        comparators[COMP_Y] = Point2D.Y_ORDER;
    }
    
    private Node2D root;
    private int size;

    public KdTree_first() {
        size = 0;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    public int size() {
        return size;
    }

    public void insert(Point2D p) {
        Node2D newNode = new Node2D(p);
        RectHV rootContRect = null;
        if (root == null) {
            rootContRect = new RectHV(0, 0, 1, 1);
        }
        root = put(root, newNode, rootContRect, DEFAULT_COMP);
    }

    private Node2D put(Node2D current, Node2D toInsert, RectHV cRect, int compType) {
        if (current == null) {
            size++;
            current = toInsert;
            toInsert.contRect = cRect;
            toInsert.comp = comparators[compType];
            toInsert.compType = compType;
        } else {
            if (current.compareTo(toInsert) > 0) {
                // to the left, to the left
                current.left = put(current.left,
                        toInsert,
                        leftRectangle(current),
                        nextCompType(compType));

            } else if (current.compareTo(toInsert) < 0) {
                // to the right, to the right
                current.right = put(current.right,
                        toInsert,
                        rightRectangle(current),
                        nextCompType(compType));
            }
        }
        return current;
    }

    public boolean contains(Point2D p) {
        Node2D toSearch = new Node2D(p);
        return has(root, toSearch);
    }

    private int nextCompType(int compType) {
        return ((compType + 1) % NR_COMP);
    }

    private boolean has(Node2D current, Node2D toSearch) {
        if (current == null) {
            return false;
        } else {
            if (current.equals(toSearch)) {
                return true;
            } else if (current.compareTo(toSearch) > 0) {
                return has(current.left, toSearch);
            } else {
                return has(current.right, toSearch);
            }
        }
    }

    public void draw() {
        //draw canvas margins
        StdDraw.line(0, 0, 0, 1);
        StdDraw.line(0, 1, 1, 1);
        StdDraw.line(1, 1, 1, 0);
        StdDraw.line(1, 0, 0, 0);

        drawSubtree(root);
    }

    private void drawSubtree(Node2D current) {
        if (current == null) {
            return;
        }

        // draw current point
        StdDraw.setPenRadius(0.01);
        StdDraw.setPenColor(StdDraw.BLACK);
        current.point.draw();
        StdDraw.setPenRadius();
        StdDraw.setPenColor();

        // draw separating line
        double x1 = 0, y1 = 0, x2 = 0, y2 = 0;

        switch (current.compType) {
            case COMP_X:
                // split current rectangle horizontally
                x1 = x2 = current.point.x();
                y1 = current.contRect.ymin();
                y2 = current.contRect.ymax();
                StdDraw.setPenColor(StdDraw.RED);
                break;
            case COMP_Y:
                // split current rectangle vertically
                y1 = y2 = current.point.y();
                x1 = current.contRect.xmin();
                x2 = current.contRect.xmax();
                StdDraw.setPenColor(StdDraw.BLUE);
                break;

            default:
                break;
        }
        StdDraw.line(x1, y1, x2, y2);
        StdDraw.show(0);
        StdDraw.setPenColor();

        drawSubtree(current.left);
        drawSubtree(current.right);
    }

    private RectHV leftRectangle(Node2D n) {
        if (n.left == null) {
            double xmin = 0, ymin = 0, xmax = 0, ymax = 0;

            xmin = n.contRect.xmin();
            ymin = n.contRect.ymin();

            if (n.compType == COMP_X) {
                xmax = n.point.x();
                ymax = n.contRect.ymax();
            } else if (n.compType == COMP_Y) {
                xmax = n.contRect.xmax();
                ymax = n.point.y();
            }

            return new RectHV(xmin, ymin, xmax, ymax);
        } else {
            return n.left.contRect;
        }
    }

    private RectHV rightRectangle(Node2D n) {
        if (n.right == null) {
            double xmin = 0, ymin = 0, xmax = 0, ymax = 0;

            if (n.compType == COMP_X) {
                xmin = n.point.x();
                ymin = n.contRect.ymin();
            } else if (n.compType == COMP_Y) {
                xmin = n.contRect.xmin();
                ymin = n.point.y();
            }

            xmax = n.contRect.xmax();
            ymax = n.contRect.ymax();

            return new RectHV(xmin, ymin, xmax, ymax);
        } else {
            return n.right.contRect;
        }
    }

    public Iterable<Point2D> range(RectHV rect) {
        Queue<Point2D> q = new Queue<Point2D>();
        rangeSearch(root, rect, q);

        return q;
    }

    private void rangeSearch(Node2D current, RectHV rect, Queue<Point2D> resQueue) {
        // see if current point is in range
        if (rect.contains(current.point)) {
            resQueue.enqueue(current.point);
        }

        if ((current.left != null) && rect.intersects(current.left.contRect)) {
            rangeSearch(current.left, rect, resQueue);
        }
        if ((current.right != null) && rect.intersects(current.right.contRect)) {
            rangeSearch(current.right, rect, resQueue);
        }
    }

    public Point2D nearest(Point2D p) {
        return nearestSearch(root, p);
    }

    private Point2D nearestSearch(Node2D current, Point2D p) {
        if (current == null) {
            return null;
        }
        Node2D firstSearch = null, secondSearch = null;
        Comparator<Point2D> comp = current.comp;

        // establish order to go down
        // always begin searching nearest point in the rectangle that contains it
        if (comp.compare(current.point, p) == 0) {
            return current.point;
        } else if (comp.compare(current.point, p) > 0) {
            firstSearch = current.left;
            secondSearch = current.right;
        } else {
            firstSearch = current.right;
            secondSearch = current.left;
        }

        Point2D nearestFirst, nearestSecond, nearest;
        double minDist = p.distanceTo(current.point);
        nearest = current.point;

        // if we have a point closer in the first child rectangle that the current point
        // consider it the nearest
        if (firstSearch != null) {
            nearestFirst = nearestSearch(firstSearch, p);
            if (minDist > p.distanceTo(nearestFirst)) {
                minDist = p.distanceTo(nearestFirst);
                nearest = nearestFirst;
            }
        }

        // if the distance to the second rectangle is bigger than the nearest
        // point found until now, do not go searching
        if (secondSearch != null) {
            if (minDist > secondSearch.contRect.distanceTo(p)) {
                nearestSecond = nearestSearch(secondSearch, p);
                if (minDist > p.distanceTo(nearestSecond)) {
                    nearest = nearestSecond;
                }
            }
        }

        return nearest;
    }

    private class Node2D implements Comparable {

        private int compType;
        private Comparator<Point2D> comp;
        private Point2D point;
        private RectHV contRect;
        private Node2D left;
        private Node2D right;

        public Node2D(Point2D point, RectHV contRect, int compType) {
            this.point = point;
            this.contRect = contRect;
            this.compType = compType;
        }

        public Node2D(Point2D point) {
            this.point = point;
        }

        @Override
        public int compareTo(Object y) {
            if (y == null) {
                return 1;
            }
            if (y.getClass() != this.getClass()) {
                return 1;
            }
            if (this == y) {
                return 0;
            }

            int compOrder = this.comp.compare(this.point, ((Node2D) y).point);
            if (compOrder == 0) {
                return this.point.compareTo(((Node2D) y).point);
            } else {
                return compOrder;
            }
        }

        @Override
        public boolean equals(Object y) {
            if (y == null) {
                return false;
            }
            if (y.getClass() != this.getClass()) {
                return false;
            }
            if (this == y) {
                return true;
            }
            return this.point.equals(((Node2D) y).point);
        }
    }
}
