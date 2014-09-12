package zimpel.smart.rr;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * A representation of a rectangle
 */
public class Rectangle {

    public int x; // x position
    public int y; // y position
    public int w; // width
    public int h; // height

    /**
     * constructor.
     * @param x Width
     * @param y Height
     */
    @JsonCreator
    public Rectangle(final int x, final int y, final int w, final int h) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
    }

}
