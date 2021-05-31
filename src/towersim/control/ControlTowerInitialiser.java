package towersim.control;

import towersim.aircraft.*;
import towersim.ground.AirplaneTerminal;
import towersim.ground.Gate;
import towersim.ground.HelicopterTerminal;
import towersim.ground.Terminal;
import towersim.tasks.Task;
import towersim.tasks.TaskList;
import towersim.tasks.TaskType;
import towersim.util.MalformedSaveException;
import towersim.util.NoSpaceException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;

import java.lang.reflect.Constructor;
import java.nio.Buffer;
import java.text.NumberFormat;
import java.util.*;

/**
 * Utility class that contains static methods for loading a control tower
 * and associated entities from file
 */
public class ControlTowerInitialiser {

    /**
     * Load number of ticks from given reader instance
     * @param reader - reader to load ticks
     * @return number of ticks
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static long loadTick(Reader reader) throws MalformedSaveException, IOException {
        try {
            BufferedReader br = new BufferedReader(reader);
            long tickElapsed;
            String content = br.readLine();
            try {
                tickElapsed = Long.parseLong(content);
                if (tickElapsed < 0) {
                    throw new MalformedSaveException("Number of ticks must not be negative");
                }
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("Not a valid tick format");
            }
            br.close();
            return tickElapsed;
        } catch (IOException ioe) {
            throw new IOException("Encountered a problem when reading/loading file");
        }
    }

    /**
     * Loads the list of all aircraft managed by the control tower from the given reader instance.
     * @param reader to load the aircraft
     * @return list of aircraft loaded
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static List<Aircraft> loadAircraft(Reader reader)
            throws IOException, MalformedSaveException {
        try {
            List<Aircraft> aircraftsLoaded = new ArrayList<>();
            BufferedReader aircraftReader = new BufferedReader(reader);
            /*
            First line should be an integer, telling how many aircraft are
            in the reader.
             */
            String firstLine = aircraftReader.readLine();
            int numOfAircraft;
            try {
                numOfAircraft = Integer.parseInt(firstLine);
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("Not a valid number of aircraft");
            }
            int i = 0;
            // loop through from second line onwards to read individual aircraft
            while (i < numOfAircraft) {
                String aircraftToDecode = aircraftReader.readLine();
                if (aircraftToDecode == null) {
                    // contains less aircraft in the reader than mentioned
                    throw new MalformedSaveException("Reader contains less aircraft"
                            + "than stated");
                }
                aircraftsLoaded.add(readAircraft(aircraftToDecode));
                i += 1;

            }
            /*
            Checks that the number of aircraft is not more than numOfAircraft stated
            in the first line.
             */

            if (!(aircraftReader.readLine() == null)) {
                throw new MalformedSaveException("Reader contains more aircraft than stated");
            }
            aircraftReader.close();
            return aircraftsLoaded;
        } catch (IOException ioe) {
            throw new IOException("Encountered a problem in reading files");
        }
    }

    /**
     * Loads takeoff queue, landing queue and map of loading aircraft from given reader instance
     * @param reader from which to load
     * @param aircraft list of all aircrafts, to validate callsigns
     * @param takeoffQueue - empty takeoff queue that aircraft will be added to
     * @param landingQueue - empty landing queue that aircraft will be added to
     * @param loadingAircraft - empty loading map that aircraft and loading times will be added to
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static void loadQueues(Reader reader, List<Aircraft> aircraft, TakeoffQueue takeoffQueue,
                                  LandingQueue landingQueue, Map<Aircraft, Integer> loadingAircraft)
            throws MalformedSaveException, IOException {
        try {
            BufferedReader bufferedReader = new BufferedReader(reader);
            readQueue(bufferedReader, aircraft, takeoffQueue);
            readQueue(bufferedReader, aircraft, landingQueue);
            readLoadingAircraft(bufferedReader, aircraft, loadingAircraft);
            bufferedReader.close();
        } catch (MalformedSaveException e) {
            throw new MalformedSaveException("The format of text read is invalid.");
        } catch (IOException ioe) {
            throw new IOException("Encountered a problem while loading the queues");
        }
    }

    /**
     * Loads list of terminals and their gates from reader instance
     * @param reader from which to load
     * @param aircraft list of aircraft, used to validate callsigns
     * @return list of terminals with their gates from the reader
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static List<Terminal> loadTerminalsWithGates(Reader reader,
                                                        List<Aircraft> aircraft)
            throws MalformedSaveException, IOException {
        try {
            BufferedReader terminalReader = new BufferedReader(reader);
            int numOfTerminals;
            List<Terminal> terminalsLoaded = new ArrayList<>();
            String firstLine = terminalReader.readLine();
            if (firstLine == "0") {
                return null;
            }
            // parse number of terminals
            try {
                numOfTerminals = Integer.parseInt(firstLine);
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("Not a valid number of terminal");
            }
            int i = 0;
            // loop through all the terminals in the reader and read the terminal and its gates
            while (i < numOfTerminals) {
                String decodedTerminal = terminalReader.readLine();
                Terminal terminalToLoad = readTerminal(decodedTerminal, terminalReader, aircraft);
                terminalsLoaded.add(terminalToLoad);
                i += 1;
            }
            /*
            Checks that after reading all the terminals stated,
            the last line should be null. Otherwise, it would indicate there are more
            terminals than stated.
             */
            if (!(terminalReader.readLine() == null)) {
                throw new MalformedSaveException("Reader contains more terminal than stated");
            }
            terminalReader.close();
            return terminalsLoaded;
        } catch (IOException ioe) {
            throw new IOException("Encountered a problem when reader the file");
        }
    }

    /**
     * Read queue from given reader instance
     * @param reader from which to read the queue
     * @param aircraft - list of all aircrafts, to validate the callsigns
     * @param queue - empty queue which aircraft will be added to
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static void readQueue(BufferedReader reader, List<Aircraft> aircraft,
                                 AircraftQueue queue) throws IOException, MalformedSaveException {
        try {
            int numOfAircrafts;
            List<String> validCallsigns = new ArrayList<>();
            // getting a list of valid callsigns
            for (Aircraft plane : aircraft) {
                validCallsigns.add(plane.getCallsign());
            }
            // if first line is null
            String firstLine = reader.readLine();
            if (firstLine == null) {
                throw new MalformedSaveException("First line should not be null");
            }
            // if first line is not null
            String[] firstLineContent = firstLine.split(":");
            //check if firstline is in valid format
            if ((firstLine.length() - firstLine.replaceAll(":", "").length()) != 1) {
                throw new MalformedSaveException("Contains more/less number of colons");
            }
            String queueType;
            queueType = firstLineContent[0];
            // checks if same queue type as passed in parameter
            if (!queueType.equals(queue.getClass().getSimpleName())) {
                throw new MalformedSaveException("not a correct queue type");
            }
            // parse number of aircrafts
            try {
                numOfAircrafts = Integer.parseInt(firstLineContent[1]);
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("not a valid number of aircrafts");
            }
            // numOfAircrafts must be positive
            if (numOfAircrafts < 0) {
                throw new MalformedSaveException("Number of aircrafts must not be negative");
            }
            // if numOfAircrafts > 0
            if (numOfAircrafts > 0) {
                String secondLine = reader.readLine();
                if ((numOfAircrafts > 0) && (secondLine.equals(null))) {
                    throw new MalformedSaveException("Second line is null when number of "
                            + "aircrafts is more than zero.");
                }
                // getting the list of aircraft (callsigns) in the queue
                String[] callsignsOfQueue = secondLine.split(",");
                /*
                Checks that number of aircraft stated in queue is the same
                as the number of callsigns listed
                 */
                if (callsignsOfQueue.length != numOfAircrafts) {
                    throw new MalformedSaveException("number of callsigns is not equal "
                            + "to numOfAircrafts");
                }
                for (String callsign : callsignsOfQueue) {
                    // if does not contain callsign in validCallsigns
                    if (!(validCallsigns.contains(callsign))) {
                        throw new MalformedSaveException("Not a valid callsign");
                    }
                    // if a valid callsign then add Aircraft to Queue
                    for (Aircraft plane : aircraft) {
                        if (plane.getCallsign().equals(callsign)) {
                            queue.addAircraft(plane);
                        }
                    }
                }
            }
        } catch (IOException ioe) {
            throw new IOException("encountered problem when reading queues");
        }
    }

    /**
     * Creates a control tower instance by reading various airport entities from given reader
     * @param tick  reader to load ticks
     * @param aircraft reader to load aircraft
     * @param queues reader to load queues
     * @param terminalsWithGates reader to load terminals
     * @return control tower created by reading the readers
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static ControlTower createControlTower(Reader tick, Reader aircraft, Reader queues,
                                                  Reader terminalsWithGates)
            throws MalformedSaveException, IOException {

        try {
            List<Aircraft> aircraftsLoaded;
            // load ticks and aircraft
            // ticks are made final to prevent method calls that might have
            // side effects on the original value (CheckStyle guide)
            final long ticksLoaded = loadTick(tick);
            aircraftsLoaded = loadAircraft(aircraft);
            // load terminals
            List<Terminal> terminalsLoaded;
            terminalsLoaded = loadTerminalsWithGates(terminalsWithGates, aircraftsLoaded);
            // load queues and map
            TakeoffQueue takeOffQueue = new TakeoffQueue();
            LandingQueue landingQueue = new LandingQueue();
            Map<Aircraft, Integer> loadingAircraft = new TreeMap<>(Comparator.comparing(
                    Aircraft::getCallsign));
            loadQueues(queues, aircraftsLoaded, takeOffQueue, landingQueue, loadingAircraft);
            // creates control tower from loaded entities
            ControlTower controlTower = new ControlTower(ticksLoaded, aircraftsLoaded, landingQueue,
                    takeOffQueue, loadingAircraft);
            // add terminals to control tower's jurisdiction
            for (Terminal terminalToAdd : terminalsLoaded) {
                controlTower.addTerminal(terminalToAdd);
            }
            return controlTower;
        } catch (IOException ioe) {
            throw new IOException("encountered problem with reading the files");
        }
    }


    /**
     * Reads an aircraft from its encoded representation in the given string
     * @param line - encoded aircraft representation
     * @return decoded Aircraft instance
     * @throws MalformedSaveException if format of given string is invalid
     */
    public static Aircraft readAircraft(String line) throws MalformedSaveException {
        String[] aircraftStringInfo = line.split(":");
        // the encoded representation should only contain 5 semicolons
        if ((line.length() - line.replaceAll(":", "").length()) != 5) {
            throw new MalformedSaveException("More/less colons expected");
        }
        // length of split should be 6
        if (aircraftStringInfo.length != 6) {
            throw new MalformedSaveException("Incorrect size of encoded rep");
        }
        String callsign = aircraftStringInfo[0];
        String aircraftCharacteristics = aircraftStringInfo[1];
        AircraftCharacteristics characteristics;
        //checks if valid aircraft characteristic
        try {
            characteristics = AircraftCharacteristics.valueOf(aircraftCharacteristics);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException("Not a valid aircraft characteristics");
        }
        // reads the task list
        TaskList taskList;
        try {
            taskList = readTaskList(aircraftStringInfo[2]);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException("not a valid task list");
        }
        // reads the fuel amount and cargo
        double fuelAmount;
        int freightOrPassenger;
        // parse fuel amount and passenger/freight onboard
        try {
            fuelAmount = Double.parseDouble(aircraftStringInfo[3]);
            freightOrPassenger = Integer.parseInt(aircraftStringInfo[5]);
            if ((fuelAmount < 0) || (fuelAmount > characteristics.fuelCapacity)
                    || (freightOrPassenger < 0)) {
                throw new MalformedSaveException("Not valid fuelAmount or "
                        + "freight/passenger num");
            }
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException("not valid fuel amount number");
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException("not a valid freight number");
        }
        // checks emergency state
        boolean hasEmergency;
        if (!(aircraftStringInfo[4].equals("false") || aircraftStringInfo[4]
                .equals("true"))) {
            throw new MalformedSaveException("not a valid emergency state");
        } else {
            hasEmergency = Boolean.parseBoolean(aircraftStringInfo[4]);
        }
        // Create an aircraft instance
        Aircraft plane;
        try {
            if (characteristics.passengerCapacity > 0) {
                plane = new PassengerAircraft(callsign, characteristics, taskList,
                        fuelAmount, freightOrPassenger);
            } else {
                plane = new FreightAircraft(callsign, characteristics, taskList,
                        fuelAmount, freightOrPassenger);
            }
            // declares state of emergency where relevant
            if (hasEmergency == true) {
                plane.declareEmergency();
            }
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException("Onboard cargo exceeds capacity");
        }
        return plane;
    }

    /**
     * Reads a task list from its encoded representation
     * @param taskListPart encoded representation of task list
     * @return decoded task list instance
     * @throws MalformedSaveException if format of given string is invalid
     */
    public static TaskList readTaskList(String taskListPart) throws MalformedSaveException {
        String[] taskStringList = taskListPart.split(",");
        List<Task> tasks = new ArrayList<>();
        /*
        Reads each task in the string and add them to the Array List.
         */
        for (String task : taskStringList) {
            // checks if LOAD task
            int loadPercent;
            if (task.startsWith("LOAD@")) {
                String[] loadTask = task.split("@");
                //checks if valid LOAD Task
                if (!(loadTask[0].equals("LOAD") || (loadTask.length != 2))) {
                    throw new MalformedSaveException();
                }
                // LOAD task should only contain one-at-symbol
                if ((task.length() - task.replace("@", "").length()) != 1) {
                    throw new MalformedSaveException("Load task contains more than one @");
                }
                // parse load percent.
                try {
                    loadPercent = Integer.parseInt(loadTask[1]);
                    if (loadPercent < 0) {
                        throw new MalformedSaveException();
                    }
                } catch (NumberFormatException nfe) {
                    throw new MalformedSaveException();
                }
                // adds to the tasks ArrayList
                tasks.add(new Task(TaskType.LOAD, loadPercent));
            } else if ((task.equals("AWAY")) || (task.equals("LAND"))
                    || (task.equals("WAIT")) || (task.equals("TAKEOFF"))) {
                tasks.add(new Task(TaskType.valueOf(task)));
            } else {
                // not a valid task type
                throw new MalformedSaveException("Not a valid task Type");
            }
        }
        // create new TaskList instance
        TaskList taskListObj;
        try {
            taskListObj = new TaskList(tasks);
        } catch (IllegalArgumentException iae) {
            throw new MalformedSaveException("Not a valid task list");
        }
        return taskListObj;
    }

    /**
     * Reads the map of currently loading aircraft from the given reader instance
     * @param reader to load map of loading aircraft
     * @param aircraft list of aircraft, used to validate callsigns
     * @param loadingAircraft empty map which aircraft and loading times will be added to
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static void readLoadingAircraft(BufferedReader reader, List<Aircraft> aircraft,
                                           Map<Aircraft, Integer> loadingAircraft) throws
            IOException, MalformedSaveException {
        try {
            int numLoadingAircraft;
            String firstLine = reader.readLine();
            // if first line is null
            if (firstLine == null) {
                throw new MalformedSaveException("First line should not be null");
            }
            String[] firstLineInfo = firstLine.split(":");
            // Ensures number of colon is 1
            if ((firstLine.length() - firstLine.replace(":", "").length()) != 1) {
                throw new MalformedSaveException();
            }
            // parse number of aircraft
            try {
                numLoadingAircraft = Integer.parseInt(firstLineInfo[1]);
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("Not a valid numLoadingAircraft");
            }
            // if numLoadingAircraft < 0
            if (numLoadingAircraft < 0) {
                throw new MalformedSaveException("Not a valid positive numLoadingAircraft");
            }
            if (numLoadingAircraft > 0) {
                String secondLine = reader.readLine();
                /*
                If number of loading aircraft is more than zero, the second line
                must not be null
                 */
                if (secondLine == null) {
                    throw new MalformedSaveException("Line is null when the number of "
                            + "loading aircrafts > 0");
                }
                String[] callsignWithTicks = secondLine.split(",");
                /*
                    Check that number of aircraft on the first line is equal to number
                    of callsigns on the second line
                     */
                if (callsignWithTicks.length != numLoadingAircraft) {
                    throw new MalformedSaveException();
                }
                int i = 0;
                while (i < numLoadingAircraft) {
                    // check number of colon for the pair is equal to one
                    if ((callsignWithTicks[i].length() - callsignWithTicks[i].replaceAll(
                            ":", "").length()) != 1) {
                        throw new MalformedSaveException("Number of colon is more/less than 1");
                    }
                    String[] aircraftInfo = callsignWithTicks[i].split(":");
                    String callsign = aircraftInfo[0];
                    int ticksRemaining;
                    //parse ticks remaining
                    try {
                        ticksRemaining = Integer.parseInt(aircraftInfo[1]);
                    } catch (NumberFormatException nfe) {
                        throw new MalformedSaveException("Not a valid tick number.");
                    }
                    // ticks must not be less than one
                    if (ticksRemaining < 1) {
                        throw new MalformedSaveException("Ticks should be 1 or more "
                                + "for loading aircraft");
                    }
                    Aircraft aircraftRead = null;
                    /*
                    Loops through the given aircraft list in the parameter and
                    checks to see which aircraft object has the same callsign as
                    the aircraft being read in this method. If there is no aircraft
                    with matching callsign, then throw error
                     */
                    for (Aircraft plane : aircraft) {
                        if (plane.getCallsign().equals(callsign)) {
                            aircraftRead = plane;
                            break;
                        }
                    }
                    // if no aircraft with matching callsign
                    if (aircraftRead == null) {
                        throw new MalformedSaveException("Callsign could not be "
                                + "found from aircraft list.");
                    }
                    // modifies the given loading map
                    loadingAircraft.put(aircraftRead, ticksRemaining);
                    i += 1;
                }
            }
        } catch (IOException ioe) {
            throw new IOException("Encountered a problem while attempting to read file.");
        }
    }

    /**
     * Reads a terminal from the given string and reads its gates from a reader instance
     * @param line encoded terminal
     * @param reader to load the gates of terminal
     * @param aircraft list of all aircraft, used to validate callsigns
     * @return decoded Terminal with its gate added
     * @throws MalformedSaveException if format of text read is invalid
     * @throws IOException if encountered an error when reading from reader
     */
    public static Terminal readTerminal(String line, BufferedReader reader,
                                        List<Aircraft> aircraft)
            throws IOException, MalformedSaveException {
        try {
            int terminalNumber;
            int numOfGates;
            Terminal terminalRead = null;
            // the number of colons must be equal to 3
            if ((line.length() - line.replaceAll(":", "").length()) != 3) {
                throw new MalformedSaveException("Incorrect number of colons");
            }
            String[] encodedTerminal = line.split(":");
            // must have size = 4 (type, number, emergency, numGates)
            if ((encodedTerminal.length != 4)) {
                throw new MalformedSaveException();
            }
            // first index must be AirplaneTerminal nor HelicopterTerminal
            if (!(encodedTerminal[0].equals("AirplaneTerminal")
                    || encodedTerminal[0].equals("HelicopterTerminal"))) {
                throw new MalformedSaveException("Not a valid terminal type");
            }
            // parse terminal number (index 1) and number of gates (index 3) of first line
            try {
                terminalNumber = Integer.parseInt(encodedTerminal[1]);
                numOfGates = Integer.parseInt(encodedTerminal[3]);
                /*
                Terminal number should not be less than one and number of gates
                must not be zero or more than maximum allowed at terminal
                 */
                if ((terminalNumber < 1) || (numOfGates < 0)
                        || (numOfGates > Terminal.MAX_NUM_GATES)) {
                    throw new MalformedSaveException();
                }
            } catch (NumberFormatException nfe) {
                throw new MalformedSaveException("Not a valid integer");
            }
            // third index must be true or false as it represents emergency state
            if (!(encodedTerminal[2].equals("true") || encodedTerminal[2].equals("false"))) {
                throw new MalformedSaveException();
            }
            // parse boolean for hasEmergency
            boolean hasEmergency = Boolean.parseBoolean(encodedTerminal[2]);

            // create the terminal
            if (encodedTerminal[0].equals("AirplaneTerminal")) {
                terminalRead = new AirplaneTerminal(terminalNumber);
            } else if (encodedTerminal[0].equals("HelicopterTerminal")) {
                terminalRead = new HelicopterTerminal(terminalNumber);
            }
            // declare a state of emergency at the terminal if relevant
            if (hasEmergency == true) {
                terminalRead.declareEmergency();
            }
            // add the gates to the terminal
            int i = 0;
            while (i < numOfGates) {
                Gate gateRead = readGate(reader.readLine(), aircraft);
                terminalRead.addGate(gateRead);
                i += 1;
            }
            return terminalRead;
        } catch (IOException | NoSpaceException e) {
            throw new IOException();
        }
    }

    /**
     * Reads a gate from encoded string
     * @param line encoded gate
     * @param aircraft list of all aircraft, used to validate callsigns
     * @return decoded gate
     * @throws MalformedSaveException if format of text read is invalid
     */
    public static Gate readGate(String line, List<Aircraft> aircraft)
            throws MalformedSaveException {
        try {
            // Number of colons should be equal to 1
            if ((line.length() - line.replaceAll(":", "").length()) != 1) {
                throw new MalformedSaveException("Incorrect number of colons");
            }
            //split line
            String[] encodedLine = line.split(":");
            if (encodedLine.length != 2) {
                throw new MalformedSaveException();
            }
            // parse gate number
            int gateNum = Integer.parseInt(encodedLine[0]);
            if (gateNum < 1) {
                throw new MalformedSaveException("Gate number should be more than 1");
            }
            Gate gate = new Gate(gateNum);

            /*
            Reads the aircraft parked at the encoded gate.
            If empty, does nothing. If occupied, then park the aircraft at gate.
             */
            if (!encodedLine[1].equals("empty")) {
                /*
                Checks if callsign of parked at the encoded gate is a valid callsign
                by finding matching aircraft in the given list.
                 */
                List<String> callsignList = new ArrayList<>();
                for (Aircraft plane : aircraft) {
                    callsignList.add(plane.getCallsign());
                    if (plane.getCallsign().equals(encodedLine[1])) {
                        try {
                            gate.parkAircraft(plane);
                        } catch (NoSpaceException e) {
                            throw new MalformedSaveException("It is throwing a NoSpaceException");
                        }
                    }
                }
                // if no aircraft with matching callsign found
                if (!callsignList.contains(encodedLine[1])) {
                    throw new MalformedSaveException();
                }
            }
            // return decoded gate instance;
            return gate;
        } catch (NumberFormatException nfe) {
            throw new MalformedSaveException();
        }
    }
}

