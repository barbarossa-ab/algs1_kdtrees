
import java.util.Comparator;

public class KdTree {

    private static final int COMP_X = 0;
    private static final int COMP_Y = 1;
    private static final int NR_COMP = 2;
    private static final int DEFAULT_COMP = COMP_X;
    private static Comparator<Point2D> comparators[] = new Comparator[NR_COMP];

    static {
        comparators[COMP_X] = Point2D.X_ORDER;
        comparators[COMP_Y] = Point2D.Y_ORDER;
    }
    private KdTree.Node root;
    private int size;

    public KdTree() {
        size = 0;
    }

    public boolean isEmpty() {
        return (root == null);
    }

    public int size() {
        return size;
    }

    public void insert(Point2D p) {
        if (!contains(p)) {
            root = insert(root, p, DEFAULT_COMP);
        }
    }

    public boolean contains(Point2D p) {
        return contains(root, p, DEFAULT_COMP);
    }

    public Iterable<Point2D> range(RectHV rect) {
        Queue<Point2D> q = new Queue<Point2D>();
        RectHV rootRect = new RectHV(0, 0, 1, 1);

        range(root, rect, rootRect, DEFAULT_COMP, q);

        return q;
    }

    public Point2D nearest(Point2D p) {
        RectHV rootRect = new RectHV(0, 0, 1, 1);
        return nearest(root, rootRect, p, DEFAULT_COMP);
    }

    private Point2D nearest(Node cNode, RectHV cNodeRect, Point2D point, int compType) {
        if (cNode == null) {
            return null;
        }
        if (cNode.point.equals(point)) {
            return cNode.point;
        }

        Node firstSearch, secondSearch;
        RectHV firstRect, secondRect;
        Comparator<Point2D> comp = comparators[compType];

        // establish order to go down
        // always begin searching nearest point in the rectangle that contains it
        if (comp.compare(cNode.point, point) > 0) {
            firstSearch = cNode.left;
            firstRect = leftRectangle(cNode.point, cNodeRect, compType);
            secondSearch = cNode.right;
            secondRect = rightRectangle(cNode.point, cNodeRect, compType);
        } else {
            firstSearch = cNode.right;
            firstRect = rightRectangle(cNode.point, cNodeRect, compType);
            secondSearch = cNode.left;
            secondRect = leftRectangle(cNode.point, cNodeRect, compType);
        }

        Point2D nearestFirst, nearestSecond, nearest;
        double minDistSquared = point.distanceSquaredTo(cNode.point);
        nearest = cNode.point;

        // if we have a point closer in the first child rectangle that the current point
        // consider it the nearest
        if (firstSearch != null) {
            nearestFirst = nearest(firstSearch, firstRect, point, nextCompType(compType));
            if (minDistSquared > point.distanceSquaredTo(nearestFirst)) {
                minDistSquared = point.distanceSquaredTo(nearestFirst);
                nearest = nearestFirst;
            }
        }

        // if the distance to the second rectangle is bigger than the nearest
        // point found until now, do not go searching
        if (secondSearch != null) {
            if (minDistSquared > secondRect.distanceSquaredTo(point)) {
                nearestSecond = nearest(secondSearch, secondRect, point, nextCompType(compType));
                if (minDistSquared > point.distanceSquaredTo(nearestSecond)) {
                    nearest = nearestSecond;
                }
            }
        }

        return nearest;

    }

    private void range(Node cNode, RectHV rect, RectHV cNodeRect, int compType, Queue<Point2D> q) {
        if (cNode == null) {
            return;
        }

        // see if current point is in range
        if (rect.contains(cNode.point)) {
            q.enqueue(cNode.point);
        }

        if (cNode.left != null) {
            RectHV lr = leftRectangle(cNode.point, cNodeRect, compType);
            if (rect.intersects(lr)) {
                range(cNode.left, rect, lr, nextCompType(compType), q);
            }
        }
        if (cNode.right != null) {
            RectHV rr = rightRectangle(cNode.point, cNodeRect, compType);
            if (rect.intersects(rr)) {
                range(cNode.right, rect, rr, nextCompType(compType), q);
            }
        }
    }

    private RectHV leftRectangle(Point2D point, RectHV contRect, int compType) {
        double xmin, ymin, xmax = 0, ymax = 0;

        xmin = contRect.xmin();
        ymin = contRect.ymin();

        if (compType == COMP_X) {
            xmax = point.x();
            ymax = contRect.ymax();
        } else if (compType == COMP_Y) {
            xmax = contRect.xmax();
            ymax = point.y();
        }

        return new RectHV(xmin, ymin, xmax, ymax);
    }

    private RectHV rightRectangle(Point2D point, RectHV contRect, int compType) {
        double xmin = 0, ymin = 0, xmax, ymax;

        if (compType == COMP_X) {
            xmin = point.x();
            ymin = contRect.ymin();
        } else if (compType == COMP_Y) {
            xmin = contRect.xmin();
            ymin = point.y();
        }

        xmax = contRect.xmax();
        ymax = contRect.ymax();

        return new RectHV(xmin, ymin, xmax, ymax);
    }

    private int nextCompType(int compType) {
        return ((compType + 1) % NR_COMP);
    }

    private Node insert(Node cNode, Point2D point, int compType) {
        if (cNode == null) {
            size++;
            return new Node(point);
        } else {
            Comparator<Point2D> comp = comparators[compType];
            if (comp.compare(cNode.point, point) > 0) {
                cNode.left = insert(cNode.left, point, nextCompType(compType));
            } else {
                cNode.right = insert(cNode.right, point, nextCompType(compType));
            }
            return cNode;
        }
    }

    private boolean contains(Node cNode, Point2D point, int compType) {
        if (cNode == null) {
            return false;
        } else {
            if (cNode.point.equals(point)) {
                return true;
            }
            Comparator<Point2D> comp = comparators[compType];
            if (comp.compare(cNode.point, point) > 0) {
                return contains(cNode.left, point, nextCompType(compType));
            } else {
                return contains(cNode.right, point, nextCompType(compType));
            }
        }
    }

    public void draw() {
        //draw canvas margins
        StdDraw.line(0, 0, 0, 1);
        StdDraw.line(0, 1, 1, 1);
        StdDraw.line(1, 1, 1, 0);
        StdDraw.line(1, 0, 0, 0);

        RectHV rootRect = new RectHV(0, 0, 1, 1);

        draw(root, rootRect, DEFAULT_COMP);
    }

    private void draw(Node cNode, RectHV contRect, int compType) {
        if (cNode == null) {
            return;
        }

        // draw current point
        StdDraw.setPenRadius(0.01);
        StdDraw.setPenColor(StdDraw.BLACK);
        cNode.point.draw();
        StdDraw.setPenRadius();
        StdDraw.setPenColor();

        // draw separating line
        double x1 = 0, y1 = 0, x2 = 0, y2 = 0;

        switch (compType) {
            case COMP_X:
                // split current rectangle horizontally
                x1 = x2 = cNode.point.x();
                y1 = contRect.ymin();
                y2 = contRect.ymax();
                StdDraw.setPenColor(StdDraw.RED);
                break;
            case COMP_Y:
                // split current rectangle vertically
                y1 = y2 = cNode.point.y();
                x1 = contRect.xmin();
                x2 = contRect.xmax();
                StdDraw.setPenColor(StdDraw.BLUE);
                break;

            default:
                break;
        }
        StdDraw.line(x1, y1, x2, y2);
        StdDraw.show(0);
        StdDraw.setPenColor();

        draw(cNode.left, leftRectangle(cNode.point, contRect, compType), nextCompType(compType));
        draw(cNode.right, rightRectangle(cNode.point, contRect, compType), nextCompType(compType));
    }

    private static class Node {

        private Point2D point;
        private Node left;
        private Node right;

        public Node(Point2D point, RectHV contRect) {
            this.point = point;
        }

        public Node(Point2D point) {
            this.point = point;
        }
    }

//    public static void main(String[] args) {
//
//        KdTree t = new KdTree();
//
//        t.insert(new Point2D(0.5, 0.5));
//        t.insert(new Point2D(0.4, 0.4));
//        t.insert(new Point2D(0.3, 0.3));
//        t.insert(new Point2D(0.2, 0.2));
//        t.insert(new Point2D(0.1, 0.1));
//        t.insert(new Point2D(0.3, 0.3));
//        t.insert(new Point2D(0.6, 0.6));
//        t.insert(new Point2D(0.7, 0.7));
//        t.insert(new Point2D(0.8, 0.8));
//    }
}