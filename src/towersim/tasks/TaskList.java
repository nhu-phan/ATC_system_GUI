package towersim.tasks;

import java.util.List;
import java.util.StringJoiner;

/**
 * Represents a circular list of tasks for an aircraft to cycle through.
 * @ass1
 */
public class TaskList {
    /** List of tasks to cycle through. */
    private final List<Task> tasks;

    /** Index of current task in tasks list. */
    private int currentTaskIndex;

    /**
     * Variable to help with checking valid constructors. It counts
     * number of times the task list has been check. The recursive call
     * of the helper methods will be halted once this variable reaches the
     * size of the task list
     */
    private int tasksCheckedNum = 0;

    /**
     * Creates a new TaskList with the given list of tasks.
     * <p>
     * Initially, the current task (as returned by {@link #getCurrentTask()}) should be the first
     * task in the given list.
     *
     * @param tasks list of tasks
     * @ass1
     */
    public TaskList(List<Task> tasks) {
        this.tasks = tasks;
        this.currentTaskIndex = 0;
        if (tasks.size() == 0) {
            throw new IllegalArgumentException(); // an empty list is invalid
        } else if (tasks.size() == 1) {
            if (!(tasks.get(0).getType().equals(TaskType.AWAY)
                    || tasks.get(0).getType().equals(TaskType.WAIT))) {
                throw new IllegalArgumentException("If size of TaskList is 1, then task "
                        + "must be WAIT or AWAY");
            }
        } else if (tasks.size() > 1) {
            boolean isValidTaskList = validTaskListHelper(tasks, 0);
            if (isValidTaskList == false) {
                throw new IllegalArgumentException("Not a valid task list");
            }
        }
    }

    private boolean validTaskListHelper(List<Task> tasksToCheck, int index) {
        /*
        Once tasksCheckedNum equals to size of tasksToCheck, then we have
        checked all the tasks in the list and they are all valid.
         */
        if (tasksCheckedNum == tasksToCheck.size()) {
            return true;
        } else {
            TaskType currentTask = tasksToCheck.get(index).getType();
            TaskType nextTask = tasksToCheck.get((index + 1) % tasksToCheck.size()).getType();

            //AWAY --> AWAY OR LAND
            if (currentTask == TaskType.AWAY) {
                if (!((nextTask == TaskType.AWAY) || (nextTask == TaskType.LAND))) {
                    return false;
                }
            } else if (currentTask == TaskType.LAND) {
                // LAND --> WAIT, LOAD
                if (!((nextTask == TaskType.WAIT) || (nextTask == TaskType.LOAD))) {
                    return false;
                }
            } else if (currentTask == TaskType.WAIT) {
                // WAIT --> WAIT OR LOAD
                if (!((nextTask == TaskType.WAIT) || (nextTask == TaskType.LOAD))) {
                    return false;
                }
            } else if (currentTask == TaskType.LOAD) {
                // LOAD --> T/O
                if (nextTask != TaskType.TAKEOFF) {
                    return false;
                }
            } else if (currentTask == TaskType.TAKEOFF) {
                // T/O --> AWAY
                if (nextTask != TaskType.AWAY) {
                    return false;
                }
            }
            tasksCheckedNum++;
            return validTaskListHelper(tasksToCheck, ++index);
        }

    }



    /**
     * Returns the current task in the list.
     *
     * @return current task
     * @ass1
     */
    public Task getCurrentTask() {
        return this.tasks.get(this.currentTaskIndex);
    }

    /**
     * Returns the task in the list that comes after the current task.
     * <p>
     * After calling this method, the current task should still be the same as it was before calling
     * the method.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * this method should return the first element of the list.
     *
     * @return next task
     * @ass1
     */
    public Task getNextTask() {
        int nextTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
        return this.tasks.get(nextTaskIndex);
    }

    /**
     * Moves the reference to the current task forward by one in the circular task list.
     * <p>
     * After calling this method, the current task should be the next task in the circular list
     * after the "old" current task.
     * <p>
     * Note that the list is treated as circular, so if the current task is the last in the list,
     * the new current task should be the first element of the list.
     * @ass1
     */
    public void moveToNextTask() {
        this.currentTaskIndex = (this.currentTaskIndex + 1) % this.tasks.size();
    }

    /**
     * Returns the human-readable string representation of this task list.
     * <p>
     * The format of the string to return is
     * <pre>TaskList currently on currentTask [taskNum/totalNumTasks]</pre>
     * where {@code currentTask} is the {@code toString()} representation of the current task as
     * returned by {@link Task#toString()},
     * {@code taskNum} is the place the current task occurs in the task list, and
     * {@code totalNumTasks} is the number of tasks in the task list.
     * <p>
     * For example, a task list with the list of tasks {@code [AWAY, LAND, WAIT, LOAD, TAKEOFF]}
     * which is currently on the {@code WAIT} task would have a string representation of
     * {@code "TaskList currently on WAIT [3/5]"}.
     *
     * @return string representation of this task list
     * @ass1
     */
    @Override
    public String toString() {
        return String.format("TaskList currently on %s [%d/%d]",
                this.getCurrentTask(),
                this.currentTaskIndex + 1,
                this.tasks.size());
    }

    /**
     * Return a machine-readable representation of the task list
     * @return machine-readable representation of the task list
     */
    public String encode() {
        int i = currentTaskIndex;
        StringJoiner encodedList = new StringJoiner(",");
        while (i < this.tasks.size() + currentTaskIndex) {
            encodedList.add(this.tasks.get(i % this.tasks.size()).encode());
            i++;
        }
        return String.valueOf(encodedList);
    }
}
