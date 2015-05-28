/*
 * =============================================================================
 * 
 *   Copyright (c) 2011-2014, The THYMELEAF team (http://www.thymeleaf.org)
 * 
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 * 
 * =============================================================================
 */
package org.thymeleaf.spring4.expression;

import org.thymeleaf.exceptions.ConfigurationException;
import org.thymeleaf.spring4.util.SpringVersionUtils;
import org.thymeleaf.util.ClassLoaderUtils;

/**
 * 
 * @author Daniel Fern&aacute;ndez
 * 
 * @since 2.1.4
 *
 */
public class Mvc {

    private static final MvcUriComponentsBuilderDelegate mvcUriComponentsBuilderDelegate;
    private static final String SPRING41_MVC_URI_COMPONENTS_BUILDER_DELEGATE_CLASS_NAME = Mvc.class.getName() + "$Spring41MvcUriComponentsBuilderDelegate";
    private static final String NON_SPRING41_MVC_URI_COMPONENTS_BUILDER_DELEGATE_CLASS_NAME = Mvc.class.getName() + "$NonSpring41MvcUriComponentsBuilderDelegate";


    static {

        final ClassLoader classLoader = ClassLoaderUtils.getClassLoader(Mvc.class);

        final String delegateClassName =
                (SpringVersionUtils.isSpring41AtLeast()?
                        SPRING41_MVC_URI_COMPONENTS_BUILDER_DELEGATE_CLASS_NAME :
                        NON_SPRING41_MVC_URI_COMPONENTS_BUILDER_DELEGATE_CLASS_NAME);

        try {
            final Class<?> implClass = Class.forName(delegateClassName, true, classLoader);
            mvcUriComponentsBuilderDelegate = (MvcUriComponentsBuilderDelegate) implClass.newInstance();
        } catch (final Exception e) {
            throw new ConfigurationException(
                    "Thymeleaf could not initialize a delegate of class \"" + delegateClassName + "\" for taking " +
                    "care of the " + SpringStandardExpressionObjectFactory.MVC_EXPRESSION_OBJECT_NAME + " expression utility object", e);
        }

    }


    public MethodArgumentBuilderWrapper url(final String mappingName) {
        return mvcUriComponentsBuilderDelegate.fromMappingName(mappingName);
    }



    static interface MvcUriComponentsBuilderDelegate {

        public MethodArgumentBuilderWrapper fromMappingName(String mappingName);

    }


    static class Spring41MvcUriComponentsBuilderDelegate implements MvcUriComponentsBuilderDelegate {

        Spring41MvcUriComponentsBuilderDelegate() {
            super();
        }

        public MethodArgumentBuilderWrapper fromMappingName(final String mappingName) {
            return new Spring41MethodArgumentBuilderWrapper(org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMappingName(mappingName));
        }

    }


    static class NonSpring41MvcUriComponentsBuilderDelegate implements MvcUriComponentsBuilderDelegate {

        NonSpring41MvcUriComponentsBuilderDelegate() {
            super();
        }

        public MethodArgumentBuilderWrapper fromMappingName(final String mappingName) {
            throw new UnsupportedOperationException(
                    "MVC URI component building is only supported in Spring versions 4.1 or newer");
        }

    }



    public static interface MethodArgumentBuilderWrapper {

        public MethodArgumentBuilderWrapper arg(int index, Object value);
        public String build();
        public String buildAndExpand(Object... uriVariables);

    }


    static class Spring41MethodArgumentBuilderWrapper implements MethodArgumentBuilderWrapper {

        private final org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.MethodArgumentBuilder builder;


        private Spring41MethodArgumentBuilderWrapper(
                final org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.MethodArgumentBuilder builder) {
            super();
            this.builder = builder;
        }

        public MethodArgumentBuilderWrapper arg(final int index, final Object value) {
            return new Spring41MethodArgumentBuilderWrapper(this.builder.arg(index, value));
        }

        public String build() {
            return this.builder.build();
        }

        public String buildAndExpand(final Object... uriVariables) {
            return this.builder.buildAndExpand(uriVariables);
        }

    }


}
