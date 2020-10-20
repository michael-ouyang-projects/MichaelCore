package tw.framework.michaelcore.ioc;

import java.lang.annotation.Annotation;

import tw.framework.michaelcore.aop.annotation.AopHandler;
import tw.framework.michaelcore.ioc.annotation.Component;
import tw.framework.michaelcore.ioc.annotation.Service;
import tw.framework.michaelcore.mvc.annotation.Controller;

public enum Components {

    COMPONENT(Component.class), CONTROLLER(Controller.class), SERVICE(Service.class), AOPHANDLER(AopHandler.class);

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
