package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.util.Encodable;

import java.util.List;
import java.util.StringJoiner;

/**
 * An abstract representation of a queue containing aircraft.
 */
public abstract class AircraftQueue implements Encodable {

    /**
     * Adds given aircraft to queue
     * @param aircraft to add
     */
    public abstract void addAircraft(Aircraft aircraft);

    /**
     * Remove and returns aircraft at front of queue.
     * @return aircraft front of queue. Null if queue is empty
     */
    public abstract Aircraft removeAircraft();

    /**
     * Returns the aircraft at the front of the queue without removing it.
     * Null if queue is empty.
     * @return aircraft front of queue
     */
    public abstract Aircraft peekAircraft();

    /**
     * Returns a list containing all aircraft in queue, in order.
     * Amending the list should not affect original queue.
     * @return aircraft front of queue
     */
    public abstract List<Aircraft> getAircraftInOrder();

    /**
     * Checks if aircraft is in queue
     * @param aircraft to check
     * @return true if aircraft in queue; otherwise false
     */
    public abstract boolean containsAircraft(Aircraft aircraft);

    /**
     * Returns human-readable string representation of queue
     * Format: QueueType [callsign1, callsign2, ... callsignN]
     * @return human-readable string representation of queue
     */
    public String toString() {
        StringJoiner callsignList = new StringJoiner(", ", "[", "]");
        for (Aircraft aircraft : this.getAircraftInOrder()) {
            callsignList.add(aircraft.getCallsign());
        }
        return String.format("%s %s", this.getClass().getSimpleName(),
                callsignList);
    }

    /**
     * Returns machine-readable string representation of queue
     * Format:
     * QueueType:numAircraft
     * callsign1,callsign2,...,callsignN
     * @return machine-readable string representation
     */
    public String encode() {
        String queueInfo = String.format("%s:%s", this.getClass().getSimpleName(),
                this.getAircraftInOrder().size());
        StringJoiner callsignList = new StringJoiner(",");
        if (getAircraftInOrder().size() > 0) {
            queueInfo += System.lineSeparator();
            for (Aircraft aircraft : this.getAircraftInOrder()) {
                callsignList.add(aircraft.getCallsign());
            }
        }
        return queueInfo + callsignList;
    }
}


