```java
/**
 * @author Sihan Liu
 */
public class MineBeanUtils {
	
    private static final String ERROR_CODE = "-1";

    /**
     * 合并该类和对应父类的所有Fields
     * @param object object
     * @return List<Field>
     */
    public static List<Field> mergeAllFields(Object object) {
        Class<?> clazz = object.getClass();
        List<Field> fields = new ArrayList<>();

        //排除父级元素,可自定义
        while (clazz != null && !Object.class.getName().equals(clazz.getName())) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
            clazz = clazz.getSuperclass();
        }
        return fields;
    }

    /**
     * 获取对象中被注解标记的字段值(可修改为返回List<Object>)
     * @param t 对象
     * @param clazz 注解
     * @return 值
     */
    public static Object getByAnnotation(Object t, Class<? extends Annotation> clazz) {
        List<Field> fields = mergeAllFields(t);

        Object value = null;
        for (Field declaredField : fields) {
            declaredField.setAccessible(true);
            if (Modifier.isFinal(declaredField.getModifiers())) {
                continue;
            }
            String object;
            Object targetObject = getTargetObjectValue(declaredField, t);
            if (targetObject == null) {
                object = ERROR_CODE;
            } else {
                object = targetObject.toString();
            }

            Object annotation = declaredField.getAnnotation(clazz);
            if (annotation != null) {
            	//如果多个字段被一个注解标记,在这里修改即可
                value = object;
            }
        }

        if (value == null || ERROR_CODE.equals(value)) {
            throw new RuntimeException("无法获取" + clazz.getSimpleName() + "注解,请检查");
        }

        return value;
    }

    /**
     * 设置对象中被注解标记的字段值
     * @param t 对象
     * @param value 值
     * @param clazz 注解
     */
    public static void setByAnnotation(Object t, Object value, Class<? extends Annotation> clazz) {
        List<Field> fields = mergeAllFields(t);

        for (Field declaredField : fields) {
            declaredField.setAccessible(true);
            if (Modifier.isFinal(declaredField.getModifiers())) {
                continue;
            }

            Object annotation = declaredField.getAnnotation(clazz);
            if (annotation != null) {
                setTargetObjectValue(declaredField, t, value);
                break;
            }
        }
    }

    /**
     * 通过内省机制获取字段值
     * @param declaredField 字段
     * @param t 对象
     * @return 值
     */
    public static Object getTargetObjectValue(Field declaredField, Object t) {
        Object object;
        try {
            PropertyDescriptor propDesc = new PropertyDescriptor(declaredField.getName(), t.getClass());
            object = propDesc.getReadMethod().invoke(t);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("获取对应数据失败");
        }
        return object;
    }

    /**
     * 通过内省机制设置字段值
     * @param declaredField 字段
     * @param t 对象
     * @param value 值
     */
    public static void setTargetObjectValue(Field declaredField, Object t, Object value) {
        try {
            PropertyDescriptor propDesc = new PropertyDescriptor(declaredField.getName(), t.getClass());
            propDesc.getWriteMethod().invoke(t, value);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("写入对应数据失败");
        }
    }
}

```

