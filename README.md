# wro4spring

Configuration support for using <a href="https://code.google.com/p/wro4j/">wro4j</a> with
<a href="http://www.springsource.org/spring-framework">Spring MVC</a>.

wro4spring serves as the base for <a href="https://github.com/sevensource/wro4spring-thymeleaf-dialect">wro4spring-thymeleaf-dialect</a>,
a <a href="http://www.thymeleaf.org">Thymeleaf</a> dialect making it easy to integrate wro4j.

## Overview
wro4spring provides configuration support for using the excellent wro4j resources optimizer with Spring. In detail,
it provides:

* @Configuration support class with sensible defaults to get you started quickly within a couple of lines.
  * all defaults can be easily customized and overriden
  * allows for easy distinction between development and production mode and their respective settings.
* WroModelAccessor, which allows you to easily retrieve a groups resources
* GroupPerFile-GroupExtractor and -Transformer - in development mode, wro4spring alters the model and creates a group per
resource. This allows you to use wro4j filters without minimization and without concatenation.

## Getting started
1. Add the following to your pom.xml
```xml
<dependency>
      <groupId>org.sevensource</groupId>
      <artifactId>wro4spring</artifactId>
      <version>X.Y.Z</version>
</dependency>
```

2. Add the following to your _web.xml_
```xml
<filter>
      <filter-name>wroFilter</filter-name>
      <filter-class>org.sevensource.wro4spring.DelegatingWroFilterProxy</filter-class>
</filter>
<filter-mapping>
      <filter-name>wroFilter</filter-name>
      <url-pattern>/static/bundles/*</url-pattern>
</filter-mapping>
```

3. Create a _wro.xml_ defining your resources. See https://code.google.com/p/wro4j/wiki/WroFileFormat

4. Create a @Configuration class - all configurable aspects and the inner workings are contained in
DefaultAbstractWro4SpringConfiguration and its superclasses.
```java
@Configuration
public class Wro4SpringConfiguration extends DefaultAbstractWro4SpringConfiguration {
      @Override
      protected String getWroFile() {
        return super.getWroFile();
      }
	
      @Override	
      public WroDeliveryConfiguration wroDeliveryConfiguration() {
        WroDeliveryConfiguration configuration = new WroDeliveryConfiguration();
        configuration.setDevelopment(isDevelopment());
        configuration.setUriPrefix("/static/bundles/");
        //configuration.setCdnDomain("cdn.foo.com");
        return configuration;
  	  }
	
      @Override
      protected boolean isDevelopment() {
        return true;
      }
}
```


5. @Import your _Wro4SpringConfiguration_ in your **root** application context, ie. the one you asked ContextLoaderListener to use.


6. Enjoy and fork, pull & report, if you wish.
