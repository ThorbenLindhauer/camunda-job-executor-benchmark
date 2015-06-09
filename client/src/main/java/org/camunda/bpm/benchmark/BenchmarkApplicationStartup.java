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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.application.ProcessApplicationInterface;
import org.camunda.bpm.benchmark.cmd.AddNodeCmd;
import org.camunda.bpm.benchmark.cmd.CleanDbCmd;
import org.camunda.bpm.benchmark.cmd.CliCommand;
import org.camunda.bpm.benchmark.cmd.ExitCmd;
import org.camunda.bpm.benchmark.cmd.GenerateReportCmd;
import org.camunda.bpm.benchmark.cmd.ReportMetricsJmxCmd;
import org.camunda.bpm.benchmark.cmd.StartJobExecutorCmd;
import org.camunda.bpm.benchmark.cmd.StartProcessCmd;
import org.camunda.bpm.benchmark.cmd.StartProcessIntervalCmd;
import org.camunda.bpm.benchmark.cmd.StopJobExecutorCmd;
import org.camunda.bpm.benchmark.cmd.SwitchEngineCmd;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngines;


/**
 * @author Thorben Lindhauer
 *
 */
public class BenchmarkApplicationStartup {

  public static void main(String[] args) throws Exception {
    ProcessApplicationInterface pa = new BenchmarkProcessApplication();
    pa.deploy();

    ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();

    BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in));
    String input;
    Map<String, CliCommand> commands = getCommands();
    BenchmarkContext context = new BenchmarkContext();
    context.setClusterManager(new ClusterManager());
    context.setProcessEngine(engine);

    System.out.println("processes.xml: " + BenchmarkApplicationStartup.class.getResource("META-INF/processes.xml"));

    ClassLoader cl = ClassLoader.getSystemClassLoader();

    URL[] urls = ((URLClassLoader)cl).getURLs();

    for(URL url: urls){
      System.out.println(url.getFile());
    }

    while ((input = inReader.readLine()) != null) {
      String[] splitInput = input.split("\\s+");
      String commandName = splitInput[0];
      String[] inputArgs = new String[0];
      if (splitInput.length > 1) {
        inputArgs = Arrays.copyOfRange(splitInput, 1, splitInput.length);
      }

      CliCommand command = commands.get(commandName);

      if (command != null) {
        command.execute(inputArgs, context);

      }
      else {
        System.out.println("Unknown cmd: " + commandName);
      }


      if ("exit".equals(commandName)) {
        break;
      }
    }
  }

  public static Map<String, CliCommand> getCommands() {
    List<CliCommand> commands = new ArrayList<CliCommand>();

    commands.add(new AddNodeCmd());
    commands.add(new StartJobExecutorCmd());
    commands.add(new StopJobExecutorCmd());
    commands.add(new StartProcessCmd());
    commands.add(new StartProcessIntervalCmd());
    commands.add(new SwitchEngineCmd());
    commands.add(new CleanDbCmd());
    commands.add(new ReportMetricsJmxCmd());
    commands.add(new GenerateReportCmd());
    commands.add(new ExitCmd());

    Map<String, CliCommand> result = new HashMap<String, CliCommand>();
    for (CliCommand command : commands) {
      result.put(command.getName(), command);
    }

    return result;

  }

}
