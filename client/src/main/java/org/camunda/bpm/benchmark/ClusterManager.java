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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.camunda.bpm.container.impl.jmx.MBeanServiceContainer;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedJobExecutorMBean;
import org.camunda.bpm.container.impl.jmx.services.JmxManagedProcessEngineMBean;
import org.camunda.bpm.container.impl.spi.ServiceTypes;

/**
 * @author Thorben Lindhauer
 *
 */
public class ClusterManager {

  protected Set<ClusterNode> nodes = new HashSet<ClusterNode>();

  public void addNode(String host, int port, List<String> engines) {
    this.nodes.add(new ClusterNode(host, port, engines));
  }

  public void startJobExecution() {
    for (ClusterNode node : nodes) {
      node.startJobExecutor(null);
    }
  }

  public void stopJobExecution() {
    for (ClusterNode node : nodes) {
      node.shutdownJobExecutor(null);
    }
  }

  public void reportMetrics() {
    for (ClusterNode node : nodes) {
      node.reportMetrics();
    }
  }

  public Set<ClusterNode> getNodes() {
    return nodes;
  }

  public static class ClusterNode {
    protected JMXServiceURL jmxUrl;
    protected JMXConnector jmxConnector;
    protected MBeanServerConnection mBeanServerConnection;
    protected List<String> processEngines;

    public ClusterNode(String host, int port, List<String> processEngines) {
      this.processEngines = processEngines;
      try {
        this.jmxUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://" + host + ":" + port + "/jmxrmi");
        openJMXConnection();
      } catch (Exception e) {
        throw new RuntimeException("Could not setup JMX Connection", e);
      }
    }

    protected ObjectName getJobAcquisitionMBeanName(String jobAcquisitionName) {
      try {
        return new ObjectName(MBeanServiceContainer.composeLocalName(ServiceTypes.JOB_EXECUTOR, jobAcquisitionName));
      } catch (MalformedObjectNameException e) {
        throw new RuntimeException(e);
      }
    }

    protected ObjectName getProcessEngineMBeanName(String jobAcquisitionName) {
      try {
        return new ObjectName(MBeanServiceContainer.composeLocalName(ServiceTypes.PROCESS_ENGINE, jobAcquisitionName));
      } catch (MalformedObjectNameException e) {
        throw new RuntimeException(e);
      }
    }

    public void startJobExecutor(String name) {
      if (name == null) {
        name = "default";
      }

      ObjectName jobAcquisitonName = getJobAcquisitionMBeanName(name);
      JmxManagedJobExecutorMBean jobExecutorMBean = MBeanServerInvocationHandler
          .newProxyInstance(mBeanServerConnection, jobAcquisitonName, JmxManagedJobExecutorMBean.class, true);
      jobExecutorMBean.start();
    }

    public void shutdownJobExecutor(String name) {
      if (name == null) {
        name = "default";
      }

      ObjectName jobAcquisitonName = getJobAcquisitionMBeanName(name);
      JmxManagedJobExecutorMBean jobExecutorMBean = MBeanServerInvocationHandler
          .newProxyInstance(mBeanServerConnection, jobAcquisitonName, JmxManagedJobExecutorMBean.class, true);

      jobExecutorMBean.shutdown();

    }

    public void reportMetrics() {
      for (String processEngine : processEngines) {
        ObjectName processEngineName = getProcessEngineMBeanName(processEngine);
        JmxManagedProcessEngineMBean engineMBean = MBeanServerInvocationHandler
            .newProxyInstance(mBeanServerConnection, processEngineName, JmxManagedProcessEngineMBean.class, true);

        engineMBean.reportDbMetrics();
      }
    }

    public void openJMXConnection() {
      try {
        this.jmxConnector = JMXConnectorFactory.connect(jmxUrl);
        this.mBeanServerConnection = jmxConnector.getMBeanServerConnection();
      } catch (Exception e) {
        throw new RuntimeException("Could not setup JMX Connection", e);
      }
    }

    public void closeJMXConnection() {
      try {
        jmxConnector.close();
      } catch (Exception e) {
        throw new RuntimeException("Could not close JMX Connection", e);
      }
    }
  }
}
