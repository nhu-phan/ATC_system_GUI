package towersim.control;

import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftType;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskType;
import towersim.util.NoSpaceException;
import towersim.util.NoSuitableGateException;
import towersim.util.Tickable;

import java.util.*;

/**
 * Represents a the control tower of an airport.
 * <p>
 * The control tower is responsible for managing the operations of the airport, including arrivals
 * and departures in/out of the airport, as well as aircraft that need to be loaded with cargo
 * at gates in terminals.
 * @ass1
 */
public class ControlTower implements Tickable {

    /** Number of ticks that have elapsed since tower was first created */
    private long ticksElapsed;

    /** Queue of aircraft waiting to land */
    private LandingQueue landingQueue;

    /** Queue of aircraft waiting to takeoff */
    private TakeoffQueue takeoffQueue;

    /** Mapping of aircraft that are loading cargo to number of ticks remaining for loading*/
    private Map<Aircraft, Integer> loadingAircraft;

    /** List of terminals under tower jurisdiction */
    private final List<Terminal> terminals;

    /** List of aircraft under tower jurisdiction */
    private final List<Aircraft> aircraft;

    /** Counter for how many times method tick() called */
    private int ticksCalled;

    /**
     * Creates a new ControlTower.
     * @ass1
     * @param ticksElapsed      number of ticks that have elapsed since tower was created
     * @param aircraft          list of aircraft management by the tower
     * @param landingQueue      queue of aircraft waiting to land
     * @param takeoffQueue      queue of aircraft waiting to takeoff
     * @param loadingAircraft   mapping of aircraft that are loading cargo to the number
     *                          of ticks remaining for loading
     */
    public ControlTower(long ticksElapsed,
                        List<Aircraft> aircraft,
                        LandingQueue landingQueue,
                        TakeoffQueue takeoffQueue,
                        Map<Aircraft, Integer> loadingAircraft) {
        this.ticksElapsed = ticksElapsed;
        this.landingQueue = landingQueue;
        this.takeoffQueue = takeoffQueue;
        this.loadingAircraft = loadingAircraft;
        this.aircraft = aircraft;
        this.terminals = new ArrayList<>();
        this.ticksCalled = 0;
    }

    /**
     * Adds the given terminal to the jurisdiction of this control tower.
     *
     * @param terminal terminal to add
     * @ass1
     */
    public void addTerminal(Terminal terminal) {
        this.terminals.add(terminal);
    }

    /**
     * Returns a list of all terminals currently managed by this control tower.
     * <p>
     * The order in which terminals appear in this list should be the same as the order in which
     * they were added by calling {@link #addTerminal(Terminal)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all terminals
     * @ass1
     */
    public List<Terminal> getTerminals() {
        return new ArrayList<>(this.terminals);
    }

    /**
     * Adds the given aircraft to the jurisdiction of this control tower.
     * <p>
     * If the aircraft's current task type is {@code WAIT} or {@code LOAD}, it should be parked at a
     * suitable gate as found by the {@link #findUnoccupiedGate(Aircraft)} method.
     * If there is no suitable gate for the aircraft, the {@code NoSuitableGateException} thrown by
     * {@code findUnoccupiedGate()} should be propagated out of this method.
     * After the aircraft has been added, it should be placed in the appropriate queues
     * by calling placeAircraftInQueues(Aircraft).
     * @param aircraft aircraft to add
     * @throws NoSuitableGateException if there is no suitable gate for an aircraft with a current
     *                                 task type of {@code WAIT} or {@code LOAD}
     * @ass1
     */
    public void addAircraft(Aircraft aircraft) throws NoSuitableGateException {
        // adds to tower jurisdiction
        this.aircraft.add(aircraft);
        TaskType currentTaskType = aircraft.getTaskList().getCurrentTask().getType();
        try {
            if (currentTaskType == TaskType.WAIT || currentTaskType == TaskType.LOAD) {
                // finds an unoccupied gate to park the given waiting/loading aircraft
                Gate gate = findUnoccupiedGate(aircraft);
                try {
                    gate.parkAircraft(aircraft);
                } catch (NoSpaceException ignored) {
                    /*
                    This should not be thrown because the findUnoccupiedGate()
                    method above returns only unoccupied gate. As such, NoSpaceException
                    should not the thrown when attempting to park at gate
                     */
                }
            }
        } catch (NoSuitableGateException e) {
            throw new NoSuitableGateException("Cannot find an unoccupied gate to park "
                    + "the waiting/loading aircraft");
        }
        // place aircraft in appropriate queue
        this.placeAircraftInQueues(aircraft);

    }

    /**
     * Returns a list of all aircraft currently managed by this control tower.
     * <p>
     * The order in which aircraft appear in this list should be the same as the order in which
     * they were added by calling {@link #addAircraft(Aircraft)}.
     * <p>
     * Adding or removing elements from the returned list should not affect the original list.
     *
     * @return all aircraft
     * @ass1
     */
    public List<Aircraft> getAircraft() {
        return new ArrayList<>(this.aircraft);
    }

    /**
     * Attempts to find an unoccupied gate in a compatible terminal for the given aircraft.
     * <p>
     * Only terminals of the same type as the aircraft's AircraftType (see
     * {@link towersim.aircraft.AircraftCharacteristics#type}) should be considered. For example,
     * for an aircraft with an AircraftType of {@code AIRPLANE}, only AirplaneTerminals may be
     * considered.
     * Terminals that are currently in a state of emergency should not be considered.
     * <p>
     * For each compatible terminal, the {@link Terminal#findUnoccupiedGate()} method should be
     * called to attempt to find an unoccupied gate in that terminal. If
     * {@code findUnoccupiedGate()} does not find a suitable gate, the next compatible terminal
     * in the order they were added should be checked instead, and so on.
     * <p>
     * If no unoccupied gates could be found across all compatible terminals, a
     * {@code NoSuitableGateException} should be thrown.
     *
     * @param aircraft aircraft for which to find gate
     * @return gate for given aircraft if one exists
     * @throws NoSuitableGateException if no suitable gate could be found
     * @ass1
     */
    public Gate findUnoccupiedGate(Aircraft aircraft) throws NoSuitableGateException {
        AircraftType aircraftType = aircraft.getCharacteristics().type;

        for (Terminal terminal : terminals) {
            /*
             * Only check for available gates at terminals that are of the same aircraft type as
             * the aircraft
             */
            if (((terminal instanceof AirplaneTerminal && aircraftType == AircraftType.AIRPLANE)
                    || (terminal instanceof HelicopterTerminal
                            && aircraftType == AircraftType.HELICOPTER))
                    && !terminal.hasEmergency()) {
                try {
                    // This terminal found a gate, return it

                    return terminal.findUnoccupiedGate();
                } catch (NoSuitableGateException e) {
                    // If this terminal has no unoccupied gates, try the next one
                }
            }
        }
        throw new NoSuitableGateException("No gate available for aircraft");
    }

    /**
     * Finds the gate where the given aircraft is parked, and returns null if the aircraft is
     * not parked at any gate in any terminal.
     *
     * @param aircraft aircraft whose gate to find
     * @return gate occupied by the given aircraft; or null if none exists
     * @ass1
     */
    public Gate findGateOfAircraft(Aircraft aircraft) {
        for (Terminal terminal : this.terminals) {
            for (Gate gate : terminal.getGates()) {
                if (Objects.equals(gate.getAircraftAtGate(), aircraft)) {
                    return gate;
                }
            }
        }
        return null;
    }

    /**
     * Advances the simulation by one tick.
     * <p>
     * On each tick, the control tower should call {@link Aircraft#tick()} on all aircraft managed
     * by the control tower.
     * <p>
     * Note that the actions performed by {@code tick()} are very simple at the moment and will be
     * expanded on in assignment 2.
     * @ass1
     */
    @Override
    public void tick() {
        //increment the counter
        this.ticksCalled++;

        // Call tick() on all other sub-entities
        for (Aircraft aircraft : this.aircraft) {
            aircraft.tick();
            //move all aircraft with AWAY or WAIT to next task
            if (aircraft.getTaskList().getCurrentTask().getType().equals(TaskType.WAIT)
                    || aircraft.getTaskList().getCurrentTask().getType().equals(TaskType.AWAY)) {
                aircraft.getTaskList().moveToNextTask();
            }
        }
        //process loading aircraft
        loadAircraft();

        // try land or allow takeoff every second tick
        if ((this.ticksCalled % 2) == 0) {
            boolean canBeLanded = this.tryLandAircraft();
            if (canBeLanded == false) {
                this.tryTakeOffAircraft();
            }
        } else if ((this.ticksCalled % 2) == 1) {
            this.tryTakeOffAircraft();
        }
        //place all aircraft in appropriate queue
        this.placeAllAircraftInQueues();
    }


    /**
     * Returns the number of ticks that have elapsed for this tower
     * If control tower was created with non-zero number of ticks, this number
     * should be taken into account
     * @return ticks elapsed
     */
    public long getTicksElapsed() {
        return this.ticksElapsed + this.ticksCalled;
    }

    /**
     * Return the queue of aircraft waiting to land
     * @return landing queue
     */
    public AircraftQueue getLandingQueue() {
        return this.landingQueue;
    }

    /**
     * Return the queue of aircraft waiting to takeoff
     * @return takeoff queue
     */
    public AircraftQueue getTakeoffQueue() {
        return this.takeoffQueue;
    }

    /**
     * Return the mapping of the loading aircraft to their remaining load times
     * @return loading aircraft map
     */
    public Map<Aircraft, Integer> getLoadingAircraft() {
        return this.loadingAircraft;
    }

    /**
     * Attempts to land one aircraft waiting in the landing queue and park it at a suitable gate.
     * If no aircraft in landing queue, return false
     * If there is one or more aircraft, find suitable gate for the first one in the queue.
     * If no suitable gate found, then aircraft should not be landed and should remain in the queue.
     * Thus, return false.
     * If there is a suitable gate, aircraft should be removed from queue and park at the gate.
     * Aircraft should be unloaded.
     * Aircraft should move onto the next task and return true
     * @return true if aircraft was successfully landed and parked; false otherwise
     */
    public boolean tryLandAircraft() {
        if (this.getLandingQueue().getAircraftInOrder().isEmpty()) {
            // if landing queue is empty, do nothing and return false
            return false;
        }
        Aircraft aircraftToLand = this.landingQueue.peekAircraft();
        try {
            // finds gate
            Gate gateToLand = this.findUnoccupiedGate(aircraftToLand);
            // removes from queue
            this.landingQueue.removeAircraft();
            // parks at gate, unload aircraft and moves to next task
            gateToLand.parkAircraft(aircraftToLand);
            aircraftToLand.unload();
            aircraftToLand.getTaskList().moveToNextTask();
            return true;
        } catch (NoSuitableGateException e) {
            // no gate found, do nothing and return false
            return false;
        } catch (NoSpaceException ignored) {
            /*
            This should not be thrown because the findUnoccupiedGate()
            method above returns only unoccupied gate. As such, NoSpaceException
            should not the thrown when attempting to park at gate
             */
        }
        return false;
    }

    /**
     * Attempts to allow one aircraft waiting in the takeoff queue to take off
     * If no aircraft in queue, do nothing.
     * If aircraft in queue, remove the first aircraft and said aircraft should
     * move to next task
     */
    public void tryTakeOffAircraft() {
        if (this.getTakeoffQueue().getAircraftInOrder().isEmpty()) {
            // if queue is empty d nothing
        } else {
            this.getTakeoffQueue().peekAircraft().getTaskList().moveToNextTask();
            this.getTakeoffQueue().removeAircraft();
        }
    }

    /**
     * Updates time remaining to load on all currently loading aircraft and removes
     * aircraft from their gate once finished loading.
     * Aicraft in loading map should have their time remaining decremented by one tick;
     * If time == 0, then remove from loading map and leave the gate it is parked.
     * Said aircraft must also move onto the next task.
     */
    public void loadAircraft() {
        /*
        To prevent Concurrent Modification Exception, create an new map
        to update/decrement the ticks .
         */
        Map<Aircraft, Integer> updatedLoadingAircraft = new HashMap<>();
        for (Map.Entry<Aircraft, Integer> aircraftWithLoad : this.getLoadingAircraft().entrySet()) {
            // decrement ticks
            int newTime = aircraftWithLoad.getValue() - 1;
            Aircraft aircraftLoaded = aircraftWithLoad.getKey();
            if (newTime > 0) { // update decremented ticks
                updatedLoadingAircraft.put(aircraftLoaded, newTime);
            } else if (newTime == 0) {
                /*
                If tick is 0 after decremented, remove from loading map.
                Do not have to add to thew new updated map.
                 */
                if (this.findGateOfAircraft(aircraftLoaded) != null) {
                    this.findGateOfAircraft(aircraftLoaded).aircraftLeaves();
                }
                // should move onto next task
                aircraftLoaded.getTaskList().moveToNextTask();
            }
        }
        // updates this tower's loadingAircraft
        this.loadingAircraft = updatedLoadingAircraft;
    }

    /**
     * Put all aircraft managed by the tower in queue.
     */
    public void placeAllAircraftInQueues() {
        for (Aircraft individualAircraft : this.getAircraft()) {
            placeAircraftInQueues(individualAircraft);
        }
    }

    /**
     * Moves the given aircraft to appropriate queue based on its current task
     * If current task type is LAND and is not in the queue, then add to landing queue.
     * If current task type if TAKEOFF and is not in queue, then add to takeoff queue.
     * If current task type is LOAD and is not in loading map, add to loading map.
     * @param aircraft to place in queue
     */
    public void placeAircraftInQueues(Aircraft aircraft) {
        if (aircraft.getTaskList().getCurrentTask().getType() == TaskType.LAND
                && !this.getLandingQueue().getAircraftInOrder().contains(aircraft)) {
            this.getLandingQueue().addAircraft(aircraft);
        } else if (aircraft.getTaskList().getCurrentTask().getType() == TaskType.TAKEOFF
                && !this.getTakeoffQueue().getAircraftInOrder().contains(aircraft)) {
            this.getTakeoffQueue().addAircraft(aircraft);
        } else if (aircraft.getTaskList().getCurrentTask().getType() == TaskType.LOAD
                && !this.getLoadingAircraft().containsKey(aircraft)) {
            this.loadingAircraft.put(aircraft, aircraft.getLoadingTime());
        }
    }

    /**
     * Returns the human-readable string representation of this control tower.
     * Format: ControlTower: numTerminals, terminals, numAircraft total aircraft (numLanding LAND,
     * numTakeoff TAKEOFF, numLoad LOAD
     * @return human-readable string representation of tower
     */
    @Override
    public String toString() {
        return String.format("ControlTower: %s terminals, %s total aircraft "
                        + "(%s LAND, %s TAKEOFF, %s LOAD)",
                this.getTerminals().size(),
                this.getAircraft().size(),
                this.getLandingQueue().getAircraftInOrder().size(),
                this.getTakeoffQueue().getAircraftInOrder().size(),
                this.getLoadingAircraft().size());
    }
}
