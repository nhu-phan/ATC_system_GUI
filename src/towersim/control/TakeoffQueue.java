package towersim.control;

import towersim.aircraft.Aircraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a first-in-first-out queue of aircraft waiting to takeoff.
 */
public class TakeoffQueue extends AircraftQueue {

    /** Queue containing all aircraft waiting to take off */
    private Queue<Aircraft> aircraftToTakeoff;

    /**
     * Constructs a takeoff queue with an initially empty queue.
     */
    public TakeoffQueue() {
        this.aircraftToTakeoff = new LinkedList();
    }

    /**
     * Adds given aircraft to queue
     * @param aircraft to add
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        this.aircraftToTakeoff.add(aircraft);
    }

    /**
     * Returns aircraft at front of queue without removing it from the queue.
     * @return aircraft at front of queue. If queue is empty, return null.
     */
    @Override
    public Aircraft peekAircraft() {
        return this.aircraftToTakeoff.peek();
    }

    /**
     * Removes and returns aircraft at front of queue.
     * @return aircraft removed. If queue is empty, return null.
     */
    @Override
    public Aircraft removeAircraft() {
        return this.aircraftToTakeoff.poll();
    }

    /**
     * Returns a list containing all aircraft in queue, in order.
     * @return list of aircraft in queue in order
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        return new LinkedList(this.aircraftToTakeoff);
    }

    /**
     * checks whether aircraft is in queue
     * @param aircraft to check
     * @return true if aircraft is in queue, otherwise false.
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return this.aircraftToTakeoff.contains(aircraft);
    }

}
