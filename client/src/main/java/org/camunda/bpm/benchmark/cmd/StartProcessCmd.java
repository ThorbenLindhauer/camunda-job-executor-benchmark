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

import org.camunda.bpm.benchmark.BenchmarkContext;
import org.camunda.bpm.engine.RuntimeService;

/**
 * @author Thorben Lindhauer
 *
 */
public class StartProcessCmd implements CliCommand {

  public String getName() {
    return "start-process";
  }

  public void execute(String[] args, BenchmarkContext context) {

    if (args.length != 2) {
      System.out.println("Requires  two arguments: process definition key and number of instances");
      return;
    }

    String processDefinitionKey = args[0];
    int numberOfInstances = Integer.parseInt(args[1]);

    for (int i = 0; i < numberOfInstances; i++) {
      RuntimeService runtimeService = context.getProcessEngine().getRuntimeService();
      runtimeService.startProcessInstanceByKey(processDefinitionKey);
    }
  }
}
