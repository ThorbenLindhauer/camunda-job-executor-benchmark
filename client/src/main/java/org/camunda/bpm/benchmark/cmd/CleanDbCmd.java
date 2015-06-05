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
package org.camunda.bpm.benchmark.cmd;

import java.util.Date;
import java.util.List;

import org.camunda.bpm.benchmark.BenchmarkContext;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.repository.Deployment;

/**
 * @author Thorben Lindhauer
 *
 */
public class CleanDbCmd implements CliCommand {

  public String getName() {
    return "clean-db";
  }

  public void execute(String[] args, BenchmarkContext context) {
    ProcessEngine engine = context.getProcessEngine();

    List<Deployment> deployments = engine.getRepositoryService().createDeploymentQuery().list();

    for (Deployment deployment : deployments) {
      engine.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }

    engine.getManagementService().deleteMetrics(new Date());
  }

}