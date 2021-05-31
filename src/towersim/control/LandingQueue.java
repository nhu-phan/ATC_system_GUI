package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.PassengerAircraft;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Represents a rule-based queue of aircraft waiting in the air to land.
 * Rules are designed to ensure that aircraft are prioritised for landing
 * based on "urgency" factors.
 */
public class LandingQueue extends AircraftQueue {

    /** Queue of aircraft waiting to land */
    private Queue<Aircraft> aircraftLandingQueue;

    /** A list of all aircraft in queue, but unsorted.
     * This is used to help sorting the aircraft according to the ordering rules
     */
    private List<Aircraft> unsortedQueue = new ArrayList<>();

    /**
     * Constructs a new LandingQueue with an initially empty queue of aircraft.
     */
    public LandingQueue() {
        this.aircraftLandingQueue = new LinkedList();
    }

    /**
     * Adds the given aircraft to the queue.
     * In this case, the queue does not yet needed to be sorted as this
     * method does not return the queue
     * @param aircraft to add to queue
     */
    @Override
    public void addAircraft(Aircraft aircraft) {
        this.unsortedQueue.add(aircraft);
    }

    /**
     * Removes and returns the aircraft at the front of the queue
     * @return aircraft at front of queue, null if queue is empty
     */
    @Override
    public Aircraft removeAircraft() {
        this.aircraftLandingQueue = new LinkedList<>(getAircraftInOrder());
        // remove from sorted queue
        Aircraft toRemoveAircraft = aircraftLandingQueue.poll();
        //remove from unsorted list
        unsortedQueue.remove(toRemoveAircraft);
        return toRemoveAircraft;
    }

    /**
     * Returns the aircraft at the front of the queue without removing it.
     * Null if queue is empty.
     * @return aircraft front of landing queue
     */
    @Override
    public Aircraft peekAircraft() {
        this.aircraftLandingQueue = new LinkedList<>(getAircraftInOrder());
        return aircraftLandingQueue.peek();
    }

    /**
     * Returns a list containing all aircraft in queue, in order.
     * Amending the list should not affect original queue.
     * @return aircraft front of queue
     */
    @Override
    public List<Aircraft> getAircraftInOrder() {
        /*
        This method uses the unsorted queue to split the aircraft
        based on their level of emergency in separate array lists.
         */
        List<Aircraft> inEmergencyAircraft = new ArrayList<>();
        List<Aircraft> lowFuelListAircraft = new ArrayList<>();
        List<Aircraft> hasPassengerAircraft = new ArrayList<>();
        List<Aircraft> otherAircraft = new ArrayList<>();

        /*
        Loops through all the aircraft in the unsorted queue and add
        them to the correct arraylist according to their of emergency/urgency
         */
        for (Aircraft unsortedAircraft : this.unsortedQueue) {
            if (unsortedAircraft.hasEmergency()) {
                inEmergencyAircraft.add(unsortedAircraft);
            } else if (unsortedAircraft.getFuelPercentRemaining() <= 20) {
                lowFuelListAircraft.add(unsortedAircraft);
            } else if ((unsortedAircraft instanceof PassengerAircraft)
                    && (unsortedAircraft.calculateOccupancyLevel() != 0)) {
                hasPassengerAircraft.add(unsortedAircraft);
            } else {
                otherAircraft.add(unsortedAircraft);
            }
        }
        /*
        Adds the arraylists into the queue by order of urgency, that is:
        list containing all the aircraft with emergency, list containing all the
        aircraft with low fuel (20% or less than fuel capacity), list containing the
        aircraft which has passengers on board and finally, the arraylist of aircraft
        does not fall within any of the urgency matters above.
        */
        this.aircraftLandingQueue = new LinkedList<>();
        this.aircraftLandingQueue.addAll(inEmergencyAircraft);
        this.aircraftLandingQueue.addAll(lowFuelListAircraft);
        this.aircraftLandingQueue.addAll(hasPassengerAircraft);
        this.aircraftLandingQueue.addAll(otherAircraft);
        return new ArrayList<>(aircraftLandingQueue);
    }

    /**
     * Checks if aircraft is in queue
     * @param aircraft to check
     * @return true if aircraft in queue; otherwise false
     */
    @Override
    public boolean containsAircraft(Aircraft aircraft) {
        return getAircraftInOrder().contains(aircraft);
    }
}
