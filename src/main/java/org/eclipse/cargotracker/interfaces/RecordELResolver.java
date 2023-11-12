package org.eclipse.cargotracker.interfaces;

import jakarta.el.*;

import java.beans.FeatureDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.lang.reflect.RecordComponent;

public class RecordELResolver extends ELResolver {

    private static final Map<Class<?>, Map<String, PropertyDescriptor>> RECORD_PROPERTY_DESCRIPTOR_CACHE = new ConcurrentHashMap<>();

    private static boolean isRecord(Object base) {
        return base != null && base.getClass().isRecord();
    }

    private static Map<String, PropertyDescriptor> getRecordPropertyDescriptors(Object base) {
        return RECORD_PROPERTY_DESCRIPTOR_CACHE
                .computeIfAbsent(base.getClass(), clazz -> Arrays
                        .stream(clazz.getRecordComponents())
                        .collect(Collectors
                                .toMap(RecordComponent::getName, recordComponent -> {
                                    try {
                                        return new PropertyDescriptor(recordComponent.getName(), recordComponent.getAccessor(), null);
                                    }
                                    catch (IntrospectionException e) {
                                        throw new IllegalStateException(e);
                                    }
                                })));
    }

    private static PropertyDescriptor getRecordPropertyDescriptor(Object base, Object property) {
        PropertyDescriptor descriptor = getRecordPropertyDescriptors(base).get(property);

        if (descriptor == null) {
            throw new PropertyNotFoundException("The record '" + base.getClass().getName() + "' does not have the field '" + property + "'.");
        }

        return descriptor;
    }

    @Override
    public Object getValue(ELContext context, Object base, Object property) {
        if (!isRecord(base) || property == null) {
            return null;
        }

        PropertyDescriptor descriptor = getRecordPropertyDescriptor(base, property);

        try {
            Object value = descriptor.getReadMethod().invoke(base);
            context.setPropertyResolved(base, property);
            return value;
        }
        catch (Exception e) {
            throw new ELException(e);
        }
    }

    @Override
    public Class<?> getType(ELContext context, Object base, Object property) {
        if (!isRecord(base) || property == null) {
            return null;
        }

        PropertyDescriptor descriptor = getRecordPropertyDescriptor(base, property);
        context.setPropertyResolved(true);
        return descriptor.getPropertyType();
    }

    @Override
    public Class<?> getCommonPropertyType(ELContext context, Object base) {
        if (!isRecord(base)) {
            return null;
        }

        return String.class;
    }

    @Override
    public boolean isReadOnly(ELContext context, Object base, Object property) {
        if (!isRecord(base)) {
            return false;
        }

        getRecordPropertyDescriptor(base, property); // Forces PropertyNotFoundException if necessary.
        context.setPropertyResolved(true);
        return true;
    }

    @Override
    public void setValue(ELContext context, Object base, Object property, Object value) {
        if (!isRecord(base)) {
            return;
        }

        throw new PropertyNotWritableException("Java Records are immutable");
    }

}
