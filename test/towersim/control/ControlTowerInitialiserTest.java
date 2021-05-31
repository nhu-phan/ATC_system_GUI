package towersim.control;

import org.junit.Before;
import org.junit.Test;
import towersim.aircraft.Aircraft;
import towersim.aircraft.AircraftCharacteristics;
import towersim.aircraft.FreightAircraft;
import towersim.aircraft.PassengerAircraft;
import towersim.ground.Gate;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import static org.junit.Assert.*;

import java.io.*;
import java.util.*;

public class ControlTowerInitialiserTest {
    private TaskList taskList1;
    private TaskList taskList2;
    private ControlTower tower1;
    private ControlTower tower2;
    private Aircraft aircraft1;
    private Aircraft aircraft2;
    private Aircraft aircraft3;
    private Aircraft aircraft4;
    private Aircraft aircraft5;

    private List<Aircraft> aircraftList1;

    private List<Aircraft> aircraftList2;
    private LandingQueue landingQueue1;
    private LandingQueue landingQueue2;
    private TakeoffQueue takeoffQueue1;
    private TakeoffQueue takeoffQueue2;
    private Map<Aircraft, Integer> loadingAircraft1;
    private Map<Aircraft, Integer> loadingAircraft2;

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


        this.aircraft1 = new FreightAircraft("ABC001", AircraftCharacteristics.SIKORSKY_SKYCRANE,
                taskList1,
                AircraftCharacteristics.SIKORSKY_SKYCRANE.fuelCapacity * 0.5,
                457);

        this.aircraft2 = new FreightAircraft("ABC002", AircraftCharacteristics.BOEING_747_8F,
                taskList2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.6,
                60000);

        this.aircraft3 = new FreightAircraft("ABC003", AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.1,
                60000);

        this.aircraft4 = new FreightAircraft("ABC006", AircraftCharacteristics.BOEING_747_8F,
                taskList2,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.05,
                60000);

        this.aircraft5 = new PassengerAircraft("ABC004", AircraftCharacteristics.AIRBUS_A320,
                taskList1,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity * 0.6,
                12);
        aircraftList1 = new ArrayList<>();
        this.aircraftList1.add(aircraft1);
        this.aircraftList1.add(aircraft2);
        this.aircraftList1.add(aircraft3);
        aircraftList2 = new ArrayList<>();
        this.aircraftList2.add(aircraft3);
        this.aircraftList2.add(aircraft4);
        this.aircraftList2.add(aircraft5);

        landingQueue1 = new LandingQueue();
        landingQueue2 = new LandingQueue();

        takeoffQueue1 = new TakeoffQueue();
        takeoffQueue2 = new TakeoffQueue();
        loadingAircraft1 = new HashMap<>();
        loadingAircraft1.put(aircraft1, 1);
        loadingAircraft1.put(aircraft2, 2);

        /* ControlTower(long ticksElapsed, List<Aircraft> aircraft,
        LandingQueue landingQueue, TakeoffQueue takeoffQueue, Map<Aircraft,Integer> loadingAircraft)
         */
        tower1 = new ControlTower(2, aircraftList1, landingQueue1, takeoffQueue1, loadingAircraft1);
        tower2 = new ControlTower(4, aircraftList2, landingQueue2, takeoffQueue2, loadingAircraft2);
    }


    /**
     * readTaskList TEST
     * @throws IOException
     * @throws MalformedSaveException
     */

    @Test
    // read task list
    public void readTaskListTest_VALID() throws IOException, MalformedSaveException {
        String taskList = "LOAD@0,TAKEOFF,AWAY,LAND";
        try {
            TaskList tasks = ControlTowerInitialiser.readTaskList(taskList);
            assertEquals(tasks.encode(), taskList);
        }
        catch (MalformedSaveException mse) {
            fail();
        }
    }


    @Test
    //valid TaskList
    public void readTaskListTest1_VALID() {
        String encodedTaskList = "LOAD@30,TAKEOFF,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            assertEquals(taskList2.toString(), taskListRead.toString());
        } catch (MalformedSaveException e) {
            fail("should not throw an exception for a valid task list");
        }
    }

    @Test
    //valid TaskList
    public void readTaskListTest2_VALID() {
        String encodedTaskList = "LOAD@30,TAKEOFF,AWAY,AWAY,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            assertEquals(encodedTaskList, taskListRead.encode());
        } catch (MalformedSaveException e) {
            fail("should not throw an exception for a valid task list");
        }
    }

    @Test
    //TaskType is not valid
    public void readTaskListTest1_TYPEINVALID() {
        String encodedTaskList = "LOAD@30,TAKOFF,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as not a valid TaskType");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //TaskType is not valid
    public void readTaskListTest2_TYPEINVALID() {
        String encodedTaskList = "LOAD@30,TAKOFF,NOTATASK,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as not a valid TaskType");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect order
    public void readTaskListTest3_INVALID() {
        String encodedTaskList = "LOAD@30,TAKEOFF,LAND,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as not correct ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect order
    public void readTaskListTest4_INVALID() {
        String encodedTaskList = "TAKEOFF,TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as not correct ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect order
    public void readTaskListTest5_INVALID() {
        String encodedTaskList = "LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as not correct ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //invalid order
    public void readTaskListTest6() {
        String encodedTaskList = "LOAD@0,TAKEOFF,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should throw an exception as not valid ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //invalid order
    public void readTaskListTest7() {
        String encodedTaskList = "LOAD@0,TAKEOFF,AWAY,WAIT";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should throw an exception as not valid ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }
    @Test
    //invalid order
    public void readTaskListTest8() {
        String encodedTaskList = "LOAD@0,TAKEOFF,AWAY,LAND,WAIT,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should throw an exception as not valid ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }
    @Test
    //invalid order
    public void readTaskListTest9() {
        String encodedTaskList = "LOAD@0,TAKEOFF,AWAY,LAND,TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should throw an exception as not valid ordering");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect format
    public void readTaskListTest10_INCORRECTFORMAT() {
        String encodedTaskList = "LOAD@0:TAKEOFF:AWAY,LAND:TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("incorrect format");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect format
    public void readTaskListTest11_INCORRECTFORMAT() {
        String encodedTaskList = "LOAD@0:TAKEOFF@:AWAY,LAND:TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("incorrect format");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // incorrect format
    public void readTaskListTest12_INCORRECTFORMAT() {
        String encodedTaskList = "LOAD@0@:TAKEOFF:AWAY,LAND:TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("incorrect format");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }
    @Test
    // incorrect format
    public void readTaskListTest13_INCORRECTFORMAT() {
        String encodedTaskList = "LOAD0:TAKEOFF:AWAY,LAND:TAKEOFF";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("incorrect format");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //more than one @
    public void readTaskListTest5_INCORRECTFORMAT() {
        String encodedTaskList = "LOAD@@3,TAKEOFF,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should throw an exception as more than one-at-symbol");
        } catch (MalformedSaveException e) {
            assert (true);
        }
    }

    @Test
    //loadPercent is not an integer
    public void readTaskListTest3() {
        String encodedTaskList = "LOAD@h,TAKOFF,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as load percentage is not an integer");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //loadPercent is less than 0
    public void readTaskListTest4() {
        String encodedTaskList = "LOAD@-3,TAKEOFF,AWAY,LAND";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("should  throw an exception as load percentage is negative");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    //empty task list
    public void readTaskListTest4_EMPTYLIST() {
        String encodedTaskList = "";
        try {
            TaskList taskListRead = ControlTowerInitialiser.readTaskList(encodedTaskList);
            fail("Should not be an empty list");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }




    /**
     * readAircraft TEST
     * @throws IOException
     */

    @Test
    // valid string
    public void readAircraftTest_VALIDFREIGHT() throws IOException {

        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664.00:false:457";
        try {

            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            assertTrue(aircraftLoaded instanceof FreightAircraft);
            assertTrue(aircraftLoaded.equals(aircraft1));
            assertTrue(!aircraftLoaded.equals(aircraft3));
            assertEquals(aircraft1.hashCode(), aircraftLoaded.hashCode());
        }
        catch (MalformedSaveException mse) {
            fail();
        }
    }

    @Test
    public void readAircraftTest_MISSINGTASKS() throws IOException {

        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:1664.00:false:457";
        try {

            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    // valid string
    public void readAircraftTest_MISSINGCALLSIGN() throws IOException {

        String aircraft1Encoded = "SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664.00:false:457";
        try {

            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    public void readAircraftTest_TWODECPLACES() throws IOException {
        /*
           this.aircraft3 = new FreightAircraft("ABC003", AircraftCharacteristics.BOEING_747_8F,
                taskList1,
                AircraftCharacteristics.BOEING_747_8F.fuelCapacity * 0.1,
                60000);
         */
        String encoded = "ABC003:BOEING_747_8F:LOAD@0,TAKEOFF,AWAY,LAND:22611.7:false:60000";
        String expected ="ABC003:BOEING_747_8F:LOAD@0,TAKEOFF,AWAY,LAND:22611.70:false:60000";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(encoded);
            assertEquals(expected, aircraftLoaded.encode());
        }
        catch (MalformedSaveException e) {
            fail();
        }

    }

    @Test
    public void readAircraftTest_VALIDPASSENGER() throws IOException {
        aircraft5.declareEmergency();
        String aircraft1Encoded = "ABC004:AIRBUS_A320:LOAD@0,TAKEOFF,AWAY,LAND:16320.00:true:12";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            assertTrue(aircraftLoaded.equals(aircraft5));
            assertTrue(!aircraftLoaded.equals(aircraft2));
            assertTrue(aircraftLoaded instanceof PassengerAircraft);
            assertTrue(!(aircraftLoaded instanceof FreightAircraft));
            assertEquals(aircraft5.hashCode(), aircraftLoaded.hashCode());
        }
        catch (MalformedSaveException mse) {
            fail();
        }
    }


    @Test
    public void  readAircraftTest_INVALIDCHARACTERISTICS() {
        String aircraft1Encoded = "ABC001:NOT_A_VALID:LOAD@0,TAKEOFF,AWAY,LAND:1664.00:false:457";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    public void  readAircraftTest_INCORRECTCOLON() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664.00:457:";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // fuel amount is not a double but a char
    public void  readAircraftTest_CHARFUEL() {
        String aircraft1Encoded = "ABC001:NOT_A_VALID:LOAD@0,TAKEOFF,AWAY,LAND:f:false:457";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // fuel amount is not a double but an integer
    public void  readAircraftTest_INTEGERFUEL() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664:false:457";
        try {
            // could be one it passengeraircraft one is freight
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            assertTrue(aircraftLoaded.equals(aircraft1));
            assertTrue(!aircraftLoaded.equals(aircraft3));
            assertEquals(aircraft1.hashCode(), aircraftLoaded.hashCode());
        }
        catch (MalformedSaveException mse) {
            fail();
        }
    }

   @Test
   // freight amount is not integer but a char
   public void  readAircraftTest_CHARFREIGHT() {
       String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664:false:f";
       try {
           Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
           fail();
       }
       catch (MalformedSaveException mse) {
           assert(true);
       }
   }

   @Test
   // cargo amount is not integer but a double
   public void  readAircraftTest_DOUBLECARGO() {
       String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664:false:457.0";
       try {
           Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
           fail();
       }
       catch (MalformedSaveException mse) {
           assert(true);
       }
   }

    @Test
    // cargo amount is not integer but a double
    public void  readAircraftTest_NOFUEL() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND::false:457";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    // cargo amount is not integer but a double
    public void  readAircraftTest_MISSINGEMERGENCY2() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664::457";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    // cargo amount is less than 0
    public void  readAircraftTest_NEGATIVECARGO() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664:false:-6";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    // cargo amount is more than capacity
    public void  readAircraftTest_EXCEEDSCAPACITY() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:1664:false:1000000";
        try {
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        }
        catch (MalformedSaveException mse) {
            assert(true);
        }
    }

    @Test
    // fuel amount is less than 0
    public void  readAircraftTestFive_NEGFUEL() {
        String aircraft1Encoded = "ABC001:SIKORSKY_SKYCRANE:LOAD@0,TAKEOFF,AWAY,LAND:-1664:false:457";
        try {
            // could be one it passengeraircraft one is freight
            Aircraft aircraftLoaded = ControlTowerInitialiser.readAircraft(aircraft1Encoded);
            fail();
        } catch (MalformedSaveException e) {
            assert (true);
        }
    }

    @Test
    // read Aircraft - valid
    public void readAircraftTest_VALID() {
        Aircraft expected = new PassengerAircraft("EXP111",AircraftCharacteristics.AIRBUS_A320,
                taskList2,
                AircraftCharacteristics.AIRBUS_A320.fuelCapacity * 0.6,
                12);
        String encodedAircraft = "EXP111:AIRBUS_A320:LOAD@30,TAKEOFF,AWAY,LAND:25560.00:false:12";
        try {
            Aircraft aircraftRead = ControlTowerInitialiser.readAircraft(encodedAircraft);
            assertEquals(expected.hashCode(), aircraftRead.hashCode());
            assertTrue(expected.equals(aircraftRead));
        } catch (MalformedSaveException e) {
            fail("Should not throw exception for valid aircraft.");
        }
    }

    @Test
    public void readAircraftTest_NEGFUEL2() {
        String encodedAircraft = "EXP111:AIRBUS_A320:LOAD@30,TAKEOFF,AWAY,LAND:-25560.00:false:12";
        try {
            Aircraft aircraftRead = ControlTowerInitialiser.readAircraft(encodedAircraft);
            fail("Should throw an exception as fuel amount is negative");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    public void readAircraftTest_LONG() {
        String encodedAircraft = "EXP111:AIRBUS_A3320:LOAD@30,TAKEOFF,AWAY,LAND:3325560.00:false:12:HELLO";
        try {
            Aircraft aircraftRead = ControlTowerInitialiser.readAircraft(encodedAircraft);
            fail("Should throw an exception as more colons detected");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    public void readAircraftTest_MISSINGCARGO() {
        String encodedAircraft = "EXP111:AIRBUS_A3320:LOAD@30,TAKEOFF,AWAY,LAND:3325560.00:false:";
        try {
            Aircraft aircraftRead = ControlTowerInitialiser.readAircraft(encodedAircraft);
            fail("Should throw an exception as less colons detected");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // cargo is not integer
    public void readAircraftTest_CARGONOTVALID() {
        String encodedAircraft = "EXP111:AIRBUS_A3320:LOAD@30,TAKEOFF,AWAY,LAND:3325560.00:false:cargo";
        try {
            Aircraft aircraftRead = ControlTowerInitialiser.readAircraft(encodedAircraft);
            fail("Should throw an exception as cargo is not a valid integer");
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }


    /**
     * readLoadingAircraft
     * @throws IOException
     */

    @Test
    public void readLoadingAircraft_VALIDLOADING() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1,ABC002:1,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        Map<Aircraft,Integer> expected = new HashMap<>();
        expected.put(aircraft1, 1);
        expected.put(aircraft2, 1);
        expected.put(aircraft3, 1);
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            assertEquals(expected.toString(), actual.toString());
        } catch (MalformedSaveException e) {
            fail();
        }
    }

    @Test
    //first line is null
    public void readLoadingAircraft_FIRSTLINENULL() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "",
                "ABC001:1,ABC002:1,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // more : detected
    public void readLoadingAircraft_MORECOLONSEXPECTED() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3:",
                "ABC001:1,ABC002:1,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test

    public void readLoadingAircraft_NUMAIRCRAFT_NOTINT() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:Int",
                "ABC001:1,ABC002:1,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // num of aircraft is > 0 but second line is null
    public void readLoadingAircraft_SECONDLINENULL() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // callsign/loading pair not valid
    public void readLoadingAircraft_INVALIDPAIR() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1:2,ABC002:1,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // callsign listed does not correspond to any aircraft listed
    public void readLoadingAircraft_NOMATCHINGCALLSIGNS() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1,ABC004:1,ABCAA3:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // ticks remaining not an integer
    public void readLoadingAircraft_NOTINTEGERTICK() throws IOException {
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1,ABC002:Int,ABC003:1");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // ticks remaining not an integer
    public void readLoadingAircraft_MORECALLSIGNTHANEXPECTED() throws IOException {
        aircraftList1.add(aircraft5);
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1,ABC002:3,ABC003:1,ABC:004");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }

    @Test
    // ticks remaining not an integer
    public void readLoadingAircraft_LESSCALLSIGNTHANEXPECTED() throws IOException {
        aircraftList1.add(aircraft5);
        String fileContents = String.join(System.lineSeparator(), "LoadingAircraft:3",
                "ABC001:1,ABC002:3");
        Map<Aircraft,Integer> actual = new HashMap<>();
        BufferedReader br = new BufferedReader(new StringReader(fileContents));
        try {
            ControlTowerInitialiser.readLoadingAircraft(br, aircraftList1, actual);
            fail();
        } catch (MalformedSaveException e) {
            assert(true);
        }
    }
}

    
    
    
