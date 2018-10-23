package blended.itestsupport

import org.apache.camel.CamelContext

trait TestContextConfigurator {
  def configure(cuts: Map[String, ContainerUnderTest], context: CamelContext): CamelContext
}
