package towersim.util;

/**
 * Denotes a class whose state can be encoded and represented as a String
 */
public interface Encodable {

    /**
     * Returns the machine-readable encoded representation of the object
     * @return machine-readable representation
     */
    String encode();
}
