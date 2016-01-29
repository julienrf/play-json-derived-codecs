(Note: this project has been renamed from [play-json-variants](https://github.com/julienrf/play-json-variants/tree/v2.0) to `play-json-derived-codecs`)

# Play JSON Derived Codecs [![Maven Central](https://img.shields.io/maven-central/v/org.julienrf/play-json-derived-codecs_2.11.svg)](https://maven-badges.herokuapp.com/maven-central/org.julienrf/play-json-derived-codecs_2.11)

`Reads`, `OWrites` and `OFormat` derivation for algebraic data types (sealed traits and case classes, possibly recursive), powered by [shapeless](http://github.com/milessabin/shapeless).

Compared to the built-in macros, this project brings support for:

- sealed traits ;
- recursive types.

The artifacts are built for Scala 2.11 and Play 2.4.

## Usage

~~~ scala
import julienrf.json.derived

case class User(name: String, age: Int)

object User {
  implicit val reads: Reads[User] = derived.reads[User]
}
~~~

The API is simple: the object `derived` has just three methods.

- `reads[A]`, derives a `Reads[A]` ;
- `owrites[A]`, derives a `OWrites[A]` ;
- `oformat[A]`, derives a `OFormat[A]`.

## Changelog

- 3.0: Use [shapeless](http://github.com/milessabin/shapeless) for the derivation
- 2.0: Generalize `transform` and `discriminator` parameters
- 1.1.0: Add support for an optional `transform` parameter (thanks to Nikita Melkozerov)
- 1.0.1: Remove unnecessary macro paradise dependency when Scala 2.11 (thanks to Kenji Yoshida)
- 1.0.0: Support for `Reads`, `Writes` and `Format`
