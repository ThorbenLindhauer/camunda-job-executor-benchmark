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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.camunda.bpm.benchmark.BenchmarkContext;

/**
 * @author Thorben Lindhauer
 *
 */
public class StartJobExecutorCmd implements CliCommand {

  public static final String DATE_FORMAT_STRING = "HH:mm:ss";
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

  public String getName() {
    return "start-job-execution";
  }

  public void execute(String[] args, BenchmarkContext context) {
    System.out.println("Starting job execution at: " + DATE_FORMAT.format(new Date()));
    context.getClusterManager().startJobExecution();
  }

}
