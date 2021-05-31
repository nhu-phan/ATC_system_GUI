package towersim.aircraft;

import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.EmergencyState;
import towersim.util.OccupancyLevel;
import towersim.util.Tickable;

import java.util.Objects;

/**
 * Represents an aircraft whose movement is managed by the system.
 * @ass1
 */
public abstract class Aircraft implements OccupancyLevel, Tickable, EmergencyState {

    /**
     * Weight of a litre of aviation fuel, in kilograms.
     * @ass1
     */
    public static final double LITRE_OF_FUEL_WEIGHT = 0.8;

    /** Unique callsign to identify the aircraft */
    private String callsign;

    /** Characteristics of this aircraft including weight, fuel capacity, etc. */
    private AircraftCharacteristics characteristics;

    /** List of tasks representing the aircraft's desired operations */
    private TaskList tasks;

    /** Current amount of fuel onboard, in litres */
    private double fuelAmount;

    /** Whether the aircraft is currently in a state of emergency */
    private boolean emergency;

    /**
     * Creates a new aircraft with the given callsign, task list, fuel capacity and amount.
     * <p>
     * Newly created aircraft should not be in a state of emergency by default.
     * <p>
     * If the given fuel amount is less than zero or greater than the aircraft's maximum fuel
     * capacity as defined in the aircraft's characteristics, then an
     * {@code IllegalArgumentException} should be thrown.
     *
     * @param callsign        unique callsign
     * @param characteristics characteristics that describe this aircraft
     * @param tasks           task list to be used by aircraft
     * @param fuelAmount      current amount of fuel onboard, in litres
     * @throws IllegalArgumentException if fuelAmount &lt; 0 or if fuelAmount &gt; fuel capacity
     * @ass1
     */
    protected Aircraft(String callsign, AircraftCharacteristics characteristics, TaskList tasks,
            double fuelAmount) {
        if (fuelAmount < 0) {
            throw new IllegalArgumentException("Amount of fuel onboard cannot be negative");
        }
        if (fuelAmount > characteristics.fuelCapacity) {
            throw new IllegalArgumentException("Amount of fuel onboard cannot exceed capacity");
        }
        this.callsign = callsign;
        this.characteristics = characteristics;
        this.tasks = tasks;
        this.fuelAmount = fuelAmount;
        this.emergency = false;
    }

    /**
     * Returns the callsign of the aircraft.
     *
     * @return aircraft callsign
     * @ass1
     */
    public String getCallsign() {
        return callsign;
    }

    /**
     * Returns the current amount of fuel onboard, in litres.
     *
     * @return current fuel amount
     * @ass1
     */
    public double getFuelAmount() {
        return fuelAmount;
    }

    /**
     * Returns this aircraft's characteristics.
     *
     * @return aircraft characteristics
     * @ass1
     */
    public AircraftCharacteristics getCharacteristics() {
        return characteristics;
    }

    /**
     * Returns the percentage of fuel remaining, rounded to the nearest whole percentage, 0 to 100.
     * <p>
     * This is calculated as 100 multiplied by the fuel amount divided by the fuel capacity,
     * rounded to the nearest integer.
     *
     * @return percentage of fuel remaining
     * @ass1
     */
    public int getFuelPercentRemaining() {
        return (int) Math.round(100 * fuelAmount / this.characteristics.fuelCapacity);
    }

    /**
     * Returns the total weight of the aircraft in its current state.
     * <p>
     * Note that for the Aircraft class, any passengers/freight carried is not included in this
     * calculation. The total weight for an aircraft is calculated as the sum of:
     * <ul>
     * <li>the aircraft's empty weight</li>
     * <li>the amount of fuel onboard the aircraft multiplied by the weight of a litre of fuel</li>
     * </ul>
     *
     * @return total weight of aircraft in kilograms
     * @ass1
     */
    public double getTotalWeight() {
        return this.getCharacteristics().emptyWeight + this.fuelAmount * LITRE_OF_FUEL_WEIGHT;
    }

    /**
     * Returns the task list of this aircraft.
     *
     * @return task list
     * @ass1
     */
    public TaskList getTaskList() {
        return this.tasks;
    }

    /**
     * Returns the number of ticks required to load the aircraft at the gate.
     * <p>
     * Different types and models of aircraft have different loading times.
     *
     * @return time to load aircraft, in ticks
     * @ass1
     */
    public abstract int getLoadingTime();

    /**
     * Updates the aircraft's state on each tick of the simulation.
     * <p>
     * Aircraft burn fuel while flying. If the aircraft's current task is {@code AWAY}, the amount
     * of fuel on the aircraft should decrease by 10% of the total capacity. If the fuel burned
     * during an {@code AWAY} tick would result in the aircraft having a negative amount of fuel,
     * the fuel amount should instead be set to zero.
     * <p>
     * Aircraft are refuelled while loading at the gate. If the aircraft's current task is
     * {@code LOAD}, the amount of fuel should increase by {@code capacity/loadingTime} litres of
     * fuel.
     * For example, if the fuel capacity is 120 litres and {@code loadingTime}
     * (returned by {@link #getLoadingTime()}) is 3, the amount of fuel should increase by
     * 40 litres each tick. Note that refuelling should not result in the aircraft's fuel onboard
     * exceeding its maximum fuel capacity.
     * @ass1
     */
    @Override
    public void tick() {
        TaskType currentTaskType = this.tasks.getCurrentTask().getType();

        // fuel amount drops by 10% of capacity each AWAY tick
        if (currentTaskType == TaskType.AWAY) {
            this.fuelAmount -= this.characteristics.fuelCapacity / 10;
            // fuel amount can't go below 0
            if (this.fuelAmount < 0) {
                this.fuelAmount = 0;
            }
        }

        // loading replenishes fuelCapacity/loadingTime of maximum fuel capacity
        if (currentTaskType == TaskType.LOAD) {
            this.fuelAmount = Math.min(this.characteristics.fuelCapacity,
                    this.fuelAmount + this.characteristics.fuelCapacity / getLoadingTime());
        }
    }

    /**
     * Returns the human-readable string representation of this aircraft.
     * <p>
     * The format of the string to return is
     * <pre>aircraftType callsign model currentTask</pre>
     * where {@code aircraftType} is the AircraftType of the aircraft's AircraftCharacteristics,
     * {@code callsign} is the aircraft's callsign, {@code model} is the string representation
     * of the aircraft's AircraftCharacteristics, and {@code currentTask} is the task type of
     * the aircraft's current task.
     * <p>
     * If the aircraft is currently in a state of emergency, the format of the string to return is
     * <pre>aicraftType callsign model currentTask (EMERGENCY)</pre>
     * For example, {@code "AIRPLANE ABC123 AIRBUS_A320 LOAD (EMERGENCY)"}.
     *
     * @return string representation of this aircraft
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("%s %s %s %s%s",
                this.characteristics.type,
                this.callsign,
                this.characteristics,
                this.tasks.getCurrentTask().getType(),
                this.emergency ? " (EMERGENCY)" : "");
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public void declareEmergency() {
        this.emergency = true;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public void clearEmergency() {
        this.emergency = false;
    }

    /**
     * {@inheritDoc}
     * @ass1
     */
    @Override
    public boolean hasEmergency() {
        return emergency;
    }

    /**
     * Unloads the aircraft of all cargo (passenger/freight) the aircraft is currently carrying
     */
    public abstract void unload();

    /**
     * Checks if two aircraft are equal. Two aircraft are equal if they have the same
     * callsign and characteristics.
     * @param obj to be compared with
     * @return true if and only if aircraft is equal to the given aircraft; otherwise false
     */
    public boolean equals(Object obj) {
        if (obj instanceof Aircraft) {
            if ((((Aircraft) obj).getCallsign().equals(this.getCallsign()))
                    && (((Aircraft) obj).getCharacteristics().equals(this.getCharacteristics()))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the hash code of the aircraft. If two aircraft are equal, they must have
     * the same hash code.
     * @return hash code of this aircraft
     */
    public int hashCode() {
        return Objects.hash(this.getCallsign(), this.getCharacteristics());
    }

    /**
     * Returns the machine-readable string representation of the aircraft
     * In the format: callsign:model:taskListEncoded:fuelAmount:emergency
     * @return machine-readable string representation
     */
    public String encode() {
        return String.format("%s:%s:%s:%s:%s",
                this.callsign,
                this.getCharacteristics().name(),
                this.tasks.encode(),
                String.format("%.2f", this.getFuelAmount()),
                this.hasEmergency());
    }
}





