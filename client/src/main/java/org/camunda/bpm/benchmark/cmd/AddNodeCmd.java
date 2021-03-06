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

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.benchmark.BenchmarkContext;

/**
 * @author Thorben Lindhauer
 *
 */
public class AddNodeCmd implements CliCommand {

  public String getName() {
    return "node";
  }

  public void execute(String[] args, BenchmarkContext context) {
    if (args.length < 2) {
      System.out.println("Requires at least two arguments: host and port for jmx (+ any number of process engine names)");
      return;
    }

    String host = args[0];
    int port = Integer.parseInt(args[1]);

    List<String> engines = new ArrayList<String>();
    for (int i = 2; i < args.length; i++) {
      engines.add(args[i]);
    }

    context.getClusterManager().addNode(host, port, engines);
  }


}
