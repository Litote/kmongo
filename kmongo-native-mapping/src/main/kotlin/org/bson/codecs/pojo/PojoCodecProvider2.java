/*
 * Copyright (C) 2016/2022 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bson.codecs.pojo;

import org.bson.codecs.Codec;
import org.bson.codecs.configuration.CodecProvider;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.diagnostics.Logger;
import org.bson.diagnostics.Loggers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.bson.assertions.Assertions.notNull;

/**
 * Copied from mongo driver.
 */
final class PojoCodecProvider2 implements CodecProvider {
    static final Logger LOGGER = Loggers.getLogger("codecs.pojo");
    private final boolean automatic;
    private final Map<Class<?>, ClassModel<?>> classModels;
    private final Set<String> packages;
    private final List<Convention2> conventions;
    private final DiscriminatorLookup discriminatorLookup;
    private final List<PropertyCodecProvider> propertyCodecProviders;

    private PojoCodecProvider2(final boolean automatic, final Map<Class<?>, ClassModel<?>> classModels, final Set<String> packages,
                               final List<Convention2> conventions, final List<PropertyCodecProvider> propertyCodecProviders) {
        this.automatic = automatic;
        this.classModels = classModels;
        this.packages = packages;
        this.conventions = conventions;
        this.discriminatorLookup = new DiscriminatorLookup(classModels, packages);
        this.propertyCodecProviders = propertyCodecProviders;
    }

    /**
     * Creates a Builder so classes or packages can be registered and configured before creating an immutable CodecProvider.
     *
     * @return the Builder
     * @see Builder#register(Class[])
     */
    public static Builder builder() {
        return new Builder();
    }

    public <T> Codec<T> get(final Class<T> clazz, final CodecRegistry registry) {
        return getPojoCodec(clazz, registry);
    }

    @SuppressWarnings("unchecked")
    private <T> PojoCodec<T> getPojoCodec(final Class<T> clazz, final CodecRegistry registry) {
        ClassModel<T> classModel = (ClassModel<T>) classModels.get(clazz);
        if (classModel != null) {
            return new PojoCodecImpl<T>(classModel, registry, propertyCodecProviders, discriminatorLookup);
        } else if (automatic || (clazz.getPackage() != null && packages.contains(clazz.getPackage().getName()))) {
            try {
                classModel = createClassModel(clazz, conventions);
                if (clazz.isInterface() || !classModel.getPropertyModels().isEmpty()) {
                    discriminatorLookup.addClassModel(classModel);
                    return new AutomaticPojoCodec<T>(new PojoCodecImpl<T>(classModel, registry, propertyCodecProviders,
                            discriminatorLookup));
                }
            } catch (Exception e) {
                LOGGER.warn(format("Cannot use '%s' with the PojoCodec.", clazz.getSimpleName()), e);
                return null;
            }
        }
        return null;
    }

    /**
     * A Builder for the PojoCodecProvider
     */
    public static final class Builder {
        private final Set<String> packages = new HashSet<String>();
        private final Map<Class<?>, ClassModel<?>> classModels = new HashMap<Class<?>, ClassModel<?>>();
        private final List<Class<?>> clazzes = new ArrayList<Class<?>>();
        private List<Convention2> conventions = null;
        private final List<PropertyCodecProvider> propertyCodecProviders = new ArrayList<PropertyCodecProvider>();
        private boolean automatic;

        /**
         * Creates the PojoCodecProvider with the classes or packages that configured and registered.
         *
         * @return the Provider
         * @see #register(Class...)
         */
        public PojoCodecProvider2 build() {
            List<Convention2> immutableConventions = conventions != null
                    ? Collections.unmodifiableList(new ArrayList<Convention2>(conventions))
                    : null;
            for (Class<?> clazz : clazzes) {
                if (!classModels.containsKey(clazz)) {
                    register(createClassModel(clazz, immutableConventions));
                }
            }
            return new PojoCodecProvider2(automatic, classModels, packages, immutableConventions, propertyCodecProviders);
        }

        /**
         * Sets whether the provider should automatically try to wrap a {@link ClassModel} for any class that is requested.
         *
         * <p>Note: As Java Beans are convention based, when using automatic settings the provider should be the last provider in the
         * registry.</p>
         *
         * @param automatic whether to automatically wrap {@code ClassModels} or not.
         * @return this
         */
        public Builder automatic(final boolean automatic) {
            this.automatic = automatic;
            return this;
        }

        /**
         * Sets the conventions to use when creating {@code ClassModels} from classes or packages.
         *
         * @param conventions a list of conventions
         * @return this
         */
        public Builder conventions(final List<Convention2> conventions) {
            this.conventions = notNull("conventions", conventions);
            return this;
        }

        /**
         * Registers a classes with the builder for inclusion in the Provider.
         *
         * <p>Note: Uses reflection for the property mapping. If no conventions are configured on the builder the
         * {@link Conventions#DEFAULT_CONVENTIONS} will be used.</p>
         *
         * @param classes the classes to register
         * @return this
         */
        public Builder register(final Class<?>... classes) {
            clazzes.addAll(asList(classes));
            return this;
        }

        /**
         * Registers classModels for inclusion in the Provider.
         *
         * @param classModels the classModels to register
         * @return this
         */
        public Builder register(final ClassModel<?>... classModels) {
            notNull("classModels", classModels);
            for (ClassModel<?> classModel : classModels) {
                this.classModels.put(classModel.getType(), classModel);
            }
            return this;
        }

        /**
         * Registers the packages of the given classes with the builder for inclusion in the Provider. This will allow classes in the
         * given packages to mapped for use with PojoCodecProvider.
         *
         * <p>Note: Uses reflection for the field mapping. If no conventions are configured on the builder the
         * {@link Conventions#DEFAULT_CONVENTIONS} will be used.</p>
         *
         * @param packageNames the package names to register
         * @return this
         */
        public Builder register(final String... packageNames) {
            packages.addAll(asList(notNull("packageNames", packageNames)));
            return this;
        }

        /**
         * Registers codec providers that receive the type parameters of properties for instances encoded and decoded
         * by a {@link PojoCodec} handled by this provider.
         *
         * <p>Note that you should prefer working with the {@link CodecRegistry}/{@link CodecProvider} hierarchy. Providers
         * should only be registered here if a codec needs to be created for custom container types like optionals and
         * collections. Support for types {@link Map} and {@link java.util.Collection} are built-in so explicitly handling
         * them is not necessary.
         *
         * @param providers property codec providers to register
         * @return this
         * @since 3.6
         */
        public Builder register(final PropertyCodecProvider... providers) {
            propertyCodecProviders.addAll(asList(notNull("providers", providers)));
            return this;
        }

        private Builder() {
        }
    }

    private static <T> ClassModel<T> createClassModel(final Class<T> clazz, final List<Convention2> conventions) {
        ClassModelBuilder2<T> builder = new ClassModelBuilder2<T>(clazz);
        if (conventions != null) {
            builder.conventions(conventions);
        }
        return builder.build();
    }
}
