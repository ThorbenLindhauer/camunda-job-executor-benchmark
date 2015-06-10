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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.benchmark.BenchmarkContext;
import org.camunda.bpm.benchmark.report.CsvReportWriter;
import org.camunda.bpm.benchmark.report.MetricsReport;
import org.joda.time.DateTime;

/**
 * @author Thorben Lindhauer
 *
 */
public class GenerateReportCmd implements CliCommand {

  public static final String DATE_FORMAT_STRING = "HH:mm:ss";
  public static final DateFormat DATE_FORMAT = new SimpleDateFormat(DATE_FORMAT_STRING);

  public String getName() {
    return "generate-report";
  }

  public void execute(String[] args, BenchmarkContext context) {
    if (args.length < 4) {
      System.out.println("Requires at least four arguments: \n"
          + "1) start date \n"
          + "2) end date for metrics reporting; \n"
          + "3) an interval in seconds that is used to report metrics for (i.e. an interval of 5 seconds means metrics are queried for 5 second intervals); \n"
          + "4) a variable number of reporter identifiers for which metrics should be collected"
          + "dates have to be specified in the format " + DATE_FORMAT_STRING);
      return;
    }

    Date startDate = null;
    Date endDate = null;
    try {
      startDate = asTimeOfToday(timeOfDayInMillis(args[0]));
      endDate = asTimeOfToday(timeOfDayInMillis(args[1]));
    } catch (ParseException e) {
      System.out.println("Could not parse dates: " + e.getMessage());
      System.out.println("Required format: " + DATE_FORMAT_STRING);
      System.out.println("No report generated.");
      return;
    }

    int metricsReportInterval = Integer.parseInt(args[2]);
    Set<String> metricsReporterIds = new HashSet<String>();
    for (int i = 3; i < args.length; i++) {
      metricsReporterIds.add(args[i]);
    }

    MetricsReport report = new MetricsReport(context.getProcessEngine(), startDate, endDate,
        metricsReportInterval, metricsReporterIds);
    report.generate(new CsvReportWriter("report.csv"));
  }

  protected Date asTimeOfToday(long millisecondsOfDay) {
    return new DateTime().withMillisOfDay((int) millisecondsOfDay).toDate();
  }

  protected long timeOfDayInMillis(String formattedTime) throws ParseException {
    Date initialDateInCurrentTimezone = DATE_FORMAT.parse("00:00:00");
    Date relativeDate = DATE_FORMAT.parse(formattedTime);
    return relativeDate.getTime() - initialDateInCurrentTimezone.getTime();
  }

}
