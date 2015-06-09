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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.camunda.bpm.benchmark.report.MetricsResults.MetricsResultInstance;

/**
 * @author Thorben Lindhauer
 *
 */
public class CsvReportWriter implements MetricsWriter {

  public static final DateFormat DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

  protected String fileName;

  public CsvReportWriter(String fileName) {
    this.fileName = fileName;
  }

  public void write(MetricsResults result) {
    File file = new File(fileName);
    List<String> lines = new ArrayList<String>();

    // header
    addLine(lines, "reporter", "metric", "operator", "intervalStart", "intervalEnd", "value");

    for (Map.Entry<String, List<MetricsResultInstance>> resultsByReporter : result.getMetricsByReporter().entrySet()) {
      for (MetricsResultInstance resultInstance : resultsByReporter.getValue()) {
        addLine(lines,
            resultsByReporter.getKey(),
            resultInstance.getMetricName(),
            resultInstance.getOperator(),
            formatDate(resultInstance.getStartDate()),
            formatDate(resultInstance.getEndDate()),
            resultInstance.getValue());
      }
    }

    System.out.println("Writing to " + file.getAbsolutePath());

    try {
      FileUtils.writeLines(file, "UTF-8", lines);
    } catch (IOException e) {
      throw new RuntimeException("Could not write to file " + fileName, e);
    }
  }

  protected void addLine(List<String> lines, Object... values) {
    StringBuilder sb = new StringBuilder();
    for (Object value : values) {
      sb.append(value);
      sb.append(";");
    }

    lines.add(sb.toString());
  }

  protected String formatDate(Date date) {
    return DATE_FORMAT.format(date);
  }
}
