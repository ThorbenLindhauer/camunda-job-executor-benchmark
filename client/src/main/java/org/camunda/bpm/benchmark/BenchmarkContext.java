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
package org.camunda.bpm.benchmark;

import java.util.Date;

import org.camunda.bpm.engine.ProcessEngine;

/**
 * @author Thorben Lindhauer
 *
 */
public class BenchmarkContext {

  protected ClusterManager clusterManager;
  protected ProcessEngine processEngine;

  protected Date currentScenarioStart;

  public ClusterManager getClusterManager() {
    return clusterManager;
  }

  public void setClusterManager(ClusterManager clusterManager) {
    this.clusterManager = clusterManager;
  }

  public ProcessEngine getProcessEngine() {
    return processEngine;
  }

  public void setProcessEngine(ProcessEngine processEngine) {
    this.processEngine = processEngine;
  }

  public void startBenchmarkScenario() {
    // remember the start date; allows calculation of throughput since process instance
    // start date cannot be used as process instances can be created upfront
    this.currentScenarioStart = new Date();
  }

  public Date getCurrentScenarioStart() {
    return currentScenarioStart;
  }
}
