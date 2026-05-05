package com.example.backendApi.annotation.openapi;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Tag(name = "")
public @interface ApiControllerTag {
    @AliasFor(annotation = Tag.class, attribute = "name")
    String name();

    @AliasFor(annotation = Tag.class, attribute = "description")
    String description() default "Backend API endpoints";
}
