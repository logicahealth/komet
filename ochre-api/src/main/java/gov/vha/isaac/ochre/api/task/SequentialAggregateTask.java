/*
 * Copyright 2015 U.S. Department of Veterans Affairs.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.vha.isaac.ochre.api.task;

import gov.vha.isaac.ochre.api.Get;
import java.util.Collection;
import javafx.concurrent.Task;

/**
 *
 * @author kec
 */
public class SequentialAggregateTask<T> extends TimedTask<T> {

	Task<?>[] subTasks;
	int currentTask = 0;

	public SequentialAggregateTask(String title, Collection<Task<?>> subTasks) {
		this(title, subTasks.toArray(new Task<?>[subTasks.size()]));
	}

	/**
	 *
	 * @param title Title for the aggregate task
	 * @param subTasks
	 */
	public SequentialAggregateTask(String title, Task<?>[] subTasks) {
		this.subTasks = subTasks;
		this.updateTitle(title);
		this.setProgressMessageGenerator((task) -> {
			int taskId = currentTask;
			if (taskId < subTasks.length) {
				updateMessage("Executing subtask: " + subTasks[taskId].getTitle()
						  + " [" + Integer.toString(currentTask + 1) + " of " + subTasks.length + " tasks]");
				updateProgress((currentTask * 100) + Math.max(0, subTasks[taskId].getProgress() * 100),
						  subTasks.length * 100);
			}
		});
		this.setCompleteMessageGenerator((task) -> {
			updateMessage(getState() + " in " + getFormattedDuration());
			updateProgress(subTasks.length * 100, subTasks.length * 100);
		});
	}

	/**
	 *
	 * @return the sub tasks of this aggregate task.
	 */
	public Task<?>[] getSubTasks() {
		return subTasks;
	}

	/**
	 * Sequentially execute the subTasks using the WorkExecutor service, and
	 * return the value of the last task in the sequence.
	 *
	 * @return T value returned by call() method of the last task
	 * @throws Exception exception thrown by any subtask
	 */
	@Override
	protected T call() throws Exception {
		setStartTime();
		try {
			Object returnValue = null;
			for (; currentTask < subTasks.length; currentTask++) {
				Get.workExecutors().getExecutor().execute(subTasks[currentTask]);
				returnValue = subTasks[currentTask].get();
			}
			return (T) returnValue;
		} finally {
			Get.activeTasks().remove(this);
		}
	}

}
