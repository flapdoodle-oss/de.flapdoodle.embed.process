package de.flapdoodle.embed.process;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE,
		ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE})
public @interface _Refactor {

}
