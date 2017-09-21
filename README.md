# Taggy

Single-line "newtype" helper for better type safety.

## Meta

* __State:__ development
* __Point People:__ [@acjay](https://github.com/acjay)

Types let programmers rely on the compiler catch programmer errors, where an operation is attempted on inapplicable data. But in real-world applications, the type of a variable isn't specific enough to capture the fact that data of the same type often aren't interchangeable from a business perspective. With tagged types, you assert the _purpose_ of the data when it enters your system, and you're protected from misusing it as it flows through the logic layers. This is particularly applicable where you find that your system--or a subsystem of it--passes values along without inspecting or modifying them.

For example, you might have a function that returns longitude and latitude for a street address:

```scala
def locateAddress(address: String): (Double, Double) = ???
```

This typed interface might catch some errors for you, but it won't stop you from accidentally calling your function on a person's name (`locateAddress(person.name)`). Or maybe more likely, since the results are both `Double`s, it would be pretty easy to get them mixed up (`val (latitude, longitude) = locateAddress(address)`).

With tagged types, the function would take the same values, but its signature might look like:

```scala
def locateAddress(address: Address): (Longitude, Latitude) = ???
```

More safe, and more readable!

As a bonus, even if you used converted this function to an anonymous function of type `Address => (Longitude, Latitude)`, it would be pretty much self-explanatory. 

## Usage

### SBT setup

Include the following line in your `build.sbt`:

```sbt
libraryDependencies ++= Seq(
  "com.acjay" %% "taggy" % "1.0.0"
)

// Enable Scala Meta macros for taggy
addCompilerPlugin("org.scalameta" % "paradise" % "3.0.0-M10" cross CrossVersion.full)

scalacOptions += "-Xplugin-require:macroparadise"

// temporary workaround for https://github.com/scalameta/paradise/issues/10
scalacOptions in (Compile, console) ~= (_ filterNot (_ contains "paradise"))
```

### Using in your code

Import the annotation:

```scala
import com.acjay.taggy.tagged
```

Then, declare your tagged type, specifying the underlying type as a string literal:

```scala
@tagged type Address = String
@tagged type Longitude = Double
@tagged type Latitude = Double
```

When data enters your system as a string, upgrade it to the tagged type:

```scala
val address = Address.fromString("123 Main Street")
val longitude = Longitude.fromDouble(44.12345)
``` 

In a lot of cases, the compiler will let you pass a tagged type where its underlying type is expected, but not always. When you need to widen back to the underlying type:

```scala
val addressAsPlainString = address.untagged // addressAsPlainString: String
```

### Additional notes

- You can often write generic implicits for [de]serialization of tagged types. For example, to do this with Spray-JSON serialization, you might do something like:

  ```scala
  import com.acjay.taggy.tag
  import com.acjay.taggy.tag.@@
  implicit def taggedStringTypeFormat[NewTypeTag](implicit reader: JsonReader[String], writer: JsonWriter[String]): JsonFormat[String @@ NewTypeTag] = new JsonFormat[String @@ NewTypeTag] {
    def read(json: JsValue) = tag[NewTypeTag](reader.read(json))
    def write(obj: String @@ NewTypeTag): JsValue = writer.write(obj)
  }
  ```

  Note that this only works as a generic JsonFormat for all the new types that have an underlying type of `String`, and it relies on the implementation detail that taggy sythesizes a phantom type tag of the same name as the `NewType`, but with `Tag` appended at the end.

  A fully generic version doesn't seem to work:

  ```scala
  // THIS CODE FAILS DUE TO IMPLICIT DIVERGENCE
  implicit def taggedTypeFormat[NewTypeTag, UnderlyingType](implicit reader: JsonReader[UnderlyingType], writer: JsonWriter[UnderlyingType]): JsonFormat[UnderlyingType @@ NewTypeTag] = new JsonFormat[UnderlyingType @@ NewTypeTag] {
    def read(json: JsValue) = tag[NewTypeTag](reader.read(json))
    def write(obj: UnderlyingType @@ NewTypeTag): JsValue = writer.write(obj)
  }
  ```

  A PR to this readme with a fully working generic version would be much appreciated!

- One known issue is that the Scala Meta compiler plugin for macro annotations appears to conflict with the code that ScalaPB produces. We solved this by moving our Protobuf `.proto` files into their own SBT subproject, without the macroparadise compiler option enabled. If you encounter any issues that seem similar, see if this approach works for you.

## Technologies

This project uses [Scalameta](http://scalameta.org/) to generate a bunch of boilerplate for making tagged types as convenient as possible to work with. It uses an implementation of tagged types [cut-and-pasted](https://github.com/milessabin/shapeless/blob/master/core/src/main/scala/shapeless/typeoperators.scala#L25-L34) from [Shapeless](https://github.com/milessabin/shapeless/) for its implementation of type tagging, but perhaps in the future other options will be offered, too.

Take a look at https://github.com/alexknvl/newtypes, which has a very similar purpose. I probably wouldn't have written this library had I known about it in advance. However, one possible advantage of the Shapeless-inspired approach here is the ability to abstract over the `@@` tagging operator.

## Running the demos

To try out the example run `sbt '+ exampleJS/run' '+ exampleJVM/run'`, which will run it for each supported Scala version.

## Releasing new versions
   
For testing changes:

1. Merge `master` into `development`.
1. Bump the version in `build.sbt` as appropriate, and add `-SNAPSHOT` to the end of the version number.
1. Update the *Changelog* as noteworthy changes are made.
1. Use the `sbt +publish` task to push snapshots to Maven Central.
1. During the testing period, merge new changes into the `development` branch, so that the `master` branch on Github always reflects the latest version on Maven Central. 

For releasing new versions:
 
1. Remove the `-SNAPSHOT` suffix in `build.sbt`.
1. Publish to Maven Central staging using `sbt +publish-signed`.
1. Update the `libraryDependencies` for the current version.
1. Follow [the Maven Central workflow](http://central.sonatype.org/pages/releasing-the-deployment.html) for releasing the next version, logging in to Maven Central Nexus with an account set up with the privilege to publish to [the Open Source Project Repository Atomic Store entry](https://issues.sonatype.org/browse/OSSRH-20964). 
  
For reference on this process, you may want to see the following links:
 
- [SBT: Deploying to Sonatype](http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html)
- [SBT-PGP Usage docs](http://www.scala-sbt.org/sbt-pgp/usage.html)
- [The Central Repository: Releasing The sdDeployment](http://central.sonatype.org/pages/releasing-the-deployment.html)
  
## Todos

- Get full publish-and-release workflow going with sbt-release and CircleCI
- Optional overrides for all generated names
- Tests (does-not-compile test)
- Streamline inclusion via SBT (as a plugin maybe?)
- Other tagging approaches (e.g. wrapper class)
- Cross-build for Scala Native?
  
## Changelog

*1.0.0*
- Change syntax to move the annnotation type parameter to the right-hand side of an assignment, inspired by NewTypes.
- Remove Shapeless dependency.

*0.0.1*
- Initial release.
