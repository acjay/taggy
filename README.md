# Taggy

Single-line helper for better type safety.

## Meta

* __State:__ development
* __Point People:__ [@acjay](https://github.com/acjay)

Types let programmers rely on the compiler catch errors where an operation is attempted on inapplicable data. But in real-world applications, the type of a variable isn't specific enough to capture the fact that data of the same type often aren't interchangeable from a business perspective.

For example, you might have a function that returns longitude and latitude for a street address:

```
def locateAddress(address: String): (Double, Double) = ???
```

This typed interface might catch some errors for you, but it won't stop you from accidentally calling your function on a person's name (`locateAddress(person.name)`). Or maybe more likely, since the results are both `Double`s, it would be pretty easy to get them mixed up (`val (lattitude, longitude) = locateAddress(address)`).

Tagged types let you attach annotate a value with its _purpose_. With tagged types, the function would take the same values, but its signature might look like:

```
def locateAddress(address: Address): (Longitude, Lattitude) = ???
```

With tagged types, you assert the purpose of the data when it enters your system, and you're protected from mishaps as it flows through the logic layers.

## Usage

Include the following line in your `build.sbt`:

```
libraryDependencies ++= Seq(
  "com.chuusai" %% "shapeless" % "2.3.2",
  "com.acjay" %% "tagged" % "0.0.1"
)
```

Import the annotation:

```
import com.acjay.tagtypetemplater.tagged
```

Then, declare your tagged type, specifying the underlying type as a string literal:

```
@tagged("String") type Address
@tagged("Double") type Longitude
@tagged("Double") type Lattitude
```

When data enters your system as a string, upgrade it to the tagged type:

```
val address = Address.fromString("123 Main Street")
val longitude = Longitude.fromDouble(44.12345)
``` 

In a lot of cases, the compiler will let you pass a tagged type where its underlying type is expected, but not always. When you need to widen back to the underlying type:

```
val addressAsPlainString = address.untagged // addressAsPlainString: String
```

## Technologies

This project uses [Scalameta](http://scalameta.org/) to generate a bunch of boilerplate for making tagged types as convenient as possible to work with. It requires [Shapeless](https://github.com/milessabin/shapeless/) for its implementation of type tagging, but perhaps in the future other options will be offered, too.

## Running the demos

To try out the example run `sbt '+ example/run'`, which will run it for each supported Scala version.

## Releasing new versions
   
For testing changes:

1. Bump the version in `build.sbt` as appropriate, and add `-SNAPSHOT` to the end of the version number.
2. Update the `libraryDependencies` line above in anticipation of the next version.
3. Use the `sbt publish-signed` task to push snapshots to Maven Central.
4. Update the *Changelog* as noteworthy changes are made.
5. During the testing period, merge new changes into the `development` branch, so that the `master` branch on Github always reflects the latest version on Maven Central. 

For releasing new versions:
 
1. Remove the `-SNAPSHOT` suffix in `build.sbt`.
2. Publish to Maven Central staging using `sbt publish-signed`.
3. Follow [the Maven Central workflow](http://central.sonatype.org/pages/releasing-the-deployment.html) for releasing the next version, logging in to Maven Central Nexus with an account set up with the privilege to publish to [the Open Source Project Repository Atomic Store entry](https://issues.sonatype.org/browse/OSSRH-20964). 
4. Merge `development` into `master` to update the canonical version on Github.
  
For reference on this process, you may want to see the following links:
 
- [SBT: Deploying to Sonatype](http://www.scala-sbt.org/0.13/docs/Using-Sonatype.html)
- [SBT-PGP Usage docs](http://www.scala-sbt.org/sbt-pgp/usage.html)
- [The Central Repository: Releasing The sdDeployment](http://central.sonatype.org/pages/releasing-the-deployment.html)
  
## Todos

- Optional overrides for all generated names
- Tests (does-not-compile test)
- Other tagging approaches (e.g. wrapper class)
  
## Changelog

*0.0.1*
- Initial release.
