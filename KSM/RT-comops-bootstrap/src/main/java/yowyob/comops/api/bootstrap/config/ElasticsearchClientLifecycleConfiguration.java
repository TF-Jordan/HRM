package yowyob.comops.api.bootstrap.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchClientLifecycleConfiguration {

    @Bean
    public static BeanFactoryPostProcessor elasticsearchDestroyMethodSanitizer() {
        return new BeanFactoryPostProcessor() {
            @Override
            public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
                disableDestroyMethod(beanFactory, "reactiveElasticsearchClient");
                disableDestroyMethod(beanFactory, "elasticsearchClient");
            }

            private void disableDestroyMethod(ConfigurableListableBeanFactory beanFactory, String beanName) {
                if (beanFactory.containsBeanDefinition(beanName)) {
                    beanFactory.getBeanDefinition(beanName).setDestroyMethodName("");
                }
            }
        };
    }
}
