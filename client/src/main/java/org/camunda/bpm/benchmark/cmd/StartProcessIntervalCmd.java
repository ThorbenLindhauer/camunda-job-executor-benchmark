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

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.camunda.bpm.benchmark.BenchmarkContext;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;

/**
 * @author Thorben Lindhauer
 *
 */
public class StartProcessIntervalCmd implements CliCommand {

  public String getName() {
    return "start-process-interval";
  }

  public void execute(String[] args, BenchmarkContext context) {

    if (args.length < 2) {
      System.out.println("Requires at least four arguments:\n "
          + "1) process definition key\n"
          + "2) number of instances\n"
          + "3) number of instances per interval\n"
          + "4) interval duration (in seconds)\n "
          + "+ optionally variable key value pairs (all are treated as integers)");
      return;
    }

    String processDefinitionKey = args[0];
    int numberOfInstances = Integer.parseInt(args[1]);
    int numberOfInstancesPerInterval = Integer.parseInt(args[2]);
    int interval = Integer.parseInt(args[3]);


    Map<String, Object> variables = new HashMap<String, Object>();
    for (int i = 2; i < args.length; i++) {
      String[] variablePair = args[i].split("=");
      if (variablePair.length != 2) {
        System.out.println("ignoring variable argument " + args[i]);
        continue;
      }

      variables.put(variablePair[0], Integer.parseInt(variablePair[1]));
    }

    Timer timer = new Timer(true);
    timer.scheduleAtFixedRate(
        new StartProcessesTask(
          context.getProcessEngine(),
          processDefinitionKey,
          numberOfInstances,
          numberOfInstancesPerInterval,
          variables),
        0,
        interval * 1000);

  }

  public static class StartProcessesTask extends TimerTask {

    protected ProcessEngine engine;
    protected String processDefinitionKey;
    protected int numInstances;
    protected int numInstancesPerInterval;
    protected Map<String, Object> variables;
    protected int numInstancesProcessed = 0;

    public StartProcessesTask(
        ProcessEngine engine,
        String processDefinitionKey,
        int numInstances,
        int numInstancesPerInterval,
        Map<String, Object> variables) {
      this.engine = engine;
      this.processDefinitionKey = processDefinitionKey;
      this.numInstances = numInstances;
      this.numInstancesPerInterval = numInstancesPerInterval;
      this.variables = variables;
    }

    public void run() {

      if (numInstancesProcessed >= numInstances) {
        cancel();
        return;
      }

      numInstancesProcessed += numInstancesPerInterval;

      RuntimeService runtimeService = engine.getRuntimeService();
      for (int i = 0; i < numInstancesPerInterval; i++) {
        runtimeService.startProcessInstanceByKey(processDefinitionKey, variables);
      }
    }

  }
}
