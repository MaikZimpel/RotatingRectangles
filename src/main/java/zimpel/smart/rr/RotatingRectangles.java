package zimpel.smart.rr;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.regex.Pattern;

import static javax.swing.JOptionPane.showMessageDialog;
import static zimpel.smart.rr.DrawingArea.RectangleOrientation.HORIZONTAL;
import static zimpel.smart.rr.DrawingArea.RectangleOrientation.VERTICAL;

/**
 * Main Class
 */
public class RotatingRectangles implements ActionListener {


    /**
     * Array of Rectangle containing the randomly generated rectangles
     */
    private Rectangle[] originalArray = new Rectangle[]{};
    /**
     * Array of Rectangle containing the solution
     */
    private Rectangle[] solutionArray = new Rectangle[]{};

    /**
     * Width of the JFrame (Program Window)
     */
    private final static int WIDTH = 800;

    /**
     * Height of the JFrame (Program Window)
     */
    private final static int HEIGHT = WIDTH / 12 * 9;

    /**
     * Window title
     */
    private final static String TITLE = "Rotating Rectangle Problem";

    /**
     * The textfield where to type in the number of horizontal rectangles
     */
    private final JTextField input;

    /**
     * The randomly generated horizontal rectangles are drawn in there
     */
    private final DrawingArea originalDrawing = new DrawingArea(originalArray, HORIZONTAL);

    /**
     * The calculated vertical rectangles are drawn in there
     */
    private final DrawingArea solutionDrawing = new DrawingArea(solutionArray, VERTICAL);

    /**
     * The Printstream to where to log error messages and debug infos (and a tool to see what's going on)
     */
    private final PrintStream consoleOut;

    /**
     * The Prinstream to save to a file
     */
    private PrintStream fileOut ;


    /**
     * Constructor.
     */
    public RotatingRectangles() {
        // Define the Window
        JFrame frame = new JFrame(TITLE);
        frame.setMinimumSize(new Dimension(WIDTH, HEIGHT));
        frame.setMaximumSize(new Dimension(WIDTH, HEIGHT));
        frame.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        // Create the panel with the textfield and the button
        final JPanel controlPanel = new JPanel(new FlowLayout());
        frame.add(controlPanel, BorderLayout.NORTH);
        final JButton ok = new JButton("OK");
        input = new JTextField();
        // here we add the actual action that happens if the Button is clicked
        ok.addActionListener(this);
        input.setColumns(5);
        input.setHorizontalAlignment(SwingConstants.RIGHT);
        input.setToolTipText("Enter the number of random shapes here.");
        controlPanel.add(new JLabel("Number of input rectangles (between 3 and 30)"));
        controlPanel.add(input);
        controlPanel.add(ok);
        // this TextArea will contain our cool Matrix style console output
        JTextArea jTerm = new JTextArea(10, 80);
        jTerm.setBackground(Color.BLACK);
        jTerm.setForeground(Color.GREEN);
        jTerm.setFont(new Font("Courier", Font.TRUETYPE_FONT, 12));
        jTerm.setBounds(20,20,100,100);
        Console console = new Console(jTerm, System.getProperty("user.name")+"$");
        frame.add(new JScrollPane(jTerm, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.SOUTH);
        // set the console out output stream to print into our jTerm
        consoleOut = new PrintStream(console);
        // create a new File in the users home directory with the name rotating-rectangles suffixed by the current date and time
        // we add the time because we are going to create a new file each time the program starts
        String filename = System.getProperty("user.home")+"/rotating-rectangles."+ new SimpleDateFormat("dMyyyy_hhmmss").format(new Date())+".json";
        try {
            // The stream is closed as soon as the program exits
            fileOut = new PrintStream(new FileOutputStream(filename));
        } catch (IOException x) {
            print("I can't open the file to save to for some reason. There is probably a SecurityManager in the way. " +
                    "Look at the stack trace for more information. You find it in the java console.\n " +
                    "I will just display the input and output data here so long. Saving to file is disabled.", consoleOut);
            x.printStackTrace();
        }

        // canvases to draw on
        JPanel drawings = new JPanel();
        drawings.setBorder(BorderFactory.createLineBorder(Color.WHITE, 10));
        drawings.setLayout(new BoxLayout(drawings, BoxLayout.Y_AXIS));
        originalDrawing.setBackground(Color.GRAY);
        solutionDrawing.setBackground(Color.BLACK);
        solutionDrawing.setForeground(Color.WHITE);
        drawings.add(originalDrawing);
        drawings.add(solutionDrawing);
        frame.add(drawings, BorderLayout.CENTER);
        frame.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // validate input
        if(!input.getText().isEmpty() && Pattern.matches("[-|+]?[0-9]*",input.getText())) {
            Integer shapeNumber = Integer.valueOf(input.getText());
            if(shapeNumber > 2 && shapeNumber < 31) {
                originalArray = createRandomShapes(shapeNumber);
                print("{INPUT:[", consoleOut, fileOut);
                int sii = 0;
                print(originalArray, consoleOut, fileOut);
                print("]}", consoleOut, fileOut);
                // draw the input data
                originalDrawing.setRectangles(originalArray);
                // make a copy of the array and put it into a list,
                // it's easier to work with
                List<Rectangle> originalList = listCopy(originalArray);
                // the solutions list contains all the calculated vertical rectangles
                List<Rectangle> solution = new ArrayList<>();
                /**
                 * Here is all the action:
                 *
                 * 1. We go from left to right and collect all horizontal rectangles that are adjacent to each other
                 * 2. We determine the height of the horizontal slice by determining the shortest rectangle of the current slice
                 * 3. We extract the resulting vertical rectangle and save it to our solutions list
                 * 4. We move the y-axis up by the height of the horizontal rectangle that we have just extracted
                 * 5. We repeat the process until the original list is empty
                 *
                 * (It's almost like Tetris)
                 *
                 */
                while(!originalList.isEmpty()) {
                    solution.add(remove(extract(getLeft(originalList)), originalList));
                }
                // transform the solution list back into an array because our display component prefers it that way
                solutionArray = solution.toArray(new Rectangle[solution.size()]);
                // draw the solution
                solutionDrawing.setRectangles(solutionArray);
                // write a textual interpretation in json format to the console and to the file
                print("{OUTPUT:[", consoleOut, fileOut);
                print(solutionArray, consoleOut, fileOut);
                print("]}", consoleOut, fileOut);
            } else {
                print("Please stick to numbers in between 3 and 30!", consoleOut);
            }

        } else {
            print("Numbers are those things that only consists of digits like 0 to 9 [0-9].", consoleOut);
        }
    }

    private List<Rectangle> getLeft(List<Rectangle> set) {
        List<Rectangle> left = new ArrayList<>();
        int x = set.get(0).x;
        for(Rectangle r : set) {
            if(r.x == x) {
                left.add(r);
            }
            x += r.w;
        }
        return left;
    }

    private Rectangle extract(List<Rectangle> set) {
        int x = set.get(0).x;
        int y = set.get(0).y;
        int w = 0;
        int h = set.get(0).h;
        for(Rectangle r : set) {
            if(r.h < h) {
                h = r.h;
            }
            w += r.w;
        }
        return new Rectangle(x,y,w,h);
    }

    private Rectangle remove(Rectangle rect, List<Rectangle> set) {
        List<Rectangle> removals = new ArrayList<>();
        for(Rectangle r : set) {
            if(r.h == rect.h && r.x <= rect.w + rect.x) {
                removals.add(r);
            }
            if(r.x <= rect.w + rect.x) {
                r.h -= rect.h;
                r.y += rect.h;
            }
        }
        set.removeAll(removals);
        return rect;
    }

    private List<Rectangle> listCopy(Rectangle[] originalArray) {
        List<Rectangle> result = new ArrayList<>();
        for(Rectangle r : originalArray) {
            result.add(new Rectangle(r.x, r.y, r.w, r.h));
        }
        return result;
    }

    private Rectangle[] createRandomShapes(final int shapeNumber) {
        Iterator<Integer> randomNumbers = new Random().ints(shapeNumber *2, 9, 25).iterator();
        Rectangle[] result = new Rectangle[shapeNumber];
        int x = 0;
        for (int i = 0; i < shapeNumber; i++){
            result[i] = new Rectangle(x,0,randomNumbers.next(), randomNumbers.next()*4);
            x = result[i].x + result[i].w;
        }
        return result;
    }

    private void print(Rectangle[] r, PrintStream ... out) {
        for(PrintStream ps : out) {
            try {
                ps.println(new ObjectMapper().writerWithDefaultPrettyPrinter().writeValueAsString(r));
                ps.flush();
            } catch (JsonProcessingException x) {
                x.printStackTrace();
            }
        }
    }

    private void print(final String s, final PrintStream ... out) {
        for(PrintStream ps : out) {
            ps.println(s);
            ps.flush();
        }
    }

    public static void main(String ... args) throws Exception {
        new RotatingRectangles();
    }
}
