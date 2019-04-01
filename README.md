# RestEZPeasy

RestEZPeasy is a experimental project with the intention to redefine the way to create rest web services.

The main objetive of ezpeasy is to be easy-to-use without adding aditional annotations to the code,
avoiding the code pollution.

 
 So, if is not need to add @Annotations to create a web service, what is needed?
 
 
 1. the '-parameters' is needed to be added to the java compiler parameters.
 2. this parm tells the compiler to keep the params name of each field insted of renaming the param to arg0, arg1, argX
 (don't worry about -parameters experienced programmers can crack your code even if it's ofuscated)
 
 RestEasy example:
 -
 A lot of code Ha!
 ```java
    @GET
    @Path("/getinfo")
    @Produces(MediaType.APPLICATION_JSON)
    public Movie movieByImdbId(@QueryParam("imdbId") String imdbId) {
        if (inventory.containsKey(imdbId)) {
            return inventory.get(imdbId);
        } else {
            return null;
        }
    }
    
    @POST
    @Path("/addmovie")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addMovie(Movie movie) {
        if (null != inventory.get(movie.getImdbId())) {
            return Response
              .status(Response.Status.NOT_MODIFIED)
              .entity("Movie is Already in the database.").build();
        }
 
        inventory.put(movie.getImdbId(), movie);
        return Response.status(Response.Status.CREATED).build();
    }
``` 
 RestEZPeasy result:
 -

 ```java
    public Movie movieByImdbId(String imdbId) {
        if (inventory.containsKey(imdbId)) {
            return inventory.get(imdbId);
        } else {
            return null;
        }
    }
    
    public Response addMovie(Movie movie) {
        if (null != inventory.get(movie.getImdbId())) {
            return Response
              .status(Response.Status.NOT_MODIFIED)
              .entity("Movie is Already in the database.").build();
        }
 
        inventory.put(movie.getImdbId(), movie);
        return Response.status(Response.Status.CREATED).build();
    }
``` 
RestEZPeasy Rules / Features:

1. The addMovie method is mapped to POST in automatic 'cause have a non-primitive non-string parameter
2. The movieByImdbId method is mapped to GET 'cause have only primitive and string only parameters.
3. The return value is application-json by default but can be changed
4. The methods always consumes application-json but can be changed btw

---

1. Only public methods are published and must have a return type != void
2. The class name and method name are used as paths
3. spark.Response and spark.Request objects are not mapped as queryParams and can be obtained 
with just adding to the method params 

```java
public Movie getMovie(String id, Response response, Request request){
    response.redirect("http://www.anotherurl.peasy/");
    response.type("application/xml");
    return "redirecting...";
}
```

> RestEZPeasy uses Gson to convert objects to json and vicebersa and sparkjava, so the configuration is basically limitless

usage:

```java

Peasy.start(8181).publish(SampleWS.class);

```

or

```java

Peasy.start(8181).publish(object);
```

SampleWS
-

```java
public class SampleWS {

    public String sayHello() {
        return "Hello World!";
    }

    public String sum(int a, int b) {
        return "The sum is: " + (a + b);
    }

    public JSONExample json(String name, int age) {
        return new JSONExample(name, age);
    }

    public JSONExample jsonExample(JSONExample example) {
        return example;
    }

    private String notpublished(String value) {
        return value;
    }

    protected String notevenpublished(String value, Response response) {
        response.redirect("http://www.anotherurl.peasy/");
        return value;
    }

}
```

