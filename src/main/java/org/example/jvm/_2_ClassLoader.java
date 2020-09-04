package org.example.jvm;

/**
 * Java 语言的类型可以分为两大类：基本类型（primitive types）和引用类型（reference types）。
 * 引用类型可将其细分为四种：类、接口、数组类和泛型参数。由于泛型参数会在编译过程中被擦除，因此 Java 虚拟机实际上只有前三种。在类、接口和
 * 数组类中，数组类是由 Java 虚拟机直接生成的，其他两种则有对应的字节流。
 *
 * 加载，是指查找字节流，并且据此创建类的过程。对于数组类来说，它并没有对应的字节流，而是由 Java 虚拟机直接生成的。对于其他的类来说，Java
 * 虚拟机则需要借助类加载器来完成查找字节流的过程。
 * 启动类加载器（boot class loader）是由 C++ 实现的，没有对应的 Java 对象，因此在 Java 中只能用 null 来指代。除了启动类加载器之外，
 * 其他的类加载器都是 java.lang.ClassLoader 的子类，因此有对应的 Java 对象。除了启动类加载器之外，另外两个重要的类加载器是
 * 扩展类加载器（extension class loader）和应用类加载器（application class loader），均由 Java 核心类库提供。
 * 启动类加载器：负责加载存放在 JRE 的 lib 目录下 jar 包中的类（以及由虚拟机参数 -Xbootclasspath 指定的类）。
 * 扩展类加载器：负责加载存放在 JRE 的 lib/ext 目录下 jar 包中的类（以及由系统变量 java.ext.dirs 指定的类）。
 * 应用类加载器：负责加载应用程序路径下的类。（这里的应用程序路径，便是指虚拟机参数 -cp/-classpath、系统变量 java.class.path 或环境变
 *              量 CLASSPATH 所指定的路径。）
 * Java 9 引入了模块系统，并且略微更改了上述的类加载器1。扩展类加载器被改名为平台类加载器（platform class loader）。Java SE 中除了少
 * 数几个关键模块，比如说 java.base 是由启动类加载器加载之外，其他的模块均由平台类加载器所加载。
 * 我们还可以加入自定义的类加载器，来实现特殊的加载方式。举例来说，我们可以对 class 文件进行加密，加载时再利用自定义的类加载器对其解密。
 * 除了加载功能之外，类加载器还提供了命名空间的作用。
 * 在 Java 虚拟机中，类的唯一性是由类加载器实例以及类的全名一同确定的。即便是同一串字节流，经由不同的类加载器加载，也会得到两个不同的类。
 * 在大型应用中，我们往往借助这一特性，来运行同一个类的不同版本。
 *
 * 链接，是指将创建成的类合并至 Java 虚拟机中，使之能够执行的过程。它可分为验证、准备以及解析三个阶段。
 * 验证阶段的目的，在于确保被加载类能够满足 Java 虚拟机的约束条件。
 * 准备阶段的目的，是为被加载类的静态字段分配内存。Java 代码中对静态字段的具体初始化，则会在稍后的初始化阶段中进行。
 * 除了分配内存外，部分 Java 虚拟机还会在此阶段构造其他跟类层次相关的数据结构，比如说用来实现虚方法的动态绑定的方法表。
 * 在 class 文件被加载至 Java 虚拟机之前，这个类无法知道其他类及其方法、字段所对应的具体地址，甚至不知道自己方法、字段的地址。因此，每当
 * 需要引用这些成员时，Java 编译器会生成一个符号引用。在运行阶段，这个符号引用一般都能够无歧义地定位到具体目标上。
 * 解析阶段的目的，正是将这些符号引用解析成为实际引用。如果符号引用指向一个未被加载的类，或者未被加载类的字段或方法，那么解析将触发这个类的
 * 加载（但未必触发这个类的链接以及初始化。）
 * Java 虚拟机规范并没有要求在链接过程中完成解析。它仅规定了：如果某些字节码使用了符号引用，那么在执行这些字节码之前，需要完成对这些符号引
 * 用的解析。
 *
 * 初始化：为标记为常量值的字段赋值，以及执行 < clinit > 方法的过程。
 * 在 Java 代码中，如果要初始化一个静态字段，我们可以在声明时直接赋值，也可以在静态代码块中对其赋值。
 * 如果直接赋值的静态字段被 final 所修饰，并且它的类型是基本类型或字符串时，那么该字段便会被 Java 编译器标记成常量值（ConstantValue），
 * 其初始化直接由Java虚拟机完成。除此之外的直接赋值操作，以及所有静态代码块中的代码，则会被Java编译器置于同一方法中，并把它命名为<clinit>。
 * Java 虚拟机会通过加锁来确保类的<clinit>方法仅被执行一次。
 * 只有当初始化完成之后，类才正式成为可执行的状态。
 * 类的初始化何时会被触发呢？JVM 规范枚举了下述多种触发情况：
 * 1.当虚拟机启动时，初始化用户指定的主类；
 * 2.当遇到用以新建目标类实例的 new 指令时，初始化 new 指令的目标类；
 * 3.当遇到调用静态方法的指令时，初始化该静态方法所在的类；
 * 4.当遇到访问静态字段的指令时，初始化该静态字段所在的类；
 * 5.子类的初始化会触发父类的初始化；
 * 6.如果一个接口定义了 default 方法，那么直接实现或者间接实现该接口的类的初始化，会触发该接口的初始化；
 * 7.使用反射 API 对某个类进行反射调用时，初始化这个类；
 * 8.当初次调用 MethodHandle 实例时，初始化该 MethodHandle 指向的方法所在的类。
 *
 * JVM 参数 -verbose:class 来打印类加载的先后顺序
 * -XX：+TraceClassLoading 可以看到类加载过程
 * Created by mbs on 2020/7/20 17:07
 */
public class _2_ClassLoader {
}
