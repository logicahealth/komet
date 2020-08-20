package sh.isaac.provider.qa;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sh.isaac.api.Get;
import sh.isaac.api.chronicle.Version;
import sh.isaac.api.coordinate.StampFilterImmutable;
import sh.isaac.api.qa.QAResults;
import sh.isaac.api.task.TimedTaskWithProgressTracker;

/**
 * Allow creation of QATasks that both check a single version, and run against the entire datastore.
 * Subject to more refactoring later, to allow multiple QA tasks to be more efficiently chained during a single iteration of 
 * datastore content
 * 
 * Automatically handles storing the QA runs / results in the QARunStorage
 * @author darmbrust
 */
public abstract class QATask extends TimedTaskWithProgressTracker<QAResults>
{
	private static Logger log = LogManager.getLogger();
	
	{
		QARunStorage qrs = Get.service(QARunStorage.class);
		qrs.qaQueued(this.getTaskId(), getFilter());
		super.stateProperty().addListener(change -> 
		{
			switch (super.getState())
			{
				case CANCELLED:
					log.info("QA task {} canceled", this.getTaskId());
					qrs.storeFailure(this.getTaskId(), "cancelled");
					break;
				case FAILED:
					log.info("QA task {} failed", this.getTaskId());
					qrs.storeFailure(this.getTaskId(), this.getException().toString());
					break;
				case RUNNING:
					log.info("QA task {} started", this.getTaskId());
					qrs.qaStarted(this.getTaskId());
					break;
				case SCHEDULED:
				case READY:
					//Don't care
					break;
				case SUCCEEDED:
					try
					{
						log.info("QA task {} completed", this.getTaskId());
						qrs.storeResult(this.getTaskId(), this.get());
					}
					catch (Exception e)
					{
						//Shouldn't happen, or already handeled by failed
					}
					break;
				default :
					throw new RuntimeException("oops");
			}
		});
	}
	
	/**
	 * Run this QATask only against the specified supplied version.  No task is executed.  Happens on the calling thread
	 * @param v
	 * @return the QA result
	 */
	public abstract QAResults checkVersion(Version v);
	
	protected abstract StampFilterImmutable getFilter();
}
