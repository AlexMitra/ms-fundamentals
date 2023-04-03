package pl.kempa.saska.config;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

public class LoadBalancerConfig {
  @Bean
  ReactorLoadBalancer<ServiceInstance> randomLoadBalancer(Environment environment,
                                                          LoadBalancerClientFactory loadBalancerClientFactory) {
    String name = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    // I created this bean because after testing it seems that with this balancer switching between
    // services more frequent
    return new RandomLoadBalancer(loadBalancerClientFactory
        .getLazyProvider(name, ServiceInstanceListSupplier.class),
        name);
  }
}
