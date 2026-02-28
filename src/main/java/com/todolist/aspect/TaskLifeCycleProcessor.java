package com.todolist.aspect;

import com.todolist.repository.TaskRepository;
import com.todolist.service.TaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

@Component
public class TaskLifeCycleProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(TaskLifeCycleProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            log.info("До инициализации бина: {} (класс: {})",
                    beanName, bean.getClass().getSimpleName());
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TaskService || bean instanceof TaskRepository) {
            log.info("После инициализации бина: {} (класс: {})",
                    beanName, bean.getClass().getSimpleName());
        }
        return bean;
    }
}