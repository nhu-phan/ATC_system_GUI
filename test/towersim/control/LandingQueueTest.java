package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;



public class LandingQueueTest {

    private TaskList taskList1;
    private TaskList taskList2;
    private Aircraft aircraft1;
    private Aircraft aircraft2;
    private Aircraft aircraftlowFuel;
    private Aircraft aircraftlowFuel2;
    private Aircraft aircraftWithPassengers;
    private Aircraft aircraftWithPassengers2;
    private LandingQueue landingQueue;


    @Before
    public void setup() {
        this.taskList1 = new TaskList(List.of(
                new Task(TaskType.LOAD, 0), // load no freight
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND)));

        this.taskList2 = new TaskList(List.of(
                new Task(TaskType.LOAD, 30), // load 30% of capacity of freight
                new Task(TaskType.TAKEOFF),
                new Task(TaskType.AWAY),
                new Task(TaskType.LAND)));

        this.aircraft1 = new FreightAircraft("ABC001", AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.6,
                AircraftCharacteristics.BOEING_747_8F.freightCapacity);

        this.aircraft2 = new FreightAircraft("ABC002", AircraftCharacteristics.BOEING_747_8F,
                taskList2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.6,
                60000);

        this.aircraftlowFuel = new FreightAircraft("ABC003", AircraftCharacteristics.BOEING_747_8F,
                taskList2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.1,
                60000);

        this.aircraftlowFuel2 = new FreightAircraft("ABC006", AircraftCharacteristics.BOEING_747_8F,
                taskList2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.05,
                60000);

        this.aircraftWithPassengers = new PassengerAircraft("ABC004", AircraftCharacteristics.AIRBUS_A320,
                taskList2,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity * 0.6,
                12);

        this.aircraftWithPassengers2 = new PassengerAircraft("ABC007", AircraftCharacteristics.AIRBUS_A320,
                taskList2,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity * 0.6,
                12);

        landingQueue = new LandingQueue();
    }

    @Test
    // checks addAircraft and containsAircraft
    public void addAircraftTestOne() {
        ;
        landingQueue.addAircraft(aircraft1);
        assertTrue(landingQueue.containsAircraft(aircraft1));
    }

    @Test
    // contains multiple aircraft in queue
    public void addAircraftTestTwo() {
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        assertTrue(landingQueue.containsAircraft(aircraft1));
        assertTrue(landingQueue.containsAircraft(aircraft2));
    }

    @Test
    public void addAircraftTestToQueue1() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft1.declareEmergency();
        landingQueue.addAircraft(aircraft1);
        // current Queue should be [aircraft1, aircraftlowFuel2, aircraftWithPassengers]
        List<Aircraft> expected = new ArrayList<>(List.of(aircraft1, aircraftlowFuel2, aircraftWithPassengers));
        assertEquals(expected, landingQueue.getAircraftInOrder());
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        List<Aircraft> expected2 = new ArrayList<>(List.of(aircraft1, aircraft2,
                aircraftlowFuel2, aircraftWithPassengers));
        assertEquals(expected2, landingQueue.getAircraftInOrder());
    }

    @Test
    public void addAircraftTestToQueue2() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft1.declareEmergency();
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        List<Aircraft> expected = new ArrayList<>(List.of(aircraft1, aircraftlowFuel2,
                aircraftWithPassengers, aircraft2));
        assertEquals(expected, landingQueue.getAircraftInOrder());
        assertEquals(4, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void addAircraftTestToQueue3() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft1.declareEmergency();
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraftWithPassengers2);
        List<Aircraft> expected = new ArrayList<>(List.of(aircraft1, aircraftlowFuel2,
                aircraftWithPassengers, aircraftWithPassengers2, aircraft2));
        assertEquals(expected, landingQueue.getAircraftInOrder());
        assertEquals(5, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void peekTest1() {
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        Aircraft peekedAircraft = landingQueue.peekAircraft();
        assertEquals(aircraft2, peekedAircraft); // as first in queue
        assertEquals(2, landingQueue.getAircraftInOrder().size()); // nothing should be removed
    }

    @Test
    public void peekTest2() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft1.declareEmergency();
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        Aircraft peekedAircraft = landingQueue.peekAircraft();
        assertEquals(aircraft1, peekedAircraft); // as first in queue
        assertEquals(4, landingQueue.getAircraftInOrder().size()); // nothing should be removed
    }

    @Test
    public void peekTest3() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraftWithPassengers2);
        Aircraft peekedAircraft = landingQueue.peekAircraft();
        assertEquals(aircraft2, peekedAircraft); // as first in queue
        assertEquals(5, landingQueue.getAircraftInOrder().size()); // nothing should be removed
    }

    @Test
    //empty queue
    public void peekTest4() {
        Aircraft peekedAircraft = landingQueue.peekAircraft();
        assertEquals(null, peekedAircraft);
        assertEquals(0, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void removeAircraftTest1() {
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        Aircraft aircraftRemoved = landingQueue.removeAircraft();
        assertEquals(aircraft2, aircraftRemoved);
        assertEquals(1, landingQueue.getAircraftInOrder().size());
        assertTrue(!landingQueue.containsAircraft(aircraft2));
    }

    @Test
    public void removeAircraftTest2() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft1.declareEmergency();
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        Aircraft aircraftRemoved = landingQueue.removeAircraft();
        assertEquals(aircraft1, aircraftRemoved);
        System.out.println(landingQueue.toString());
        assertEquals(3, landingQueue.getAircraftInOrder().size());
        assertTrue(!landingQueue.containsAircraft(aircraft1));
    }

    @Test
    public void removeAircraftTest3() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraftWithPassengers2);
        Aircraft aircraftRemoved = landingQueue.removeAircraft();
        assertEquals(aircraft2, aircraftRemoved);
        assertEquals(4, landingQueue.getAircraftInOrder().size());

    }

    @Test
    // if queue is null
    public void removeAircraftTest4() {
        Aircraft aircraftRemoved = landingQueue.removeAircraft();
        assertEquals(null, aircraftRemoved);
        assertEquals(0, landingQueue.getAircraftInOrder().size());

    }

    @Test
    // amending returned list won't affect original queue
    public void getAircraftInOrderTest1() {
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        List<Aircraft> expected = new ArrayList<>(List.of(aircraft2, aircraft1));
        // amend the returned list
        landingQueue.getAircraftInOrder().add(aircraftlowFuel2);
        // need to check that list remains the same
        assertEquals(expected, landingQueue.getAircraftInOrder());
        assertEquals(2, landingQueue.getAircraftInOrder().size());
    }

    @Test
    // amending returned list won't affect original queue
    public void getAircraftInOrderTest2() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraftWithPassengers2);
        List<Aircraft> expected = new ArrayList<>(List.of(aircraft2, aircraftlowFuel2,
                aircraftWithPassengers, aircraftWithPassengers2, aircraft1));
        // amend the returned list
        landingQueue.getAircraftInOrder().add(aircraftlowFuel2);
        landingQueue.getAircraftInOrder().remove(aircraft1);
        landingQueue.getAircraftInOrder().add(aircraft2);
        // need to check that list remains the same
        assertEquals(expected, landingQueue.getAircraftInOrder());
        assertEquals(5, landingQueue.getAircraftInOrder().size());
    }

    @Test
    public void containsAircraftTest1() {
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraftlowFuel);
        assertEquals(true, landingQueue.containsAircraft(aircraft2));
        assertEquals(true, landingQueue.containsAircraft(aircraftlowFuel));
    }

    @Test
    public void containsAircraftTest2() {
        assertEquals(false, landingQueue.containsAircraft(aircraftWithPassengers));
    }

    @Test
    public void toStringTest1() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraftWithPassengers2);
        String expected = "LandingQueue [ABC002, ABC006, ABC004, ABC007, ABC001]";
        assertEquals(expected, landingQueue.toString());
    }

    @Test
    public void toStringTest2() {
        String expected = "LandingQueue []";
        assertEquals(expected, landingQueue.toString());

    }

    @Test
    public void encodeTest1() {
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraftWithPassengers);
        String expected = String.join(System.lineSeparator(), "LandingQueue:3",
                "ABC004,ABC001,ABC002");
        assertEquals(expected, landingQueue.encode());
    }

    @Test
    // check encode, empty queue
    public void encodeTest2() {
        assertEquals("LandingQueue:0", landingQueue.encode());
    }

    @Test
    public void encodeTest3() {
        landingQueue.addAircraft(aircraftlowFuel2);
        landingQueue.addAircraft(aircraftWithPassengers);
        aircraft2.declareEmergency();
        landingQueue.addAircraft(aircraft2);
        landingQueue.addAircraft(aircraft1);
        landingQueue.addAircraft(aircraftWithPassengers2);
        String expected = String.join(System.lineSeparator(), "LandingQueue:5",
                "ABC002,ABC006,ABC004,ABC007,ABC001");
        assertEquals(expected, landingQueue.encode());
    }
}