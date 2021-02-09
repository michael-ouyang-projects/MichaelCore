package tw.framework.michaelcore.ioc.enumeration;

import java.lang.annotation.Annotation;

import tw.framework.michaelcore.ioc.annotation.components.AopHandler;
import tw.framework.michaelcore.ioc.annotation.components.Component;
import tw.framework.michaelcore.ioc.annotation.components.Configuration;
import tw.framework.michaelcore.ioc.annotation.components.Controller;
import tw.framework.michaelcore.ioc.annotation.components.OrmRepository;
import tw.framework.michaelcore.ioc.annotation.components.Repository;
import tw.framework.michaelcore.ioc.annotation.components.RestController;
import tw.framework.michaelcore.ioc.annotation.components.Service;

public enum Components {

    COMPONENT(Component.class),
    CONFIGURATION(Configuration.class),
    CONTROLLER(Controller.class),
    RESTCONTROLLER(RestController.class),
    SERVICE(Service.class),
    REPOSITORY(Repository.class),
    ORMREPOSITORY(OrmRepository.class),
    AOPHANDLER(AopHandler.class);

    private Class<? extends Annotation> componentClass;

    private Components(Class<? extends Annotation> componentClass) {
        this.componentClass = componentClass;
    }

    private Class<? extends Annotation> getClazz() {
        return componentClass;
    }

    public static boolean isComponentClass(Class<?> clazz) {
        for (Components component : values()) {
            if (clazz.isAnnotationPresent(component.getClazz())) {
                return true;
            }
        }
        return false;
    }

}