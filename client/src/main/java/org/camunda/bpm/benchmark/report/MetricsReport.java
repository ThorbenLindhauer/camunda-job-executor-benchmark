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
import java.util.Set;

import org.camunda.bpm.benchmark.report.MetricsResults.MetricsResultInstance;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.management.Metrics;
import org.camunda.bpm.engine.management.MetricsQuery;

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
    Metrics.JOB_SUCCESSFUL
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

      Date intervalStartDate = startDate;
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

    writer.write(results);
  }

  protected String[] getReportedMetrics() {
    return DEFAULT_REPORTED_METRICS;
  }

}
