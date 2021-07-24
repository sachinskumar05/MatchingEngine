package org.ObjectLayout;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

/**
 * A builder used for instantiating a {@link StructuredArray}&lt;T&gt;
 * <p>
 * {@link StructuredArrayBuilder} follows the commonly used builder pattern, and is useful for
 * capturing the instantiation parameters of {@link StructuredArray}s.
 * </p>
 * <p>
 * {@link StructuredArrayBuilder}s can be created for "flat" and "nested" {@link StructuredArray}
 * constructs, and can range from the simplest forms used when default constructors are employed, to forms that
 * supply customized per-element construction arguments that can take construction context (such as index,
 * containing array, and arbitrary data passed in a contextCookie) into account.
 * </p>
 * A simple example of using a {@link StructuredArrayBuilder} to instantiate a StructuredArray is:
 * <blockquote><pre>
 * StructuredArray&lt;MyElementClass&gt; array =
 *      new StructuredArrayBuilder(StructuredArray.class, MyElementClass.class, length).
 *          build();
 * </pre></blockquote>
 * <p>
 * An example of passing specific (but identical) construction arguments to element constructors is:
 * <blockquote><pre>
 * Constructor&lt;MyElementClass&gt; constructor = MyElementClass.class.getConstructor(int.Class, int.Class);
 *
 * StructuredArray&lt;MyElementClass&gt; array =
 *      new StructuredArrayBuilder(
 *          StructuredArray.class,
 *          MyElementClass.class,
 *          length).
 *          elementCtorAndArgs(constructor, initArg1, initArg2).
 *          build();
 * </pre></blockquote>
 * Some examples of providing per-element construction parameters that depend on construction context include:
 * <blockquote><pre>
 * Constructor&lt;MyElementClass&gt; constructor = MyElementClass.class.getConstructor(long.Class, long.Class);
 *
 * // Using a pre-constructed elementCtorAndArgsProvider:
 * StructuredArray&lt;MyElementClass&gt; array =
 *      new StructuredArrayBuilder(
 *          StructuredArray.class,
 *          MyElementClass.class,
 *          length).
 *          elementCtorAndArgsProvider(myCtorAndArgsProvider).
 *          build();
 *
 * // Using a Lambda expression for elementCtorAndArgsProvider:
 * StructuredArray&lt;MyElementClass&gt; array2 =
 *      new StructuredArrayBuilder(
 *          StructuredArray.class,
 *          MyElementClass.class,
 *          length).
 *          elementCtorAndArgsProvider(
 *              context -&gt; new CtorAndArgs&lt;MyElementClass&gt;(
 *                  constructor, context.getIndex(), context.getIndex() * 2)
 *          ).
 *          build();
 *
 * // Using an anonymous class for elementCtorAndArgsProvider:
 * StructuredArray&lt;MyElementClass&gt; array3 =
 *      new StructuredArrayBuilder(
 *          StructuredArray.class,
 *          MyElementClass.class,
 *          length).
 *          elementCtorAndArgsProvider(
 *              new CtorAndArgsProvider() {
 *                  {@literal @}Override
 *                  public CtorAndArgs getForContext(ConstructionContext context) throws NoSuchMethodException {
 *                      return new CtorAndArgs(constructor, context.getIndex(), context.getIndex() * 2);
 *                  }
 *              }
 *          ).
 *          build();
 * </pre></blockquote>
 *
 * @param <S> The class of the StructuredArray that is to be instantiated by the builder
 * @param <T> The class of the elements in the StructuredArray that is to be instantiated the builder
 */
public class StructuredArrayBuilder<S extends StructuredArray<T>, T>
        extends AbstractStructuredArrayBuilder<S, T> {

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating arrays of type S with
     * elements of type T, and the given length.
     *
     * Note: This constructor form cannot be used with an element type T that extends StructuredArray. Use the
     * constructor form {@link StructuredArrayBuilder#StructuredArrayBuilder(Class, StructuredArrayBuilder, long)}
     * to create builders that instantiate nested StructuredArrays.
     *
     * @param arrayClass The class of the array to be built by this builder
     * @param elementClass The class of elements in the array to be built by this builder
     * @param length The length of the array to be build by this builder
     */
    public StructuredArrayBuilder(final Class<S> arrayClass,
                                  final Class<T> elementClass,
                                  final long length) {
        super(arrayClass, elementClass, length);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating arrays of type S with
     * elements of type T, and the given length.
     *
     * Note: This constructor form cannot be used with an element type T that extends StructuredArray. Use the
     * constructor form {@link StructuredArrayBuilder#StructuredArrayBuilder(Class, StructuredArrayBuilder, long)}
     * to create builders that instantiate nested StructuredArrays.
     *
     * @param lookup The lookup object to use for accessing constructors when resolving this builder
     * @param arrayClass The class of the array to be built by this builder
     * @param elementClass The class of elements in the array to be built by this builder
     * @param length The length of the array to be build by this builder
     */
    public StructuredArrayBuilder(MethodHandles.Lookup lookup,
                                  final Class<S> arrayClass,
                                  final Class<T> elementClass,
                                  final long length) {
        super(lookup, arrayClass, elementClass, length);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating arrays of the given array model.
     *
     * @param arrayModel The model of the array to be built by this builder
     */
    public StructuredArrayBuilder(final StructuredArrayModel<S, T> arrayModel) {
        super(arrayModel);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating arrays of the given array model.
     *
     * @param lookup The lookup object to use for accessing constructors when resolving this builder
     * @param arrayModel The model of the array to be built by this builder
     */
    public StructuredArrayBuilder(MethodHandles.Lookup lookup,
                                  final StructuredArrayModel<S, T> arrayModel) {
        super(lookup, arrayModel);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating an array of type S with
     * elements of type T, and the given length. Used when T extends StructuredArray, such that the
     * arrays instantiated by this builder would include nested StructuredArrays.
     *
     * @param arrayClass The class of the array to be built by this builder
     * @param subArrayBuilder The builder used for creating individual array elements (which are themselves
     *                        StructuredArrays)
     * @param length The length of the array to be build by this builder
     * @param <A> The class or the subArray (should match T for the StructuredArrayBuilder)
     * @param <E> The element class in the subArray.
     */
    public <A extends StructuredArray<E>, E> StructuredArrayBuilder(final Class<S> arrayClass,
                                                                    final StructuredArrayBuilder<A, E> subArrayBuilder,
                                                                    final long length) {
        super(arrayClass, subArrayBuilder, length);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating an array of type S with
     * elements of type T, and the given length. Used when T extends StructuredArray, such that the
     * arrays instantiated by this builder would include nested StructuredArrays.
     *
     * @param lookup The lookup object to use for accessing constructors when resolving this builder
     * @param arrayClass The class of the array to be built by this builder
     * @param subArrayBuilder The builder used for creating individual array elements (which are themselves
     *                        StructuredArrays)
     * @param length The length of the array to be build by this builder
     * @param <A> The class or the subArray (should match T for the StructuredArrayBuilder)
     * @param <E> The element class in the subArray.
     */
    public <A extends StructuredArray<E>, E> StructuredArrayBuilder(
            MethodHandles.Lookup lookup,
            final Class<S> arrayClass,
            final StructuredArrayBuilder<A, E> subArrayBuilder,
            final long length) {
        super(lookup, arrayClass, subArrayBuilder, length);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating an array of type S with
     * elements of type T, and the given length. Used when T extends AbstractPrimitiveArray, such that the
     * arrays instantiated by this builder would include elements that are PrimitiveArrays.
     *
     * @param arrayClass The class of the array to be built by this builder
     * @param subArrayBuilder The builder used for creating individual array elements (which are themselves
     *                        subclassable PrimitiveArrays)
     * @param length The length of the array to be build by this builder
     * @param <A> The class in the subArray (should match T for the StructuredArrayBuilder)
     */
    public <A extends AbstractPrimitiveArray> StructuredArrayBuilder(final Class<S> arrayClass,
                                                                         final PrimitiveArrayBuilder<A> subArrayBuilder,
                                                                         final long length) {
        super(arrayClass, subArrayBuilder, length);
    }

    /**
     * Constructs a new {@link StructuredArrayBuilder} object for creating an array of type S with
     * elements of type T, and the given length. Used when T extends AbstractPrimitiveArray, such that the
     * arrays instantiated by this builder would include elements that are PrimitiveArrays.
     *
     * @param lookup The lookup object to use for accessing constructors when resolving this builder
     * @param arrayClass The class of the array to be built by this builder
     * @param subArrayBuilder The builder used for creating individual array elements (which are themselves
     *                        subclassable PrimitiveArrays)
     * @param length The length of the array to be build by this builder
     * @param <A> The class in the subArray (should match T for the StructuredArrayBuilder)
     */
    public <A extends AbstractPrimitiveArray> StructuredArrayBuilder(
            MethodHandles.Lookup lookup,
            final Class<S> arrayClass,
            final PrimitiveArrayBuilder<A> subArrayBuilder,
            final long length) {
        super(lookup, arrayClass, subArrayBuilder, length);
    }

    /**
     * Set the {@link CtorAndArgsProvider} used for determining the constructor and construction
     * arguments used for each element in the array. The provider may use the specific element's
     * construction context in determining the construction parameters.
     *
     * Note: This method overlaps in purpose with the alternate methods for controlling element
     * construction. Namely {@link StructuredArrayBuilder#elementCtorAndArgs(CtorAndArgs)} and
     * {@link StructuredArrayBuilder#elementCtorAndArgs(Constructor, Object...)}.
     *
     * @param ctorAndArgsProvider The provider used for determining the constructor and construction
     *                            arguments used for each element in instantiated arrays
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> elementCtorAndArgsProvider(final CtorAndArgsProvider<T> ctorAndArgsProvider) {
        return (StructuredArrayBuilder<S, T>) super.elementCtorAndArgsProvider(ctorAndArgsProvider);
    }

    /**
     * Set the {@link CtorAndArgs} to be used for all elements in the array. All elements will be constructed with
     * the same constructor and the same arguments.
     *
     * Note: This method overlaps in purpose with the alternate methods for controlling element
     * construction. Namely {@link StructuredArrayBuilder#elementCtorAndArgsProvider(CtorAndArgsProvider)} and
     * {@link StructuredArrayBuilder#elementCtorAndArgs(Constructor, Object...)}.
     *
     * @param ctorAndArgs The constructor and arguments used for all elements in arrays
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> elementCtorAndArgs(final CtorAndArgs<T> ctorAndArgs) {
        return (StructuredArrayBuilder<S, T>) super.elementCtorAndArgs(ctorAndArgs);
    }

    /**
     * Set the {@link Constructor} and construction arguments to be used for all elements in the array.
     * All elements will be constructed with the same constructor and the same arguments.
     *
     * Note: This method overlaps in purpose with the alternate methods for controlling element
     * construction. Namely {@link StructuredArrayBuilder#elementCtorAndArgsProvider(CtorAndArgsProvider)} and
     * {@link StructuredArrayBuilder#elementCtorAndArgs(CtorAndArgs)}.
     *
     * @param constructor The constructor used for all elements in arrays
     * @param args The construction arguments supplied for the constructor, used for all elements in the array
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> elementCtorAndArgs(final Constructor<T> constructor, final Object... args) {
        return (StructuredArrayBuilder<S, T>) super.elementCtorAndArgs(constructor, args);
    }

    /**
     * Set the {@link CtorAndArgs} to be used in constructing arrays.
     * Setting the means for array construction is Required if the array class (S) does not support a
     * default constructor, or if a non-default construction of the array instance is needed.
     *
     * @param arrayCtorAndArgs The constructor and arguments used for constructing arrays
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> arrayCtorAndArgs(final CtorAndArgs<S> arrayCtorAndArgs) {
        return (StructuredArrayBuilder<S, T>) super.arrayCtorAndArgs(arrayCtorAndArgs);
    }

    /**
     * Set the {@link Constructor} and construction arguments to be used in constructing arrays.
     * Setting the means for array construction is Required if the array class (S) does not support a
     * default constructor, or if a non-default construction of the array instance is needed.
     *
     * @param constructor The constructor used for constructing arrays
     * @param args The construction arguments supplied for the constructor
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> arrayCtorAndArgs(final Constructor<S> constructor, final Object... args) {
        return (StructuredArrayBuilder<S, T>) super.arrayCtorAndArgs(constructor, args);
    }

    /**
     * Set the (opaque) contextCookie object associated with this builder. This contextCookie object will be
     * set in {@link ConstructionContext} object passed to the element
     * {@link CtorAndArgsProvider} provider for each element in instantiated arrays.
     *
     * @param contextCookie the contextCookie object
     * @return The builder
     */
    public StructuredArrayBuilder<S, T> contextCookie(final Object contextCookie) {
        return (StructuredArrayBuilder<S, T>) super.contextCookie(contextCookie);
    }

    /**
     * Resolve any not-yet-resolved constructor information needed by this builder. Calling resolve() is not
     * necessary ahead of building, but it is useful for ensuring resolution works ahead of actual building
     * attempts.
     *
     * @return This builder
     * @throws IllegalArgumentException if the array constructor or element constructor fail to resolve given
     * the current information in the builder
     */
    public StructuredArrayBuilder<S, T> resolve() {
        return (StructuredArrayBuilder<S, T>) super.resolve();
    }

    /**
     * Build a {@link StructuredArray} according to the information captured in this builder
     * @return A newly instantiated {@link StructuredArray}
     *
     * @throws IllegalArgumentException if the array constructor or element constructor fail to resolve given
     * the current information in the builder
     */
    public S build() throws IllegalArgumentException {
        return super.build();
    }

    /**
     * Get the {@link StructuredArrayModel} that describes the arrays built by this builder
     *
     * @return The {@link StructuredArrayModel} that describes the arrays built by this builder
     */
    public StructuredArrayModel<S, T> getArrayModel() {
        return super.getArrayModel();
    }

    /**
     * Get the {@link StructuredArrayBuilder} for the elements of the arrays built by
     * this builder. Null
     *
     * @return The {@link StructuredArrayBuilder} for the elements of the arrays built by
     * this builder. Null if the array element type T does not extend {@link StructuredArray}.
     */
    public StructuredArrayBuilder getStructuredSubArrayBuilder() {
        return (StructuredArrayBuilder) super.getStructuredSubArrayBuilder();
    }

    /**
     * Get the {@link StructuredArrayBuilder} for the elements of the arrays built by
     * this builder. Null
     *
     * @return The {@link StructuredArrayBuilder} for the elements of the arrays built by
     * this builder. Null if the array element type T does not extend {@link StructuredArray}.
     */
    public PrimitiveArrayBuilder getPrimitiveSubArrayBuilder() {
        return super.getPrimitiveSubArrayBuilder();
    }

    /**
     * Get the {@link CtorAndArgs} describing the constructor and arguments used to instantiate arrays with
     * this builder. May be null if non of {@link StructuredArrayBuilder#arrayCtorAndArgs},
     * {@link StructuredArrayBuilder#resolve()} or
     * {@link StructuredArrayBuilder#build()} have been called yet.
     * @return The {@link CtorAndArgs} describing the constructor and arguments used to instantiate arrays with
     * this builder.
     */
    public CtorAndArgs<S> getArrayCtorAndArgs() {
        return super.getArrayCtorAndArgs();
    }

    /**
     * Get the {@link CtorAndArgsProvider} which provides the constructor and arguments used to construct
     * individual elements of arrays instantiated with this builder. May be null if non of
     * {@link StructuredArrayBuilder#elementCtorAndArgsProvider},
     * {@link StructuredArrayBuilder#elementCtorAndArgs},
     * {@link StructuredArrayBuilder#resolve} or
     * {@link StructuredArrayBuilder#build} have been called yet.
     * @return The {@link CtorAndArgsProvider} which provides the constructor and arguments used to construct
     * individual elements of arrays instantiated with this builder
     */
    public CtorAndArgsProvider<T> getElementCtorAndArgsProvider() {
        return super.getElementCtorAndArgsProvider();
    }

    /**
     * Get the (opaque) contextCookie object associated with this builder. This contextCookie object will be
     * set in {@link ConstructionContext} object passed to the element
     * {@link CtorAndArgsProvider} provider for each element in instantiated arrays.
     * @return The (opaque) contextCookie object associated with this builder
     */
    public Object getContextCookie() {
        return super.getContextCookie();
    }
}
