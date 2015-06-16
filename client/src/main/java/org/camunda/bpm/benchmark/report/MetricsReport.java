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
package org.camunda.bpm.benchmark.report;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.camunda.bpm.benchmark.report.MetricsResults.MetricsResultInstance;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;
import org.joda.time.DateTime;

/**
 * @author Thorben Lindhauer
 *
 */
public class MetricsReport {

  protected Date startDate;
  protected Date endDate;
  protected Set<String> reporterIds;
  protected ProcessEngine engine;

  protected int resultResolutionInSeconds = 5;

  public static final String[] DEFAULT_REPORTED_METRICS = new String[]{
    Metrics.JOB_ACQUIRED_SUCCESS,
    Metrics.JOB_ACQUIRED_FAILURE,
    Metrics.JOB_SUCCESSFUL,
    Metrics.JOB_FAILED,
    Metrics.JOB_LOCKED_EXCLUSIVE,
    Metrics.JOB_ACQUISITION_ATTEMPT
  };

  public MetricsReport(ProcessEngine engine, Date startDate, Date endDate, int resultResolutionInSeconds,
      Set<String> reporterIds) {
    this.engine = engine;
    this.startDate = startDate;
    this.endDate = endDate;
    this.reporterIds = reporterIds;
    this.resultResolutionInSeconds = resultResolutionInSeconds;
  }

  public void generate(MetricsWriter writer) {
    MetricsResults results = new MetricsResults();

    MetricsQuery metricsQuery = engine.getManagementService()
        .createMetricsQuery();


    for (String reporter : reporterIds) {
      metricsQuery.reporter(reporter);

      Date intervalStartDate = new DateTime(startDate).minus(resultResolutionInSeconds * 1000 * 3).toDate();
      while (intervalStartDate.getTime() < endDate.getTime()) {
        Date intervalEndDate = new Date(intervalStartDate.getTime()
            + (resultResolutionInSeconds * 1000));

        metricsQuery.startDate(intervalStartDate).endDate(intervalEndDate);

        for (String metric : getReportedMetrics()) {
          metricsQuery.name(metric);
          long sum = metricsQuery.sum();

          results.submitResult(reporter,
              new MetricsResultInstance(intervalStartDate, intervalEndDate, metric, "sum", sum));
        }

        intervalStartDate = intervalEndDate;
      }
    }

    addThroughputResults(engine, startDate, results);

    writer.write(results);
  }

  protected void addThroughputResults(ProcessEngine engine, Date benchmarkStartDate, MetricsResults results) {
    List<HistoricProcessInstance> historicProcessInstances = engine.getHistoryService()
        .createHistoricProcessInstanceQuery().list();

    long maxEndTime = 0L;

    double meanDuration = 0.0d;
    for (HistoricProcessInstance instance : historicProcessInstances) {
      meanDuration += (double) getActualDuration(instance, benchmarkStartDate) / (double) historicProcessInstances.size();

      if (instance.getEndTime().getTime() > maxEndTime) {
        maxEndTime = instance.getEndTime().getTime();
      }
    }

    results.submitAggregatedResult(
        new MetricsResultInstance(startDate, endDate, "process-instance-duration", "avg", meanDuration));

    double avgMeanDeviation = 0.0d;
    for (HistoricProcessInstance instance : historicProcessInstances) {
      avgMeanDeviation += Math.pow((double) getActualDuration(instance, benchmarkStartDate) - (double) meanDuration, 2.0d)
          / (double) historicProcessInstances.size();
    }

    double stdDevDuration = Math.sqrt(avgMeanDeviation);
    results.submitAggregatedResult(
        new MetricsResultInstance(startDate, endDate, "process-instance-duration", "stddev", stdDevDuration));

    results.submitAggregatedResult(new MetricsResultInstance(startDate, endDate, "process-instance-duration",
        "overall-duration", maxEndTime - benchmarkStartDate.getTime()));
  }

  public long getActualDuration(HistoricProcessInstance instance, Date benchmarkStartTime) {
    if (instance.getStartTime().getTime() < benchmarkStartTime.getTime()) {
      return instance.getEndTime().getTime() - benchmarkStartTime.getTime();
    }
    else {
      return instance.getDurationInMillis();
    }
  }

  protected String[] getReportedMetrics() {
    return DEFAULT_REPORTED_METRICS;
  }

}
