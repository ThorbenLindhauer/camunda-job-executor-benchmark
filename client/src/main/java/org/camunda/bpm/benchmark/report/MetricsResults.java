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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class MetricsResults {

  protected Map<String, List<MetricsResultInstance>> resultsByReporter =
      new HashMap<String, List<MetricsResultInstance>>();
  protected List<MetricsResultInstance> aggregatedResults =
      new ArrayList<MetricsResults.MetricsResultInstance>();

  public Map<String, List<MetricsResultInstance>> getMetricsByReporter() {
    return resultsByReporter;
  }

  public void submitResult(String reporter, MetricsResultInstance result) {
    List<MetricsResultInstance> resultsForReporter = resultsByReporter.get(reporter);
    if (resultsForReporter == null) {
      resultsForReporter = new ArrayList<MetricsResultInstance>();
      resultsByReporter.put(reporter, resultsForReporter);
    }

    resultsForReporter.add(result);
  }

  public void submitAggregatedResult(MetricsResultInstance result) {
    aggregatedResults.add(result);
  }

  public List<MetricsResultInstance> getAggregatedResults() {
    return aggregatedResults;
  }

  public static class MetricsResultInstance {

    protected Date startDate;
    protected Date endDate;
    protected String metricName;
    protected String operator;
    protected Number value;

    public MetricsResultInstance(Date startDate, Date endDate, String metricName,
        String operator, Number value) {
      this.startDate = startDate;
      this.endDate = endDate;
      this.metricName = metricName;
      this.operator = operator;
      this.value = value;
    }

    public String getMetricName() {
      return metricName;
    }
    public void setMetricName(String metricName) {
      this.metricName = metricName;
    }
    public String getOperator() {
      return operator;
    }
    public void setOperator(String operator) {
      this.operator = operator;
    }
    public Number getValue() {
      return value;
    }
    public void setValue(Number value) {
      this.value = value;
    }
    public Date getStartDate() {
      return startDate;
    }
    public void setStartDate(Date startDate) {
      this.startDate = startDate;
    }
    public Date getEndDate() {
      return endDate;
    }
    public void setEndDate(Date endDate) {
      this.endDate = endDate;
    }
  }
}
