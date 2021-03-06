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
import java.util.Random;

import org.camunda.bpm.benchmark.BenchmarkContext;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cmd.StartProcessInstanceCmd;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.interceptor.CommandExecutor;

/**
 * @author Thorben Lindhauer
 *
 */
public class StartProcessCmd implements CliCommand {

  public String getName() {
    return "start-process";
  }

  public void execute(String[] args, BenchmarkContext context) {

    if (args.length < 2) {
      System.out.println("Requires at least two arguments: process definition key and number of instances + optionally variable key value pairs (all are treated as integers)");
      return;
    }

    final String processDefinitionKey = args[0];
    final int numberOfInstances = Integer.parseInt(args[1]);

    final Map<String, VariableProvider> variableProviders = new HashMap<String, VariableProvider>();
    for (int i = 2; i < args.length; i++) {
      String[] variablePair = args[i].split("=");
      if (variablePair.length != 2) {
        System.out.println("ignoring variable argument " + args[i]);
        continue;
      }

      VariableProvider valueProvider = determineVariableProvider(variablePair[1]);
      variableProviders.put(variablePair[0], valueProvider);
    }

    ProcessEngineConfigurationImpl configuration = (ProcessEngineConfigurationImpl) context.getProcessEngine()
        .getProcessEngineConfiguration();
    CommandExecutor commandExecutor = configuration.getCommandExecutorTxRequired();

    // execute in one transaction
    commandExecutor.execute(new Command<Void>() {

      public Void execute(CommandContext commandContext) {
        for (int i = 0; i < numberOfInstances; i++) {
          Map<String, Object> variables = resolveVariables(variableProviders);

          new StartProcessInstanceCmd(processDefinitionKey, null, null, null, variables).execute(commandContext);
        }
        return null;
      }
    });
  }

  protected VariableProvider determineVariableProvider(String string) {
    if ("$random".equals(string)) {
      return new RandomVariableProvider();
    }
    else {
      return new IntegerVariableProvider(string);
    }
  }

  protected Map<String, Object> resolveVariables(Map<String, VariableProvider> providers) {
    Map<String, Object> variables = new HashMap<String, Object>();

    for (Map.Entry<String, VariableProvider> provider : providers.entrySet()) {
      variables.put(provider.getKey(), provider.getValue().resolveVariable());
    }

    return variables;
  }

  public static interface VariableProvider {

    Object resolveVariable();
  }

  public static class RandomVariableProvider implements VariableProvider {

    protected Random random = new Random();

    public Object resolveVariable() {
      return random.nextInt(Integer.MAX_VALUE);
    }
  }

  public static class IntegerVariableProvider  implements VariableProvider {
    protected Integer value;

    public IntegerVariableProvider(String value) {
      this.value = Integer.parseInt(value);
    }

    public Object resolveVariable() {
      return value;
    }
  }
}
