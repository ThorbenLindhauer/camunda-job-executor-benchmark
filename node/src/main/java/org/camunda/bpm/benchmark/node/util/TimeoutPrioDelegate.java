/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.camunda.bpm.benchmark.node.util;

import java.util.Date;
import java.util.List;
import java.util.Random;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandContextListener;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;

/**
 * @author Thorben Lindhauer
 *
 */
public class TimeoutPrioDelegate implements JavaDelegate {

  protected static final int SLEEP_MEAN = 100;
  // 95% of sampled values are within three standard deviations
  protected static final int SLEEP_STDDEV = 30;

  public void execute(DelegateExecution execution) throws Exception {
    final Random random = new Random();
    double sleepTime = (random.nextGaussian() * SLEEP_STDDEV) + SLEEP_MEAN;
    Thread.sleep(Math.max((long) sleepTime, 0L));

    // simulating chaning priorities
    final JobEntity currentJob = Context.getJobExecutorContext().getCurrentJob();
    Context.getCommandContext().registerCommandContextListener(new CommandContextListener() {

      public void onCommandFailed(CommandContext commandContext, Throwable t) {
      }

      public void onCommandContextClose(CommandContext commandContext) {
        List<JobEntity> newJobs = commandContext.getDbEntityManager().getCachedEntitiesByType(JobEntity.class);

        // before flushing, change the due dates such that some jobs are rather inserted at the front of the queue
        for (JobEntity newJob : newJobs) {
          long dueDateOffset = (long) (random.nextGaussian() * 1000);
          Date newDueDate = currentJob.getDuedate();
          if (newDueDate == null) {
            newDueDate = new Date();
          }

          newDueDate = new Date(newDueDate.getTime() + dueDateOffset);
          newJob.setDuedate(newDueDate);
        }
      }
    });
  }
}
