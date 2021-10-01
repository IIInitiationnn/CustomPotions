package xyz.iiinitiationnn.custompotions;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.bukkit.Color;
import xyz.iiinitiationnn.custompotions.utils.ColourUtil;

import java.io.Serializable;
import java.util.List;

import static xyz.iiinitiationnn.custompotions.utils.ColourUtil.getChatColor;
import static xyz.iiinitiationnn.custompotions.utils.ColourUtil.randomDefaultColour;

public class Colour implements Serializable {
    /*Colour RED = new Colour(0xFF, 0x00, 0x00);
    Colour ORANGE = new Colour(0xFF, 0xA5, 0x00);
    Colour YELLOW = new Colour(0xFF, 0xFF, 0x00);
    Colour OLIVE = new Colour(0x80, 0x80, 0x00);
    Colour LIME = new Colour(0x00, 0xFF, 0x00);
    Colour GREEN = new Colour(0x00, 0x80, 0x00);
    Colour TEAL = new Colour(0x00, 0x80, 0x80);
    Colour AQUA = new Colour(0x00, 0xFF, 0xFF);
    Colour FUCHSIA = new Colour(0xFF, 0x00, 0xFF);
    Colour MAROON = new Colour(0x80, 0x00, 0x00);
    Colour PURPLE = new Colour(0x80, 0x00, 0x80);
    Colour BLUE = new Colour(0x00, 0x00, 0xFF);
    Colour NAVY = new Colour(0x00, 0x00, 0x80);
    Colour WHITE = new Colour(0xFF, 0xFF, 0xFF);
    Colour SILVER = new Colour(0xC0, 0xC0, 0xC0);
    Colour GRAY = new Colour(0x80, 0x80, 0x80);
    Colour BLACK = new Colour(0x00, 0x00, 0x00);
    List<Colour> listColour = new ArrayList<Colour>() {{
        add(RED);
        add(ORANGE);
        add(YELLOW);
        add(OLIVE);
        add(LIME);
        add(GREEN);
        add(TEAL);
        add(AQUA);
        add(FUCHSIA);
        add(MAROON);
        add(PURPLE);
        add(BLUE);
        add(NAVY);
        add(WHITE);
        add(SILVER);
        add(GRAY);
        add(BLACK);
    }};*/
    private int r;
    private int g;
    private int b;

    // Constructors

    /**
     * Construct a random Colour from the default colours.
     */
    public Colour() {
        Colour random = randomDefaultColour();
        this.r = random.r;
        this.g = random.g;
        this.b = random.b;
    }

    /**
     * Construct a Colour from a Bukkit Color.
     */
    public Colour(Color c) {
        this.r = c.getRed();
        this.g = c.getGreen();
        this.b = c.getBlue();
    }

    /**
     * Construct a Colour from given RGB values (0 <= RGB <= 255).
     */
    public Colour(int r, int g, int b) {
        Validate.isTrue(hasValidRGB(r, g, b), String.format("RGB values not between 0-255: (%d, %d, %d)", r, g, b));
        this.r = r;
        this.g = g;
        this.b = b;
    }

    // Methods
    public static boolean hasValidRGB(int r, int g, int b) {
        return r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255;
    }
    public int getR() {
        return this.r;
    }
    public int getG() {
        return this.g;
    }
    public int getB() {
        return this.b;
    }
    public double dist(Colour that) {
        int diffR = Math.abs(this.r - that.r);
        int diffG = Math.abs(this.g - that.g);
        int diffB = Math.abs(this.b - that.b);
        double avgR = (this.r + that.r) * 0.5;

        // Redmean
        return (2 + avgR / 256) * diffR * diffR + 4 * diffG * diffG + (2 + (255 - avgR) / 256) * diffB * diffB;
    }

    /**
     * Returns Bukkit object representing a Color using the CustomPotions Colour type.
     */
    public Color toBukkitColor() {
        return Color.fromRGB(this.r, this.g, this.b);
    }

    public Colour closestMatchFromList(List<Colour> colours) {
        colours.sort((Colour a, Colour b) -> this.dist(a) >= this.dist(b) ? 1 : -1);
        /*for (Colour c : colours)
            Main.log.info(ColourUtil.colourChatColorMap().get(c) + String.format("%d, %d, %d", c.r, c.g, c.b));*/
        return colours.get(0);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 31). // two randomly chosen prime numbers
            append(this.r).
            append(this.g).
            append(this.b).
            toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Colour))
            return false;
        if (obj == this)
            return true;

        Colour c = (Colour) obj;
        return new EqualsBuilder().
            append(this.r, c.r).
            append(this.g, c.g).
            append(this.b, c.b).
            isEquals();
    }


}
