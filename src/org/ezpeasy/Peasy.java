package org.ezpeasy;


import com.google.gson.Gson;
import spark.ExceptionHandler;
import spark.Request;
import spark.Response;

import java.lang.reflect.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static spark.Spark.*;

public class Peasy {

    private static Gson gson;

    private Peasy(int port, Consumer<Exception> initExceptionHandler) {
        port(port);

        spark.Spark.initExceptionHandler(initExceptionHandler);

        spark.Spark.exception(Exception.class, (ex, request, response) -> ex.printStackTrace());

        spark.Spark.init();
        gson = new Gson();

    }

    public static Peasy start(int port) {
        return new Peasy(port, Throwable::printStackTrace);
    }

    public static Peasy start(int port, Consumer<Exception> initExceptionHandler) {
        return new Peasy(port, initExceptionHandler);
    }

    public static void setGson(Gson gson) {
        Peasy.gson = gson;
    }

    public <T extends Exception> void addExceptionHandler(Class<T> clazz, ExceptionHandler<T> exceptionHandler) {
        spark.Spark.exception(clazz, exceptionHandler);
    }

    public void publish(Class clazz) {
        List<Method> methods = getMethods(clazz);

        if (methods.isEmpty()) {
            System.out.println("Without methods!");
            return;
        }

        Constructor[] declaredConstructors = clazz.getDeclaredConstructors();

        if (declaredConstructors.length == 0) {
            return;
        }

        Optional<Constructor> opContructor = Arrays.stream(declaredConstructors)
                .filter(a -> a.getParameterCount() == 0)
                .findFirst();

        Object parent = null;

        if (opContructor.isPresent()) {
            try {
                parent = opContructor.get().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        if (parent == null) {
            return;
        }

        publish(parent, methods, clazz);


    }

    public void publish(Object object) {
        List<Method> methods = getMethods(object.getClass());

        if (methods.isEmpty()) {
            System.out.println("Without methods!");
            return;
        }

        publish(object, methods, object.getClass());

    }

    private void publish(final Object parent, List<Method> methods, Class clazz) {
        for (Method method : methods) {
            String path = "/" + clazz.getName() + "/" + method.getName();

            if (isPost(method)) {

                System.out.println("POST: " + path);

                post(path, ((request, response) -> invoke(request, response, method, parent)));

            } else {

                System.out.println("GET : " + path);
                get(path, ((request, response) -> invoke(request, response, method, parent)));
            }
        }

    }

    private String invoke(Request request, Response response, Method method, Object parent) throws Exception {


        response.type("application/json");

        if (method.getParameterCount() == 0) {
            return parseReturnValue(method, method.invoke(parent).toString());
        }

        final Set<String> queryParams = request.queryParams();

        final Parameter[] parameters = method.getParameters();

        int paramCount = 0;

        for (Parameter parameter : parameters) {

            if (parameter.getType() == Request.class || parameter.getType() == Response.class) {
                continue;
            }

            if (parameter.getType() == String.class || parameter.getType().isPrimitive()) {
                paramCount++;
            }
        }

        if (queryParams.size() < paramCount) {
            throw new IllegalArgumentException("Param requiered not present " + queryParams.size() + " vs " + paramCount);
        }

        final List<Object> result = new ArrayList<>();

        for (Parameter parameter : parameters) {

            if (parameter.getType() == Request.class) {
                result.add(request);
            } else if (parameter.getType() == Response.class) {
                result.add(response);
            } else if (isNotJSON(parameter)) {
                result.add(parse(parameter, request.queryParams(parameter.getName())));
            } else {
                result.add(gson.fromJson(request.body(), parameter.getType()));
            }
        }

        return parseReturnValue(method, method.invoke(parent, result.toArray()));

    }

    private String parseReturnValue(Method method, Object value) {

        if (method.getReturnType().isPrimitive() || method.getReturnType() == String.class) {
            return value.toString();
        }

        return gson.toJson(value);
    }

    private Object parse(Parameter parameter, String value) {

        if (parameter.getType() == long.class) {
            return Long.parseLong(value);
        } else if (parameter.getType() == Long.class) {
            return Long.valueOf(value);
        } else if (parameter.getType() == int.class) {
            return Integer.parseInt(value);
        } else if (parameter.getType() == Integer.class) {
            return Integer.valueOf(value);
        } else if (parameter.getType() == String.class) {
            return value;
        } else if (parameter.getType() == Character.class) {
            return value.toCharArray();
        } else if (parameter.getType() == short.class) {
            return Short.parseShort(value);
        } else if (parameter.getType() == Short.class) {
            return Short.valueOf(value);
        } else if (parameter.getType() == Double.class) {
            return Double.valueOf(value);
        } else if (parameter.getType() == double.class) {
            return Double.parseDouble(value);
        } else if (parameter.getType() == float.class) {
            return Float.parseFloat(value);
        } else if (parameter.getType() == Float.class) {
            return Float.valueOf(value);
        }

        return null;
    }

    private boolean isPost(Method method) {

        if (method.getParameterCount() == 0) {
            return false;
        }


        for (Parameter parameter : method.getParameters()) {
            if (parameter.getType() == Response.class || parameter.getType() == Request.class) {
                continue;
            }

            if (parameter.getType() == String.class) {
                continue;
            }

            if (!parameter.getType().isPrimitive()) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotJSON(Parameter parameter) {

        if (parameter.getType().isPrimitive()) {
            return true;
        } else if (parameter.getType() == String.class) {
            return true;
        }

        return true;
    }

    private List<Method> getMethods(Class clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .filter(this::filterMethod)
                .collect(Collectors.toList());
    }

    private boolean filterMethod(Method method) {

        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }

        Class<?> returnType = method.getReturnType();

        return returnType != Void.class;
    }

}
